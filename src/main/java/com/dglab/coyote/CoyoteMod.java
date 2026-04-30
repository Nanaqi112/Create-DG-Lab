package com.dglab.coyote;

import com.dglab.coyote.block.CoyoteBlocks;
import com.dglab.coyote.block.StressSensorBlock;
import com.dglab.coyote.blockentity.StressSensorBlockEntity;
import com.dglab.coyote.config.CoyoteConfig;
import com.dglab.coyote.network.CoyoteManager;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(CoyoteMod.MOD_ID)
public class CoyoteMod {

    public static final String MOD_ID = "create-kinetic-link";

    public static final DeferredRegister<net.minecraft.world.level.block.Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MOD_ID);

    public static final RegistryObject<StressSensorBlock> STRESS_SENSOR = BLOCKS.register("stress_sensor", StressSensorBlock::new);
    public static final RegistryObject<BlockEntityType<StressSensorBlockEntity>> STRESS_SENSOR_BE = BLOCK_ENTITIES.register("stress_sensor", () ->
            BlockEntityType.Builder.of(StressSensorBlockEntity::new, STRESS_SENSOR.get()).build(null));

    public CoyoteMod() {
        IEventBus modEventBus = net.minecraftforge.fml.Logging.FMLBus;

        BLOCKS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        CoyoteBlocks.register(modEventBus);
    }

    public static void init(FMLCommonSetupEvent event) {
        CoyoteConfig.load();
        CoyoteManager.start();
    }

    public static void clientInit(FMLClientSetupEvent event) {
    }
}