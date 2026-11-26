package com.snoopycorp.amcore.machinetier.recipe;

import com.simibubi.create.content.kinetics.deployer.ItemApplicationRecipe;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import com.snoopycorp.amcore.AMCore;
import com.snoopycorp.amcore.machinetier.MachineTier;
import com.snoopycorp.amcore.machinetier.TierProcess;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The tiered recipe types, {@code amcore:<tier>_<process>}. Each uses the JSON format of the matching Create type;
 * the tier is carried by the type alone.
 */
public enum AMRecipeTypes implements IRecipeTypeInfo {
    ADVANCED_PRESSING(TierProcess.PRESSING, MachineTier.ADVANCED),
    ELITE_PRESSING(TierProcess.PRESSING, MachineTier.ELITE),
    ULTIMATE_PRESSING(TierProcess.PRESSING, MachineTier.ULTIMATE),
    ADVANCED_COMPACTING(TierProcess.COMPACTING, MachineTier.ADVANCED),
    ELITE_COMPACTING(TierProcess.COMPACTING, MachineTier.ELITE),
    ULTIMATE_COMPACTING(TierProcess.COMPACTING, MachineTier.ULTIMATE),
    ADVANCED_MIXING(TierProcess.MIXING, MachineTier.ADVANCED),
    ELITE_MIXING(TierProcess.MIXING, MachineTier.ELITE),
    ULTIMATE_MIXING(TierProcess.MIXING, MachineTier.ULTIMATE),
    ADVANCED_CUTTING(TierProcess.CUTTING, MachineTier.ADVANCED),
    ELITE_CUTTING(TierProcess.CUTTING, MachineTier.ELITE),
    ULTIMATE_CUTTING(TierProcess.CUTTING, MachineTier.ULTIMATE),
    ADVANCED_DEPLOYING(TierProcess.DEPLOYING, MachineTier.ADVANCED),
    ELITE_DEPLOYING(TierProcess.DEPLOYING, MachineTier.ELITE),
    ULTIMATE_DEPLOYING(TierProcess.DEPLOYING, MachineTier.ULTIMATE),
    ADVANCED_FILLING(TierProcess.FILLING, MachineTier.ADVANCED),
    ELITE_FILLING(TierProcess.FILLING, MachineTier.ELITE);

    private final TierProcess process;
    private final MachineTier tier;
    private final ResourceLocation id;
    private final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> serializer;
    private final DeferredHolder<RecipeType<?>, RecipeType<?>> type;

    AMRecipeTypes(TierProcess process, MachineTier tier) {
        this.process = process;
        this.tier = tier;
        String name = tier.getSerializedName() + "_" + process.getSerializedName();
        this.id = AMCore.id(name);
        this.serializer = Registers.SERIALIZERS.register(name, this::createSerializer);
        this.type = Registers.TYPES.register(name, () -> RecipeType.simple(id));
    }

    public static void register(IEventBus modEventBus) {
        Registers.SERIALIZERS.register(modEventBus);
        Registers.TYPES.register(modEventBus);
    }

    /** The type of a process at a tier, or {@code null} if there is none (base tier, ultimate filling). */
    public static AMRecipeTypes of(TierProcess process, MachineTier tier) {
        for (AMRecipeTypes type : values()) {
            if (type.process == process && type.tier == tier) {
                return type;
            }
        }
        return null;
    }

    private RecipeSerializer<?> createSerializer() {
        return switch (process) {
            case PRESSING -> new StandardProcessingRecipe.Serializer<>(params -> new TieredPressingRecipe(this, params));
            case COMPACTING -> new StandardProcessingRecipe.Serializer<>(params -> new TieredCompactingRecipe(this, params));
            case MIXING -> new StandardProcessingRecipe.Serializer<>(params -> new TieredMixingRecipe(this, params));
            case CUTTING -> new StandardProcessingRecipe.Serializer<>(params -> new TieredCuttingRecipe(this, params));
            case DEPLOYING -> new ItemApplicationRecipe.Serializer<>(params -> new TieredDeployingRecipe(this, params));
            case FILLING -> new StandardProcessingRecipe.Serializer<>(params -> new TieredFillingRecipe(this, params));
        };
    }

    public TierProcess process() {
        return process;
    }

    public MachineTier tier() {
        return tier;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends RecipeSerializer<?>> T getSerializer() {
        return (T) serializer.get();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <I extends RecipeInput, R extends Recipe<I>> RecipeType<R> getType() {
        return (RecipeType<R>) type.get();
    }

    /** {@link #getType()} cast to a recipe class, for generic lookups. */
    @SuppressWarnings("unchecked")
    public <R extends Recipe<?>> RecipeType<R> typeOf(Class<R> recipeClass) {
        return (RecipeType<R>) type.get();
    }

    private static final class Registers {
        private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
                DeferredRegister.create(Registries.RECIPE_SERIALIZER, AMCore.MODID);
        private static final DeferredRegister<RecipeType<?>> TYPES =
                DeferredRegister.create(Registries.RECIPE_TYPE, AMCore.MODID);
    }
}
