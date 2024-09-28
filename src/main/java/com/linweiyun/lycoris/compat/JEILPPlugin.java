package com.linweiyun.lycoris.compat;

import com.linweiyun.lycoris.LycPerseusMod;
import com.linweiyun.lycoris.recipetype.AssemblingMachineRecipe;
import com.linweiyun.lycoris.recipetype.DecompositionExtractorRecipe;
import com.linweiyun.lycoris.recipetype.DimensionFinderRecipeType;
import com.linweiyun.lycoris.recipetype.WeaponWorkbenchRecipeType;
import com.linweiyun.lycoris.screen.AssemblingMachineScreen;
import com.linweiyun.lycoris.screen.DecompositionExtractorScreen;
import com.linweiyun.lycoris.screen.DimensionFinderScreen;
import com.linweiyun.lycoris.screen.WeaponWorkbenchScreen;
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
        registration.addRecipeCategories(new DimensionFinderCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new WeaponWorkbenchCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();

        List<AssemblingMachineRecipe> AssemblingMachineRecipes = recipeManager.getAllRecipesFor(AssemblingMachineRecipe.Type.INSTANCE);
        registration.addRecipes(AssemblingMachineCategory.ASSEMBLING_MACHINE_TYPE, AssemblingMachineRecipes);

        List<DecompositionExtractorRecipe> DecompositionExtractorRecipes = recipeManager.getAllRecipesFor(DecompositionExtractorRecipe.Type.INSTANCE);
        registration.addRecipes(DecompositionExtractorCategory.DECOMPOSITION_EXTRACTOR_TYPE, DecompositionExtractorRecipes);

        List<DimensionFinderRecipeType> DimensionFinderRecipes = recipeManager.getAllRecipesFor(DimensionFinderRecipeType.Type.INSTANCE);
        registration.addRecipes(DimensionFinderCategory.DIMENSION_FINDER_TYPE, DimensionFinderRecipes);

        List<WeaponWorkbenchRecipeType> WeaponWorkbenchRecipes = recipeManager.getAllRecipesFor(WeaponWorkbenchRecipeType.Type.INSTANCE);
        registration.addRecipes(WeaponWorkbenchCategory.WEAPON_WORKBENCH_RECIPE_TYPE_RECIPE_TYPE, WeaponWorkbenchRecipes);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(AssemblingMachineScreen.class, 90, 29, 21, 16, AssemblingMachineCategory.ASSEMBLING_MACHINE_TYPE);
        registration.addRecipeClickArea(DecompositionExtractorScreen.class, 63, 21, 12, 10, DecompositionExtractorCategory.DECOMPOSITION_EXTRACTOR_TYPE);
        registration.addRecipeClickArea(DimensionFinderScreen.class, 57, 23, 54, 54, DimensionFinderCategory.DIMENSION_FINDER_TYPE);
        registration.addRecipeClickArea(WeaponWorkbenchScreen.class, 129, 54, 10, 14, WeaponWorkbenchCategory.WEAPON_WORKBENCH_RECIPE_TYPE_RECIPE_TYPE);
    }
}