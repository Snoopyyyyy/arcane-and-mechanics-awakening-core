package com.snoopycorp.amcore.kubejs;

import com.snoopycorp.amcore.AMCore;

import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.registry.BuilderTypeRegistry;

import net.minecraft.core.registries.Registries;

public class AMKubeJSPlugin implements KubeJSPlugin {
    @Override
    public void registerBuilderTypes(BuilderTypeRegistry registry) {
        registry.of(Registries.FLUID, reg -> reg.add(AMCore.id("molten"), MoltenFluidBuilder.class, MoltenFluidBuilder::new));
    }
}
