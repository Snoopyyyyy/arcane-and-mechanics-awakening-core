package com.snoopycorp.amcore.machinetier.recipe;

import java.util.Set;

import com.snoopycorp.amcore.machinetier.MachineTier;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ItemLike;

/**
 * A recipe of a tiered type. Implementations extend the matching Create recipe class and override
 * {@code getType()}, {@code getSerializer()} and {@code getTypeInfo()} so that only machines of the right tier
 * look them up.
 */
public interface TieredRecipe {
    AMRecipeTypes tierType();

    default MachineTier tier() {
        return tierType().tier();
    }

    /** Sequenced assembly step text with the minimum tier, e.g. "Pressing (Advanced+)". */
    static Component describe(Component base, AMRecipeTypes type) {
        return Component.translatable(MachineTier.ASSEMBLY_KEY, base, type.tier().getDisplayName());
    }

    static void addMachines(Set<ItemLike> machines, AMRecipeTypes type) {
        machines.addAll(MachineTier.machines(type.process(), type.tier()));
    }
}
