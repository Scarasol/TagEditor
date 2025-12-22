package com.scarasol.tageditor.mixin;

import com.scarasol.tageditor.TagEditorMod;
import com.scarasol.tageditor.compat.tacz.TaczTagHelper;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.AbstractIngredient;
import net.minecraftforge.common.crafting.DifferenceIngredient;
import net.minecraftforge.common.crafting.PartialNBTIngredient;
import net.minecraftforge.common.util.RecipeMatcher;
import net.minecraftforge.fml.ModList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Scarasol
 */
@Mixin(ShapelessRecipe.class)
public abstract class ShapelessRecipeMixin implements CraftingRecipe {


    @Shadow @Final private NonNullList<Ingredient> ingredients;

    @Inject(method = "matches(Lnet/minecraft/world/Container;Lnet/minecraft/world/level/Level;)Z", cancellable = true, at = @At("HEAD"))
    private void tagEditor$test(Container container, Level level, CallbackInfoReturnable<Boolean> cir) {
        boolean hasTacz = false;
        int nonEmptyCount = 0;

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) {
                nonEmptyCount++;
                if (ModList.get().isLoaded("tacz")
                        && TaczTagHelper.getTaczId(stack) != null) {
                    hasTacz = true;
                    break;
                }
            }
        }
        if (!hasTacz) {
            return;
        }

        if (nonEmptyCount != this.ingredients.size()) {
            cir.setReturnValue(false);
            return;
        }

        List<ItemStack> inputs = new ArrayList<>();
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) {
                inputs.add(stack);
            }
        }

        cir.setReturnValue(RecipeMatcher.findMatches(inputs, this.ingredients) != null);
    }
}
