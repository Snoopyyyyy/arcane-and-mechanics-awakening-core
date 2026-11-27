package com.snoopycorp.amcore.machinetier;

import java.util.Optional;

import com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity;
import com.simibubi.create.content.kinetics.deployer.DeployerRecipeSearchEvent;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.snoopycorp.amcore.AMCore;
import com.snoopycorp.amcore.machinetier.recipe.AMRecipeTypes;
import com.snoopycorp.amcore.machinetier.recipe.TieredDeployingRecipe;

import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;

/**
 * Deployer: tiered steps and recipes through Create's recipe search event. The highest priority wins; Create uses 100
 * for its sequenced assembly step and 50 for its recipes, so tiered ones sit above, highest tier first.
 */
@EventBusSubscriber(modid = AMCore.MODID)
public final class DeployerTierHandler {
    private static final int BASE_PRIORITY = 200;
    private static final int PRIORITY_PER_TIER = 20;

    private DeployerTierHandler() {
    }

    @SubscribeEvent
    static void onDeployerRecipeSearch(DeployerRecipeSearchEvent event) {
        DeployerBlockEntity deployer = event.getBlockEntity();
        Level level = deployer.getLevel();
        if (level == null) {
            return;
        }
        MachineTier tier = MachineTier.of(deployer.getBlockState());
        if (tier.isBase()) {
            return;
        }
        RecipeWrapper inventory = event.getInventory();
        // Added from the highest priority down: a lookup only runs if nothing better was found yet
        for (MachineTier allowed : tier.allowedDescending()) {
            AMRecipeTypes type = AMRecipeTypes.of(TierProcess.DEPLOYING, allowed);
            int priority = BASE_PRIORITY + PRIORITY_PER_TIER * allowed.ordinal();
            event.addRecipe(() -> SequencedAssemblyRecipe.getRecipe(level, inventory,
                    type.typeOf(TieredDeployingRecipe.class), TieredDeployingRecipe.class), priority + 1);
            event.addRecipe(() -> findRecipe(level, inventory, type), priority);
        }
    }

    private static Optional<RecipeHolder<TieredDeployingRecipe>> findRecipe(Level level, RecipeWrapper inventory, AMRecipeTypes type) {
        for (RecipeHolder<TieredDeployingRecipe> holder : TieredRecipeLookup.recipes(level, type, TieredDeployingRecipe.class)) {
            if (holder.value().matches(inventory, level)) {
                return Optional.of(holder);
            }
        }
        return Optional.empty();
    }
}
