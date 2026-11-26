package com.snoopycorp.amcore.machinetier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.snoopycorp.amcore.machinetier.recipe.AMRecipeTypes;
import com.snoopycorp.amcore.machinetier.recipe.TieredFillingRecipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Recipe searches shared by the machine hooks. Every search walks the allowed tiers from the highest down and, for
 * each tier, tries the sequenced assembly step before the plain recipes, so the highest tier wins.
 */
public final class TieredRecipeLookup {
    private TieredRecipeLookup() {
    }

    /** Plain recipes of a tiered type that may run in automation, in recipe manager order. */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <R extends Recipe<?>> List<RecipeHolder<R>> recipes(Level level, AMRecipeTypes type, Class<R> recipeClass) {
        RecipeType recipeType = type.typeOf(recipeClass);
        List<RecipeHolder<?>> all = level.getRecipeManager().getAllRecipesFor(recipeType);
        List<RecipeHolder<R>> result = new ArrayList<>();
        for (RecipeHolder<?> holder : all) {
            if (recipeClass.isInstance(holder.value()) && !AllRecipeTypes.shouldIgnoreInAutomation(holder)) {
                result.add((RecipeHolder<R>) holder);
            }
        }
        return result;
    }

    /** Next step of a sequenced assembly the item is in, if that step is of the given tiered type. */
    public static <R extends ProcessingRecipe<?, ?>> Optional<RecipeHolder<R>> sequencedStep(Level level, ItemStack stack,
            AMRecipeTypes type, Class<R> recipeClass, Predicate<? super RecipeHolder<R>> filter) {
        List<RecipeHolder<R>> steps = SequencedAssemblyRecipe.getRecipes(level, stack, type.typeOf(recipeClass), recipeClass, filter);
        return steps.isEmpty() ? Optional.empty() : Optional.of(steps.getFirst());
    }

    /** First step or recipe, highest tier first, accepted by {@code matches} (used for both). */
    public static <R extends ProcessingRecipe<?, ?>> Optional<RecipeHolder<R>> find(Level level, MachineTier tier,
            TierProcess process, Class<R> recipeClass, ItemStack stack, Predicate<? super RecipeHolder<R>> matches) {
        return find(level, tier, process, recipeClass, stack, matches, matches);
    }

    /**
     * First step or recipe, highest tier first. Steps are filtered by {@code stepFilter} (the sequenced assembly has
     * already checked the item), plain recipes by {@code matches}.
     */
    public static <R extends ProcessingRecipe<?, ?>> Optional<RecipeHolder<R>> find(Level level, MachineTier tier,
            TierProcess process, Class<R> recipeClass, ItemStack stack, Predicate<? super RecipeHolder<R>> stepFilter,
            Predicate<? super RecipeHolder<R>> matches) {
        for (MachineTier allowed : tier.allowedDescending()) {
            AMRecipeTypes type = AMRecipeTypes.of(process, allowed);
            if (type == null) {
                continue;
            }
            Optional<RecipeHolder<R>> step = sequencedStep(level, stack, type, recipeClass, stepFilter);
            if (step.isPresent()) {
                return step;
            }
            for (RecipeHolder<R> holder : recipes(level, type, recipeClass)) {
                if (matches.test(holder)) {
                    return Optional.of(holder);
                }
            }
        }
        return Optional.empty();
    }

    // Filling, mirroring FillingBySpout

    /** Like {@code FillingBySpout.canItemBeFilled}: a tiered step or recipe accepts the item, whatever the fluid. */
    public static boolean canItemBeFilled(Level level, MachineTier tier, ItemStack stack) {
        SingleRecipeInput input = new SingleRecipeInput(stack);
        return find(level, tier, TierProcess.FILLING, TieredFillingRecipe.class, stack,
                holder -> holder.value().matches(input, level)).isPresent();
    }

    /**
     * Tiered filling recipe for the item and the fluid type. Like {@code FillingBySpout.getRequiredAmountForItem}, the
     * fluid amount is not checked here; the spout compares the required amount with its tank.
     */
    public static Optional<RecipeHolder<TieredFillingRecipe>> findFilling(Level level, MachineTier tier, ItemStack stack, FluidStack fluid) {
        SingleRecipeInput input = new SingleRecipeInput(stack);
        return find(level, tier, TierProcess.FILLING, TieredFillingRecipe.class, stack,
                holder -> holder.value().matches(input, level) && holder.value().getRequiredFluid().ingredient().test(fluid));
    }

    /**
     * Tiered filling recipe that can fill the item with exactly {@code fluid} (type and amount), as
     * {@code FillingBySpout.fillItem} checks.
     */
    public static Optional<RecipeHolder<TieredFillingRecipe>> findFillingWith(Level level, MachineTier tier, ItemStack stack, FluidStack fluid) {
        SingleRecipeInput input = new SingleRecipeInput(stack);
        return find(level, tier, TierProcess.FILLING, TieredFillingRecipe.class, stack,
                holder -> holder.value().matches(input, level) && holder.value().getRequiredFluid().test(fluid));
    }
}
