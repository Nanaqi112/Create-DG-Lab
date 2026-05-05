package com.jiemo.createdglab.gui;

import com.jiemo.createdglab.block.StressSensorBlockEntity;
import com.jiemo.createdglab.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class SensorMenu extends AbstractContainerMenu {
    private final BlockPos blockPos;
    private final ContainerLevelAccess levelAccess;

    // DataSlot only supports int, so we store float*100
    private int stressInt = 0;
    private int capacityInt = 0;
    private int overStressedInt = 0;

    // Server-side constructor
    public SensorMenu(int containerId, Inventory playerInv, BlockPos pos) {
        super(ModMenus.SENSOR.get(), containerId);
        this.blockPos = pos;
        this.levelAccess = ContainerLevelAccess.create(playerInv.player.level(), pos);

        // Sync stress data from server to client
        addDataSlot(new DataSlot() {
            @Override
            public int get() { return stressInt; }
            @Override
            public void set(int value) { stressInt = value; }
        });
        addDataSlot(new DataSlot() {
            @Override
            public int get() { return capacityInt; }
            @Override
            public void set(int value) { capacityInt = value; }
        });
        addDataSlot(new DataSlot() {
            @Override
            public int get() { return overStressedInt; }
            @Override
            public void set(int value) { overStressedInt = value; }
        });
    }

    // Client-side constructor (from network)
    public SensorMenu(int containerId, Inventory playerInv, FriendlyByteBuf extraData) {
        this(containerId, playerInv, extraData.readBlockPos());
    }

    @Override
    public void broadcastChanges() {
        // Read latest data from block entity on server side
        levelAccess.execute((level, pos) -> {
            if (!level.isClientSide) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof StressSensorBlockEntity sensor) {
                    this.stressInt = (int) (sensor.getStress() * 100);
                    this.capacityInt = (int) (sensor.getCapacity() * 100);
                    this.overStressedInt = sensor.isOverStressed() ? 1 : 0;
                }
            }
        });
        super.broadcastChanges();
    }

    public float getStress() { return stressInt / 100f; }
    public float getCapacity() { return capacityInt / 100f; }
    public boolean isOverStressed() { return overStressedInt > 0; }
    public BlockPos getBlockPos() { return blockPos; }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return levelAccess.evaluate((level, pos) ->
                level.getBlockEntity(pos) instanceof StressSensorBlockEntity
                        && player.canInteractWithBlock(pos, 4.0), true);
    }
}
