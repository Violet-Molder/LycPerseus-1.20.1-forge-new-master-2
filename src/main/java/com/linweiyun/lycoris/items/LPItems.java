package com.linweiyun.lycoris.items;

import com.linweiyun.lycoris.LycPerseusMod;
import com.linweiyun.lycoris.block.LPBlocks;
import com.linweiyun.lycoris.init.LPRegistryMethods;
import com.linweiyun.lycoris.items.custom.BatteryItem;
import com.linweiyun.lycoris.items.custom.ExtraItem;
import com.linweiyun.lycoris.items.custom.SoulItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class LPItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, LycPerseusMod.MOD_ID);
    public static final RegistryObject<Item> DIMENSION_FINDER_ITEM = ITEMS.register("dimension_finder",
            ()->new BlockItem(LPBlocks.DIMENSION_FINDER.get(), new Item.Properties()));
    public static final RegistryObject<Item> ASSEMBLING_MACHINE_ITEM = ITEMS.register("assembling_machine",
            ()-> new BlockItem(LPBlocks.ASSEMBLING_MACHINE.get(), new Item.Properties()));

    public static final RegistryObject<Item> DECOMPOSITION_EXTRACTOR_ITEM = ITEMS.register("decomposition_extractor",
            ()-> new BlockItem(LPBlocks.DECOMPOSITION_EXTRACTOR.get(), new Item.Properties()));
    public static final RegistryObject<Item> WEAPON_WORKBENCH_ITEM = ITEMS.register("weapon_workbench",
            ()-> new BlockItem(LPBlocks.WEAPON_WORKBENCH.get(), new Item.Properties()));



    public static final RegistryObject<Item> BIOSOLID_FUELS = ITEMS.register("biosolid_fuels",
            ()->new SoulItem(new Item.Properties().durability(16)));
    public static final RegistryObject<Item> DIVINE_CRYSTAL = ITEMS.register("divine_crystal",
            ()->new SoulItem(new Item.Properties().durability(64)));
    public static final RegistryObject<Item> PHILOSOPHER_STONE = ITEMS.register("philosopher_stone",
            ()->new SoulItem(new Item.Properties().durability(1024)));


    public static final RegistryObject<Item> AUXILIARY_REACTION_FURNACE = ITEMS.register("auxiliary_reaction_furnace",
            ()->new ExtraItem(new Item.Properties().stacksTo(1), 1));
    public static final RegistryObject<Item> UTILITY_MINING_RUNE = ITEMS.register("utility_mining_rune",
            ()->new ExtraItem(new Item.Properties().stacksTo(1), 2));
    public static final RegistryObject<Item> SHADOW_TRANSMISSION_MODULE = ITEMS.register("shadow_transmission_module",
            ()->new ExtraItem(new Item.Properties().stacksTo(1), 3));


    public static final RegistryObject<Item> POTATO_POWER = LPRegistryMethods.registryItem("potato_power", ITEMS,
            ()-> new BatteryItem(new Item.Properties().durability(8)));
    public static final RegistryObject<Item> DISPOSABLE_BATTERIES    = LPRegistryMethods.registryItem("disposable_batteries", ITEMS,
            ()-> new BatteryItem(new Item.Properties().durability(32)));
    public static final RegistryObject<Item> STORAGE_BATTERY = LPRegistryMethods.registryItem("storage_battery", ITEMS,
            ()-> new BatteryItem(new Item.Properties().durability(128)));



   public static void register(IEventBus eventBus)
    {
        ITEMS.register(eventBus);
    }
}


























