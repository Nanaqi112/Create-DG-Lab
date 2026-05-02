package com.jiemo.createdglab.websocket;

import com.google.gson.JsonObject;

public class DGLabProtocol {
    public static final String APP_CLIENT_ID = "1234-123456789-12345-12345-01";
    public static final int HEARTBEAT_INTERVAL_MS = 60000;
    public static final int MAX_STRENGTH = 200;

    public static JsonObject createBindMessage(String clientId, String targetId, String message) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "bind");
        json.addProperty("clientId", clientId);
        json.addProperty("targetId", targetId);
        json.addProperty("message", message);
        return json;
    }

    public static JsonObject createHeartbeatMessage(String clientId, String targetId) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "heartbeat");
        json.addProperty("clientId", clientId);
        json.addProperty("targetId", targetId);
        json.addProperty("message", "200");
        return json;
    }

    public static JsonObject createMsgMessage(String clientId, String targetId, String message) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "msg");
        json.addProperty("clientId", clientId);
        json.addProperty("targetId", targetId);
        json.addProperty("message", message);
        return json;
    }

    public static JsonObject createBreakMessage(String clientId, String targetId) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "break");
        json.addProperty("clientId", clientId);
        json.addProperty("targetId", targetId);
        json.addProperty("message", "209");
        return json;
    }

    public static String buildStrengthCommand(int channel, int value) {
        return "strength-" + channel + "+2+" + Math.min(Math.max(value, 0), MAX_STRENGTH);
    }

    public static String buildPulseCommand(String channel, String hexData) {
        return "pulse-" + channel + ":[" + hexData + "]";
    }

    public static String buildClearCommand(int channel) {
        return "clear-" + channel;
    }

    public static int clampStrength(int value) {
        return Math.min(Math.max(value, 0), MAX_STRENGTH);
    }
}
