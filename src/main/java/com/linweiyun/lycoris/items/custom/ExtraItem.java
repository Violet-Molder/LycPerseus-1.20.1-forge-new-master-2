package com.linweiyun.lycoris.items.custom;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ExtraItem extends Item {
    private int extraType;
    public ExtraItem(Properties pProperties, int extraType) {
        super(pProperties);
        this.extraType = extraType;
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        if (extraType == 1){
            pTooltipComponents.add(Component.translatable("tooltip.lycperseus.extra_1"));
        }
        if (extraType == 2){
            pTooltipComponents.add(Component.translatable("tooltip.lycperseus.extra_2"));
        }
        if (extraType == 3){
            pTooltipComponents.add(Component.translatable("tooltip.lycperseus.extra_3"));
        }
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }
}
