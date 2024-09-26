package com.linweiyun.lycoris.compat;


import com.linweiyun.lycoris.LycPerseusMod;
import com.linweiyun.lycoris.block.LPBlocks;
import com.linweiyun.lycoris.items.LPItems;
import com.linweiyun.lycoris.recipetype.AssemblingMachineRecipe;
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


public class AssemblingMachineCategory implements IRecipeCategory<AssemblingMachineRecipe> {

    public static final ResourceLocation UID = new ResourceLocation(LycPerseusMod.MOD_ID, "assembling_machine");
    public static final ResourceLocation TEXTURE = new ResourceLocation(LycPerseusMod.MOD_ID,
            "textures/gui/jei/assembling_machine_jei.png");

    public static final RecipeType<AssemblingMachineRecipe> ASSEMBLING_MACHINE_TYPE =
            new RecipeType<>(UID, AssemblingMachineRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public AssemblingMachineCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 110, 48);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(LPBlocks.ASSEMBLING_MACHINE.get()));
    }

    @Override
    public RecipeType<AssemblingMachineRecipe> getRecipeType() {
        return ASSEMBLING_MACHINE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.lyc_perseus.assembling_machine");
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
    public void setRecipe(IRecipeLayoutBuilder builder, AssemblingMachineRecipe recipe, IFocusGroup focuses) {
        // 根据实际的输入项数量设置槽位
        for (int i = 0; i < Math.min(recipe.getIngredients().size(), 6); i++) { // 确保不会超过6个槽位
            int x = 7 + (i % 3) * 18; // 槽位x坐标
            int y = 7 + (i / 3) * 18; // 槽位y坐标

            builder.addSlot(RecipeIngredientRole.INPUT, x, y)
                    .addIngredients(recipe.getIngredients().get(i));
        }

        // 创建一个 List<ItemStack>
        List<ItemStack> batteryStacks = new ArrayList<>();

        // 添加 LPItems 中的三个物品
        batteryStacks.add(new ItemStack(LPItems.POTATO_POWER.get()));
        batteryStacks.add(new ItemStack(LPItems.DISPOSABLE_BATTERIES.get()));
        batteryStacks.add(new ItemStack(LPItems.STORAGE_BATTERY.get()));

        builder.addSlot(RecipeIngredientRole.OUTPUT, 87, 7).addItemStacks(batteryStacks);

        builder.addSlot(RecipeIngredientRole.OUTPUT, 87, 25).addItemStack(recipe.getResultItem(null));

    }

    @Override
    public void draw(AssemblingMachineRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        guiGraphics.drawString(Minecraft.getInstance().font, "无序合成", 40, -15, 0xffffff);
   }
}
