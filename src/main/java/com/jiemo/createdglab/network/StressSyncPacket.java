package com.jiemo.createdglab.network;

import com.jiemo.createdglab.CreateDGLab;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record StressSyncPacket(BlockPos pos, float stress, float capacity, boolean overStressed)
        implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<StressSyncPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(CreateDGLab.MODID, "stress_sync"));

    public static final StreamCodec<ByteBuf, StressSyncPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, StressSyncPacket::pos,
            ByteBufCodecs.FLOAT, StressSyncPacket::stress,
            ByteBufCodecs.FLOAT, StressSyncPacket::capacity,
            ByteBufCodecs.BOOL, StressSyncPacket::overStressed,
            StressSyncPacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
