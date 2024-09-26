package com.linweiyun.lycoris.init;

import com.linweiyun.lycoris.items.LPItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class LPRegistryMethods {
    public static <T extends Item> RegistryObject<Item> registryItem(String name, DeferredRegister<Item> RItem, Supplier<T> item){
        return RItem.register(name, item);
    }

    public static <T extends Block> RegistryObject<Block> registryBlock(String name, DeferredRegister<Block> RBlock, Supplier<T> block){
        RegistryObject<T> blockObject = RBlock.register(name, block);
        registryBlockItem(name, LPItems.ITEMS, blockObject);
        return (RegistryObject<Block>) blockObject;
    }

    private static <T extends Block> RegistryObject<Item> registryBlockItem(String name, DeferredRegister<Item> RItem, RegistryObject<T> block){
        return RItem.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

}