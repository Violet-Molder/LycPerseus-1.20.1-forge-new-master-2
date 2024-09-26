package com.linweiyun.lycoris.screen;
import com.linweiyun.lycoris.LycPerseusMod;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class LycorisMenuType {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, LycPerseusMod.MOD_ID);


    public static final RegistryObject<MenuType<AssemblingMachineMenu>> ASSEMBLING_MACHINE_MENU =
            registerMenuType(AssemblingMachineMenu::new,"assembling_machine_menu_type");
    public static final RegistryObject<MenuType<DecompositionExtractorMenu>> DECOMPOSITION_EXTRACTOR_MENU =
            registerMenuType(DecompositionExtractorMenu::new,"decomposition_extractor_menu_type");

    public static final RegistryObject<MenuType<DimensionFinderMenu>> DIMENSION_FINDER_MENU_TYPE =
            registerMenuType(DimensionFinderMenu::new,"dimension_finder_menu_type");


    private static <T extends AbstractContainerMenu>RegistryObject<MenuType<T>> registerMenuType(IContainerFactory<T> factory,
                                                                                                 String name){
        return MENU_TYPES.register(name,()-> IForgeMenuType.create(factory));
    }
    public static void register(IEventBus eventBus){
        MENU_TYPES.register(eventBus);
    }
}
