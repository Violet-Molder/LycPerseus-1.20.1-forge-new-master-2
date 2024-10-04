package com.linweiyun.lycoris.compat;


import com.linweiyun.lycoris.LycPerseusMod;
import com.linweiyun.lycoris.block.LPBlocks;
import com.linweiyun.lycoris.items.LPItems;
import com.linweiyun.lycoris.recipetype.WeaponWorkbenchRecipeType;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;


public class WeaponWorkbenchCategory implements IRecipeCategory<WeaponWorkbenchRecipeType> {

    public static final ResourceLocation UID = new ResourceLocation(LycPerseusMod.MOD_ID, "weapon_workbench");
    public static final ResourceLocation TEXTURE = new ResourceLocation(LycPerseusMod.MOD_ID,
            "textures/gui/jei/weapon_workbench_jei.png");

    public static final RecipeType<WeaponWorkbenchRecipeType> WEAPON_WORKBENCH_RECIPE_TYPE_RECIPE_TYPE =
            new RecipeType<>(UID, WeaponWorkbenchRecipeType.class);

    private final IDrawable background;
    private final IDrawable icon;

    public WeaponWorkbenchCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 140, 67);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(LPBlocks.WEAPON_WORKBENCH.get()));
    }

    @Override
    public RecipeType<WeaponWorkbenchRecipeType> getRecipeType() {
        return WEAPON_WORKBENCH_RECIPE_TYPE_RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.lyc_perseus.weapon_workbench");
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
    public void setRecipe(IRecipeLayoutBuilder builder, WeaponWorkbenchRecipeType recipe, IFocusGroup focuses) {
        // 根据实际的输入项数量设置槽位
        for (int i = 0; i < Math.min(recipe.getIngredients().size(), 18); i++) {
            // 计算新的坐标
            int x = 26 + (i % 6) * 18; // 槽位x坐标
            int y = 7 + (i / 6) * 19; // 槽位y坐标

            // 获取当前索引的配料
            Ingredient ingredient = recipe.getIngredients().get(i);

            // 遍历配料中的所有物品堆栈
            for (ItemStack stack : ingredient.getItems()) {
                ItemStack outStack = new ItemStack(stack.getItem());

                // 处理NBT数据
                for (int h = 1; h <= 4; h++) {
                    String nbtId = WeaponWorkbenchRecipeType.getNbtId(stack, h);
                    if (!nbtId.isEmpty()) {
                        CompoundTag nbt = outStack.getOrCreateTag();
                        String nbtValue = WeaponWorkbenchRecipeType.getNbtValue(stack, h);
                        nbt.putString(nbtId, nbtValue);
                        outStack.setTag(nbt);
                    }
                }
                // 添加槽位
                builder.addSlot(RecipeIngredientRole.OUTPUT, x, y).addItemStack(outStack);
        }

            // 创建一个 List<ItemStack>

            // 添加 LPItems 中的三个物品

            builder.addSlot(RecipeIngredientRole.INPUT, 6, 7).addItemStack(recipe.getResultItem(null));


        }
    }
}
