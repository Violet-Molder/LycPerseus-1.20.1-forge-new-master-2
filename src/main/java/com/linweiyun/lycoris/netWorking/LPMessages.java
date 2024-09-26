package com.linweiyun.lycoris.netWorking;

import com.linweiyun.lycoris.LycPerseusMod;
import com.linweiyun.lycoris.netWorking.packet.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class LPMessages {

    private static SimpleChannel INSTANCE;

    private static int packetId = 0;

    private static int id() {
        return packetId++;
    }

    public static void register() {
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(LycPerseusMod.MOD_ID, "messages"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE = net;

        net.messageBuilder(EnergySyncS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(EnergySyncS2CPacket::new)
                .encoder(EnergySyncS2CPacket::toBytes)
                .consumerMainThread(EnergySyncS2CPacket:: handle)
                .add();

        net.messageBuilder(ProgressSyncS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(ProgressSyncS2CPacket::new)
                .encoder(ProgressSyncS2CPacket::toBytes)
                .consumerMainThread(ProgressSyncS2CPacket:: handle)
                .add();
        net.messageBuilder(DimFinIsOpenSyncS2CPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(DimFinIsOpenSyncS2CPacket::new)
                .encoder(DimFinIsOpenSyncS2CPacket::toBytes)
                .consumerMainThread(DimFinIsOpenSyncS2CPacket:: handle)
                .add();
        net.messageBuilder(DimFinIsOpenToClientSyncS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(DimFinIsOpenToClientSyncS2CPacket::new)
                .encoder(DimFinIsOpenToClientSyncS2CPacket::toBytes)
                .consumerMainThread(DimFinIsOpenToClientSyncS2CPacket:: handle)
                .add();

        net.messageBuilder(DimFinIsExtraItemSyncS2CPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(DimFinIsExtraItemSyncS2CPacket::new)
                .encoder(DimFinIsExtraItemSyncS2CPacket::toBytes)
                .consumerMainThread(DimFinIsExtraItemSyncS2CPacket:: handle)
                .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }
    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
    public static <MSG> void sendToClients(MSG message) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), message);
    }
}
