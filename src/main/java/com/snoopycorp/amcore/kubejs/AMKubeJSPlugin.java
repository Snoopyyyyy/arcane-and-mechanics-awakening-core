package com.snoopycorp.amcore.kubejs;

import com.snoopycorp.amcore.AMCore;
import com.snoopycorp.amcore.machinetier.recipe.AMRecipeTypes;

import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.schema.RecipeNamespace;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaRegistry;
import dev.latvian.mods.kubejs.registry.BuilderTypeRegistry;

import net.minecraft.core.registries.Registries;

public class AMKubeJSPlugin implements KubeJSPlugin {
    /**
     * KubeJS reads {@code kubejs.plugins.txt} once per mod, and this jar holds two mods (amcore and create_metallurgy),
     * so it creates this plugin twice. Only the first instance does anything.
     */
    private static AMKubeJSPlugin active;

    public AMKubeJSPlugin() {
        if (active == null) {
            active = this;
        }
    }

    private boolean isDuplicate() {
        return active != this;
    }

    @Override
    public void registerBuilderTypes(BuilderTypeRegistry registry) {
        if (isDuplicate()) {
            return;
        }
        registry.of(Registries.FLUID, reg -> reg.add(AMCore.id("molten"), MoltenFluidBuilder.class, MoltenFluidBuilder::new));
        registry.of(Registries.ITEM, reg -> reg.add(AMCore.id("hot_stuff"), HotStuffBuilder.class, HotStuffBuilder::new));
    }

    /**
     * Each tiered type reuses the KubeJS Create schema of its Create type, e.g. {@code event.recipes.amcore.advanced_pressing}
     * takes the same arguments and methods as {@code event.recipes.create.pressing}.
     */
    @Override
    public void registerRecipeSchemas(RecipeSchemaRegistry registry) {
        if (isDuplicate()) {
            return;
        }
        RecipeNamespace namespace = registry.namespace(AMCore.MODID);
        for (AMRecipeTypes type : AMRecipeTypes.values()) {
            try {
                namespace.withExistingParent(type.getId().getPath(), type.process().createTypeId());
            } catch (RuntimeException e) {
                AMCore.LOGGER.error("Could not register the KubeJS recipe schema {} (is KubeJS Create installed?)", type.getId(), e);
            }
        }
    }
}
