package com.linweiyun.lycoris.compat;

import com.linweiyun.lycoris.LycPerseusMod;
import com.linweiyun.lycoris.recipetype.AssemblingMachineRecipe;
import com.linweiyun.lycoris.recipetype.DecompositionExtractorRecipe;
import com.linweiyun.lycoris.screen.AssemblingMachineScreen;
import com.linweiyun.lycoris.screen.DecompositionExtractorScreen;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.List;

@JeiPlugin
public class JEILPPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(LycPerseusMod.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new AssemblingMachineCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new DecompositionExtractorCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();

        List<AssemblingMachineRecipe> DimensionFinderRecipes = recipeManager.getAllRecipesFor(AssemblingMachineRecipe.Type.INSTANCE);
        registration.addRecipes(AssemblingMachineCategory.ASSEMBLING_MACHINE_TYPE, DimensionFinderRecipes);

        List<DecompositionExtractorRecipe> DecompositionExtractorRecipes = recipeManager.getAllRecipesFor(DecompositionExtractorRecipe.Type.INSTANCE);
        registration.addRecipes(DecompositionExtractorCategory.DECOMPOSITION_EXTRACTOR_TYPE, DecompositionExtractorRecipes);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(AssemblingMachineScreen.class, 90, 29, 21, 16, AssemblingMachineCategory.ASSEMBLING_MACHINE_TYPE);
        registration.addRecipeClickArea(DecompositionExtractorScreen.class, 63, 21, 12, 10, DecompositionExtractorCategory.DECOMPOSITION_EXTRACTOR_TYPE);
    }
}