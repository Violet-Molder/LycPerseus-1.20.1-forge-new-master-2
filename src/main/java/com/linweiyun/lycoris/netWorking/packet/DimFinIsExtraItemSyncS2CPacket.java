package com.linweiyun.lycoris.netWorking.packet;

import com.linweiyun.lycoris.block.blockentity.DimensionFinderEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class DimFinIsExtraItemSyncS2CPacket {
    private final BlockPos pos;
    private final int extra1;
    private final int extra2;
    private final int extra3;
    public DimFinIsExtraItemSyncS2CPacket(BlockPos pos, int extra1, int extra2, int extra3)
    {
        this.pos = pos;
        this.extra1 = extra1;
        this.extra2 = extra2;
        this.extra3 = extra3;
    }
    public DimFinIsExtraItemSyncS2CPacket(FriendlyByteBuf buf)
    {
        this.pos = buf.readBlockPos();
        this.extra1 = buf.readInt();
        this.extra2 = buf.readInt();
        this.extra3 = buf.readInt();
    }
    public void toBytes(FriendlyByteBuf buf)
    {
        buf.writeBlockPos(pos);
        buf.writeInt(extra1);
        buf.writeInt(extra2);
        buf.writeInt(extra3);
    }
    public boolean handle(Supplier<NetworkEvent.Context> supplier)
    {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                Level world = player.level();
                BlockEntity blockEntity = world.getBlockEntity(pos);
                if (blockEntity instanceof DimensionFinderEntity entity){
                    entity.setExtra1(extra1);
                    entity.setExtra2(extra2);
                    entity.setExtra3(extra3);
                }
            }
        });
        return true;
    }
}
