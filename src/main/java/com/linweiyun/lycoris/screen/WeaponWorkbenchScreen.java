package com.linweiyun.lycoris.screen;

import com.linweiyun.lycoris.LycPerseusMod;
import com.linweiyun.lycoris.block.blockentity.WeaponWorkbenchEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.state.BlockState;

public class WeaponWorkbenchScreen extends AbstractContainerScreen<WeaponWorkbenchMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(LycPerseusMod.MOD_ID, "textures/gui/weapon_workbench_gui.png");
    WeaponWorkbenchEntity entity = getMenu().entity;
    BlockState state = entity.getBlockState();

    public WeaponWorkbenchScreen(WeaponWorkbenchMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
        this.imageWidth = 174;
        this.imageHeight = 164;
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

            guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, 173, 164, this.imageWidth, this.imageHeight);

        RenderSystem.disableBlend();



    }
    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // 不做任何事情，以阻止渲染标题
    }


    // 先添加背景，在添加super渲染容器，在添加提示信息。
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        guiGraphics.drawString(Minecraft.getInstance().font, "物品栏", this.leftPos + 18, this.topPos + 71, 0x000000);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

    }
    @Override
    protected void renderTooltip(GuiGraphics pGuiGraphics, int pX, int pY) {
        super.renderTooltip(pGuiGraphics, pX, pY);
    }
}