package com.scarasol.tageditor.compat.tacz.network;

import com.scarasol.tageditor.compat.tacz.TaczTagHelper;
import com.scarasol.tageditor.network.NetworkHandler;
import com.scarasol.tageditor.network.TagSyncPacket;
import com.scarasol.tageditor.util.TagHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author Scarasol
 */
public record TaczSyncPacket(Map<ResourceLocation, Set<ResourceLocation>> itemAdd) {

    public static TaczSyncPacket decode(FriendlyByteBuf buf) {
        return new TaczSyncPacket(buf.readMap(FriendlyByteBuf::readResourceLocation, friendlyByteBuf -> new HashSet<>(friendlyByteBuf.readList(FriendlyByteBuf::readResourceLocation))));
    }

    public static void encode(TaczSyncPacket msg, FriendlyByteBuf buf) {

        buf.writeMap(msg.itemAdd,
                FriendlyByteBuf::writeResourceLocation,
                (friendlyByteBuf, set) -> friendlyByteBuf.writeCollection(set, FriendlyByteBuf::writeResourceLocation));
    }

    public static void handler(TaczSyncPacket msg, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (msg != null) {
                if (!context.get().getDirection().getReceptionSide().isServer()) {
                    if (ModList.get().isLoaded("tacz")) {
                        TaczTagHelper.resetTag();
                        msg.itemAdd.forEach((key, value) -> {
                            TaczTagHelper.addAllTags(TagKey.create(Registries.ITEM, key), value);
                        });

                    }
                }
            }
        });
        context.get().setPacketHandled(true);
    }
}
