package com.linweiyun.lycoris;

import com.linweiyun.lycoris.block.LPBlocks;
import com.linweiyun.lycoris.init.LycorisInit;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import software.bernie.geckolib.GeckoLib;

@Mod(LycPerseusMod.MOD_ID)
public class LycPerseusMod
{
    public static final String MOD_ID = "lyc_perseus";

    public LycPerseusMod()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::addCreative);


        LycorisInit.registerLPMenuType(modEventBus);
        LycorisInit.registerLPBlocks(modEventBus);
        LycorisInit.registerLPItems(modEventBus);
        LycorisInit.registerLPBlockEntities(modEventBus);
        LycorisInit.registerLPRecipeType(modEventBus);
        LycorisInit.registerLPItemgroups(modEventBus);
        GeckoLib.initialize();



    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            LycorisInit.registerLPScreens();
            ItemBlockRenderTypes.setRenderLayer(LPBlocks.DECOMPOSITION_EXTRACTOR.get(), RenderType.cutout());

        }
    }
}
