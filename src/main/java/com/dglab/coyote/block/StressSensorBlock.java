package com.dglab.coyote.block;

import com.dglab.coyote.CoyoteMod;
import com.dglab.coyote.blockentity.StressSensorBlockEntity;
import com.simibubi.create.content.kinetics.base.HorizontalDirectionalBlock;
import com.simibubi.create.foundation.block.connected.ConnectedTextureRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class StressSensorBlock extends HorizontalDirectionalBlock {

    public static final DirectionProperty FACING = DirectionProperty.create("facing",
            Direction.Plane.HORIZONTAL.facingSupplier());

    public StressSensorBlock() {
        super();
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    public DirectionProperty getFacingProperty() {
        return FACING;
    }

    @Override
    public boolean hasShaft(LevelAccessor world, BlockPos pos) {
        return true;
    }

    @Override
    public Direction getShaftDirection(BlockState state) {
        return state.getValue(FACING);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new StressSensorBlockEntity(pos, state);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return Voxels.halfBox(Direction.UP, Direction.DOWN);
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof StressSensorBlockEntity) {
                ((StressSensorBlockEntity) be).disconnect();
            }
        }
        super.onRemove(state, world, pos, newState, moved);
    }
}