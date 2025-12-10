package com.scarasol.tageditor.network;

import com.scarasol.tageditor.compat.tacz.network.TaczSyncPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import static com.scarasol.tageditor.TagEditorMod.MODID;

/**
 * @author Scarasol
 */
public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel PACKET_HANDLER = NetworkRegistry.newSimpleChannel(new ResourceLocation(MODID, MODID), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
    private static int messageID = 0;

    public static <T> void addNetworkMessage() {
        PACKET_HANDLER.registerMessage(messageID++, TagSyncPacket.class, TagSyncPacket::encode, TagSyncPacket::decode, TagSyncPacket::handler);
        PACKET_HANDLER.registerMessage(messageID++, TaczSyncPacket.class, TaczSyncPacket::encode, TaczSyncPacket::decode, TaczSyncPacket::handler);

    }
}
