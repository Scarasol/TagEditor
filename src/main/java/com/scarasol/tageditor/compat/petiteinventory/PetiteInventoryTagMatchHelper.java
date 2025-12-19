package com.scarasol.tageditor.compat.petiteinventory;

import com.scarasol.tageditor.compat.tacz.TaczTagHelper;
import com.sighs.petiteinventory.init.Area;
import com.sighs.petiteinventory.init.AreaEvent;
import com.sighs.petiteinventory.loader.EntryCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Set;

/**
 * @author Scarasol
 */
public class PetiteInventoryTagMatchHelper {
    @Nullable
    public static String taczMatchItem(ItemStack itemStack) {
        ResourceLocation taczItemId = TaczTagHelper.getTaczId(itemStack);
        if (taczItemId == null) {
            return null;
        }
        Set<ResourceLocation> tags = TaczTagHelper.getAllItemTags(taczItemId);
        if (tags.isEmpty()) {
            return EntryCache.UnitMapCache.getOrDefault(taczItemId.toString(), null);
        }
        for (ResourceLocation tag : tags) {
            String item = EntryCache.matchTag(tag);
            if (item != null) {
                return item;
            }
        }
        return EntryCache.UnitMapCache.getOrDefault(taczItemId.toString(), null);
    }

    public static Area getTaczItemArea(ItemStack itemStack, int width, int height) {
        String sizeString = taczMatchItem(itemStack);
        if (sizeString != null) {
            String[] size = sizeString.replace(" ", "").split("\\*");
            width = Integer.parseInt(size[0]);
            height = Integer.parseInt(size[1]);
        }
        return new Area(width, height, itemStack);
    }

    public static void replaceArea(AreaEvent event) {
        if ("tacz".equals(ForgeRegistries.ITEMS.getKey(event.itemStack.getItem()).getNamespace())) {
            Area area = PetiteInventoryTagMatchHelper.getTaczItemArea(event.itemStack, event.width, event.height);
            event.width = area.width();
            event.height = area.height();
        }
    }

    public static void init() {
        MinecraftForge.EVENT_BUS.addListener(PetiteInventoryTagMatchHelper::replaceArea);
    }
}
