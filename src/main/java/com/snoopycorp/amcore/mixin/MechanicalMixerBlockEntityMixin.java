package com.snoopycorp.amcore.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.simibubi.create.content.kinetics.mixer.MechanicalMixerBlockEntity;
import com.snoopycorp.amcore.machinetier.MachineTier;

/**
 * Mixer speed bonus: shortens the processing time Create computes when a recipe starts (the value stored in
 * {@code processingTicks}).
 */
@Mixin(MechanicalMixerBlockEntity.class)
public abstract class MechanicalMixerBlockEntityMixin {
    @ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(II)I"))
    private int amcore$tierProcessingTicks(int ticks) {
        MachineTier tier = MachineTier.of(((MechanicalMixerBlockEntity) (Object) this).getBlockState());
        if (tier.isBase()) {
            return ticks;
        }
        return Math.max(1, (int) Math.round(ticks / tier.speedMultiplier()));
    }
}
