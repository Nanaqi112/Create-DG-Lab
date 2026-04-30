package com.dglab.coyote.network;

import com.dglab.coyote.config.CoyoteConfig;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CoyoteManager {

    private static CoyoteManager instance;
    private CoyoteWebSocketClient webSocketClient;
    private final ConcurrentLinkedQueue<PulseCommand> commandQueue;
    private final ExecutorService executorService;
    private boolean isConnected = false;

    public static final int DEFAULT_PORT = 29782;
    public static final String DEFAULT_HOST = "127.0.0.1";

    private CoyoteManager() {
        commandQueue = new ConcurrentLinkedQueue<>();
        executorService = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "Coyote-WebSocket-Thread");
            t.setDaemon(true);
            return t;
        });
    }

    public static CoyoteManager getInstance() {
        if (instance == null) {
            instance = new CoyoteManager();
        }
        return instance;
    }

    public static void start() {
        getInstance().connect();
    }

    public void connect() {
        String host = CoyoteConfig.serverHost;
        int port = CoyoteConfig.serverPort;

        executorService.execute(() -> {
            try {
                String uri = "ws://" + host + ":" + port;
                webSocketClient = new CoyoteWebSocketClient(new URI(uri));
                webSocketClient.connect();
            } catch (URISyntaxException e) {
                System.err.println("[CoyoteManager] Invalid URI: " + e.getMessage());
            }
        });
    }

    public void disconnect() {
        if (webSocketClient != null) {
            webSocketClient.close();
        }
        isConnected = false;
    }

    public void sendPulse(int strength, boolean isFastPulse) {
        commandQueue.offer(new PulseCommand(strength, isFastPulse));

        executorService.execute(this::processQueue);
    }

    private void processQueue() {
        if (!isConnected || webSocketClient == null) {
            return;
        }

        PulseCommand cmd;
        while ((cmd = commandQueue.poll()) != null) {
            JsonObject json = new JsonObject();
            json.addProperty("strength", cmd.strength);
            json.addProperty("fastPulse", cmd.fastPulse);

            String message = json.toString();
            webSocketClient.send(message);
        }
    }

    private class CoyoteWebSocketClient extends WebSocketClient {

        public CoyoteWebSocketClient(URI serverURI) {
            super(serverURI);
            setConnectionLostTimeout(0);
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            isConnected = true;
            System.out.println("[CoyoteManager] Connected to DG-Lab server");
        }

        @Override
        public void onMessage(String message) {
            System.out.println("[CoyoteManager] Received: " + message);
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            isConnected = false;
            System.out.println("[CoyoteManager] Disconnected: " + reason);
        }

        @Override
        public void onError(Exception ex) {
            System.err.println("[CoyoteManager] WebSocket error: " + ex.getMessage());
        }
    }

    private static class PulseCommand {
        final int strength;
        final boolean fastPulse;

        PulseCommand(int strength, boolean fastPulse) {
            this.strength = strength;
            this.fastPulse = fastPulse;
        }
    }
}