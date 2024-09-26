package com.linweiyun.lycoris.block.blockentity;

import com.eliotlash.mclib.math.functions.limit.Min;
import com.linweiyun.lycoris.GenAnimations.AssemblingMachineBlockEntityRender;
import com.linweiyun.lycoris.LycPerseusMod;
import com.linweiyun.lycoris.block.custom.AssemblingMachine;
import com.linweiyun.lycoris.items.custom.BatteryItem;
import com.linweiyun.lycoris.recipetype.AssemblingMachineRecipe;
import com.linweiyun.lycoris.screen.AssemblingMachineMenu;
import net.minecraft.client.Minecraft;
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
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import software.bernie.geckolib.util.RenderUtils;

import java.util.Optional;

public class AssemblingMachineBlockEntity extends BlockEntity implements MenuProvider, GeoBlockEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

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
    private int progress = 0;
    private int maxProgress = 100;
    private boolean isStart = false;


    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    public AssemblingMachineBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(LPBlockEntities.ASSEMBLING_MACHINE.get(), pPos, pBlockState);

        this.data = new ContainerData() {
            @Override
            public int get(int i) {
                return switch (i) {
                    case 0 -> AssemblingMachineBlockEntity.this.progress;
                    case 1 -> AssemblingMachineBlockEntity.this.maxProgress;
                    case 2 -> AssemblingMachineBlockEntity.this.isStart ? 1 : 0;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> AssemblingMachineBlockEntity.this.progress = value;
                    case 1 -> AssemblingMachineBlockEntity.this.maxProgress = value;
                    case 2 -> AssemblingMachineBlockEntity.this.isStart = value != 0;
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
        return new AssemblingMachineMenu(i, inventory, this, this.data);
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
        pTag.putInt("assembling_machine.progress", this.progress);

        super.saveAdditional(pTag);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        itemStackHandler.deserializeNBT(pTag.getCompound("inventory"));
        progress = pTag.getInt("assembling_machine.progress");
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemStackHandler.getSlots());
        for (int i = 0; i < itemStackHandler.getSlots(); i++) {
            inventory.setItem(i, itemStackHandler.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    //静态方法，每次回调都会更新状态
    public static void tick(Level level, BlockPos blockPos, BlockState state, AssemblingMachineBlockEntity entity) {
        if (level.isClientSide){
            return;
        }
        AssemblingMachine block = (AssemblingMachine) state.getBlock();
        ItemStack itemStack = entity.itemStackHandler.getStackInSlot(6);
        //  仅仅在服务器端运行
        // 判断是否能够合成
        if(hasRecipe(entity) && itemStack.getItem() instanceof BatteryItem){
            if (!entity.isStart){
                entity.setStart(true);
                block.setIsOn(state, level, blockPos, true);
                setChanged(level,blockPos,state);
            }
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
            if (entity.isStart){
                entity.isStart = false;
                block.setIsOn(state, level, blockPos, false);
            }
            setChanged(level,blockPos,state);
        }

    }

    // 合成物品
    private static void craftItem(AssemblingMachineBlockEntity entity) {
        Level level = entity.level;    //获取实体所在的世界
        //新建一个仓库，槽位数量为该方块实体拥有的槽位数量。和方块本身的槽位不一样，这是创建一个新槽位用来与合成表比对
        SimpleContainer inventory = new SimpleContainer(entity.itemStackHandler.getSlots());
        for(int i=0;i < entity.itemStackHandler.getSlots(); i++) {
            inventory.setItem(i,entity.itemStackHandler.getStackInSlot(i));//将上面新建的仓库各槽位的物品设置为放置在该方块内各槽位的物品
        }
        // 获得当前的recipe
        Optional<AssemblingMachineRecipe> recipe = level.getRecipeManager().getRecipeFor(
                AssemblingMachineRecipe.Type.INSTANCE,inventory,level);
        if(hasRecipe(entity)){//检测有没有这个配方
            if (!recipe.isPresent()) return;//如果这个配方是空的则直接返回
            //获取配方并检测是否满足合成条件
            AssemblingMachineRecipe Trepice = recipe.get();
            //如果满足合成条件则开始合成
            if (Trepice.matches(inventory, level)) {
//                //从槽位中逐个取出合成原料，从1开始。0是催化剂。
//                for (int i = 1; i < 7; i ++){
//                    entity.itemStackHandler.extractItem(i, 1, false);
//                }
                //将合成结果放入槽位7

                for (Ingredient ingredient : Trepice.getIngredients()) {
                    for (ItemStack stack : ingredient.getItems()) {
                        int extractNumber = stack.getCount();
                        extractItem(entity, stack);
                    }
                }
                entity.itemStackHandler.setStackInSlot(7, new ItemStack(recipe.get().getResultItem(level.registryAccess()).getItem(),
                        entity.itemStackHandler.getStackInSlot(7).getCount()
                                + recipe.get().getResultItem(level.registryAccess()).getCount()));
                //重置合成进度
                entity.resetProgress();
            }
            // 合成的结果是recipe的result

        }
    }
    // 从槽位中提取指定数量的物品
    private static void extractItem(AssemblingMachineBlockEntity entity, ItemStack stack) {
        for (int i = 0; i < 6; i++) { // 从槽位1开始
            ItemStack slotStack = entity.itemStackHandler.getStackInSlot(i);
            if (!slotStack.isEmpty() && slotStack.getItem() == stack.getItem()) {
                int count = Math.min(stack.getCount(), slotStack.getCount());
                slotStack.shrink(count);
                if (stack.getCount() <= 0) break;
            }
        }
    }

    // 是否具有合成表
    private static boolean hasRecipe(AssemblingMachineBlockEntity entity) {
        Level level = entity.level;
        SimpleContainer inventory = new SimpleContainer(entity.itemStackHandler.getSlots());
        for(int i=0;i<entity.itemStackHandler.getSlots();i++){
            inventory.setItem(i,entity.itemStackHandler.getStackInSlot(i));
        }
        Optional<AssemblingMachineRecipe> recipe = level.getRecipeManager().getRecipeFor(AssemblingMachineRecipe.Type
                .INSTANCE,inventory,level);
        if (recipe.isPresent()){
            entity.maxProgress = recipe.get().getTime() * 20;
            setChanged(level,entity.worldPosition,entity.getBlockState());
        }


        return recipe.isPresent() && canInsertAmountInToOutputSlot(inventory)&&
                canInsertItemToOutputSlot(inventory,recipe.get().getResultItem(level.registryAccess()));
    }
    // 判断插入slot是是否是相同的item，以及是否为空
    private static boolean canInsertItemToOutputSlot(SimpleContainer inventory, ItemStack itemStack) {
        return inventory.getItem(7).getItem() == itemStack.getItem() || inventory.getItem(7).isEmpty();
    }
    // 判断堆叠是否已满，还能否放入item
    private static boolean canInsertAmountInToOutputSlot(SimpleContainer inventory) {
        return inventory.getItem(7).getMaxStackSize() > inventory.getItem(7).getCount();
    }





    private void resetProgress(){
        this.progress = 0;
    }

    private boolean getIsStart(){
        return this.isStart;
    }
    private void setStart(boolean isStart){
        this.isStart = isStart;
    }


    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
            controllerRegistrar.add(new AnimationController<>(this, "controller", 1, this::predicate));

    }
    private <T extends GeoAnimatable> PlayState predicate(AnimationState<T> tAnimationState) {

        AssemblingMachineBlockEntity entity = (AssemblingMachineBlockEntity) tAnimationState.getAnimatable();
        BlockPos pos = entity.getBlockPos();
        BlockState state =entity.getLevel().getBlockState(pos);
        if ((state.getBlock() instanceof AssemblingMachine)){
            AssemblingMachine block = (AssemblingMachine) state.getBlock();
            if (block.getIsOn(state)) {
                tAnimationState.getController().setAnimation(RawAnimation.begin().then("block.animation", Animation.LoopType.LOOP));
            }
            if (!block.getIsOn(state)){
                tAnimationState.getController().setAnimation(RawAnimation.begin().then("close", Animation.LoopType.PLAY_ONCE));
            }
        }




        return PlayState.CONTINUE;
    }

    @Override
    public double getTick(Object blockEntity) {
        return RenderUtils.getCurrentTick();
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

}
