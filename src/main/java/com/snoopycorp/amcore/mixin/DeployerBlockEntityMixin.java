package com.snoopycorp.amcore.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity;
import com.snoopycorp.amcore.machinetier.MachineTier;

/**
 * Deployer speed bonus: the value taken off the timer each tick, also used by the hand animation.
 */
@Mixin(DeployerBlockEntity.class)
public abstract class DeployerBlockEntityMixin {
    @Inject(method = "getTimerSpeed", at = @At("RETURN"), cancellable = true)
    private void amcore$tierSpeed(CallbackInfoReturnable<Integer> cir) {
        int speed = cir.getReturnValueI();
        if (speed == 0) {
            return;
        }
        MachineTier tier = MachineTier.of(((DeployerBlockEntity) (Object) this).getBlockState());
        if (!tier.isBase()) {
            cir.setReturnValue((int) Math.round(speed * tier.speedMultiplier()));
        }
    }
}
