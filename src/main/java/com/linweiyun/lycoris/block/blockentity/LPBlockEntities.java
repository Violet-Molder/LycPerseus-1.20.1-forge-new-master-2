package com.linweiyun.lycoris.block.blockentity;

import com.linweiyun.lycoris.LycPerseusMod;
import com.linweiyun.lycoris.block.LPBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class LPBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister
            .create(ForgeRegistries.BLOCK_ENTITY_TYPES, LycPerseusMod.MOD_ID);

    public static final RegistryObject<BlockEntityType<DimensionFinderEntity>> DIMENSION_FINDER = BLOCK_ENTITIES.register("dimension_finder",
            () -> BlockEntityType.Builder.of(DimensionFinderEntity::new, LPBlocks.DIMENSION_FINDER.get()).build(null));

    public static final RegistryObject<BlockEntityType<AssemblingMachineBlockEntity>> ASSEMBLING_MACHINE = BLOCK_ENTITIES.register("assembling_machine",
            () -> BlockEntityType.Builder.of(AssemblingMachineBlockEntity::new, LPBlocks.ASSEMBLING_MACHINE.get()).build(null));

    public static final RegistryObject<BlockEntityType<DecompositionExtractorBlockEntity>> DECOMPOSITION_EXTRACTOR = BLOCK_ENTITIES.register("decomposition_extractor",
            () -> BlockEntityType.Builder.of(DecompositionExtractorBlockEntity::new, LPBlocks.DECOMPOSITION_EXTRACTOR.get()).build(null));


    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
