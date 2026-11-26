package com.snoopycorp.amcore.machinetier.recipe;

import com.simibubi.create.content.kinetics.mixer.CompactingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class TieredCompactingRecipe extends CompactingRecipe implements TieredRecipe {
    private final AMRecipeTypes tierType;

    public TieredCompactingRecipe(AMRecipeTypes tierType, ProcessingRecipeParams params) {
        super(params);
        this.tierType = tierType;
    }

    @Override
    public AMRecipeTypes tierType() {
        return tierType;
    }

    @Override
    public RecipeType<?> getType() {
        return tierType.getType();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return tierType.getSerializer();
    }

    @Override
    public IRecipeTypeInfo getTypeInfo() {
        return tierType;
    }
}
