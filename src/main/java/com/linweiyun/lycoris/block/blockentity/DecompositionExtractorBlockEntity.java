package com.linweiyun.lycoris.block.blockentity;

import com.linweiyun.lycoris.items.custom.BatteryItem;
import com.linweiyun.lycoris.recipetype.AssemblingMachineRecipe;
import com.linweiyun.lycoris.recipetype.DecompositionExtractorRecipe;
import com.linweiyun.lycoris.screen.AssemblingMachineMenu;
import com.linweiyun.lycoris.screen.DecompositionExtractorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.AirItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

public class DecompositionExtractorBlockEntity extends BlockEntity implements MenuProvider {

    public final ItemStackHandler itemStackHandler = new ItemStackHandler(8) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return true;
        }
    };



    protected final ContainerData data;
    private int     progress = 0;
    private int maxProgress = 100;


    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    public DecompositionExtractorBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(LPBlockEntities.DECOMPOSITION_EXTRACTOR.get(), pPos, pBlockState);

        this.data = new ContainerData() {
            @Override
            public int get(int i) {
                return switch (i) {
                    case 0 -> DecompositionExtractorBlockEntity.this.progress;
                    case 1 -> DecompositionExtractorBlockEntity.this.maxProgress;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> DecompositionExtractorBlockEntity.this.progress = value;
                    case 1 -> DecompositionExtractorBlockEntity.this.maxProgress = value;
                }
            }

            @Override
            public int getCount() {
                // 增加额外的三个int变量和一个boolean变量，共6个数据项
                return 2;
            }
        };
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("factory.name:dimension_finder");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new DecompositionExtractorMenu(i, inventory, this, this.data);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {

        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }

        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemStackHandler);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        pTag.put("inventory", itemStackHandler.serializeNBT());
        pTag.putInt("decomposition_extractor.progress", this.progress);

        super.saveAdditional(pTag);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        itemStackHandler.deserializeNBT(pTag.getCompound("inventory"));
        progress = pTag.getInt("decomposition_extractor.progress");
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemStackHandler.getSlots());
        for (int i = 0; i < itemStackHandler.getSlots(); i++) {
            inventory.setItem(i, itemStackHandler.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    //静态方法，每次回调都会更新状态
    public static void tick(Level level, BlockPos blockPos, BlockState state, DecompositionExtractorBlockEntity entity) {
        if (level.isClientSide){
            return;
        }
        ItemStack itemStack = entity.itemStackHandler.getStackInSlot(6);
        //  仅仅在服务器端运行
        // 判断是否能够合成
        if(hasRecipe(entity) && itemStack.getItem() instanceof BatteryItem){
            // 进度增加
            entity.progress +=1 ;
            setChanged(level,blockPos,state);
            // 如果进度条满了
            if(entity.progress >= entity.maxProgress){
                // 合成一个物品
                craftItem(entity);
                itemStack.setDamageValue(itemStack.getDamageValue()+1);
                if (itemStack.getDamageValue() >= itemStack.getMaxDamage()){
                    itemStack.shrink(1);
                }

            }
        }else{
            // 没有合成表就重置
            entity.resetProgress();
            setChanged(level,blockPos,state);
        }


    }

    // 合成物品
    private static void craftItem(DecompositionExtractorBlockEntity entity) {
        Level level = entity.level;    // 获取实体所在的世界
        // 新建一个仓库，槽位数量为该方块实体拥有的槽位数量
        SimpleContainer inventory = new SimpleContainer(entity.itemStackHandler.getSlots());
        for (int i = 0; i < entity.itemStackHandler.getSlots(); i++) {
            inventory.setItem(i, entity.itemStackHandler.getStackInSlot(i)); // 将上面新建的仓库各槽位的物品设置为放置在该方块内各槽位的物品
        }
        // 获得当前的recipe
        Optional<DecompositionExtractorRecipe> recipe = level.getRecipeManager().getRecipeFor(
                DecompositionExtractorRecipe.Type.INSTANCE, inventory, level);
        if (hasRecipe(entity)) { // 检测有没有这个配方
            if (!recipe.isPresent()) return; // 如果这个配方是空的则直接返回
            // 获取配方并检测是否满足合成条件
            DecompositionExtractorRecipe Trepice = recipe.get();
            // 如果满足合成条件则开始合成
            if (Trepice.matches(inventory, level)) {
                // 将合成结果放入槽位
                for (Ingredient ingredient : recipe.get().getIngredients()) {
                    for (ItemStack itemStack : ingredient.getItems()) {
                        Random random = new Random();
                        int isEmptySlot = DecompositionExtractorRecipe.getIsEmpty(itemStack);
                        int emptyProbability = random.nextInt(100) + 1;
                        ItemStack craftItem = new ItemStack(Items.AIR);
                        if (emptyProbability <= isEmptySlot){
                            craftItem = new ItemStack(Items.AIR);
                        } else {
                            String nbtId = DecompositionExtractorRecipe.getNbtId(itemStack);
                            String nbtValue = DecompositionExtractorRecipe.getNbtValue(itemStack);

                            // 创建或获取 NBT 数据
                            CompoundTag nbt = itemStack.getOrCreateTag();

                            // 添加药水效果的 NBT 标签
                            nbt.putString(nbtId, nbtValue);

                            // 设置 NBT 数据
                            itemStack.setTag(nbt);

                            int randomNumber = random.nextInt(100) + 1;
                            int number = DecompositionExtractorRecipe.getMaxCount(itemStack) - DecompositionExtractorRecipe.getMinCount(itemStack) + 1;
                            int addCount = 0;
                            int perCount = 100 / number;
                            for (int i = 0; i < number; i++) {
                                if (randomNumber <= perCount * (i + 1)) {
                                    addCount = DecompositionExtractorRecipe.getMinCount(itemStack) + i;
                                    break;
                                }
                            }
                            boolean added = false;
                            for (int j = 0; j < 6; j++) {
                                ItemStack currentStack = entity.itemStackHandler.getStackInSlot(j);
                                if (itemStack.getItem().equals(currentStack.getItem())) {
                                    if (currentStack.getCount() + addCount <= itemStack.getMaxStackSize()) {
                                        craftItem = new ItemStack(itemStack.getItem(), currentStack.getCount() + addCount);
                                        craftItem.setTag(itemStack.getTag());
                                        entity.itemStackHandler.setStackInSlot(j, craftItem);
                                        added = true;
                                        break;
                                    } else if (currentStack.getCount() + addCount > itemStack.getMaxStackSize()) {
                                        int overflow = addCount - (itemStack.getMaxStackSize() - currentStack.getCount());
                                        entity.itemStackHandler.setStackInSlot(j, new ItemStack(itemStack.getItem(), itemStack.getMaxStackSize()));
                                        addCount = overflow;
                                    }
                                }
                                if (entity.itemStackHandler.getStackInSlot(j).isEmpty()) {
                                    // 将新 ItemStack 放入空槽位
                                    craftItem = new ItemStack(itemStack.getItem(), addCount);
                                    craftItem.setTag(itemStack.getTag()); // 确保 NBT 数据正确复制
                                    entity.itemStackHandler.setStackInSlot(j, craftItem);
                                    added = true;
                                    break;
                                }
                            }
                            if (!added) {
                                // 如果没有找到合适的槽位，则尝试再次寻找空槽位
                                for (int j = 0; j < 6; j++) {
                                    if (entity.itemStackHandler.getStackInSlot(j).isEmpty()) {
                                        // 将新 ItemStack 放入空槽位
                                        craftItem = new ItemStack(itemStack.getItem(), addCount);
                                        craftItem.setTag(itemStack.getTag()); // 确保 NBT 数据正确复制
                                        entity.itemStackHandler.setStackInSlot(j, craftItem);
                                        added = true;
                                        break;
                                    }
                                }
                            }
                            if (!added) {
                                // 如果仍然没有找到合适的槽位，则停止合成
                                return;
                            }
                        }

                        // 获取药水效果

                    }
                }
                entity.itemStackHandler.extractItem(7, 1, false);
                // 重置合成进度
                entity.resetProgress();
            }
        }
    }

    // 是否具有合成表
    private static boolean hasRecipe(DecompositionExtractorBlockEntity entity) {
        Level level = entity.level;
        SimpleContainer inventory = new SimpleContainer(entity.itemStackHandler.getSlots());
        for (int i = 0; i < entity.itemStackHandler.getSlots(); i++) {
            inventory.setItem(i, entity.itemStackHandler.getStackInSlot(i));
        }
        Optional<DecompositionExtractorRecipe> recipe = level.getRecipeManager().getRecipeFor(
                DecompositionExtractorRecipe.Type.INSTANCE, inventory, level);
        if (recipe.isPresent()) {
            entity.maxProgress = recipe.get().getTime() * 20;
            setChanged(level, entity.worldPosition, entity.getBlockState());
        }
        return recipe.isPresent() &&
                canInsertItemToOutputSlot(inventory, recipe);
    }
    // 判断插入slot是是否是相同的item，以及是否为空
    private static boolean canInsertItemToOutputSlot(SimpleContainer inventory, Optional<DecompositionExtractorRecipe> recipe) {
        if (!recipe.isPresent()) {
            return false;
        }

        DecompositionExtractorRecipe Trepice = recipe.get();
        Map<Item, Integer> itemCounts = new HashMap<>();
        int totalRequiredEmptySlots = 0;

        // 计算每种物品所需的总数量
        for (Ingredient ingredient : Trepice.getIngredients()) {
            for (ItemStack itemStack : ingredient.getItems()) {
                Item item = itemStack.getItem();
                int maxCount = Trepice.getMaxCount(itemStack);
                itemCounts.put(item, itemCounts.getOrDefault(item, 0) + maxCount);
            }
        }

        // 计算每个物品所需的最大空槽位数
        for (Map.Entry<Item, Integer> entry : itemCounts.entrySet()) {
            Item item = entry.getKey();
            int requiredCount = entry.getValue();
            int availableSpace = 0;

            for (int i = 0; i < 6; i++) {
                ItemStack stack = inventory.getItem(i);
                if (stack.isEmpty()) {
                    availableSpace += item.getMaxStackSize();
                } else if (stack.getItem() == item) {
                    availableSpace += item.getMaxStackSize() - stack.getCount();
                }
            }

            if (availableSpace < requiredCount) {
                return false; // 如果有一个物品没有足够的空槽位，则停止合成
            }
        }

        // 计算总的空槽位数
        int haveEmptySlot = 0;
        for (int i = 0; i < 6; i++) {
            if (inventory.getItem(i).isEmpty()) {
                haveEmptySlot++;
            }
        }

        // 计算总的所需空槽位数
        for (Map.Entry<Item, Integer> entry : itemCounts.entrySet()) {
            Item item = entry.getKey();
            int requiredCount = entry.getValue();
            int availableSpace = 0;

            for (int i = 0; i < 6; i++) {
                ItemStack stack = inventory.getItem(i);
                if (stack.isEmpty()) {
                    availableSpace += item.getMaxStackSize();
                } else if (stack.getItem() == item) {
                    availableSpace += item.getMaxStackSize() - stack.getCount();
                }
            }

            totalRequiredEmptySlots += (requiredCount - availableSpace) / item.getMaxStackSize();
        }

        return haveEmptySlot >= totalRequiredEmptySlots;
    }



    private void resetProgress(){
        this.progress = 0;
    }



}
