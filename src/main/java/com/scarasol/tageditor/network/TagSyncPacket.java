package com.scarasol.tageditor.network;

import com.scarasol.tageditor.util.TagHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;
import java.util.function.Supplier;

/**
 * @author Scarasol
 */
public record TagSyncPacket(ResourceLocation tag, List<ResourceLocation> itemAdd, List<ResourceLocation> itemRemove) {

    public static TagSyncPacket decode(FriendlyByteBuf buf) {
        return new TagSyncPacket(buf.readResourceLocation(), buf.readList(FriendlyByteBuf::readResourceLocation), buf.readList(FriendlyByteBuf::readResourceLocation));
    }

    public static void encode(TagSyncPacket msg, FriendlyByteBuf buf) {
        buf.writeResourceLocation(msg.tag);
        buf.writeCollection(msg.itemAdd, FriendlyByteBuf::writeResourceLocation);
        buf.writeCollection(msg.itemRemove, FriendlyByteBuf::writeResourceLocation);
    }

    public static void handler(TagSyncPacket msg, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (msg != null) {
                if (!context.get().getDirection().getReceptionSide().isServer()) {
                    TagHelper.refreshTag(msg.tag, msg.itemAdd, msg.itemRemove);
                } else {

                    ServerPlayer serverPlayer = context.get().getSender();

                    if (serverPlayer != null && serverPlayer.hasPermissions(2)) {
                        ServerLevel serverLevel = serverPlayer.serverLevel();
                        MinecraftServer server = serverLevel.getServer();
                        TagHelper.saveTag(server.registryAccess().registryOrThrow(Registries.ITEM), msg.tag, msg.itemAdd, msg.itemRemove);
                        server.getPlayerList().getPlayers().forEach(player -> {
                            if (!player.equals(serverPlayer)) {
                                NetworkHandler.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> player), msg);
                            }
                        });
                    }
                }
            }
        });
        context.get().setPacketHandled(true);
    }

}
