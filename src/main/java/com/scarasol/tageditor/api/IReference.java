package com.scarasol.tageditor.api;

import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

/**
 * @author Scarasol
 */
public interface IReference {

    @Nullable
    ResourceLocation tagEditor$getTaczId();

    void tagEditor$setTaczId(ResourceLocation resourceLocation);
}
