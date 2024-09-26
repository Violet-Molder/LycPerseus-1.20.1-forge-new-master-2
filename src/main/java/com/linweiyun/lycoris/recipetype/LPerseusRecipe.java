package com.linweiyun.lycoris.recipetype;

import com.linweiyun.lycoris.LycPerseusMod;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class LPerseusRecipe {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, LycPerseusMod.MOD_ID);


    public static final RegistryObject<RecipeSerializer<AssemblingMachineRecipe>> ASSEMBLING_MACHINE_RECIPE =
            SERIALIZERS.register("assembling_machine_recipe", () -> AssemblingMachineRecipe.Serializer.INSTANCE);


    public static final RegistryObject<RecipeSerializer<DecompositionExtractorRecipe>> DECOMPOSITION_EXTRACTOR_RECIPE =
            SERIALIZERS.register("decomposition_extractor_recipe", () -> DecompositionExtractorRecipe.Serializer.INSTANCE);

    public static final RegistryObject<RecipeSerializer<LPDimensionFinderRecipeType>> LV1_INJECTING_MAGIC_ALTAR_RECIPE_TYPE =
            SERIALIZERS.register("dimension_finder", () -> LPDimensionFinderRecipeType.Serializer.INSTANCE);


    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
    }
}
