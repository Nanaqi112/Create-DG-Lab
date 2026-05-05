package com.jiemo.createdglab.registry;

import com.jiemo.createdglab.CreateDGLab;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(CreateDGLab.MODID);

    public static final DeferredItem<Item> STRESS_SENSOR = ITEMS.register("stress_sensor",
            () -> new SensorBlockItem(ModBlocks.STRESS_SENSOR.get(), new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
