package com.linweiyun.lycoris.compat;


import com.linweiyun.lycoris.LycPerseusMod;
import com.linweiyun.lycoris.block.LPBlocks;
import com.linweiyun.lycoris.items.LPItems;
import com.linweiyun.lycoris.recipetype.AssemblingMachineRecipe;
import com.linweiyun.lycoris.recipetype.DimensionFinderRecipeType;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;


public class DimensionFinderCategory implements IRecipeCategory<DimensionFinderRecipeType> {

    public static final ResourceLocation UID = new ResourceLocation(LycPerseusMod.MOD_ID, "dimension_finder");
    public static final ResourceLocation TEXTURE = new ResourceLocation(LycPerseusMod.MOD_ID,
            "textures/gui/jei/dimension_finder_jei.png");

    public static final RecipeType<DimensionFinderRecipeType> DIMENSION_FINDER_TYPE =
            new RecipeType<>(UID, DimensionFinderRecipeType.class);

    private final IDrawable background;
    private final IDrawable icon;

    public DimensionFinderCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 134, 60);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(LPBlocks.DIMENSION_FINDER.get()));
    }

    @Override
    public RecipeType<DimensionFinderRecipeType> getRecipeType() {
        return DIMENSION_FINDER_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.lyc_perseus.dimension_finder");
    }

    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, DimensionFinderRecipeType recipe, IFocusGroup focuses) {
        // 根据实际的输入项数量设置槽位
        builder.addSlot(RecipeIngredientRole.INPUT, 9, 22)
                .addIngredients(recipe.getIngredients().get(0));

        // 创建一个 List<ItemStack>
        List<ItemStack> batteryStacks = new ArrayList<>();

        // 添加 LPItems 中的三个物品
        batteryStacks.add(new ItemStack(LPItems.BIOSOLID_FUELS.get()));
        batteryStacks.add(new ItemStack(LPItems.DIVINE_CRYSTAL.get()));
        batteryStacks.add(new ItemStack(LPItems.PHILOSOPHER_STONE.get()));

        builder.addSlot(RecipeIngredientRole.OUTPUT, 32, 22).addItemStacks(batteryStacks);

        builder.addSlot(RecipeIngredientRole.OUTPUT, 110, 22).addItemStack(recipe.getResultItem(null));

    }

}
