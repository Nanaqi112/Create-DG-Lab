package com.jiemo.createdglab;

import com.jiemo.createdglab.config.ModConfig;
import com.jiemo.createdglab.network.ModPackets;
import com.jiemo.createdglab.registry.ModBlockEntities;
import com.jiemo.createdglab.registry.ModBlocks;
import com.jiemo.createdglab.registry.ModCreativeTab;
import com.jiemo.createdglab.registry.ModItems;
import com.jiemo.createdglab.websocket.WebSocketServerManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(CreateDGLab.MODID)
public class CreateDGLab {
    public static final String MODID = "createdglab";
    public static final Logger LOGGER = LogManager.getLogger();

    public CreateDGLab(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        // Register config
        ModLoadingContext.get().registerConfig(Type.COMMON, ModConfig.SPEC);

        // Register deferred registers
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModCreativeTab.register(modEventBus);

        // Register lifecycle events
        modEventBus.addListener(this::commonSetup);

        // Register forge events
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.addListener(this::onServerStopping);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ModPackets.register();
            WebSocketServerManager.getInstance().start();
            LOGGER.info("[Create DG-Lab] v1.0.0 loaded - WebSocket server started");
        });
    }

    private void onServerStopping(ServerStoppingEvent event) {
        WebSocketServerManager.getInstance().stop();
    }
}
