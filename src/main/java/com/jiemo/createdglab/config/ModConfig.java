package com.jiemo.createdglab.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ModConfig {
    public static final ForgeConfigSpec SPEC;

    // WebSocket
    public static final ForgeConfigSpec.IntValue WS_PORT;
    public static final ForgeConfigSpec.ConfigValue<String> WS_HOST;

    // Strength mapping (stress ratio thresholds)
    public static final ForgeConfigSpec.DoubleValue LOW_THRESHOLD;
    public static final ForgeConfigSpec.DoubleValue MID_THRESHOLD;

    // Channel mapping
    public static final ForgeConfigSpec.BooleanValue CHANNEL_A_ENABLED;
    public static final ForgeConfigSpec.BooleanValue CHANNEL_B_ENABLED;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        // WebSocket section
        builder.push("websocket");
        WS_PORT = builder
                .comment("WebSocket server port (default: 8877)")
                .defineInRange("port", 8877, 1024, 65535);
        WS_HOST = builder
                .comment("WebSocket server host (0.0.0.0 = all interfaces)")
                .define("host", "0.0.0.0");
        builder.pop();

        // Strength mapping section
        builder.push("strength");
        LOW_THRESHOLD = builder
                .comment("Stress ratio below this value produces no output (0.0-1.0)")
                .defineInRange("lowThreshold", 0.6, 0.0, 1.0);
        MID_THRESHOLD = builder
                .comment("Stress ratio where output ramps steeply (0.0-1.0)")
                .defineInRange("midThreshold", 0.85, 0.0, 1.0);
        builder.pop();

        // Channel mapping section
        builder.push("channel");
        CHANNEL_A_ENABLED = builder
                .comment("Send stress data to DG-Lab channel A")
                .define("channelA", true);
        CHANNEL_B_ENABLED = builder
                .comment("Send stress data to DG-Lab channel B")
                .define("channelB", false);
        builder.pop();

        SPEC = builder.build();
    }
}
