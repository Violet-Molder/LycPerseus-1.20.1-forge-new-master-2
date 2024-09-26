package com.linweiyun.lycoris.screen.renderer;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;

public abstract class LPInfoArea {
    protected final Rect2i area;

    protected LPInfoArea(Rect2i area){
        this.area = area;
    }

    public abstract void draw(GuiGraphics guiGraphics);
}
