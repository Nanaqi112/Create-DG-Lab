package com.jiemo.createdglab.block;

import com.jiemo.createdglab.CreateDGLab;
import com.jiemo.createdglab.config.ModConfig;
import com.jiemo.createdglab.registry.ModBlockEntities;
import com.jiemo.createdglab.websocket.WebSocketServerManager;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class StressSensorBlockEntity extends KineticBlockEntity {

    private float lastRatio = -1f;
    private boolean lastOverStressed = false;
    private int tickCounter = 0;
    private boolean forceUpdate = false;
    private static final int UPDATE_INTERVAL = 4; // ticks between WebSocket updates (~200ms)
    private static final float CHANGE_THRESHOLD = 0.01f; // 1% change threshold

    private boolean channelA = true;
    private boolean channelB = false;

    // Waveform must be resent periodically to keep device outputting
    private int waveformTickCounter = 0;
    private static final int WAVEFORM_REFRESH_INTERVAL = 20; // resend waveform every 1 second

    // Track connected state for blockstate update (controls antenna bulb)
    private boolean lastConnected = false;

    public StressSensorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.STRESS_SENSOR.get(), pos, state);
    }

    @Override
    public void updateFromNetwork(float maxStress, float currentStress, int networkSize) {
        super.updateFromNetwork(maxStress, currentStress, networkSize);
        forceUpdate = true;
        setChanged();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, StressSensorBlockEntity be) {
        be.tick();
    }

    @Override
    public void tick() {
        super.tick();

        if (level == null || level.isClientSide) return;

        // Update connected blockstate for antenna bulb
        boolean currentlyConnected = WebSocketServerManager.getInstance().isConnected();
        if (currentlyConnected != lastConnected) {
            lastConnected = currentlyConnected;
            BlockState newState = getBlockState().setValue(StressSensorBlock.CONNECTED, currentlyConnected);
            level.setBlock(worldPosition, newState, 3);
        }

        tickCounter++;
        waveformTickCounter++;

        boolean shouldUpdate = forceUpdate || tickCounter >= UPDATE_INTERVAL;
        if (!shouldUpdate && lastRatio >= 0) return;
        tickCounter = 0;

        float currentRatio = computeStressRatio();
        boolean currentlyOverStressed = isOverStressed();

        boolean changed = forceUpdate
                || Math.abs(currentRatio - lastRatio) > CHANGE_THRESHOLD
                || currentlyOverStressed != lastOverStressed;

        forceUpdate = false;

        float percent = mapIntensityPercent(currentRatio, currentlyOverStressed);
        WebSocketServerManager ws = WebSocketServerManager.getInstance();

        // Sync to client when values change
        if (changed || lastRatio < 0) {
            lastRatio = currentRatio;
            lastOverStressed = currentlyOverStressed;
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }

        // Send waveform + strength to device (both channels synced)
        if (percent > 0 || currentlyOverStressed) {
            boolean needsWaveform = currentlyOverStressed
                    || waveformTickCounter >= WAVEFORM_REFRESH_INTERVAL
                    || changed;

            if (needsWaveform) {
                waveformTickCounter = 0;
                ws.sendStressWaveform(percent, currentlyOverStressed);

                CreateDGLab.LOGGER.info("[DG-Lab] Waveform+strength: {}% stress={}/{} overStressed={}",
                        String.format("%.0f", percent * 100),
                        String.format("%.1f", stress), String.format("%.1f", capacity),
                        currentlyOverStressed);
            }
        } else if (changed && lastRatio >= 0) {
            // Stress dropped to 0 — stop output
            ws.sendStressWaveform(0, false);
            waveformTickCounter = 0;
        }
    }

    private float computeStressRatio() {
        if (capacity <= 0) return 0f;
        return stress / capacity;
    }

    /**
     * Map stress ratio to output percentage (0.0 ~ 1.0).
     * This percentage is then multiplied by the app's configured max strength per channel.
     */
    private float mapIntensityPercent(float ratio, boolean overStressed) {
        if (overStressed) {
            return 1.0f; // 100% of user's max
        }

        double low = ModConfig.LOW_THRESHOLD.get();
        double mid = ModConfig.MID_THRESHOLD.get();

        if (ratio <= low) {
            return 0f;
        } else if (ratio <= mid) {
            // Ramp from 0% to 60%
            float t = (float) ((ratio - low) / (mid - low));
            return t * 0.6f;
        } else if (ratio <= 1.0f) {
            // Ramp from 60% to 100%
            float t = (float) ((ratio - mid) / (1.0 - mid));
            return 0.6f + t * 0.4f;
        } else {
            return 1.0f;
        }
    }

    public Component getStatusMessage() {
        float ratio = computeStressRatio();
        boolean overStressed = isOverStressed();

        String status;
        ChatFormatting color;
        if (!hasNetwork()) {
            status = "Not connected to kinetic network";
            color = ChatFormatting.GRAY;
        } else if (overStressed) {
            status = String.format("OVERLOADED! Stress: %.1f / %.1f (%.0f%%)", stress, capacity, ratio * 100);
            color = ChatFormatting.RED;
        } else {
            status = String.format("Stress: %.1f / %.1f (%.0f%%)", stress, capacity, ratio * 100);
            if (ratio > 0.85) color = ChatFormatting.YELLOW;
            else if (ratio > 0.6) color = ChatFormatting.GOLD;
            else color = ChatFormatting.GREEN;
        }

        String wsStatus = WebSocketServerManager.getInstance().isConnected()
                ? "Connected to DG-Lab"
                : "Waiting for DG-Lab connection";

        return Component.literal("[Create DG-Lab] " + status).withStyle(color)
                .append(Component.literal(" | " + wsStatus).withStyle(ChatFormatting.GRAY));
    }

    public void setChannelA(boolean enabled) { this.channelA = enabled; setChanged(); }
    public void setChannelB(boolean enabled) { this.channelB = enabled; setChanged(); }
    public boolean isChannelA() { return channelA; }
    public boolean isChannelB() { return channelB; }
    public float getLastRatio() { return lastRatio; }
    public float getStress() { return stress; }
    public float getCapacity() { return capacity; }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putBoolean("ChannelA", channelA);
        tag.putBoolean("ChannelB", channelB);
        tag.putFloat("LastRatio", lastRatio);
        tag.putFloat("Stress", stress);
        tag.putFloat("Capacity", capacity);
        tag.putBoolean("OverStressed", overStressed);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        channelA = tag.getBoolean("ChannelA");
        channelB = tag.getBoolean("ChannelB");
        lastRatio = tag.getFloat("LastRatio");
        stress = tag.getFloat("Stress");
        capacity = tag.getFloat("Capacity");
        overStressed = tag.getBoolean("OverStressed");
    }
}
