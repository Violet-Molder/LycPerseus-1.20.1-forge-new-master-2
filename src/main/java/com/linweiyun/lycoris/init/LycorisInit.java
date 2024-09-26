package com.linweiyun.lycoris.init;


import com.linweiyun.lycoris.LPItemGroups;
import com.linweiyun.lycoris.block.LPBlocks;
import com.linweiyun.lycoris.block.blockentity.LPBlockEntities;
import com.linweiyun.lycoris.items.LPItems;
import com.linweiyun.lycoris.recipetype.LPerseusRecipe;
import com.linweiyun.lycoris.screen.*;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.eventbus.api.IEventBus;

public class LycorisInit {
    public static void registerLPItems (IEventBus eventBus) {
        LPItems.register(eventBus);
    }
    public static void registerLPBlocks (IEventBus eventBus) {
        LPBlocks.register(eventBus);
    }
    public static void registerLPBlockEntities (IEventBus eventBus) {
        LPBlockEntities.register(eventBus);
    }

    public static void registerLPMenuType (IEventBus eventBus) {
        LycorisMenuType.register(eventBus);
    }
    public static void registerLPRecipeType (IEventBus eventBus) {
        LPerseusRecipe.register(eventBus);
    }
    public static void registerLPScreens() {
        MenuScreens.register(LycorisMenuType.DIMENSION_FINDER_MENU_TYPE.get(), DimensionFinderScreen::new);
        MenuScreens.register(LycorisMenuType.ASSEMBLING_MACHINE_MENU.get(), AssemblingMachineScreen::new);
        MenuScreens.register(LycorisMenuType.DECOMPOSITION_EXTRACTOR_MENU.get(), DecompositionExtractorScreen::new);
    }

    public static void registerLPItemgroups(IEventBus eventBus) {
        LPItemGroups.register(eventBus);
    }

}
