package com.linweiyun.lycoris.screen;

import com.linweiyun.lycoris.block.LPBlocks;
import com.linweiyun.lycoris.block.blockentity.WeaponWorkbenchEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class WeaponWorkbenchMenu extends AbstractContainerMenu  {
    public final WeaponWorkbenchEntity entity;
    private final Level level;
    private final ContainerData data;

    public WeaponWorkbenchMenu(int id, Inventory inventory, FriendlyByteBuf extraData){
        this(id,inventory, inventory.player.level().getBlockEntity( extraData.readBlockPos()),new SimpleContainerData(20));
    }

    public WeaponWorkbenchMenu(int id, Inventory inv, BlockEntity entity, ContainerData data){
        super(LycorisMenuType.WEAPON_WORKBENCH_MENU_TYPE.get(),id);
        // 检查slots是否为8
        checkContainerSize(inv,8);
        this.entity = (WeaponWorkbenchEntity) entity;
        this.level = inv.player.level();
        this.data =data;
        // 处理玩家的背包和物品栏


        // 为当的menu增加3个slot，分别对应itemhandler的0,1,2,
        this.entity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            addSlot(new SlotItemHandler(handler, 0, 143, 13));
            addSlot(new SlotItemHandler(handler, 1, 143, 53){
                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    return false;
                }
            });
            int row = 0;
            int col = 0;

            for (int i = 0; i < 18; i++) {
                if (col >= 6) {
                    row++;
                    col = 0;
                }
                    addSlot(new SlotItemHandler(handler, i + 2, 13 + col * 19, 13 + row * 19){
                        @Override
                        public boolean mayPickup(Player playerIn) {
                            return false;
                        }

                        @Override
                        public boolean mayPlace(@NotNull ItemStack stack) {
                            return false;
                        }
                    });

                // 添加槽位


                col++;
            }
        });
        addPlayerInventory(inv);
        addPlayerHotbar(inv);
        // 将进度数据同步到Client
        addDataSlots(data);
    }

    @Override
    public boolean clickMenuButton(Player pPlayer, int pId) {
        return super.clickMenuButton(pPlayer, pId);
    }

    @Override
    public void clicked(int pSlotId, int pButton, ClickType pClickType, Player pPlayer) {
        if (!pPlayer.level().isClientSide){
            if (pSlotId >= 2 && pSlotId < 20){
                ItemStack itemStack = entity.itemStackHandler.getStackInSlot(pSlotId).copy();
                ItemStack outStack = entity.itemStackHandler.getStackInSlot(1);
                // 确保输出的物品保留 NBT 标签
                if (!itemStack.isEmpty()) {
                    if (itemStack.getItem() == outStack.getItem() &&
                            outStack.getCount() < outStack.getMaxStackSize()){
                        itemStack.setCount(outStack.getCount() + 1);
                    }
                    // 数量加 1

                    // 将输出的物品放入 1 号槽位
                    entity.itemStackHandler.setStackInSlot(1, itemStack);
                }
            } else if (pSlotId == 1){
                ItemStack itemStack = entity.itemStackHandler.getStackInSlot(1);
                if (!itemStack.isEmpty()){
                    entity.itemStackHandler.extractItem(0,entity.itemStackHandler.getStackInSlot(1).getCount(),false);
                }

            }
        }

        super.clicked(pSlotId, pButton, pClickType, pPlayer);
    }

    private void addPlayerInventory(Inventory playerInventory){
        for (int i=0;i<3;i++){
            for (int l =0;l<9;l++){
                this.addSlot(new Slot(playerInventory,l+i*9+9,8+l*18,84+i*18));
            }
        }
    }


    private void addPlayerHotbar(Inventory playerInventory){
        for(int i=0;i<9;i++){
            this.addSlot(new Slot(playerInventory,i,8+i*18,142));
        }
    }



    // 快速移动物品用，直接复制
    // CREDIT GOES TO: diesieben07 | https://github.com/diesieben07/SevenCommons
    // must assign a slot number to each of the slots used by the GUI.
    // For this container, we can see both the tile inventory's slots as well as the player inventory slots and the hotbar.
    // Each time we add a Slot to the container, it automatically increases the slotIndex, which means
    //  0 - 8 = hotbar slots (which will map to the InventoryPlayer slot numbers 0 - 8)
    //  9 - 35 = player inventory slots (which map to the InventoryPlayer slot numbers 9 - 35)
    //  36 - 44 = TileInventory slots, which map to our TileEntity slot numbers 0 - 8)
    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;
    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    private static final int TE_INVENTORY_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;

    // THIS YOU HAVE TO DEFINE!
    private static final int TE_INVENTORY_SLOT_COUNT = 20;  // must be the number of slots you have!


    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        // 1. 获取目标槽位
        Slot sourceSlot = slots.get(index);

        // 2. 检查槽位是否存在且包含物品
        if (sourceSlot == null || !sourceSlot.hasItem()) {
            // 如果不存在或无物品，则返回空物品堆栈
            return ItemStack.EMPTY;
        }

        // 3. 获取源槽位上的物品堆栈并创建一份副本
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        // 4. 判断点击的槽位属于哪一类
        if (index < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT) {
            // 4.1 如果是Vanilla容器槽位（如玩家背包槽位）
            // 尝试将物品堆栈转移到TileEntity（TE）库存中
            if (!moveItemStackTo(sourceStack, TE_INVENTORY_FIRST_SLOT_INDEX, TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT, false)) {
                // 移动失败则返回空物品堆栈
                return ItemStack.EMPTY;
            }
        } else if (index < TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT) {
            // 4.2 如果是TE槽位（如方块内部的特殊库存槽位）
            // 尝试将物品堆栈转移到玩家库存中
            if (index >= 2 && index < 20) {
                // 禁止快速移动
                return copyOfSourceStack;
            }


            if (!moveItemStackTo(sourceStack, VANILLA_FIRST_SLOT_INDEX, VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT, false)) {
                // 移动失败则返回空物品堆栈
                return ItemStack.EMPTY;
            }
        } else {
            // 4.3 非法槽位索引，打印错误信息并返回空物品堆栈
            return ItemStack.EMPTY;
        }

        // 5. 检查源槽位上的物品堆栈是否已完全移除（数量为0）
        if (sourceStack.getCount() == 0) {
            // 若完全移除，则将槽位内容设为空物品堆栈
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            // 否则，标记槽位内容已更改
            sourceSlot.setChanged();
        }

        // 6. 调用槽位的`onTake`方法，通知其物品已被玩家取走
        sourceSlot.onTake(playerIn, sourceStack);

        // 7. 返回源物品堆栈的副本，用于客户端同步
        return copyOfSourceStack;
    }
    // 每个menu需要实现的方法
    // 用于检查玩家是否在blockentity 8个方块内。
    @Override
    public boolean stillValid(Player player) {
        // containerLevelAccess 在服务器创建一个 containerlevelaccesss对象，提供了当前的世界和方块位置是否在一个封闭的范围
        return stillValid(ContainerLevelAccess.create(level,entity.getBlockPos()),
                player, LPBlocks.WEAPON_WORKBENCH.get());
    }


}
