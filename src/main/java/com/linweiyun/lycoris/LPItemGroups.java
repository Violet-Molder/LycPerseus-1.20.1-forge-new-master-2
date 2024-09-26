package com.linweiyun.lycoris;

import com.linweiyun.lycoris.block.LPBlocks;
import com.linweiyun.lycoris.items.LPItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class LPItemGroups {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, LycPerseusMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> LP_TAB =
            CREATIVE_MODE_TABS.register("lp_tab_1",
                    () -> CreativeModeTab.builder().icon(()-> new ItemStack(LPBlocks.DIMENSION_FINDER.get()))
                            .title(Component.translatable("creativetab.lp_tab"))
                            .displayItems((pParameters, pOutbut) -> {
                                pOutbut.accept(LPItems.AUXILIARY_REACTION_FURNACE.get());
                                pOutbut.accept(LPItems.SHADOW_TRANSMISSION_MODULE.get());
                                pOutbut.accept(LPItems.UTILITY_MINING_RUNE.get());
                                pOutbut.accept(LPItems.PHILOSOPHER_STONE.get());
                                pOutbut.accept(LPItems.DIVINE_CRYSTAL.get());
                                pOutbut.accept(LPItems.BIOSOLID_FUELS.get());
                                pOutbut.accept(LPBlocks.DIMENSION_FINDER.get());

                                pOutbut.accept(LPBlocks.ASSEMBLING_MACHINE.get());
                                pOutbut.accept(LPBlocks.DECOMPOSITION_EXTRACTOR.get());
                                pOutbut.accept(LPItems.POTATO_POWER.get());
                                pOutbut.accept(LPItems.DISPOSABLE_BATTERIES.get());
                                pOutbut.accept(LPItems.STORAGE_BATTERY.get());



                            })

                            .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
