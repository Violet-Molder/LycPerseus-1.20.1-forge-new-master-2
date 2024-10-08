package com.linweiyun.lycoris.screen;

import com.linweiyun.lycoris.LycPerseusMod;
import com.linweiyun.lycoris.block.blockentity.DimensionFinderEntity;
import com.linweiyun.lycoris.block.custom.DimensionFinder;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.state.BlockState;

public class DimensionFinderScreen extends AbstractContainerScreen<DimensionFinderMenu> {
    private static final ResourceLocation TEXTURE_CLOSE =
            new ResourceLocation(LycPerseusMod.MOD_ID, "textures/gui/dimension_finder_gui_close.png");
    private static final ResourceLocation TEXTURE_OPEN =
            new ResourceLocation(LycPerseusMod.MOD_ID, "textures/gui/dimension_finder_gui_open.png");
    DimensionFinderEntity entity = getMenu().entity;
    BlockState state = entity.getBlockState();
    DimensionFinder block = (DimensionFinder) state.getBlock();

    public DimensionFinderScreen(DimensionFinderMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
        this.imageWidth = 191;
        this.imageHeight = 182;
    }
    @Override
    protected void init() {
        super.init();
    }



    // 渲染背景
    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int gx, int gy) {
        RenderSystem.setShaderColor(1, 1, 1, 1); // 设置颜色
        RenderSystem.enableBlend(); // 启用混合模式
        RenderSystem.defaultBlendFunc(); // 设置默认混合函数

        if (!menu.isOpen())
        {
            guiGraphics.blit(TEXTURE_CLOSE, this.leftPos, this.topPos, 0, 2, 191, 180, this.imageWidth, this.imageHeight);

        } else {
            guiGraphics.blit(TEXTURE_OPEN, this.leftPos, this.topPos, 0, 2, 191, 180, this.imageWidth, this.imageHeight);
            renderProgressArrow(guiGraphics, this.leftPos + 110, this.topPos + 90);
        }
        RenderSystem.disableBlend();



    }
    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // 不做任何事情，以阻止渲染标题
    }


    private void renderProgressArrow(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.blit(TEXTURE_OPEN, x, y, 0,0 ,menu.getScaledProgress(),2, this.imageWidth, this.imageHeight);
    }


    // 先添加背景，在添加super渲染容器，在添加提示信息。
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
    @Override
    protected void renderTooltip(GuiGraphics pGuiGraphics, int pX, int pY) {
        super.renderTooltip(pGuiGraphics, pX, pY);
    }
}