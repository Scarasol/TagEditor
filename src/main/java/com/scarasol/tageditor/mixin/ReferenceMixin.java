package com.scarasol.tageditor.mixin;

import com.scarasol.tageditor.api.IReference;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * @author Scarasol
 */
@Mixin(Holder.Reference.class)
public abstract class ReferenceMixin implements IReference {
    @Unique
    private ResourceLocation tagEditor$taczID;

    @Override
    @Unique
    public ResourceLocation tagEditor$getTaczId() {
        return tagEditor$taczID;
    }

    @Override
    @Unique
    public void tagEditor$setTaczId(ResourceLocation resourceLocation) {
        tagEditor$taczID = resourceLocation;
    }
}
