package com.linweiyun.lycoris.screen;

import com.linweiyun.lycoris.LycPerseusMod;
import com.linweiyun.lycoris.block.blockentity.DimensionFinderEntity;
import com.linweiyun.lycoris.items.LPItems;
import com.linweiyun.lycoris.netWorking.LPMessages;
import com.linweiyun.lycoris.netWorking.packet.DimFinIsExtraItemSyncS2CPacket;
import com.linweiyun.lycoris.netWorking.packet.DimFinIsOpenSyncS2CPacket;
import com.linweiyun.lycoris.screen.renderer.LPEnergyInfoArea;
import com.linweiyun.lycoris.screen.renderer.LPProgressInfoArea;
import com.linweiyun.lycoris.until.LPMouseUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;

import java.util.Optional;

public class DimensionFinderScreen extends AbstractContainerScreen<DimensionFinderMenu> {
    private int extra1;
    private int extra2;
    private int extra3;
    private static final ResourceLocation TEXTURE_CLOSE =
            new ResourceLocation(LycPerseusMod.MOD_ID, "textures/gui/dimension_finder_gui_main_close.png");
    private static final ResourceLocation TEXTURE_OPEN =
            new ResourceLocation(LycPerseusMod.MOD_ID, "textures/gui/dimension_finder_gui_main_open.png");
    private LPEnergyInfoArea energyInfoArea;
    private LPProgressInfoArea progressInfoArea;
    ImageButton startButton;
    DimensionFinderEntity entity = getMenu().entity;

    public DimensionFinderScreen(DimensionFinderMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
        this.imageWidth = 221;
        this.imageHeight = 182;
    }
    private boolean isOpen;

    @Override
    protected void init() {
        super.init();
        assignEnergyInfoArea();
        assignProgressInfoArea();
        int xStart;
        int yStart;

        if (entity.getIsStart()) {
            xStart = 209;
            yStart = 30;
        } else {
            xStart = 4;
            yStart = 79;
        }


        startButton = new ImageButton(this.leftPos + 4, this.topPos + 79, 9, 9, xStart, yStart,
                9, TEXTURE_CLOSE, this.imageWidth, this.imageHeight, e ->{
            if (entity.getIsStart()){
                closeButton();
                isOpen = false;
            } else {
                openButton();
                isOpen = true;
            }
        });


        this.addWidget(startButton);
    }
    private void openButton(){
        for (int i = 0; i < 3; i ++){
            Item item = entity.itemStackHandler.getStackInSlot(i + 3).getItem();
            if (item == LPItems.AUXILIARY_REACTION_FURNACE.get()){
                extra1++;
            } else if (item == LPItems.UTILITY_MINING_RUNE.get()) {
                extra2++;
            } else if (item == LPItems.SHADOW_TRANSMISSION_MODULE.get()){
                extra3++;
            }
        }
        entity.setStart(true);

        LPMessages.sendToServer(new DimFinIsOpenSyncS2CPacket(entity.getBlockPos(), true));
        LPMessages.sendToServer(new DimFinIsExtraItemSyncS2CPacket(entity.getBlockPos(), extra1, extra2, extra3));
        extra1 = 0;
        extra2 = 0;
        extra3 = 0;

    }
    private void closeButton(){
        LPMessages.sendToServer(new DimFinIsOpenSyncS2CPacket(entity.getBlockPos(), false));
        LPMessages.sendToServer(new DimFinIsExtraItemSyncS2CPacket(entity.getBlockPos(), 0, 0, 0));
        entity.setStart(false);
    }
    private void assignEnergyInfoArea() {
        int x = this.leftPos + 7;
        int y = this.topPos + 46;

        energyInfoArea = new LPEnergyInfoArea(x, y, entity.getEnergyStorage());
    }

    private void assignProgressInfoArea() {
        int x = this.leftPos + 124;
        int y = this.topPos + 92;

        progressInfoArea = new LPProgressInfoArea(x, y,getMenu());
    }



    // 渲染背景
    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int gx, int gy) {

        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        if (!entity.getIsStart())
        {
            guiGraphics.blit(TEXTURE_CLOSE, this.leftPos, this.topPos, 0, 0, 205, 182, this.imageWidth, this.imageHeight);
        } else {
            guiGraphics.blit(TEXTURE_OPEN, this.leftPos, this.topPos, 0, 0, 205, 182, this.imageWidth, this.imageHeight);
        }



        RenderSystem.disableBlend();
    }





    // 先添加背景，在添加super渲染容器，在添加提示信息。
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        guiGraphics.blit(TEXTURE_CLOSE, this.leftPos + 7, this.topPos + 46, 208, 8, 6, 22, this.imageWidth, this.imageHeight);//能量槽背景
        energyInfoArea.draw(guiGraphics);
        guiGraphics.blit(TEXTURE_CLOSE, this.leftPos + 7, this.topPos + 46, 7, 46, 6, 22, this.imageWidth, this.imageHeight);//能量槽背景
        progressInfoArea.draw(guiGraphics);
        this.startButton.render(guiGraphics, mouseX, mouseY, partialTicks);
        if (entity.getIsStart()){
            guiGraphics.blit(TEXTURE_CLOSE, this.leftPos + 4, this.topPos + 79, 207, 37, 9, 9, this.imageWidth, this.imageHeight);

        } else {
            guiGraphics.blit(TEXTURE_CLOSE, this.leftPos + 4, this.topPos + 79, 4, 79, 9, 9, this.imageWidth, this.imageHeight);
        }



    }
    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int x = this.leftPos + 5;
        int y = this.topPos;
        renderEnergyAreaTooltips(guiGraphics, mouseX, mouseY, x, y);
    }

    private void renderEnergyAreaTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY, int x, int y) {
        if (isMouseAboveArea(mouseX, mouseY, x, y, 0, 42, 9, 31)) {
            guiGraphics.renderTooltip(this.font, energyInfoArea.getTooltips(), Optional.empty(), mouseX - x, mouseY - y);
        }
    }

    private boolean isMouseAboveArea(int pMouseX, int pMouseY, int x, int y, int offsetX, int offsetY, int width, int height){
        return LPMouseUtil.isMouseOver(pMouseX, pMouseY, x + offsetX, y + offsetY, width, height);
    }
    @Override
    protected void renderTooltip(GuiGraphics pGuiGraphics, int pX, int pY) {
        super.renderTooltip(pGuiGraphics, pX, pY);
    }
}