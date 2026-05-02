package com.jiemo.createdglab.websocket;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.jiemo.createdglab.CreateDGLab;
import com.jiemo.createdglab.config.ModConfig;
import com.jiemo.createdglab.util.WaveformGenerator;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * WebSocket server manager - protocol copied from DGLab-Craft-1.19.2
 */
public class WebSocketServerManager {
    private static final WebSocketServerManager INSTANCE = new WebSocketServerManager();
    private static final Gson GSON = new Gson();
    private static final Pattern STRENGTH_PATTERN = Pattern.compile("(\\d+)");

    // Fixed clientId - MUST be this value for DG-Lab APP to recognize us
    private static final String FIXED_CLIENT_ID = "1234-123456789-12345-12345-01";

    private WebSocketServer server;
    private WebSocket connectedClient;

    // generatedClientId: UUID generated on connect, sent as clientId in bind message
    // targetId: set to generatedClientId after onOpen, used as targetId in all messages
    private String generatedClientId;
    private String targetId;

    private boolean isRunning = false;
    private boolean isBound = false;
    private Timer heartbeatTimer;

    private int appAStrength = 0;
    private int appBStrength = 0;
    private int appAMaxStrength = 100;
    private int appBMaxStrength = 100;

    private int lastSentIntensityA = 0;
    private int lastSentIntensityB = 0;

    // Channel status for HUD display
    private double channelAIntensity = 0;
    private double channelBIntensity = 0;
    private String channelAStatus = "Idle";
    private String channelBStatus = "Idle";

    private WebSocketServerManager() {}

    public static WebSocketServerManager getInstance() {
        return INSTANCE;
    }

    public void start() {
        if (isRunning) return;

        int port = ModConfig.WS_PORT.get();
        String host = ModConfig.WS_HOST.get();

        try {
            InetAddress address = host.equals("0.0.0.0") ? null : InetAddress.getByName(host);
            InetSocketAddress addr = address != null ? new InetSocketAddress(address, port) : new InetSocketAddress(port);

            server = new WebSocketServer(addr) {
                @Override
                public void onOpen(WebSocket conn, ClientHandshake handshake) {
                    handleOpen(conn);
                }

                @Override
                public void onClose(WebSocket conn, int code, String reason, boolean remote) {
                    handleClose(conn);
                }

                @Override
                public void onMessage(WebSocket conn, String message) {
                    handleMessage(conn, message);
                }

                @Override
                public void onError(WebSocket conn, Exception ex) {
                    CreateDGLab.LOGGER.error("[DG-Lab] WebSocket error: {}", ex.getMessage());
                }

                @Override
                public void onStart() {
                    CreateDGLab.LOGGER.info("[DG-Lab] WebSocket server started on {}:{}", host, port);
                }
            };

            server.start();
            isRunning = true;

        } catch (Exception e) {
            CreateDGLab.LOGGER.error("[DG-Lab] Failed to start WebSocket server: {}", e.getMessage());
        }
    }

    public void stop() {
        if (server != null) {
            try {
                server.stop();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            isRunning = false;
            isBound = false;
            connectedClient = null;
            stopHeartbeat();
            CreateDGLab.LOGGER.info("[DG-Lab] WebSocket server stopped");
        }
    }

    /**
     * onOpen - send bind message with generated UUID as clientId.
     * Format matches DGLab-Craft exactly:
     * {"type":"bind","clientId":"<UUID>","targetId":"","message":"targetId"}
     */
    private void handleOpen(WebSocket conn) {
        if (connectedClient != null) {
            conn.send("{\"type\":\"error\",\"message\":\"400\"}");
            conn.close();
            return;
        }

        connectedClient = conn;
        appAStrength = 0;
        appBStrength = 0;
        appAMaxStrength = 100;
        appBMaxStrength = 100;

        // Generate UUID for this session
        generatedClientId = UUID.randomUUID().toString();
        targetId = generatedClientId;

        // Send bind request - raw JSON string like DGLab-Craft
        conn.send("{\"type\":\"bind\",\"clientId\":\"" + generatedClientId + "\",\"targetId\":\"\",\"message\":\"targetId\"}");

        CreateDGLab.LOGGER.info("[DG-Lab] Client connected, sent bind: clientId={}", generatedClientId);

        // Start heartbeat timer (fires immediately, then every 60s)
        startHeartbeat();
    }

    private void handleClose(WebSocket conn) {
        if (conn == connectedClient) {
            connectedClient = null;
            isBound = false;
            generatedClientId = null;
            targetId = null;
            lastSentIntensityA = 0;
            lastSentIntensityB = 0;
            channelAIntensity = 0;
            channelBIntensity = 0;
            channelAStatus = "Idle";
            channelBStatus = "Idle";
            stopHeartbeat();
            CreateDGLab.LOGGER.info("[DG-Lab] Client disconnected");
        }
    }

    private void handleMessage(WebSocket conn, String message) {
        try {
            JsonObject json = GSON.fromJson(message, JsonObject.class);
            if (json == null) return;

            String type = json.has("type") ? json.get("type").getAsString() : null;

            if ("bind".equals(type)) {
                handleBind(conn, json);
            } else if ("heartbeat".equals(type)) {
                handleHeartbeat(conn, json);
            } else if ("msg".equals(type)) {
                handleMsg(conn, json);
            } else if ("break".equals(type)) {
                handleBreak(conn, json);
            }
        } catch (Exception e) {
            CreateDGLab.LOGGER.error("[DG-Lab] Error parsing message: {}", e.getMessage());
        }
    }

    /**
     * Bind validation - matches DGLab-Craft exactly:
     * 1. message must be "DGLAB"
     * 2. clientId must be FIXED_CLIENT_ID ("1234-123456789-12345-12345-01")
     * 3. targetId must match our generatedClientId
     *
     * Response includes statusCode:200 like DGLab-Craft.
     */
    private void handleBind(WebSocket conn, JsonObject json) {
        String msgContent = json.has("message") ? json.get("message").getAsString() : null;
        String appClientId = json.has("clientId") ? json.get("clientId").getAsString() : null;
        String receivedTargetId = json.has("targetId") ? json.get("targetId").getAsString() : null;

        CreateDGLab.LOGGER.info("[DG-Lab] Bind: appClientId={}, targetId={}, message={}", appClientId, receivedTargetId, msgContent);

        if (!"DGLAB".equals(msgContent)) {
            CreateDGLab.LOGGER.warn("[DG-Lab] Bind rejected: message != DGLAB");
            return;
        }
        if (!FIXED_CLIENT_ID.equals(appClientId)) {
            CreateDGLab.LOGGER.warn("[DG-Lab] Bind rejected: clientId != FIXED_CLIENT_ID, got={}", appClientId);
            return;
        }
        if (!generatedClientId.equals(receivedTargetId)) {
            CreateDGLab.LOGGER.warn("[DG-Lab] Bind rejected: targetId mismatch, expected={}, got={}", generatedClientId, receivedTargetId);
            return;
        }

        isBound = true;

        // Send bind response - raw JSON like DGLab-Craft (includes statusCode)
        conn.send("{\"type\":\"bind\",\"clientId\":\"" + FIXED_CLIENT_ID + "\",\"targetId\":\"" + appClientId + "\",\"message\":\"200\",\"statusCode\":200}");

        CreateDGLab.LOGGER.info("[DG-Lab] Bound successfully to app: {}", appClientId);
    }

    /**
     * Heartbeat response - matches DGLab-Craft format.
     * Uses FIXED_CLIENT_ID as clientId, not the UUID.
     */
    private void handleHeartbeat(WebSocket conn, JsonObject json) {
        String receivedTargetId = json.has("targetId") ? json.get("targetId").getAsString() : null;
        String responseTargetId = (receivedTargetId != null && !receivedTargetId.isEmpty()) ? receivedTargetId : targetId;
        conn.send("{\"type\":\"heartbeat\",\"clientId\":\"" + FIXED_CLIENT_ID + "\",\"targetId\":\"" + responseTargetId + "\",\"message\":\"200\"}");
    }

    private void handleMsg(WebSocket conn, JsonObject json) {
        String message = json.has("message") ? json.get("message").getAsString() : null;
        if (message != null && message.startsWith("strength-")) {
            parseStrengthFeedback(message);
        }
    }

    private void handleBreak(WebSocket conn, JsonObject json) {
        isBound = false;
        connectedClient = null;
        stopHeartbeat();
    }

    private void parseStrengthFeedback(String message) {
        String[] parts = message.substring(9).split("\\+");
        if (parts.length >= 4) {
            // New format: strength-0+0+<A_max>+<B_max>
            try {
                appAMaxStrength = Integer.parseInt(parts[2]);
                appBMaxStrength = Integer.parseInt(parts[3]);
                CreateDGLab.LOGGER.info("[DG-Lab] Strength limits: A={}, B={}", appAMaxStrength, appBMaxStrength);
            } catch (NumberFormatException e) {
                CreateDGLab.LOGGER.error("[DG-Lab] Failed to parse strength: {}", message);
            }
        } else if (parts.length >= 3) {
            // Old format: strength-<channel>+<mode>+<value>
            try {
                int channel = Integer.parseInt(parts[0]);
                int value = Integer.parseInt(parts[2]);
                if (channel == 1) appAMaxStrength = value;
                else if (channel == 2) appBMaxStrength = value;
            } catch (NumberFormatException e) {
                // ignore
            }
        }
    }

    /**
     * Send strength as a percentage of each channel's app-configured max.
     */
    public void updateStrengthPercent(float percent, boolean useChannelA, boolean useChannelB) {
        if (!isBound || connectedClient == null) return;

        float clamped = Math.min(Math.max(percent, 0f), 1f);

        if (useChannelA) {
            int value = Math.round(clamped * appAMaxStrength);
            if (value != lastSentIntensityA) {
                sendMessage("strength-1+2+" + value);
                lastSentIntensityA = value;
                channelAIntensity = percent * 100;
                channelAStatus = value > 0 ? String.format("%d%%", value) : "Idle";
            }
        }
        if (useChannelB) {
            int value = Math.round(clamped * appBMaxStrength);
            if (value != lastSentIntensityB) {
                sendMessage("strength-2+2+" + value);
                lastSentIntensityB = value;
                channelBIntensity = percent * 100;
                channelBStatus = value > 0 ? String.format("%d%%", value) : "Idle";
            }
        }
    }

    /**
     * Send waveform for stress feedback.
     * Both channels are synced with the same waveform and strength.
     * Sequence: clear-1, clear-2 -> pulse-A -> pulse-B -> strength-1, strength-2
     */
    public void sendStressWaveform(float strengthPercent, boolean overStressed) {
        if (!isBound || connectedClient == null) return;

        float clamped = Math.min(Math.max(strengthPercent, 0f), 1f);
        int strengthValue = Math.round(clamped * appAMaxStrength);
        List<String> frames = WaveformGenerator.getWaveformForStress(clamped, overStressed);

        // Clear both channels
        sendMessage("clear-1");
        sendMessage("clear-2");

        // Send waveform to both channels
        sendPulseMessage("A", frames);
        sendPulseMessage("B", frames);

        // Set strength on both channels (synced)
        sendMessage("strength-1+2+" + strengthValue);
        sendMessage("strength-2+2+" + strengthValue);

        lastSentIntensityA = strengthValue;
        lastSentIntensityB = strengthValue;
        channelAIntensity = clamped * 100;
        channelBIntensity = clamped * 100;
        channelAStatus = overStressed ? "ALARM" : String.format("%d%%", strengthValue);
        channelBStatus = overStressed ? "ALARM" : String.format("%d%%", strengthValue);
    }

    /**
     * Clear waveform queues.
     */
    public void clearWaveforms(boolean useChannelA, boolean useChannelB) {
        if (!isBound || connectedClient == null) return;
        if (useChannelA) sendMessage("clear-1");
        if (useChannelB) sendMessage("clear-2");
    }

    /**
     * Send message - uses FIXED_CLIENT_ID as clientId, targetId as targetId.
     * Format matches DGLab-Craft sendMessage exactly.
     */
    private void sendMessage(String message) {
        if (connectedClient == null || !isBound) return;
        Map<String, String> msg = new HashMap<>();
        msg.put("type", "msg");
        msg.put("message", message);
        msg.put("clientId", FIXED_CLIENT_ID);
        msg.put("targetId", targetId != null ? targetId : "");
        connectedClient.send(GSON.toJson(msg));
    }

    /**
     * Send pulse message with hex data.
     * Format: pulse-A:["0A0A0A0A64646464","0A0A0A0A00000000",...]
     * Each element is 16 uppercase hex chars.
     * Matches DGLab-Craft sendPulseMessage exactly.
     */
    private void sendPulseMessage(String channel, List<String> frames) {
        StringBuilder hexArray = new StringBuilder("[");
        for (int i = 0; i < frames.size(); i++) {
            if (i > 0) hexArray.append(",");
            String hex = frames.get(i).toUpperCase();
            while (hex.length() < 16) hex = "0" + hex;
            if (hex.length() > 16) hex = hex.substring(0, 16);
            hexArray.append("\"").append(hex).append("\"");
        }
        hexArray.append("]");
        sendMessage("pulse-" + channel + ":" + hexArray.toString());
    }

    private void startHeartbeat() {
        stopHeartbeat();
        heartbeatTimer = new Timer("DGLab-Heartbeat", true);
        heartbeatTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (connectedClient != null && connectedClient.isOpen()) {
                    Map<String, String> heartbeat = new HashMap<>();
                    heartbeat.put("type", "heartbeat");
                    heartbeat.put("message", "200");
                    heartbeat.put("clientId", FIXED_CLIENT_ID);
                    heartbeat.put("targetId", targetId != null ? targetId : "");
                    connectedClient.send(GSON.toJson(heartbeat));
                }
            }
        }, 0, 60000); // fires immediately, then every 60s
    }

    private void stopHeartbeat() {
        if (heartbeatTimer != null) {
            heartbeatTimer.cancel();
            heartbeatTimer = null;
        }
    }

    public void resetStrengthTracking() {
        lastSentIntensityA = -1;
        lastSentIntensityB = -1;
    }

    public boolean isConnected() { return isBound && connectedClient != null && connectedClient.isOpen(); }
    public boolean isRunning() { return isRunning; }

    public String getServerAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (ni.isLoopback() || !ni.isUp()) continue;
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            // fall through
        }
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "localhost";
        }
    }

    public int getServerPort() { return ModConfig.WS_PORT.get(); }

    public String getQrCodeUrl() {
        return "https://www.dungeon-lab.com/app-download.php#DGLAB-SOCKET#ws://"
                + getServerAddress() + ":" + getServerPort() + "/" + FIXED_CLIENT_ID;
    }

    public int getAppAStrength() { return appAStrength; }
    public int getAppBStrength() { return appBStrength; }
    public int getAppAMaxStrength() { return appAMaxStrength; }
    public int getAppBMaxStrength() { return appBMaxStrength; }

    public double getChannelAIntensity() { return channelAIntensity; }
    public double getChannelBIntensity() { return channelBIntensity; }
    public String getChannelAStatus() { return channelAStatus; }
    public String getChannelBStatus() { return channelBStatus; }
    public int getLastSentIntensityA() { return lastSentIntensityA; }
    public int getLastSentIntensityB() { return lastSentIntensityB; }
}
