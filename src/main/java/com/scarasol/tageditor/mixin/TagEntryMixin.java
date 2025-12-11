package com.scarasol.tageditor.mixin;

import com.scarasol.tageditor.compat.tacz.TaczTagHelper;
import com.scarasol.tageditor.configuration.CommonConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.TagEntry;
import net.minecraftforge.fml.ModList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

/**
 * @author Scarasol
 */
@Mixin(TagEntry.class)
public abstract class TagEntryMixin {

    @Shadow @Final private TagKey<Item> tag;

    @Inject(method = "createItemStack", at = @At("TAIL"))
    private void tagEditor$createItemStack(Consumer<ItemStack> consumer, LootContext lootContext, CallbackInfo ci) {
        if (!CommonConfig.FORCE_COMPAT.get() && ModList.get().isLoaded("tacz")) {
            TaczTagHelper.getItemStacks(this.tag).forEach(consumer::accept);
        }
    }

}
