package com.snoopycorp.amcore.mixin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.simibubi.create.content.kinetics.saw.SawBlockEntity;
import com.simibubi.create.content.processing.recipe.ProcessingInventory;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.recipe.RecipeConditions;
import com.snoopycorp.amcore.machinetier.MachineTier;
import com.snoopycorp.amcore.machinetier.TierProcess;
import com.snoopycorp.amcore.machinetier.TieredRecipeLookup;
import com.snoopycorp.amcore.machinetier.recipe.AMRecipeTypes;
import com.snoopycorp.amcore.machinetier.recipe.TieredCuttingRecipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

/**
 * Saw: tiered cutting steps and recipes, and the speed bonus.
 */
@Mixin(SawBlockEntity.class)
public abstract class SawBlockEntityMixin {
    @Shadow
    public ProcessingInventory inventory;

    @Shadow
    private FilteringBehaviour filtering;

    /**
     * The saw cycles through the recipes of this list. When a tiered step or recipe fits, the list only holds those of
     * the highest fitting tier, so the highest tier wins and the saw still cycles between recipes of that tier.
     */
    @Inject(method = "getRecipes", at = @At("RETURN"), cancellable = true)
    private void amcore$findTieredCutting(CallbackInfoReturnable<List<RecipeHolder<? extends Recipe<?>>>> cir) {
        SawBlockEntity self = (SawBlockEntity) (Object) this;
        Level level = self.getLevel();
        if (level == null) {
            return;
        }
        MachineTier tier = MachineTier.of(self.getBlockState());
        if (tier.isBase()) {
            return;
        }
        ItemStack stack = inventory.getStackInSlot(0);
        Predicate<RecipeHolder<? extends Recipe<?>>> outputFilter = RecipeConditions.outputMatchesFilter(filtering);
        Predicate<RecipeHolder<? extends Recipe<?>>> ingredient = RecipeConditions.firstIngredientMatches(stack);

        for (MachineTier allowed : tier.allowedDescending()) {
            AMRecipeTypes type = AMRecipeTypes.of(TierProcess.CUTTING, allowed);
            Optional<RecipeHolder<TieredCuttingRecipe>> step = TieredRecipeLookup.sequencedStep(level, stack, type,
                    TieredCuttingRecipe.class, holder -> true);
            if (step.isPresent()) {
                if (filtering.test(step.get().value().getResultItem(level.registryAccess()))) {
                    cir.setReturnValue(List.of(step.get()));
                    return;
                }
                continue;
            }
            List<RecipeHolder<? extends Recipe<?>>> recipes = new ArrayList<>();
            for (RecipeHolder<TieredCuttingRecipe> holder : TieredRecipeLookup.recipes(level, type, TieredCuttingRecipe.class)) {
                if (outputFilter.test(holder) && ingredient.test(holder)) {
                    recipes.add(holder);
                }
            }
            if (!recipes.isEmpty()) {
                cir.setReturnValue(recipes);
                return;
            }
        }
    }

    /** Processing speed per tick, after Create's own clamp. Runs on both sides. */
    @ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(FFF)F"))
    private float amcore$tierSpeed(float speed) {
        return (float) (speed * MachineTier.of(((SawBlockEntity) (Object) this).getBlockState()).speedMultiplier());
    }
}
