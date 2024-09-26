package com.linweiyun.lycoris.screen.renderer;

import com.linweiyun.lycoris.block.blockentity.DimensionFinderEntity;
import com.linweiyun.lycoris.screen.DimensionFinderMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.List;

public class LPProgressInfoArea extends LPInfoArea{

    DimensionFinderMenu menu;
    public LPProgressInfoArea(int xMin, int yMin){
        this(xMin, yMin, null, 31, 2);
    }
    public LPProgressInfoArea(int xMin, int yMin, DimensionFinderMenu menu){
        this(xMin, yMin, menu, 31, 2);
    }
    public LPProgressInfoArea(int xMin, int yMin, DimensionFinderMenu menu, int width, int height){
        super(new Rect2i(xMin, yMin, width, height));
        this. menu= menu;
    }

    public List<Component> getTooltips() {
        return List.of(Component.literal("Progress: " + menu.getScaledProgress()));
    }

    @Override
    public void draw(GuiGraphics guiGraphics) {
        final int width = area.getWidth();
        int stored = menu.getScaledProgress();

        guiGraphics.fillGradient(
                area.getX(), area.getY(),
                area.getX() + stored, area.getY() + area.getHeight(),
                0xff8bfcef, 0xff78c9f3
        );
    }
}
