package com.linweiyun.lycoris.block.blockentity;

import com.linweiyun.lycoris.block.custom.DimensionFinder;
import com.linweiyun.lycoris.items.custom.ExtraItem;
import com.linweiyun.lycoris.items.custom.SoulItem;
import com.linweiyun.lycoris.netWorking.LPMessages;
import com.linweiyun.lycoris.netWorking.packet.*;
import com.linweiyun.lycoris.recipetype.LPDimensionFinderRecipeType;
import com.linweiyun.lycoris.screen.DimensionFinderMenu;
import com.linweiyun.lycoris.until.LPSoulEnergyStorage;
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
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.Random;

public class DimensionFinderEntity extends BlockEntity implements MenuProvider {
    private static float extraCount1 = 1.0f;
    private static float extraCount2 = 0.2f;
    private static int extraCount3 = 15;
    private static final int SOUL_VAULE = 1;

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
    private int soulValue;
    private int extra1;
    private int extra2;
    private int extra3;
    private boolean isStart = false;

    public void setExtra1(int value){
        this.extra1 = value;
    }
    public void setExtra2(int value){
        this.extra2 = value;
    }
    public void setExtra3(int value){
        this.extra3 = value;
    }
    public void setStart(boolean isStart){
        this.isStart = isStart;
        if (!level.isClientSide && level != null){
            level.setBlock(getBlockPos(), getBlockState().setValue(DimensionFinder.IS_ON, isStart), 3);
        }
    }
    public int getExtra1(){
        return this.extra1;
    }
    public int getExtra2(){
        return this.extra2;
    }
    public int getExtra3(){
        return this.extra3;
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


    private final LPSoulEnergyStorage SOUL_ENERGY_STORAGE = new LPSoulEnergyStorage(200000, 80000) {
        @Override
        public void onEnergyChanged() {
            setChanged();
            LPMessages.sendToClients(new EnergySyncS2CPacket(this.energy, getBlockPos()));
        }
    };



    private LazyOptional<IEnergyStorage> lazyEnergyHandler = LazyOptional.empty();
    public IEnergyStorage getEnergyStorage() {
        return SOUL_ENERGY_STORAGE;
    }

    public void setEnergyLevel(int energy) {
        this.SOUL_ENERGY_STORAGE.setEnergy(energy);
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
                    case 2 -> DimensionFinderEntity.this.extra1;
                    case 3 -> DimensionFinderEntity.this.extra2;
                    case 4 -> DimensionFinderEntity.this.extra3;
                    // 注意：布尔值需要转换为int，0表示false，1表示true
                    case 5 -> DimensionFinderEntity.this.isStart ? 1 : 0;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> DimensionFinderEntity.this.progress = value;
                    case 1 -> DimensionFinderEntity.this.maxProgress = value;
                    case 2 -> DimensionFinderEntity.this.extra1 = value;
                    case 3 -> DimensionFinderEntity.this.extra2 = value;
                    case 4 -> DimensionFinderEntity.this.extra3 = value;
                    // 注意：从int转换回boolean，非0值视为true
                    case 5 -> DimensionFinderEntity.this.isStart = value != 0;
                }
            }

            @Override
            public int getCount() {
                // 增加额外的三个int变量和一个boolean变量，共6个数据项
                return 6;
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

        if (cap == ForgeCapabilities.ENERGY) {
            return lazyEnergyHandler.cast();
        }

        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }

        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemStackHandler);
        lazyEnergyHandler = LazyOptional.of(() -> SOUL_ENERGY_STORAGE);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
        lazyEnergyHandler.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        pTag.put("inventory", itemStackHandler.serializeNBT());
        pTag.putInt("dimension_finder.progress", this.progress);
        pTag.putInt("dimension_finder.soul_value", SOUL_ENERGY_STORAGE.getEnergyStored());

        super.saveAdditional(pTag);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        itemStackHandler.deserializeNBT(pTag.getCompound("inventory"));
        progress = pTag.getInt("dimension_finder.progress");
        SOUL_ENERGY_STORAGE.setEnergy(pTag.getInt("dimension_finder.soul_value"));
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
        if (pBlockEntity.getIsStart()){
            //接受能量
            if (hasSoulItemInThirdSlot(pBlockEntity) && pBlockEntity.SOUL_ENERGY_STORAGE.getEnergyStored() < pBlockEntity.SOUL_ENERGY_STORAGE.getMaxEnergyStored()) {
                SoulItem soulItem = (SoulItem) pBlockEntity.itemStackHandler.getStackInSlot(2).getItem();
                if ((pBlockEntity.getEnergyStorage().getMaxEnergyStored() - pBlockEntity.getEnergyStorage().getEnergyStored()) >= soulItem.getSoulVaule()){
                    pBlockEntity.SOUL_ENERGY_STORAGE.receiveEnergy(soulItem.getSoulVaule(), false);
                    pBlockEntity.itemStackHandler.setStackInSlot(2, new ItemStack(soulItem, pBlockEntity.itemStackHandler.getStackInSlot(2).getCount() - 1));
                }

            }

            if(hasRecipe(pBlockEntity) && pBlockEntity.SOUL_ENERGY_STORAGE.getEnergyStored() > 0){

                //新建一个仓库，槽位数量为该方块实体拥有的槽位数量。和方块本身的槽位不一样，这是创建一个新槽位用来与合成表比对
                SimpleContainer inventory = new SimpleContainer(pBlockEntity.itemStackHandler.getSlots());
                inventory.setItem(0, pBlockEntity.itemStackHandler.getStackInSlot(0).copy());
                // 获得当前的recipe
                Optional<LPDimensionFinderRecipeType> recipe = pLevel.getRecipeManager().getRecipeFor(
                        LPDimensionFinderRecipeType.Type.INSTANCE,inventory,pLevel);

                //进度上限，也就是需要多长时间
                pBlockEntity.maxProgress = (int) (recipe.get().getTime() * (1-extraCount2* pBlockEntity.getExtra3()));
                pBlockEntity.progress++;

                //消耗能量，默认每tick消耗10点
                extractEnergy(pBlockEntity, (int) (SOUL_VAULE / (pBlockEntity.getExtra1() * extraCount1 + 1) ));
                setChanged(pLevel, pPos, pState);

                if (pBlockEntity.progress >= pBlockEntity.maxProgress) {
                    craftItem(pBlockEntity);
                    pBlockEntity.resetProgress();
                }

            } else {
                pBlockEntity.resetProgress();
                setChanged(pLevel, pPos, pState);
            }
        }


    }
    private static void extractEnergy(DimensionFinderEntity pBlockEntity, int soulValue) {
        pBlockEntity.SOUL_ENERGY_STORAGE.extractEnergy(soulValue, false);
    }

    private static boolean hasSoulItemInThirdSlot(DimensionFinderEntity entity){
        return entity.itemStackHandler.getStackInSlot(2).getItem() instanceof SoulItem;
    }

    private static void craftItem(DimensionFinderEntity pBlockEntity) {
        if (hasRecipe(pBlockEntity)){
            Level level = pBlockEntity.level;    //获取实体所在的世界
            //新建一个仓库，槽位数量为该方块实体拥有的槽位数量。和方块本身的槽位不一样，这是创建一个新槽位用来与合成表比对
            SimpleContainer inventory = new SimpleContainer(pBlockEntity.itemStackHandler.getSlots());
            inventory.setItem(0, pBlockEntity.itemStackHandler.getStackInSlot(0).copy());
            // 获得当前的recipe
            Optional<LPDimensionFinderRecipeType> recipe = level.getRecipeManager().getRecipeFor(
                    LPDimensionFinderRecipeType.Type.INSTANCE,inventory,level);

            if (hasRecipe(pBlockEntity)){
                if (!recipe.isPresent()) return;

                LPDimensionFinderRecipeType Trecipe = recipe.get();
                if (Trecipe.matches(inventory, level)) {
                    Random random = new Random();
                    int randomNumber = random.nextInt(100);
                    boolean isDouble = randomNumber < (extraCount3 * pBlockEntity.getExtra2());
                    if (isDouble){
                        pBlockEntity.itemStackHandler.setStackInSlot(1, new ItemStack(recipe.get().getResultItem(level.registryAccess()).getItem(),
                                pBlockEntity.itemStackHandler.getStackInSlot(1).getCount() + 2));
                    }
                    else {
                        pBlockEntity.itemStackHandler.setStackInSlot(1, new ItemStack(recipe.get().getResultItem(level.registryAccess()).getItem(),
                                pBlockEntity.itemStackHandler.getStackInSlot(1).getCount() + 1));
                    }

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
        Optional<LPDimensionFinderRecipeType> recipe = level.getRecipeManager().getRecipeFor(LPDimensionFinderRecipeType.Type.INSTANCE,
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
