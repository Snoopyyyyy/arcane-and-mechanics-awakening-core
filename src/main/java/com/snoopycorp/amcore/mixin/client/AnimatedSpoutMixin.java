package com.snoopycorp.amcore.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.simibubi.create.compat.jei.category.animations.AnimatedSpout;
import com.snoopycorp.amcore.machinetier.compat.jei.TierMachineDisplay;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;

/**
 * Spout bottom of the tier machine (Create Encased variants have their own).
 */
@Mixin(AnimatedSpout.class)
public abstract class AnimatedSpoutMixin {
    @ModifyExpressionValue(method = "draw", at = @At(value = "FIELD",
            target = "Lcom/simibubi/create/AllPartialModels;SPOUT_BOTTOM:Ldev/engine_room/flywheel/lib/model/baked/PartialModel;"))
    private PartialModel amcore$tierSpoutBottom(PartialModel bottom) {
        return TierMachineDisplay.spoutBottom(bottom);
    }
}
