package com.dglab.coyote.blockentity;

import com.dglab.coyote.config.CoyoteConfig;
import com.dglab.coyote.network.CoyoteManager;
import com.simibubi.create.content.kinetics.base.IKineticNode;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.Shafticulate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraftforge.network.NetworkEvent;

public class StressSensorBlockEntity extends KineticBlockEntity {

    private boolean connectedToNetwork = false;
    private float lastStressRatio = -1;
    private int tickCounter = 0;

    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public StressSensorBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    public void tick() {
        super.tick();

        tickCounter++;
        if (tickCounter % 20 != 0) {
            return;
        }

        if (level == null || level.isClientSide) {
            return;
        }

        float stressRatio = calculateStressRatio();
        boolean isOverloaded = checkOverload();

        if (Math.abs(stressRatio - lastStressRatio) > 0.01f) {
            lastStressRatio = stressRatio;
            sendStressUpdate(stressRatio, isOverloaded);
        }
    }

    private float calculateStressRatio() {
        if (!(getBlockState().getBlock() instanceof IKineticNode)) {
            return 0f;
        }

        IKineticNode node = (IKineticNode) getBlockState().getBlock();
        Direction shaftDir = getPrimaryFacing();

        if (shaftDir == null) {
            return 0f;
        }

        return 0.5f;
    }

    private boolean checkOverload() {
        return false;
    }

    private void sendStressUpdate(float stressRatio, boolean isOverloaded) {
        int strength;

        if (stressRatio < 0.1f) {
            strength = 0;
        } else if (isOverloaded) {
            strength = 100;
        } else {
            float normalizedRatio = (stressRatio - 0.1f) / 0.9f;
            strength = (int) (10 + normalizedRatio * 70);
        }

        CoyoteManager.getInstance().sendPulse(strength, isOverloaded);
    }

    @Override
    public void invalidate() {
        super.invalidate();
        CoyoteManager.getInstance().disconnect();
    }
}