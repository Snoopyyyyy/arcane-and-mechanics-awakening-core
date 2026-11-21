package com.snoopycorp.amcore.data.registry;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

import com.snoopycorp.amcore.AMCore;
import com.snoopycorp.amcore.damage.AMDamageTypes;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;

public class AMDatapackProvider extends DatapackBuiltinEntriesProvider {
    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.DAMAGE_TYPE, AMDamageTypes::bootstrap);

    public AMDatapackProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, BUILDER, Set.of(AMCore.MODID));
    }
}
