package com.linweiyun.lycoris.block.blockentity;

import com.linweiyun.lycoris.LycPerseusMod;
import com.linweiyun.lycoris.block.LPBlockEntities;
import com.linweiyun.lycoris.block.custom.WeaponWorkbench;
import com.linweiyun.lycoris.items.custom.BatteryItem;
import com.linweiyun.lycoris.items.custom.ExtraItem;
import com.linweiyun.lycoris.items.custom.SoulItem;
import com.linweiyun.lycoris.recipetype.DecompositionExtractorRecipe;
import com.linweiyun.lycoris.recipetype.WeaponWorkbenchRecipeType;
import com.linweiyun.lycoris.screen.WeaponWorkbenchMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.MinecartHopper;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;


public class WeaponWorkbenchEntity extends BlockEntity implements MenuProvider {

    public ItemStackHandler itemStackHandler = new ItemStackHandler(20) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }



        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (isBelowHopperOrHopperMinecart()){
                return ItemStack.EMPTY;
            }
            return super.extractItem(slot, amount, simulate);
        }
    };
    private boolean isBelowHopperOrHopperMinecart() {
        Level level = getLevel();
        if (level != null) {
            BlockPos belowPos = getBlockPos().below();
            BlockState belowState = level.getBlockState(belowPos);
            Block belowBlock = belowState.getBlock();

            if (belowBlock == Blocks.HOPPER) {
                return true;
            }

            AABB aabb = new AABB(belowPos);
            for (Entity entity : level.getEntities((Entity) null, aabb, e -> e instanceof MinecartHopper)) {
                if (entity instanceof MinecartHopper) {
                    return true;
                }
            }
        }
        return false;
    }



    protected final ContainerData data;






    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    public WeaponWorkbenchEntity(BlockPos pPos, BlockState pBlockState) {
        super(LPBlockEntities.WEAPON_WORKBENCH.get(), pPos, pBlockState);

        this.data = new ContainerData() {
            @Override
            public int get(int i) {
                return switch (i) {
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                }
            }

            @Override
            public int getCount() {
                // 增加额外的三个int变量和一个boolean变量，共6个数据项
                return 0;
            }
        };
    }



    @Override
    public Component getDisplayName() {
        return Component.literal("factory.name:weapon_workbench");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new WeaponWorkbenchMenu(i, inventory, this, this.data);
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

        super.saveAdditional(pTag);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        itemStackHandler.deserializeNBT(pTag.getCompound("inventory"));
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemStackHandler.getSlots());
        for (int i = 0; i < 2; i++) {
            inventory.setItem(i, itemStackHandler.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, WeaponWorkbenchEntity pBlockEntity) {
        if (pLevel.isClientSide()) {
            return;
        }

            if(hasRecipe(pBlockEntity)){
                    //新建一个仓库，槽位数量为该方块实体拥有的槽位数量。和方块本身的槽位不一样，这是创建一个新槽位用来与合成表比对
                    SimpleContainer inventory = new SimpleContainer(pBlockEntity.itemStackHandler.getSlots());
                    inventory.setItem(0, pBlockEntity.itemStackHandler.getStackInSlot(0).copy());
                    // 获得当前的recipe
                    Optional<WeaponWorkbenchRecipeType> recipe = pLevel.getRecipeManager().getRecipeFor(
                            WeaponWorkbenchRecipeType.Type.INSTANCE,inventory,pLevel);
                NonNullList<Ingredient> ingredients = recipe.get().getIngredients();
                // 设置 NBT 标签并将原料放入 2-23 号槽位
                for (int i = 0; i < ingredients.size(); i++) {
                    Ingredient ingredient = ingredients.get(i);
                    for (ItemStack stack : ingredient.getItems()) {
                        ItemStack outStack = new ItemStack(stack.getItem());
                        for (int j = 1; j <= 4; j++){
                            String nbtId = WeaponWorkbenchRecipeType.getNbtId(stack, j);
                            if (!nbtId.isEmpty()) {
                                CompoundTag nbt = outStack.getOrCreateTag();
                                String nbtValue = WeaponWorkbenchRecipeType.getNbtValue(stack, j);
                                nbt.putString(nbtId, nbtValue);
                                outStack.setTag(nbt);
                            }
                        }


                        pBlockEntity.itemStackHandler.setStackInSlot(i + 2, outStack);
                    }
                }
            } else {
                for (int i = 1; i < 20; i++){
                    pBlockEntity.itemStackHandler.setStackInSlot(i, ItemStack.EMPTY);
                }
            }


    }

    private static boolean hasRecipe(WeaponWorkbenchEntity entity){
        Level level = entity.level;
        SimpleContainer inventory = new SimpleContainer(entity.itemStackHandler.getSlots());
        for (int i = 0; i < 2; i++){
            inventory.setItem(i, entity.itemStackHandler.getStackInSlot(i));
        }


        assert level != null;
        Optional<WeaponWorkbenchRecipeType> recipe = level.getRecipeManager().getRecipeFor(WeaponWorkbenchRecipeType.Type.INSTANCE,
                inventory,level);

        return recipe.isPresent();

    }

}
