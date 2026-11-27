package com.snoopycorp.amcore.machinetier.compat.jei;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.compat.jei.category.DeployingCategory;
import com.simibubi.create.compat.jei.category.MixingCategory;
import com.simibubi.create.compat.jei.category.PackingCategory;
import com.simibubi.create.compat.jei.category.PressingCategory;
import com.simibubi.create.compat.jei.category.SawingCategory;
import com.simibubi.create.compat.jei.category.SpoutCategory;
import com.simibubi.create.content.fluids.transfer.FillingRecipe;
import com.simibubi.create.content.kinetics.deployer.DeployerApplicationRecipe;
import com.simibubi.create.content.kinetics.press.PressingRecipe;
import com.simibubi.create.content.kinetics.saw.CuttingRecipe;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.snoopycorp.amcore.AMCore;
import com.snoopycorp.amcore.machinetier.MachineTier;
import com.snoopycorp.amcore.machinetier.TierProcess;
import com.snoopycorp.amcore.machinetier.recipe.AMRecipeTypes;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/**
 * One JEI category per tiered recipe type, drawn by Create's own categories. Catalysts of a tier are the machines of
 * that tier and above, read from the tier tags.
 */
@JeiPlugin
public class AMJEIPlugin implements IModPlugin {
    private static final ResourceLocation UID = AMCore.id("jei_plugin");
    private static final ResourceLocation BASIN = ResourceLocation.fromNamespaceAndPath("create", "basin");

    private final List<TieredCategory> categories = new ArrayList<>();

    private record TieredCategory(AMRecipeTypes type, CreateRecipeCategory<?> category) {
    }

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        categories.clear();
        for (AMRecipeTypes type : AMRecipeTypes.values()) {
            categories.add(new TieredCategory(type, createCategory(type)));
        }
        registration.addRecipeCategories(categories.stream().map(TieredCategory::category).toArray(IRecipeCategory[]::new));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        categories.forEach(tiered -> tiered.category().registerRecipes(registration));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        for (TieredCategory tiered : categories) {
            TierProcess process = tiered.type().process();
            for (Block machine : MachineTier.machines(process, tiered.type().tier())) {
                registration.addRecipeCatalyst(new ItemStack(machine), tiered.category().getRecipeType());
            }
            if (process == TierProcess.COMPACTING || process == TierProcess.MIXING) {
                registration.addRecipeCatalyst(new ItemStack(item(BASIN)), tiered.category().getRecipeType());
            }
        }
    }

    private static CreateRecipeCategory<?> createCategory(AMRecipeTypes type) {
        ResourceLocation id = type.getId();
        Item machine = iconMachine(type);
        return switch (type.process()) {
            case PRESSING -> new CreateRecipeCategory.Builder<>(PressingRecipe.class)
                    .addTypedRecipes(type)
                    .doubleItemIcon(machine, item("create", "iron_sheet"))
                    .emptyBackground(177, 70)
                    .build(id, PressingCategory::new);
            case COMPACTING -> new CreateRecipeCategory.Builder<>(BasinRecipe.class)
                    .addTypedRecipes(type)
                    .doubleItemIcon(machine, item(BASIN))
                    .emptyBackground(177, 103)
                    .build(id, PackingCategory::standard);
            case MIXING -> new CreateRecipeCategory.Builder<>(BasinRecipe.class)
                    .addTypedRecipes(type)
                    .doubleItemIcon(machine, item(BASIN))
                    .emptyBackground(177, 103)
                    .build(id, MixingCategory::standard);
            case CUTTING -> new CreateRecipeCategory.Builder<>(CuttingRecipe.class)
                    .addTypedRecipes(type)
                    .doubleItemIcon(machine, item("minecraft", "oak_log"))
                    .emptyBackground(177, 70)
                    .build(id, SawingCategory::new);
            case DEPLOYING -> new CreateRecipeCategory.Builder<>(DeployerApplicationRecipe.class)
                    .addTypedRecipes(type)
                    .doubleItemIcon(machine, item("create", "brass_hand"))
                    .emptyBackground(177, 70)
                    .build(id, DeployingCategory::new);
            case FILLING -> new CreateRecipeCategory.Builder<>(FillingRecipe.class)
                    .addTypedRecipes(type)
                    .doubleItemIcon(machine, item("minecraft", "water_bucket"))
                    .emptyBackground(177, 70)
                    .build(id, SpoutCategory::new);
        };
    }

    /** The category icon shows the machine of the tier when the pack defines one, Create's machine otherwise. */
    private static Item iconMachine(AMRecipeTypes type) {
        Block machine = MachineTier.displayMachine(type.process(), type.tier());
        return machine != null ? machine.asItem() : item(type.process().baseMachine());
    }

    private static Item item(String namespace, String path) {
        return item(ResourceLocation.fromNamespaceAndPath(namespace, path));
    }

    private static Item item(ResourceLocation id) {
        return BuiltInRegistries.ITEM.get(id);
    }
}
