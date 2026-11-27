package com.snoopycorp.amcore.mixin;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.content.kinetics.press.MechanicalPressBlockEntity;
import com.simibubi.create.content.kinetics.press.PressingRecipe;
import com.snoopycorp.amcore.machinetier.MachineTier;
import com.snoopycorp.amcore.machinetier.TierProcess;
import com.snoopycorp.amcore.machinetier.TieredRecipeLookup;
import com.snoopycorp.amcore.machinetier.recipe.TieredPressingRecipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

/**
 * Press on a belt or above items on the ground: tiered pressing steps and recipes come before Create's.
 */
@Mixin(MechanicalPressBlockEntity.class)
public abstract class MechanicalPressBlockEntityMixin {
    @Inject(method = "getRecipe", at = @At("HEAD"), cancellable = true)
    private void amcore$findTieredPressing(ItemStack stack, CallbackInfoReturnable<Optional<RecipeHolder<PressingRecipe>>> cir) {
        MechanicalPressBlockEntity self = (MechanicalPressBlockEntity) (Object) this;
        Level level = self.getLevel();
        if (level == null) {
            return;
        }
        MachineTier tier = MachineTier.of(self.getBlockState());
        if (tier.isBase()) {
            return;
        }
        SingleRecipeInput input = new SingleRecipeInput(stack);
        // Like Create, a step is taken as soon as the item is at that step of a sequenced assembly
        TieredRecipeLookup.find(level, tier, TierProcess.PRESSING, TieredPressingRecipe.class, stack,
                holder -> true, holder -> holder.value().matches(input, level))
                .ifPresent(holder -> cir.setReturnValue(Optional.of(amcore$widen(holder))));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static RecipeHolder<PressingRecipe> amcore$widen(RecipeHolder<TieredPressingRecipe> holder) {
        return (RecipeHolder) holder;
    }
}
