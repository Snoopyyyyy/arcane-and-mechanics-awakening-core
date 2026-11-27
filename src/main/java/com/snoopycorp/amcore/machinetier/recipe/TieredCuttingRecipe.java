package com.snoopycorp.amcore.machinetier.recipe;

import java.util.Set;

import com.simibubi.create.content.kinetics.saw.CuttingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;

public class TieredCuttingRecipe extends CuttingRecipe implements TieredRecipe {
    private final AMRecipeTypes tierType;

    public TieredCuttingRecipe(AMRecipeTypes tierType, ProcessingRecipeParams params) {
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

    @Override
    public Component getDescriptionForAssembly() {
        return TieredRecipe.describe(super.getDescriptionForAssembly(), tierType);
    }

    @Override
    public void addRequiredMachines(Set<ItemLike> machines) {
        TieredRecipe.addMachines(machines, tierType);
    }
}
