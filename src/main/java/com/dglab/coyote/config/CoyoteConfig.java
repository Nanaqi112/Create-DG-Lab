package com.dglab.coyote.config;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class CoyoteConfig {

    public static String serverHost = "127.0.0.1";
    public static int serverPort = 29782;

    private static final String CONFIG_FILE_NAME = "dglab-coyote.cfg";
    private static Properties config;

    public static void load() {
        config = new Properties();
        Path configPath = getConfigPath();

        if (!Files.exists(configPath)) {
            save();
            return;
        }

        try (InputStream input = Files.newInputStream(configPath)) {
            config.load(input);
            serverHost = config.getProperty("serverHost", serverHost);
            serverPort = Integer.parseInt(config.getProperty("serverPort", String.valueOf(serverPort)));
        } catch (IOException e) {
            System.err.println("[CoyoteConfig] Failed to load: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("[CoyoteConfig] Invalid port format");
        }
    }

    public static void save() {
        config = new Properties();
        config.setProperty("serverHost", serverHost);
        config.setProperty("serverPort", String.valueOf(serverPort));

        try (OutputStream output = Files.newOutputStream(getConfigPath())) {
            config.store(output, "DG Lab Coyote Configuration");
        } catch (IOException e) {
            System.err.println("[CoyoteConfig] Failed to save: " + e.getMessage());
        }
    }

    private static Path getConfigPath() {
        return net.minecraftforge.fml.loading.FMLPaths.CONFIGDIR.get().resolve(CONFIG_FILE_NAME);
    }
}