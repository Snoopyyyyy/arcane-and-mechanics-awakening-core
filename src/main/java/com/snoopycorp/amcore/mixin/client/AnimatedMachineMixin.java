package com.snoopycorp.amcore.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.simibubi.create.compat.jei.category.animations.AnimatedDeployer;
import com.simibubi.create.compat.jei.category.animations.AnimatedMixer;
import com.simibubi.create.compat.jei.category.animations.AnimatedPress;
import com.simibubi.create.compat.jei.category.animations.AnimatedSaw;
import com.simibubi.create.compat.jei.category.animations.AnimatedSpout;
import com.snoopycorp.amcore.machinetier.compat.jei.TierMachineDisplay;

import net.minecraft.world.level.block.state.BlockState;

/**
 * Create's JEI machines take their block from {@code AllBlocks.<machine>.getDefaultState()}; in amcore's categories the
 * tier machine is drawn instead.
 */
@Mixin({ AnimatedPress.class, AnimatedMixer.class, AnimatedSaw.class, AnimatedDeployer.class, AnimatedSpout.class })
public abstract class AnimatedMachineMixin {
    @ModifyExpressionValue(method = "draw", at = @At(value = "INVOKE",
            target = "Lcom/tterrag/registrate/util/entry/BlockEntry;getDefaultState()Lnet/minecraft/world/level/block/state/BlockState;"))
    private BlockState amcore$tierMachine(BlockState state) {
        return TierMachineDisplay.swap(state);
    }
}
