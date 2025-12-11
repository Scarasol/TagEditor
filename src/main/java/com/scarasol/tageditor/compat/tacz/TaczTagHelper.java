package com.scarasol.tageditor.compat.tacz;

import com.google.common.collect.Maps;
import com.scarasol.tageditor.TagEditorMod;
import com.scarasol.tageditor.api.IReference;
import com.scarasol.tageditor.compat.tacz.network.TaczSyncPacket;
import com.scarasol.tageditor.network.NetworkHandler;
import com.scarasol.tageditor.util.TagTuple;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.api.item.IAttachment;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.builder.AmmoItemBuilder;
import com.tacz.guns.api.item.builder.AttachmentItemBuilder;
import com.tacz.guns.api.item.builder.GunItemBuilder;
import com.tacz.guns.init.ModItems;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Scarasol
 */
public class TaczTagHelper {

    public static final Map<ResourceLocation, Set<ResourceLocation>> TACZ_TAG = Maps.newHashMap();

    @Nullable
    public static ResourceLocation getTaczId(ItemStack itemStack) {
        ResourceLocation resourceLocation = null;
        IGun gun = IGun.getIGunOrNull(itemStack);
        if (gun == null) {
            IAmmo ammo = IAmmo.getIAmmoOrNull(itemStack);
            if (ammo == null) {
                IAttachment attachment = IAttachment.getIAttachmentOrNull(itemStack);
                if (attachment != null) {
                    resourceLocation = attachment.getAttachmentId(itemStack);
                }
            }else {
                resourceLocation = ammo.getAmmoId(itemStack);
            }
        } else {
            resourceLocation = gun.getGunId(itemStack);
        }
        return resourceLocation;
    }

    public static boolean taczIs(TagKey<Item> tag, ItemStack itemStack) {
        Set<ResourceLocation> itemSet = TACZ_TAG.get(tag.location());
        if (itemSet == null) {
            return false;
        }
        return itemSet.contains(getTaczId(itemStack));
    }


    public static void addTag(TagKey<Item> tagKey, ResourceLocation resourceLocation) {
        Set<ResourceLocation> itemSet = TACZ_TAG.get(tagKey.location());
        if (itemSet == null) {
            itemSet = new HashSet<>();
        }
        itemSet.add(resourceLocation);
        TACZ_TAG.put(tagKey.location(), itemSet);
    }

    public static void addAllTags(TagKey<Item> tagKey, Set<ResourceLocation> resourceLocation) {
        Set<ResourceLocation> itemSet = TACZ_TAG.get(tagKey.location());
        if (itemSet == null) {
            itemSet = new HashSet<>();
        }
        itemSet.addAll(resourceLocation);
        TACZ_TAG.put(tagKey.location(), itemSet);
    }

    public static void removeTag(TagKey<Item> tagKey, ResourceLocation resourceLocation) {
        Set<ResourceLocation> itemSet = TACZ_TAG.get(tagKey.location());
        if (itemSet == null) {
            return;
        }
        itemSet.remove(resourceLocation);
        if (itemSet.isEmpty()) {
           TACZ_TAG.remove(tagKey.location());
        }else {
            TACZ_TAG.put(tagKey.location(), itemSet);
        }
    }

    public static void removeAllTags(TagKey<Item> tagKey, Set<ResourceLocation> resourceLocation) {
        Set<ResourceLocation> itemSet = TACZ_TAG.get(tagKey.location());
        if (itemSet == null) {
            return;
        }
        itemSet.removeAll(resourceLocation);
        if (itemSet.isEmpty()) {
            TACZ_TAG.remove(tagKey.location());
        }else {
            TACZ_TAG.put(tagKey.location(), itemSet);
        }
    }

    public static void initItemStack(ItemStack itemStack, Holder<Item> holder) {
        if (holder instanceof IReference iReference) {

            initItemStack(itemStack, iReference.tagEditor$getTaczId());
        }
    }

    public static void initItemStack(ItemStack itemStack, ResourceLocation resourceLocation) {
        if (resourceLocation == null) {
            return;
        }
        Item item = itemStack.getItem();
        if (item instanceof IGun iGun) {
            iGun.setGunId(itemStack, resourceLocation);
        } else if (item instanceof IAmmo iAmmo) {
            iAmmo.setAmmoId(itemStack, resourceLocation);
        } else if (item instanceof IAttachment iAttachment) {
            iAttachment.setAttachmentId(itemStack, resourceLocation);
        }
    }

    public static List<Holder<Item>> getHolder(HolderOwner<Item> itemRegistry, TagKey<Item> tagKey) {
        List<Holder<Item>> holders = new ArrayList<>();
        Set<ResourceLocation> resourceLocations = TACZ_TAG.get(tagKey.location());
        if (resourceLocations != null) {
            resourceLocations.forEach(resourceLocation -> {
                Holder<Item> holder = createItemHolder(itemRegistry, resourceLocation);
                if (holder != null) {
                    holders.add(holder);
                }
            });
        }
        return holders;
    }

    @Nullable
    public static Holder<Item> createItemHolder(HolderOwner<Item> itemRegistry, ResourceLocation resourceLocation) {
        Item item = getItem(resourceLocation);
        if (item == null) {
            return null;
        }
        ResourceLocation keyName = ForgeRegistries.ITEMS.getKey(item);
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, keyName);

        Holder.Reference<Item> holder = Holder.Reference.createStandAlone(itemRegistry, itemKey);

        holder.bindValue(item);
        if (holder instanceof IReference reference) {
            reference.tagEditor$setTaczId(resourceLocation);
        }
        return holder;
    }

    @Nullable
    public static Item getItem(ResourceLocation resourceLocation) {
        if (TimelessAPI.getCommonGunIndex(resourceLocation).isPresent()) {
            return ModItems.MODERN_KINETIC_GUN.get();
        } else if (TimelessAPI.getCommonAmmoIndex(resourceLocation).isPresent()) {
            return ModItems.AMMO.get();
        } else if (TimelessAPI.getCommonAttachmentIndex(resourceLocation).isPresent()) {
            return ModItems.ATTACHMENT.get();
        }
        return null;
    }



    public static List<ItemStack> getItemStacks(TagKey<Item> tagKey) {
        List<ItemStack> itemStacks = new ArrayList<>();
        Set<ResourceLocation> resourceLocations = TACZ_TAG.get(tagKey.location());
        if (resourceLocations != null) {
            resourceLocations.forEach(resourceLocation -> {
                if (TimelessAPI.getCommonGunIndex(resourceLocation).isPresent()) {
                    itemStacks.add(GunItemBuilder.create().setId(resourceLocation).build());
                } else if (TimelessAPI.getCommonAmmoIndex(resourceLocation).isPresent()) {
                    itemStacks.add(AmmoItemBuilder.create().setId(resourceLocation).build());
                } else if (TimelessAPI.getCommonAttachmentIndex(resourceLocation).isPresent()) {
                    itemStacks.add(AttachmentItemBuilder.create().setId(resourceLocation).build());
                }
            });
        }
        return itemStacks;
    }

    public static Set<TagTuple<String, Boolean>> saveItems(TagKey<Item> tagKey) {
        Set<ResourceLocation> resourceLocations = TACZ_TAG.get(tagKey.location());
        if (resourceLocations != null) {
            return resourceLocations.stream().map(resourceLocation -> new TagTuple<>(resourceLocation.toString(), true))
            .collect(Collectors.toSet());
        }
        return new HashSet<>();
    }

    public static void syncTagToPlayer(ServerPlayer player) {
        NetworkHandler.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> player), new TaczSyncPacket(TACZ_TAG));
    }

    public static void resetTag() {
        TACZ_TAG.clear();
    }
}
