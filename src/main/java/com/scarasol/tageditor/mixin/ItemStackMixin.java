package com.scarasol.tageditor.mixin;

import com.scarasol.tageditor.compat.tacz.TaczTagHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Scarasol
 */
@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Inject(method = "is(Lnet/minecraft/tags/TagKey;)Z", cancellable = true, at = @At("HEAD"))
    private void tagEditor$is(TagKey<Item> tagKey, CallbackInfoReturnable<Boolean> cir) {
        if (ModList.get().isLoaded("tacz")) {
            if (TaczTagHelper.taczIs(tagKey, (ItemStack) (Object)this)) {
                cir.setReturnValue(true);
            }
        }
    }
}
