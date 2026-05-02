package com.jiemo.createdglab.network;

import com.jiemo.createdglab.block.StressSensorBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class StressSyncPacket {
    private final BlockPos pos;
    private final float stress;
    private final float capacity;
    private final boolean overStressed;

    public StressSyncPacket(BlockPos pos, float stress, float capacity, boolean overStressed) {
        this.pos = pos;
        this.stress = stress;
        this.capacity = capacity;
        this.overStressed = overStressed;
    }

    public static void encode(StressSyncPacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeFloat(packet.stress);
        buf.writeFloat(packet.capacity);
        buf.writeBoolean(packet.overStressed);
    }

    public static StressSyncPacket decode(FriendlyByteBuf buf) {
        return new StressSyncPacket(buf.readBlockPos(), buf.readFloat(), buf.readFloat(), buf.readBoolean());
    }

    public static void handle(StressSyncPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (Minecraft.getInstance().level != null) {
                var be = Minecraft.getInstance().level.getBlockEntity(packet.pos);
                if (be instanceof StressSensorBlockEntity sensor) {
                    // Client-side update handled by the block entity's updateFromNetwork
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
