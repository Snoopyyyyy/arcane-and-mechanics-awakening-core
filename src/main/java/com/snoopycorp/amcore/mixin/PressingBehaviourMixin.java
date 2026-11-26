package com.snoopycorp.amcore.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.content.kinetics.press.PressingBehaviour;
import com.snoopycorp.amcore.machinetier.MachineTier;

/**
 * Press speed bonus (belt, ground and basin). Applied after Create's cap of 60; Create clamps the head at the pressing
 * point when a tick would cross it, so a fast press never skips the moment it presses. Runs on both sides, which keeps
 * the animation in step.
 */
@Mixin(PressingBehaviour.class)
public abstract class PressingBehaviourMixin {
    @Inject(method = "getRunningTickSpeed", at = @At("RETURN"), cancellable = true)
    private void amcore$tierSpeed(CallbackInfoReturnable<Integer> cir) {
        int speed = cir.getReturnValueI();
        if (speed == 0) {
            return;
        }
        MachineTier tier = MachineTier.of(((PressingBehaviour) (Object) this).blockEntity.getBlockState());
        if (!tier.isBase()) {
            cir.setReturnValue((int) Math.round(speed * tier.speedMultiplier()));
        }
    }
}
