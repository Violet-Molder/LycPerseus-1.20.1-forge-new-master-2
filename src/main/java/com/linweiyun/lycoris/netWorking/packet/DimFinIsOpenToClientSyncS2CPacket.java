package com.linweiyun.lycoris.netWorking.packet;

import com.linweiyun.lycoris.block.blockentity.DimensionFinderEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class DimFinIsOpenToClientSyncS2CPacket {
    private final BlockPos pos;
    private final Boolean isStart;
    public DimFinIsOpenToClientSyncS2CPacket(BlockPos pos, Boolean isStart)
    {
        this.pos = pos;
        this.isStart = isStart;
    }
    public DimFinIsOpenToClientSyncS2CPacket(FriendlyByteBuf buf)
    {
        this.pos = buf.readBlockPos();
        this.isStart = buf.readBoolean();
    }
    public void toBytes(FriendlyByteBuf buf)
    {
        buf.writeBlockPos(pos);
        buf.writeBoolean(isStart);
    }


    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().level != null) {
                BlockEntity blockEntity = Minecraft.getInstance().level.getBlockEntity(pos);
                if (blockEntity instanceof DimensionFinderEntity) {
                    ((DimensionFinderEntity) blockEntity).setStart(isStart);
                }
            }
        });
        return true;
    }
}
