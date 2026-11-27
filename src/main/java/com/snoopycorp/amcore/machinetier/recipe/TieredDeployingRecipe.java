package com.snoopycorp.amcore.machinetier.recipe;

import java.util.Set;

import com.simibubi.create.content.kinetics.deployer.DeployerApplicationRecipe;
import com.simibubi.create.content.kinetics.deployer.ItemApplicationRecipeParams;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;

public class TieredDeployingRecipe extends DeployerApplicationRecipe implements TieredRecipe {
    private final AMRecipeTypes tierType;

    public TieredDeployingRecipe(AMRecipeTypes tierType, ItemApplicationRecipeParams params) {
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
