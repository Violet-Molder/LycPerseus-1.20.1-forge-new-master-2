package com.linweiyun.lycoris.init;

import com.linweiyun.lycoris.items.LPItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class LPRegistry {
    public static <T extends Block> RegistryObject<T> registerBlock(String name, DeferredRegister<Block> RBlock , Supplier<T> block)
    {
        RegistryObject<T> blockObject = RBlock.register(name, block);
        registerBlcokItem(name, blockObject);
        return blockObject;
    }
    private static <T extends Block>  RegistryObject<Item> registerBlcokItem(String name, RegistryObject<T> block){
        return LPItems.ITEMS.register(name, () -> new BlockItem(block.get(),new Item.Properties()));

    }
}
