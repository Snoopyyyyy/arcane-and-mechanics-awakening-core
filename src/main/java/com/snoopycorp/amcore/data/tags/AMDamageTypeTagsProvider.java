package com.snoopycorp.amcore.data.tags;

import java.util.concurrent.CompletableFuture;

import com.snoopycorp.amcore.AMCore;
import com.snoopycorp.amcore.damage.AMDamageTypes;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageType;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class AMDamageTypeTagsProvider extends TagsProvider<DamageType> {
    public AMDamageTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
        super(output, Registries.DAMAGE_TYPE, lookupProvider, AMCore.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(DamageTypeTags.IS_FIRE).add(AMDamageTypes.MOLTEN);
        tag(DamageTypeTags.NO_KNOCKBACK).add(AMDamageTypes.MOLTEN);
        tag(DamageTypeTags.PANIC_ENVIRONMENTAL_CAUSES).add(AMDamageTypes.MOLTEN);

        tag(DamageTypeTags.IS_FIRE).add(AMDamageTypes.HOT_TOUCH);
        tag(DamageTypeTags.NO_KNOCKBACK).add(AMDamageTypes.HOT_TOUCH);
        tag(DamageTypeTags.BYPASSES_ARMOR).add(AMDamageTypes.HOT_TOUCH);
    }
}
