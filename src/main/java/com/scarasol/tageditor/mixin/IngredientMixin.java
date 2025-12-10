package com.scarasol.tageditor.mixin;

import com.scarasol.tageditor.compat.tacz.TaczTagHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Scarasol
 */
@Mixin(Ingredient.class)
public abstract class IngredientMixin {

    @Shadow public abstract ItemStack[] getItems();

    @Inject(method = "test(Lnet/minecraft/world/item/ItemStack;)Z", cancellable = true, at = @At("RETURN"))
    private void tagEditor$test(ItemStack itemStackTest, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue() && ModList.get().isLoaded("tacz")) {
            ResourceLocation resourceLocation = TaczTagHelper.getTaczId(itemStackTest);
            if (resourceLocation != null) {
                for(ItemStack itemstack : this.getItems()) {
                    if (resourceLocation.equals(TaczTagHelper.getTaczId(itemstack))) {
                        return;
                    }
                }
                cir.setReturnValue(false);
            }

        }
    }
}
