package com.linweiyun.lycoris.block;

import com.linweiyun.lycoris.LycPerseusMod;
import com.linweiyun.lycoris.block.custom.*;
import com.linweiyun.lycoris.block.custom.DecompositionExtractor;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class LPBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, LycPerseusMod.MOD_ID);

    public static final RegistryObject<Block> DIMENSION_FINDER = BLOCKS.register("dimension_finder",
            ()-> new DimensionFinder(Block.Properties.of().strength(0.8f, 6.0f).noOcclusion()));

    public static final RegistryObject<Block> ASSEMBLING_MACHINE = BLOCKS.register("assembling_machine",
            ()-> new AssemblingMachine(Block.Properties.of().strength(0.8f, 6.0f).noOcclusion()));

    public static final RegistryObject<Block> DECOMPOSITION_EXTRACTOR = BLOCKS.register("decomposition_extractor",
            ()-> new DecompositionExtractor(Block.Properties.of().strength(0.8f, 6.0f).noOcclusion()));

    public static final RegistryObject<Block> WEAPON_WORKBENCH = BLOCKS.register("weapon_workbench",
            ()-> new WeaponWorkbench(Block.Properties.of().strength(0.8f, 6.0f).noOcclusion()));


    public static void register(IEventBus eventBus)
    {
        BLOCKS.register(eventBus);
    }
}
