package com.jiemo.createdglab.registry;

import com.jiemo.createdglab.CreateDGLab;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, CreateDGLab.MODID);

    public static final RegistryObject<Item> STRESS_SENSOR = ITEMS.register("stress_sensor",
            () -> new SensorBlockItem(ModBlocks.STRESS_SENSOR.get(), new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
