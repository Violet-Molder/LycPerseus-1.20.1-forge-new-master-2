package com.linweiyun.lycoris.screen.renderer;

import com.linweiyun.lycoris.until.LPSoulEnergyStorage;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraftforge.energy.IEnergyStorage;

import java.awt.*;
import java.util.List;

public class LPEnergyInfoArea extends LPInfoArea{
   private final IEnergyStorage energy;
   public LPEnergyInfoArea(int xMin, int yMin){
       this(xMin, yMin, null, 6, 22);
   }
   public LPEnergyInfoArea(int xMin, int yMin, IEnergyStorage energy){
       this(xMin, yMin, energy, 6, 22);
   }
   public LPEnergyInfoArea(int xMin, int yMin, IEnergyStorage energy, int width, int height){
       super(new Rect2i(xMin, yMin, width, height));
       this.energy = energy;
   }

   public List<Component> getTooltips() {
       return List.of(Component.literal("Energy: " + energy.getEnergyStored()));
   }

    @Override
    public void draw(GuiGraphics guiGraphics) {
       final int height = area.getHeight();
       int stored = (int)(height * (energy.getEnergyStored()/(float) energy.getMaxEnergyStored()));
       guiGraphics.fillGradient(
               area.getX(), area.getY()+(height-stored),
               area.getX() + area.getWidth(), area.getY() + area.getHeight(),
               0xff8bfcef, 0xff78c9f3
       );
    }
}
