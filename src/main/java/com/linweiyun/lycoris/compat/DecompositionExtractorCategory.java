package com.linweiyun.lycoris.compat;


import com.linweiyun.lycoris.LycPerseusMod;
import com.linweiyun.lycoris.block.LPBlocks;
import com.linweiyun.lycoris.items.LPItems;
import com.linweiyun.lycoris.recipetype.DecompositionExtractorRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;


public class DecompositionExtractorCategory implements IRecipeCategory<DecompositionExtractorRecipe> {

    public static final ResourceLocation UID = new ResourceLocation(LycPerseusMod.MOD_ID, "decomposition_extractor");
    public static final ResourceLocation TEXTURE = new ResourceLocation(LycPerseusMod.MOD_ID,
            "textures/gui/jei/decomposition_extractor_jei.png");

    public static final RecipeType<DecompositionExtractorRecipe> DECOMPOSITION_EXTRACTOR_TYPE =
            new RecipeType<>(UID, DecompositionExtractorRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public DecompositionExtractorCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 110, 48);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(LPBlocks.DECOMPOSITION_EXTRACTOR.get()));
    }

    @Override
    public RecipeType<DecompositionExtractorRecipe> getRecipeType() {
        return DECOMPOSITION_EXTRACTOR_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.lyc_perseus.decomposition_extractor");
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
    public void setRecipe(IRecipeLayoutBuilder builder, DecompositionExtractorRecipe recipe, IFocusGroup focuses) {
        // 根据实际的输入项数量设置槽位
        for (int i = 0; i < Math.min(recipe.getIngredients().size(), 6); i++) { // 确保不会超过6个槽位
            int x = 50 + (i % 3) * 18; // 槽位x坐标
            int y = 7 + (i / 3) * 18; // 槽位y坐标

            builder.addSlot(RecipeIngredientRole.OUTPUT, x, y)
                    .addIngredients(recipe.getIngredients().get(i));
        }

        // 创建一个 List<ItemStack>
        List<ItemStack> batteryStacks = new ArrayList<>();

        // 添加 LPItems 中的三个物品
        batteryStacks.add(new ItemStack(LPItems.POTATO_POWER.get()));
        batteryStacks.add(new ItemStack(LPItems.DISPOSABLE_BATTERIES.get()));
        batteryStacks.add(new ItemStack(LPItems.STORAGE_BATTERY.get()));

        builder.addSlot(RecipeIngredientRole.INPUT, 7, 7).addIngredients(recipe.getOutput());
        builder.addSlot(RecipeIngredientRole.INPUT, 7, 25).addItemStacks(batteryStacks);



    }
}
