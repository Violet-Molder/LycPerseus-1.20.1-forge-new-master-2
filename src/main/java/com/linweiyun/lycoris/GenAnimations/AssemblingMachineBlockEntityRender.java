package com.linweiyun.lycoris.GenAnimations;

import com.linweiyun.lycoris.block.blockentity.AssemblingMachineBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class AssemblingMachineBlockEntityRender extends GeoBlockRenderer<AssemblingMachineBlockEntity> {
    public AssemblingMachineBlockEntityRender(BlockEntityRendererProvider.Context context) {
        super(new AssemblingMachineAniamation());
    }
}
