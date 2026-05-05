package com.jiemo.createdglab.registry;

import com.jiemo.createdglab.CreateDGLab;
import com.jiemo.createdglab.block.StressSensorBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, CreateDGLab.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<StressSensorBlockEntity>> STRESS_SENSOR =
            BLOCK_ENTITIES.register("stress_sensor",
                    () -> BlockEntityType.Builder.of(StressSensorBlockEntity::new, ModBlocks.STRESS_SENSOR.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
