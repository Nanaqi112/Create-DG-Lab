package com.dglab.coyote.block;

import net.minecraft.core.Direction;

public class Voxels {

    public static net.minecraft.world.phys.shapes.VoxelShape halfBox(Direction up, Direction down) {
        return net.minecraft.world.phys.shapes.VoxelShapes.box(0.125, 0, 0.125, 0.875, 1, 0.875);
    }
}