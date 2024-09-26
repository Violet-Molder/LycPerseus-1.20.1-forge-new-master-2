package com.linweiyun.lycoris.items.custom;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

public class BatteryItem extends Item {
    public BatteryItem(Properties pProperties) {
        super(pProperties);
    }

    //是否显示附魔的效果
    @Override
    public boolean isFoil(ItemStack pStack) {
        return false;
    }

    // 可选：设置 Rarity 为 COMMON，通常附魔只对 RARE 或以上生效
    @Override
    public Rarity getRarity(ItemStack stack) {
        return Rarity.COMMON;
    }

    @Override
    public boolean isEnchantable(ItemStack pStack) {
        return false;
    }

}
