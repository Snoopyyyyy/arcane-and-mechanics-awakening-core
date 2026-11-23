package com.snoopycorp.amcore.damage;

import com.snoopycorp.amcore.AMCore;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageEffects;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;

public final class AMDamageTypes {
    public static final ResourceKey<DamageType> MOLTEN = ResourceKey.create(Registries.DAMAGE_TYPE, AMCore.id("molten"));
    public static final ResourceKey<DamageType> HOT_TOUCH = ResourceKey.create(Registries.DAMAGE_TYPE, AMCore.id("hot_touch"));

    private AMDamageTypes() {
    }

    public static void bootstrap(BootstrapContext<DamageType> context) {
        context.register(MOLTEN, new DamageType(AMCore.MODID + ".molten", DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER, 0.1F, DamageEffects.BURNING));
        context.register(HOT_TOUCH, new DamageType(AMCore.MODID + ".hot_touch", DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER, 0.1F, DamageEffects.BURNING));
    }
}
