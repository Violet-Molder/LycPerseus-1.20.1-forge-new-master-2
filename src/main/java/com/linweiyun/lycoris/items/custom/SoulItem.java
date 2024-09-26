package com.linweiyun.lycoris.items.custom;

import net.minecraft.world.item.Item;

public class SoulItem extends Item {
    private final int soulVaule;
    public SoulItem(Properties pProperties, int soulVaule) {
        super(pProperties);
        this.soulVaule = soulVaule;
    }

    public int getSoulVaule() {
        return soulVaule;
    }
}
