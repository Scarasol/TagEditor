package com.scarasol.tageditor.mixin;

import com.scarasol.tageditor.api.ITagValue;
import com.scarasol.tageditor.compat.tacz.TaczTagHelper;
import com.scarasol.tageditor.configuration.CommonConfig;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fml.ModList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Collection;
import java.util.List;

/**
 * @author Scarasol
 */
@Mixin(Ingredient.TagValue.class)
public abstract class TagValueMixin implements ITagValue {

    @Shadow @Final private TagKey<Item> tag;

    @Inject(method = "getItems", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void tagEditors(CallbackInfoReturnable<Collection<ItemStack>> cir, List<ItemStack> list) {
        if (!CommonConfig.FORCE_COMPAT.get() && ModList.get().isLoaded("tacz")) {
            list.addAll(TaczTagHelper.getItemStacks(this.tag));
        }
    }

    @Override
    @Unique
    public TagKey<Item> tagEditor$getTag() {
        return this.tag;
    }
}
