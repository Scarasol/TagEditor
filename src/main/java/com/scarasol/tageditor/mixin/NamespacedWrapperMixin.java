package com.scarasol.tageditor.mixin;

import com.mojang.serialization.Lifecycle;
import com.scarasol.tageditor.TagEditorMod;
import com.scarasol.tageditor.api.IReference;
import com.scarasol.tageditor.compat.tacz.TaczTagHelper;
import com.scarasol.tageditor.configuration.CommonConfig;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ILockableRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;


@Mixin(targets = "net.minecraftforge.registries.NamespacedWrapper")
public abstract class NamespacedWrapperMixin<T> extends MappedRegistry<T> implements ILockableRegistry {

    public NamespacedWrapperMixin(ResourceKey<? extends Registry<T>> p_249899_, Lifecycle p_252249_) {
        super(p_249899_, p_252249_);
    }

    @Shadow
    protected abstract HolderSet.Named<T> createTag(TagKey<T> name);

    @Shadow public abstract Registry<T> freeze();

    @Inject(method = "getTag", cancellable = true, at = @At("RETURN"))
    private void tagEditor$getTag(TagKey<T> tagKey, CallbackInfoReturnable<Optional<HolderSet.Named<T>>> cir) {

        if (CommonConfig.FORCE_COMPAT.get() && ModList.get().isLoaded("tacz") && tagKey.registry().equals(Registries.ITEM)) {

            List<Holder<Item>> extraHolders = TaczTagHelper.getHolder((HolderOwner<Item>) holderOwner(), (TagKey<Item>) tagKey);
            if (!extraHolders.isEmpty()) {
                Optional<HolderSet.Named<T>> original = cir.getReturnValue();
                Set<Holder<T>> combinedSet = new LinkedHashSet<>();
                original.ifPresent(named -> combinedSet.addAll(named.stream().toList()));
                extraHolders.stream()
                        .filter(holder -> combinedSet.stream().noneMatch(holder2 -> {
                            if (holder instanceof IReference iReference) {
                                ResourceLocation resourceLocation = iReference.tagEditor$getTaczId();
                                if (resourceLocation != null && holder2 instanceof IReference iReference2) {
                                    return resourceLocation.equals(iReference2.tagEditor$getTaczId());
                                }
                            }
                            return false;

                        }))
                        .forEach(holder -> combinedSet.add((Holder<T>) holder));

                HolderSet.Named<T> combinedNamed = createTag(tagKey);
                combinedNamed.bind(combinedSet.stream().toList());

                cir.setReturnValue(Optional.of(combinedNamed));
            }
        }
    }
}
