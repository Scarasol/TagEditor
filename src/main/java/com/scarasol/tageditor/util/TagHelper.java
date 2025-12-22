package com.scarasol.tageditor.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;
import com.scarasol.tageditor.TagEditorMod;
import com.scarasol.tageditor.compat.tacz.TaczTagHelper;
import com.scarasol.tageditor.network.NetworkHandler;
import com.scarasol.tageditor.network.TagSyncPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.tags.TagKey;

import net.minecraft.world.item.AirItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;


import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Scarasol
 */
public class TagHelper {

    public static final Set<ResourceLocation> BUFFER_ITEMS_ADD = Sets.newHashSet();
    public static final Set<ResourceLocation> BUFFER_ITEMS_REMOVE = Sets.newHashSet();

    @Nullable
    public static TagKey<Item> BUFFER_TAG = null;


    public static void changeItem(ItemStack itemStack) {
        if (BUFFER_TAG == null) {
            return;
        }
        ResourceLocation resourceLocation = null;
        if (ModList.get().isLoaded("tacz")) {
            resourceLocation = TaczTagHelper.getTaczId(itemStack);
        }
        if (resourceLocation == null) {
            resourceLocation = ForgeRegistries.ITEMS.getKey(itemStack.getItem());
        }
        if (itemStack.is(BUFFER_TAG)) {
            changeItem(resourceLocation, BUFFER_ITEMS_REMOVE);
        } else {
            changeItem(resourceLocation, BUFFER_ITEMS_ADD);
        }
    }

    public static void changeItem(ResourceLocation resourceLocation, Set<ResourceLocation> set) {
        if (set.contains(resourceLocation)) {
            set.remove(resourceLocation);
        } else {
            set.add(resourceLocation);
        }
    }

    public static void resetBuffer() {
        resetItemList();
        BUFFER_TAG = null;
    }

    public static void resetItemList() {
        BUFFER_ITEMS_ADD.clear();
        BUFFER_ITEMS_REMOVE.clear();
    }

    public static void saveTag() {
        if (BUFFER_TAG != null) {
            refreshTag(BUFFER_TAG, BUFFER_ITEMS_ADD, BUFFER_ITEMS_REMOVE);
            NetworkHandler.PACKET_HANDLER.sendToServer(new TagSyncPacket(BUFFER_TAG.location(),
                    BUFFER_ITEMS_ADD.stream().toList(),
                    BUFFER_ITEMS_REMOVE.stream().toList()));
        }
        resetItemList();
    }

    public static void saveTag(Registry<Item> registry, ResourceLocation tagResourceLocation, List<ResourceLocation> itemAddResourceLocation, List<ResourceLocation> itemRemoveResourceLocation) {
        TagKey<Item> tag = TagKey.create(Registries.ITEM, tagResourceLocation);
        Set<ResourceLocation> addItem = itemAddResourceLocation.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<ResourceLocation> removeItem = itemRemoveResourceLocation.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        saveTag(registry, tag, addItem, removeItem);
    }

    public static void saveTag(Registry<Item> registry, TagKey<Item> tagKey, Set<ResourceLocation> addItem, Set<ResourceLocation> removeItem) {
        Set<TagTuple<String, Boolean>> fileTags = JsonHandler.readJsonList(tagKey.location());
        Set<TagTuple<String, Boolean>> needSave = refreshTag(tagKey, addItem, removeItem);
        fileTags.forEach(fileTag -> {
            if (ModList.get().isLoaded("tacz")) {
                if (TaczTagHelper.getItem(new ResourceLocation(fileTag.getA())) != null) {
                    return;
                }
            }
            if (!needSave.removeIf(needSaveTag -> needSaveTag.equals(fileTag) && !needSaveTag.getB().equals(fileTag.getB()))) {
                needSave.add(fileTag);
            }
        });


        syncTag(registry, tagKey, needSave);
        if (ModList.get().isLoaded("tacz")) {
            needSave.addAll(TaczTagHelper.saveItems(tagKey));
        }
        JsonHandler.writeListToJson(needSave, tagKey.location());
        TagEditorMod.LOGGER.debug("Save All Custom Tags!");
    }

    public static Set<TagTuple<String, Boolean>> refreshTag(TagKey<Item> tagKey, Set<ResourceLocation> addItem, Set<ResourceLocation> removeItem) {
        Set<TagTuple<String, Boolean>> needSave = Sets.newHashSet();
        addItem.stream()
                .filter(Objects::nonNull)
                .forEach(resourceLocation -> {
                    Item item = ForgeRegistries.ITEMS.getValue(resourceLocation);
                    if (item == null || item instanceof AirItem) {
                        if (ModList.get().isLoaded("tacz")) {
                            TaczTagHelper.addTag(tagKey, resourceLocation);
                        }
                    } else {
                        addTag(item, tagKey);
                        needSave.add(new TagTuple<>(resourceLocation.toString(), true));
                    }

                });
        removeItem.stream()
                .filter(Objects::nonNull)
                .forEach(resourceLocation -> {
                    Item item = ForgeRegistries.ITEMS.getValue(resourceLocation);
                    if (item == null || item instanceof AirItem) {
                        if (ModList.get().isLoaded("tacz")) {
                            TaczTagHelper.removeTag(tagKey, resourceLocation);
                        }
                    } else {
                        removeTag(item, tagKey);
                        needSave.add(new TagTuple<>(resourceLocation.toString(), false));
                    }
                });
        return needSave;
    }

    public static void refreshTag(ResourceLocation tagResourceLocation, List<ResourceLocation> itemAddResourceLocation, List<ResourceLocation> itemRemoveResourceLocation) {
        TagKey<Item> tag = TagKey.create(Registries.ITEM, tagResourceLocation);
        Set<ResourceLocation> addItem = itemAddResourceLocation.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<ResourceLocation> removeItem = itemRemoveResourceLocation.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        refreshTag(tag, addItem, removeItem);
    }

    public static void addAllTags(Item item, Set<TagKey<Item>> tag) {
        Holder.Reference<Item> itemHolder = item.builtInRegistryHolder();
        Set<TagKey<Item>> tags = itemHolder.tags().collect(Collectors.toSet());
        tags.addAll(tag);
        itemHolder.bindTags(tags);
    }

    public static void addTag(Item item, TagKey<Item> tag) {
        Holder.Reference<Item> itemHolder = item.builtInRegistryHolder();
        Set<TagKey<Item>> tags = itemHolder.tags().collect(Collectors.toSet());
        tags.add(tag);
        itemHolder.bindTags(tags);
    }

    public static void removeAllTags(Item item, Set<TagKey<Item>> tag) {
        Holder.Reference<Item> itemHolder = item.builtInRegistryHolder();
        Set<TagKey<Item>> tags = itemHolder.tags().collect(Collectors.toSet());
        tags.removeAll(tag);
        itemHolder.bindTags(tags);
    }

    public static void removeTag(Item item, TagKey<Item> tag) {
        Holder.Reference<Item> itemHolder = item.builtInRegistryHolder();
        Set<TagKey<Item>> tags = itemHolder.tags().collect(Collectors.toSet());
        tags.remove(tag);
        itemHolder.bindTags(tags);
    }

    public static boolean shouldRender(ItemStack itemStack) {

        ResourceLocation resourceLocation = null;
        if (ModList.get().isLoaded("tacz")) {
            resourceLocation = TaczTagHelper.getTaczId(itemStack);
        }
        if (resourceLocation == null) {
            resourceLocation = ForgeRegistries.ITEMS.getKey(itemStack.getItem());
        }
        if (BUFFER_TAG == null) {
            return false;
        } else {
            return itemStack.is(BUFFER_TAG) || BUFFER_ITEMS_ADD.contains(resourceLocation) || BUFFER_ITEMS_REMOVE.contains(resourceLocation);
        }
    }

    public static int getRenderColor(ItemStack itemStack) {
        ResourceLocation resourceLocation = null;
        if (ModList.get().isLoaded("tacz")) {
            resourceLocation = TaczTagHelper.getTaczId(itemStack);
        }
        if (resourceLocation == null) {
            resourceLocation = ForgeRegistries.ITEMS.getKey(itemStack.getItem());
        }
        if (BUFFER_TAG != null) {
            if (BUFFER_ITEMS_REMOVE.contains(resourceLocation)) {
                return 0xC62828;
            } else if (itemStack.is(BUFFER_TAG)) {
                return 0x1E88E5;
            } else {
                return 0x00C853;
            }
        }
        return -1;
    }

    public static void renderItemBackGround(GuiGraphics guiGraphics, ItemStack stack, int x, int y) {
        if (stack.isEmpty() || !shouldRender(stack)) {
            return;
        }

        int color = getRenderColor(stack);
        if (color < 0) {
            return;
        }
        int alpha = 200;
        int argb = (alpha << 24) | (color & 0xFFFFFF);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        guiGraphics.fill(x, y, x + 16, y + 16, argb);
        RenderSystem.disableBlend();
    }

    public static void syncTags(RegistryAccess registryAccess, Map<String, Set<TagTuple<String, Boolean>>> map) {
        Registry<Item> registry = registryAccess.registryOrThrow(Registries.ITEM);
        map.forEach((key, value) -> syncTag(registry, TagKey.create(Registries.ITEM, new ResourceLocation(key)), value));
    }

    public static void syncTag(Registry<Item> registry, TagKey<Item> tagKey, Set<TagTuple<String, Boolean>> customTags) {
        Optional<HolderSet.Named<Item>> maybe = registry.getTag(tagKey);
        List<Holder<Item>> holders = maybe
                .map(named -> StreamSupport.stream(named.spliterator(), false).collect(Collectors.toList()))
                .orElseGet(ArrayList::new);
        customTags.forEach(tuple -> {
            ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, new ResourceLocation(tuple.getA()));
            Optional<Holder.Reference<Item>> itemHolder = registry.getHolder(itemKey);
            if (itemHolder.isPresent()) {
                Holder.Reference<Item> holder = itemHolder.get();
                if (tuple.getB()) {
                    if (!holders.contains(holder)) {
                        holders.add(holder);
                    }
                } else {
                    holders.remove(holder);
                }
            }
        });
        if (!holders.isEmpty()) {
            registry.getOrCreateTag(tagKey).bind(holders);
        } else {
            maybe.ifPresent(itemNamed -> itemNamed.bind(Lists.newArrayList()));
        }
    }

    public static void parseTags(Map<String, Set<TagTuple<String, Boolean>>> map) {

        Map<Item, Set<TagKey<Item>>> itemAddMap = Maps.newHashMap();
        Map<Item, Set<TagKey<Item>>> itemRemoveMap = Maps.newHashMap();
        TagKey<Item> currentTag;
        for (Map.Entry<String, Set<TagTuple<String, Boolean>>> entry : map.entrySet()) {
            currentTag = TagKey.create(Registries.ITEM, new ResourceLocation(entry.getKey()));

            for (TagTuple<String, Boolean> tuple : entry.getValue()) {
                ResourceLocation resourceLocation = new ResourceLocation(tuple.getA());
                Item item = ForgeRegistries.ITEMS.getValue(resourceLocation);
                if (item != null && !(item instanceof AirItem)) {
                    if (tuple.getB()) {
                        if (itemAddMap.containsKey(item)) {
                            itemAddMap.get(item).add(currentTag);
                        } else {
                            itemAddMap.put(item, Sets.newHashSet(currentTag));
                        }
                    } else {
                        if (itemRemoveMap.containsKey(item)) {
                            itemRemoveMap.get(item).add(currentTag);
                        } else {
                            itemRemoveMap.put(item, Sets.newHashSet(currentTag));
                        }
                    }
                } else {
                    if (ModList.get().isLoaded("tacz")) {
                        TaczTagHelper.addTag(currentTag, resourceLocation);
                    }
                }
            }
        }
        for (Map.Entry<Item, Set<TagKey<Item>>> entry : itemAddMap.entrySet()) {
            addAllTags(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<Item, Set<TagKey<Item>>> entry : itemRemoveMap.entrySet()) {
            removeAllTags(entry.getKey(), entry.getValue());
        }
    }


}
