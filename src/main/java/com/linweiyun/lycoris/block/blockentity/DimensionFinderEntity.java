package com.linweiyun.lycoris.block.blockentity;

import com.linweiyun.lycoris.block.LPBlockEntities;
import com.linweiyun.lycoris.block.custom.DimensionFinder;
import com.linweiyun.lycoris.items.LPItems;
import com.linweiyun.lycoris.items.custom.ExtraItem;
import com.linweiyun.lycoris.items.custom.SoulItem;
import com.linweiyun.lycoris.recipetype.DimensionFinderRecipeType;
import com.linweiyun.lycoris.screen.DimensionFinderMenu;
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
import net.minecraft.world.item.ItemStack;
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

import java.util.Optional;
import java.util.Random;

public class DimensionFinderEntity extends BlockEntity implements MenuProvider {
    private static float extraCount1 = 1.0f;
    private static float extraCount2 = 0.2f;
    private static int extraCount3 = 15;

    public final ItemStackHandler itemStackHandler = new ItemStackHandler(6) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case 0, 1 -> !(stack.getItem() instanceof SoulItem) && !(stack.getItem() instanceof ExtraItem);
                case 2 -> stack.getItem() instanceof SoulItem;
                case 3, 4, 5 -> stack.getItem() instanceof ExtraItem;
                default -> super.isItemValid(slot, stack);
            };
        }
    };



    protected final ContainerData data;
    private int     progress = 0;
    private int maxProgress = 100;
    private boolean isStart = false;
    public void setStart(boolean isStart){
        this.isStart = isStart;
        if (!level.isClientSide && level != null){
            level.setBlock(getBlockPos(), getBlockState().setValue(DimensionFinder.IS_ON, isStart), 3);
        }
    }
    public boolean getIsStart(){
        return this.isStart;
    }
    public void setProgress(int progress){
        this.progress = this.progress + progress;
    }
    public void setMaxProgress(int maxProgress){
        this.maxProgress = maxProgress;
    }






    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    public DimensionFinderEntity(BlockPos pPos, BlockState pBlockState) {
        super(LPBlockEntities.DIMENSION_FINDER.get(), pPos, pBlockState);

        this.data = new ContainerData() {
            @Override
            public int get(int i) {
                return switch (i) {
                    case 0 -> DimensionFinderEntity.this.progress;
                    case 1 -> DimensionFinderEntity.this.maxProgress;
                    // 注意：布尔值需要转换为int，0表示false，1表示true
                    case 2 -> DimensionFinderEntity.this.isStart ? 1 : 0;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> DimensionFinderEntity.this.progress = value;
                    case 1 -> DimensionFinderEntity.this.maxProgress = value;
                    // 注意：从int转换回boolean，非0值视为true
                    case 2 -> DimensionFinderEntity.this.isStart = value != 0;
                }
            }

            @Override
            public int getCount() {
                // 增加额外的三个int变量和一个boolean变量，共6个数据项
                return 3;
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
        return new DimensionFinderMenu(i, inventory, this, this.data);
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
        pTag.putInt("dimension_finder.progress", this.progress);

        super.saveAdditional(pTag);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        itemStackHandler.deserializeNBT(pTag.getCompound("inventory"));
        progress = pTag.getInt("dimension_finder.progress");
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemStackHandler.getSlots());
        for (int i = 0; i < itemStackHandler.getSlots(); i++) {
            inventory.setItem(i, itemStackHandler.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inventory);
    }
    public int getProgress() {
        return progress;
    }
    public int getMaxProgress() {
        return maxProgress;
    }

    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, DimensionFinderEntity pBlockEntity) {
        if (pLevel.isClientSide()) {
            return;
        }

            if(hasRecipe(pBlockEntity) && pBlockEntity.itemStackHandler.getStackInSlot(2).getItem() instanceof SoulItem){
                pBlockEntity.setStart(true);
                int e1 = 0;
                int e2 = 0;
                int e3 = 0;
                for (int i = 0; i < 3; i++){

                    if (pBlockEntity.itemStackHandler.getStackInSlot(i+3).getItem() == LPItems.AUXILIARY_REACTION_FURNACE.get()){
                        e1++;
                    } else if (pBlockEntity.itemStackHandler.getStackInSlot(i+3).getItem() == LPItems.UTILITY_MINING_RUNE.get()){
                        e2++;
                    } else if (pBlockEntity.itemStackHandler.getStackInSlot(i+3).getItem() == LPItems.SHADOW_TRANSMISSION_MODULE.get()){
                        e3++;
                    }
                }
                ItemStack itemStack = pBlockEntity.itemStackHandler.getStackInSlot(2);
                if (itemStack.getDamageValue() + 4 - e1 <= itemStack.getMaxDamage()){
                    //新建一个仓库，槽位数量为该方块实体拥有的槽位数量。和方块本身的槽位不一样，这是创建一个新槽位用来与合成表比对
                    SimpleContainer inventory = new SimpleContainer(pBlockEntity.itemStackHandler.getSlots());
                    inventory.setItem(0, pBlockEntity.itemStackHandler.getStackInSlot(0).copy());
                    // 获得当前的recipe
                    Optional<DimensionFinderRecipeType> recipe = pLevel.getRecipeManager().getRecipeFor(
                            DimensionFinderRecipeType.Type.INSTANCE,inventory,pLevel);


                    //进度上限，也就是需要多长时间
                    pBlockEntity.maxProgress = (int) (recipe.get().getTime() * 20 * (1-extraCount2 * e3));
                    pBlockEntity.progress++;

                    setChanged(pLevel, pPos, pState);

                    if (pBlockEntity.progress >= pBlockEntity.maxProgress) {

                        craftItem(pBlockEntity);
                        Random random = new Random();
                        int randomNumber = random.nextInt(100);
                        boolean isDouble = randomNumber < (extraCount3 * e2);
                        if (isDouble){
                            craftItem(pBlockEntity);
                        }
                        itemStack.setDamageValue(itemStack.getDamageValue()+4-e1);
                        if (itemStack.getDamageValue() >= itemStack.getMaxDamage()){
                            itemStack.shrink(1);
                        }
                        pBlockEntity.resetProgress();
                    }
                } else {
                    pBlockEntity.setStart(false);
                    pBlockEntity.resetProgress();
                    setChanged(pLevel, pPos, pState);
                }



            } else {
                pBlockEntity.setStart(false);
                pBlockEntity.resetProgress();
                setChanged(pLevel, pPos, pState);
            }


    }

    private static void craftItem(DimensionFinderEntity pBlockEntity) {
        if (hasRecipe(pBlockEntity)){
            Level level = pBlockEntity.level;    //获取实体所在的世界
            //新建一个仓库，槽位数量为该方块实体拥有的槽位数量。和方块本身的槽位不一样，这是创建一个新槽位用来与合成表比对
            SimpleContainer inventory = new SimpleContainer(pBlockEntity.itemStackHandler.getSlots());
            inventory.setItem(0, pBlockEntity.itemStackHandler.getStackInSlot(0).copy());
            // 获得当前的recipe
            Optional<DimensionFinderRecipeType> recipe = level.getRecipeManager().getRecipeFor(
                    DimensionFinderRecipeType.Type.INSTANCE,inventory,level);

            if (hasRecipe(pBlockEntity)){
                if (!recipe.isPresent()) return;

                DimensionFinderRecipeType Trecipe = recipe.get();
                if (Trecipe.matches(inventory, level)) {

                        pBlockEntity.itemStackHandler.setStackInSlot(1, new ItemStack(recipe.get().getResultItem(level.registryAccess()).getItem(),
                                pBlockEntity.itemStackHandler.getStackInSlot(1).getCount() + 1));

                }
            }
        }
    }

    private void resetProgress(){
        this.progress = 0;
    }


    private static boolean hasRecipe(DimensionFinderEntity entity){
        Level level = entity.level;
        SimpleContainer inventory = new SimpleContainer(entity.itemStackHandler.getSlots());
        for (int i = 0; i < 2; i++){
            inventory.setItem(i, entity.itemStackHandler.getStackInSlot(i));
        }


        assert level != null;
        Optional<DimensionFinderRecipeType> recipe = level.getRecipeManager().getRecipeFor(DimensionFinderRecipeType.Type.INSTANCE,
                inventory,level);
        return recipe.isPresent() && canInsertAmountInToOutputSlot(inventory) && canInsertItemToOutputSlot(inventory, recipe.get().getResultItem(level.registryAccess()));

    }

    // 判断插入slot是是否是相同的item，以及是否为空
    private static boolean canInsertItemToOutputSlot(SimpleContainer inventory, ItemStack itemStack) {
        return inventory.getItem(1).getItem() == itemStack.getItem() || inventory.getItem(1).isEmpty();
    }
    // 判断堆叠是否已满，还能否放入item
    private static boolean canInsertAmountInToOutputSlot(SimpleContainer inventory) {
        return inventory.getItem(1).getMaxStackSize()> inventory.getItem(1).getCount();
    }
}
