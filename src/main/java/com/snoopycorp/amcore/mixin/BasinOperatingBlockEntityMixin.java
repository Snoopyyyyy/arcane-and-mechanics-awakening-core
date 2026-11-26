package com.snoopycorp.amcore.mixin;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.content.kinetics.mixer.MechanicalMixerBlockEntity;
import com.simibubi.create.content.kinetics.press.MechanicalPressBlockEntity;
import com.simibubi.create.content.processing.basin.BasinOperatingBlockEntity;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.snoopycorp.amcore.machinetier.MachineTier;
import com.snoopycorp.amcore.machinetier.TierProcess;
import com.snoopycorp.amcore.machinetier.TieredRecipeLookup;
import com.snoopycorp.amcore.machinetier.recipe.AMRecipeTypes;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.Level;

/**
 * Basin processing (compacting under a press, mixing under a mixer). Create sorts the matching recipes, then takes the
 * first one; tiered recipes are put in front, highest tier first. The mixer's override calls this method first, so
 * one injection covers both machines.
 * <p>
 * {@code matchStaticFilters} is left alone: its result is cached under a key shared by every press or mixer.
 */
@Mixin(BasinOperatingBlockEntity.class)
public abstract class BasinOperatingBlockEntityMixin {
    @Shadow
    protected abstract <I extends RecipeInput> boolean matchBasinRecipe(Recipe<I> recipe);

    @Inject(method = "getMatchingRecipes", at = @At("RETURN"), cancellable = true)
    private void amcore$addTieredBasinRecipes(CallbackInfoReturnable<List<Recipe<?>>> cir) {
        BasinOperatingBlockEntity self = (BasinOperatingBlockEntity) (Object) this;
        TierProcess process;
        if (self instanceof MechanicalPressBlockEntity) {
            process = TierProcess.COMPACTING;
        } else if (self instanceof MechanicalMixerBlockEntity) {
            process = TierProcess.MIXING;
        } else {
            return;
        }
        Level level = self.getLevel();
        if (level == null) {
            return;
        }
        MachineTier tier = MachineTier.of(self.getBlockState());
        if (tier.isBase()) {
            return;
        }

        List<Recipe<?>> recipes = new ArrayList<>();
        for (MachineTier allowed : tier.allowedDescending()) {
            AMRecipeTypes type = AMRecipeTypes.of(process, allowed);
            for (RecipeHolder<BasinRecipe> holder : TieredRecipeLookup.recipes(level, type, BasinRecipe.class)) {
                if (matchBasinRecipe(holder.value())) {
                    recipes.add(holder.value());
                }
            }
        }
        if (recipes.isEmpty()) {
            return;
        }
        recipes.addAll(cir.getReturnValue());
        cir.setReturnValue(recipes);
    }
}
