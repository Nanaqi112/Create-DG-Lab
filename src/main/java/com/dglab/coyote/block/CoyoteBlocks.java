package com.dglab.coyote.block;

import com.dglab.coyote.CoyoteMod;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class CoyoteBlocks {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, CoyoteMod.MOD_ID);

    public static final RegistryObject<Item> STRESS_SENSOR_ITEM = ITEMS.register("stress_sensor", () ->
            new BlockItem(CoyoteMod.STRESS_SENSOR.get(), new Item.Properties()
                    .tab(CreativeModeTab.TAB_REDSTONE)));

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}