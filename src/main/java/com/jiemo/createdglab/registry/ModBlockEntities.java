package com.jiemo.createdglab.registry;

import com.jiemo.createdglab.CreateDGLab;
import com.jiemo.createdglab.block.StressSensorBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, CreateDGLab.MODID);

    public static final RegistryObject<BlockEntityType<StressSensorBlockEntity>> STRESS_SENSOR =
            BLOCK_ENTITIES.register("stress_sensor",
                    () -> BlockEntityType.Builder.of(StressSensorBlockEntity::new, ModBlocks.STRESS_SENSOR.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
