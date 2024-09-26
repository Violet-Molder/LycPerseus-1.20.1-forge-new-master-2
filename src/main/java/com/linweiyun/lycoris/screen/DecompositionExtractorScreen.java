package com.linweiyun.lycoris.screen;

import com.linweiyun.lycoris.LycPerseusMod;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class DecompositionExtractorScreen extends AbstractContainerScreen<DecompositionExtractorMenu> {
    private static final ResourceLocation BackgroundTexture =
            new ResourceLocation(LycPerseusMod.MOD_ID,"textures/gui/decomposition_extractor_gui.png");

    public DecompositionExtractorScreen(DecompositionExtractorMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
        this.imageWidth = 214;
        this.imageHeight = 165;
    }

    @Override
    protected void init() {
        super.init();
    }
    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int gx, int gy) {
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        guiGraphics.blit(BackgroundTexture, this.leftPos, this.topPos, 0, 0, 176, this.imageHeight, this.imageWidth, this.imageHeight);
        RenderSystem.disableBlend();
        int x  = (this.width - this.imageWidth) / 2;
        int y  = (this.height - this.imageHeight) / 2;
        renderProgressArrow(guiGraphics, x + 68,y + 69);
    }

    private void renderProgressArrow(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.blit(BackgroundTexture, x, y, 176,12 ,menu.getScaledProgress(),5, this.imageWidth, this.imageHeight);



    }

    // 先添加背景，在添加super渲染容器，在添加提示信息。
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
    }

    @Override
    protected void renderTooltip(GuiGraphics pGuiGraphics, int pX, int pY) {
        super.renderTooltip(pGuiGraphics, pX, pY);
    }
}