package com.linweiyun.lycoris.netWorking.packet;

import com.linweiyun.lycoris.block.blockentity.DimensionFinderEntity;
import com.linweiyun.lycoris.screen.DimensionFinderMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ProgressSyncS2CPacket {
    private final int progress;
    private final int maxProgress;
    private final BlockPos pos;

    public ProgressSyncS2CPacket(int progress, int maxProgress ,BlockPos pos){
        this.progress = progress;
        this.maxProgress = maxProgress;
        this.pos = pos;
    }

    public ProgressSyncS2CPacket(FriendlyByteBuf buf) {
        this.progress = buf.readInt();
        this.maxProgress = buf.readInt();
        this.pos = buf.readBlockPos();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(progress);
        buf.writeInt(maxProgress);
        buf.writeBlockPos(pos);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().level.getBlockEntity(pos) instanceof DimensionFinderEntity blockEntity) {
                if (progress >= 0){
                    blockEntity.setProgress(progress);
                }
                if (maxProgress >= 0){
                    blockEntity.setMaxProgress(maxProgress);
                }
            }
        });
        return true;
    }
}
