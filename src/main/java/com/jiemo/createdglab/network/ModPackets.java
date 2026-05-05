package com.jiemo.createdglab.network;

import com.jiemo.createdglab.CreateDGLab;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = CreateDGLab.MODID)
public class ModPackets {

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        registrar.playToClient(
                StressSyncPacket.TYPE,
                StressSyncPacket.STREAM_CODEC,
                ModPackets::handleStressSync
        );
    }

    private static void handleStressSync(StressSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().level != null) {
                var be = Minecraft.getInstance().level.getBlockEntity(packet.pos());
                if (be instanceof com.jiemo.createdglab.block.StressSensorBlockEntity sensor) {
                    // Client-side update handled by the block entity's sync
                }
            }
        });
    }
}
