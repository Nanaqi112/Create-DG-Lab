package com.jiemo.createdglab.registry;

import com.jiemo.createdglab.CreateDGLab;
import com.jiemo.createdglab.block.StressSensorBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(CreateDGLab.MODID);

    public static final DeferredBlock<Block> STRESS_SENSOR = BLOCKS.register("stress_sensor",
            () -> new StressSensorBlock(BlockBehaviour.Properties.of()
                    .noOcclusion()
                    .strength(3.0f)
                    .requiresCorrectToolForDrops()));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
