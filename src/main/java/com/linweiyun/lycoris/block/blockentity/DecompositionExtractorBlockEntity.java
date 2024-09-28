package com.linweiyun.lycoris.block.blockentity;

import com.linweiyun.lycoris.block.LPBlockEntities;
import com.linweiyun.lycoris.items.custom.BatteryItem;
import com.linweiyun.lycoris.recipetype.DecompositionExtractorRecipe;
import com.linweiyun.lycoris.recipetype.WeaponWorkbenchRecipeType;
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


                        //计算不输出物品的可能性
                        int isEmptySlot = DecompositionExtractorRecipe.getIsEmpty(itemStack);
                        int emptyProbability = random.nextInt(100) + 1;
                        ItemStack craftItem = new ItemStack(Items.AIR);
                        if (emptyProbability <= isEmptySlot){
                            craftItem = new ItemStack(Items.AIR);
                        }

                        //正常合成
                        else {
                            //获取和添加nbt
                            ItemStack outStack = new ItemStack(itemStack.getItem());
                            String nbtId = DecompositionExtractorRecipe.getNbtId(itemStack);
                            if (!nbtId.isEmpty()) {
                                CompoundTag nbt = outStack.getOrCreateTag();
                                String nbtValue = DecompositionExtractorRecipe.getNbtValue(itemStack);
                                nbt.putString(nbtId, nbtValue);
                                outStack.setTag(nbt);
                            }


                            //计算输出多少个物品
                            int randomNumber = random.nextInt(100) + 1;
                            int number = DecompositionExtractorRecipe.getMaxCount(itemStack) - DecompositionExtractorRecipe.getMinCount(itemStack) + 1;
                            int addCount = 0;//最终输出的物品数量
                            int perCount = 100 / number;
                            for (int i = 0; i < number; i++) {
                                if (randomNumber <= perCount * (i + 1)) {
                                    addCount = DecompositionExtractorRecipe.getMinCount(itemStack) + i;
                                    break;
                                }
                            }


                            boolean added = false;
                            for (int j = 0; j < 6; j++) {
                                ItemStack currentStack = entity.itemStackHandler.getStackInSlot(j);//获取槽位当前物品
                                if (outStack.getItem().equals(currentStack.getItem())) {
                                    if (currentStack.getCount() < outStack.getMaxStackSize()){
                                        if (currentStack.getCount() + addCount <= outStack.getMaxStackSize()) {
                                            outStack.setCount(currentStack.getCount() + addCount);
                                            entity.itemStackHandler.setStackInSlot(j, outStack);
                                            added = true;
                                            break;
                                        } else if (currentStack.getCount() + addCount > itemStack.getMaxStackSize()) {
                                            int overflow = addCount - (itemStack.getMaxStackSize() - currentStack.getCount());
                                            outStack.setCount(outStack.getMaxStackSize());
                                            entity.itemStackHandler.setStackInSlot(j, outStack);
                                            addCount = overflow;
                                        }
                                    }

                                }
                                if (entity.itemStackHandler.getStackInSlot(j).isEmpty()) {
                                    if (addCount <= outStack.getMaxStackSize()){
                                        outStack.setCount(addCount);
                                    }
                                    entity.itemStackHandler.setStackInSlot(j, outStack);
                                    added = true;
                                    break;
                                }
                            }
                            if (!added) {
                                // 如果没有找到合适的槽位，则尝试再次寻找空槽位
                                for (int j = 0; j < 6; j++) {
                                    if (entity.itemStackHandler.getStackInSlot(j).isEmpty()) {
                                        if (addCount <= outStack.getMaxStackSize()){
                                            outStack.setCount(addCount);
                                        }
                                        entity.itemStackHandler.setStackInSlot(j, outStack);
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
        int airSlot = 0;
        int requireSlot = recipe.get().getIngredients().size();
        int canStackSlot = 0;
        for (int i = 0; i < 6; i++) {
            ItemStack itemStack = inventory.getItem(i);
            if (itemStack.isEmpty()) {
                airSlot++;
            }
            if (airSlot >= requireSlot){
                return true;
            }
            for (Ingredient ingredient : recipe.get().getIngredients()){

                for (ItemStack recipeStack : ingredient.getItems()){
                    if (itemStack.getItem().equals(recipeStack.getItem())){
                        if (itemStack.getCount() + DecompositionExtractorRecipe.getMaxCount(recipeStack) <= itemStack.getMaxStackSize()){
                            canStackSlot++;
                        }
                    }
                }
            }
        }
        return airSlot + canStackSlot >= requireSlot;
    }



    private void resetProgress(){
        this.progress = 0;
    }



}
