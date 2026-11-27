package com.snoopycorp.amcore.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.simibubi.create.compat.jei.category.animations.AnimatedMixer;
import com.snoopycorp.amcore.machinetier.compat.jei.TierMachineDisplay;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;

/**
 * Mixer head of the tier machine (Create Encased variants have their own).
 */
@Mixin(AnimatedMixer.class)
public abstract class AnimatedMixerMixin {
    @ModifyExpressionValue(method = "draw", at = @At(value = "FIELD",
            target = "Lcom/simibubi/create/AllPartialModels;MECHANICAL_MIXER_HEAD:Ldev/engine_room/flywheel/lib/model/baked/PartialModel;"))
    private PartialModel amcore$tierMixerHead(PartialModel head) {
        return TierMachineDisplay.mixerHead(head);
    }
}
