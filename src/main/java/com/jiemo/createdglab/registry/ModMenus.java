package com.jiemo.createdglab.registry;

import com.jiemo.createdglab.CreateDGLab;
import com.jiemo.createdglab.gui.SensorMenu;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(BuiltInRegistries.MENU, CreateDGLab.MODID);

    public static final Supplier<MenuType<SensorMenu>> SENSOR =
            MENUS.register("sensor", () -> IMenuTypeExtension.create(SensorMenu::new));

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
