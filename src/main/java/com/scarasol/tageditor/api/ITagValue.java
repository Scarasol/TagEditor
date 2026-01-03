package com.scarasol.tageditor.api;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/**
 * @author Scarasol
 */
public interface ITagValue {

    TagKey<Item> tagEditor$getTag();
}
