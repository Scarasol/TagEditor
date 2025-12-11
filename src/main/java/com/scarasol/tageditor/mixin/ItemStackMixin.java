package com.scarasol.tageditor.mixin;

import com.scarasol.tageditor.TagEditorMod;
import com.scarasol.tageditor.compat.tacz.TaczTagHelper;
import com.scarasol.tageditor.configuration.CommonConfig;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Scarasol
 */
@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Inject(method = "<init>(Lnet/minecraft/core/Holder;)V", at = @At("TAIL"))
    private void tagEditorItemStack(Holder<Item> holder, CallbackInfo ci) {
        if (CommonConfig.FORCE_COMPAT.get() && ModList.get().isLoaded("tacz")) {
            TaczTagHelper.initItemStack((ItemStack) (Object)this, holder);
        }
    }

    @Inject(method = "<init>(Lnet/minecraft/core/Holder;I)V", at = @At("TAIL"))
    private void tagEditorItemStack(Holder<Item> holder, int count, CallbackInfo ci) {
        if (CommonConfig.FORCE_COMPAT.get() && ModList.get().isLoaded("tacz")) {
            TaczTagHelper.initItemStack((ItemStack) (Object)this, holder);
        }
    }

    @Inject(method = "is(Lnet/minecraft/tags/TagKey;)Z", cancellable = true, at = @At("HEAD"))
    private void tagEditor$is(TagKey<Item> tagKey, CallbackInfoReturnable<Boolean> cir) {
        if (ModList.get().isLoaded("tacz")) {
            if (TaczTagHelper.taczIs(tagKey, (ItemStack) (Object)this)) {
                cir.setReturnValue(true);
            }
        }
    }
}
