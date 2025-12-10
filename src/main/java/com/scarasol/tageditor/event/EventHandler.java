package com.scarasol.tageditor.event;

import com.scarasol.tageditor.TagEditorMod;
import com.scarasol.tageditor.compat.tacz.TaczTagHelper;
import com.scarasol.tageditor.network.NetworkHandler;
import com.scarasol.tageditor.util.JsonHandler;
import com.scarasol.tageditor.util.TagHelper;
import com.scarasol.tageditor.util.TagTuple;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.Map;
import java.util.Set;

/**
 * @author Scarasol
 */
@Mod.EventBusSubscriber
public class EventHandler {


    @SubscribeEvent
    public static void addItem(TagsUpdatedEvent event) {
        if (event.getUpdateCause() == TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD) {
            if (ModList.get().isLoaded("tacz")) {
                TaczTagHelper.resetTag();
            }
            Map<String, Set<TagTuple<String, Boolean>>> map = JsonHandler.readAllJsonValues();
            TagHelper.parseTags(map);
            TagHelper.syncTags(event.getRegistryAccess(), map);
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server != null && ModList.get().isLoaded("tacz")) {
                server.getPlayerList().getPlayers().forEach(TaczTagHelper::syncTagToPlayer);
            }
            TagEditorMod.LOGGER.debug("Add All Custom Tags!");
        }

    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        Screen screen = event.getScreen();
        if (!(screen instanceof CreativeModeInventoryScreen containerScreen)) {
            return;
        }

        GuiGraphics guiGraphics = event.getGuiGraphics();
        int guiLeft = containerScreen.getGuiLeft();
        int guiTop  = containerScreen.getGuiTop();

        for (Slot slot : containerScreen.getMenu().slots) {
            TagHelper.renderItemBackGround(guiGraphics, slot.getItem(),
                    slot.x + guiLeft,
                    slot.y + guiTop);
        }
    }

    @SubscribeEvent
    public static void Login(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            if (ModList.get().isLoaded("tacz")) {
                TaczTagHelper.syncTagToPlayer(serverPlayer);
            }
        }
    }

}
