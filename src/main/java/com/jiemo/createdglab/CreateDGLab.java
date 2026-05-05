package com.jiemo.createdglab;

import com.jiemo.createdglab.config.ModConfig;
import com.jiemo.createdglab.registry.ModBlockEntities;
import com.jiemo.createdglab.registry.ModBlocks;
import com.jiemo.createdglab.registry.ModCreativeTab;
import com.jiemo.createdglab.registry.ModItems;
import com.jiemo.createdglab.websocket.WebSocketServerManager;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

@Mod(CreateDGLab.MODID)
public class CreateDGLab {
    public static final String MODID = "createdglab";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CreateDGLab(IEventBus modEventBus, ModContainer modContainer) {
        // Register config
        modContainer.registerConfig(Type.COMMON, ModConfig.SPEC);

        // Register deferred registers
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModCreativeTab.register(modEventBus);

        // Register lifecycle events
        modEventBus.addListener(this::commonSetup);

        // Register game events
        NeoForge.EVENT_BUS.addListener(this::onServerStopping);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            WebSocketServerManager.getInstance().start();
            LOGGER.info("[Create DG-Lab] v1.1.0 loaded - WebSocket server started");
        });
    }

    private void onServerStopping(ServerStoppingEvent event) {
        WebSocketServerManager.getInstance().stop();
    }
}
