package com.linweiyun.lycoris.GenAnimations;

import com.linweiyun.lycoris.LycPerseusMod;
import com.linweiyun.lycoris.block.blockentity.AssemblingMachineBlockEntity;
import com.linweiyun.lycoris.block.custom.AssemblingMachine;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class AssemblingMachineAniamation extends GeoModel<AssemblingMachineBlockEntity> {

    private final ResourceLocation model = new ResourceLocation(LycPerseusMod.MOD_ID, "geo/assembling_machine.geo.json");
    private final ResourceLocation texture = new ResourceLocation(LycPerseusMod.MOD_ID, "textures/block/assembling_machine.png");
    private final ResourceLocation animations = new ResourceLocation(LycPerseusMod.MOD_ID, "animations/assembling_machine.animation.json");


    @Override
    public ResourceLocation getModelResource(AssemblingMachineBlockEntity assemblingMachine) {
        return this.model;
    }

    @Override
    public ResourceLocation getTextureResource(AssemblingMachineBlockEntity assemblingMachine) {
        return this.texture;
    }

    @Override
    public ResourceLocation getAnimationResource(AssemblingMachineBlockEntity assemblingMachine) {
        return this.animations;
    }
}
