package com.jiemo.createdglab.registry;

import com.jiemo.createdglab.CreateDGLab;
import com.jiemo.createdglab.block.StressSensorBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, CreateDGLab.MODID);

    public static final RegistryObject<Block> STRESS_SENSOR = BLOCKS.register("stress_sensor",
            () -> new StressSensorBlock(BlockBehaviour.Properties.of()
                    .noOcclusion()
                    .strength(3.0f)
                    .requiresCorrectToolForDrops()));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
