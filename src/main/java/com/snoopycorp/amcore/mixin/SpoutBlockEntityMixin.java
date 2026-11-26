package com.snoopycorp.amcore.mixin;

import java.util.List;
import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.fluids.spout.SpoutBlockEntity;
import com.snoopycorp.amcore.machinetier.MachineTier;
import com.snoopycorp.amcore.machinetier.TieredRecipeLookup;
import com.snoopycorp.amcore.machinetier.recipe.TieredFillingRecipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Spout: tank capacity and filling speed per tier, and tiered filling recipes.
 * <p>
 * The spout does not look up its recipes itself; it calls static methods of {@code FillingBySpout}, which do not know
 * the spout. Each call is wrapped here: a tiered step or recipe is tried first, then the original call runs, so
 * bottles, buckets and other mods' additions to {@code FillingBySpout} keep working.
 */
@Mixin(SpoutBlockEntity.class)
public abstract class SpoutBlockEntityMixin {
    private static final String FILLING_BY_SPOUT = "Lcom/simibubi/create/content/fluids/spout/FillingBySpout;";

    private MachineTier amcore$tier() {
        return MachineTier.of(((SpoutBlockEntity) (Object) this).getBlockState());
    }

    /** Capacity is set when the block entity is created; the block state is already known then. */
    @ModifyArg(method = "addBehaviours", index = 1, at = @At(value = "INVOKE",
            target = "Lcom/simibubi/create/foundation/blockEntity/behaviour/fluid/SmartFluidTankBehaviour;single(Lcom/simibubi/create/foundation/blockEntity/SmartBlockEntity;I)Lcom/simibubi/create/foundation/blockEntity/behaviour/fluid/SmartFluidTankBehaviour;"))
    private int amcore$tierCapacity(int capacity) {
        return amcore$tier().spoutCapacity(capacity);
    }

    /**
     * {@code FILLING_TIME} (20), where {@code processingTicks} starts for an item on a belt and for a block below. The
     * spout fills when the counter reaches 5, so it never starts below 6.
     */
    @ModifyConstant(method = { "whenItemHeld", "tick" }, constant = @Constant(intValue = 20))
    private int amcore$tierFillingTime(int fillingTime) {
        MachineTier tier = amcore$tier();
        if (tier.isBase()) {
            return fillingTime;
        }
        return Math.max(6, (int) Math.round(fillingTime / tier.speedMultiplier()));
    }

    @WrapOperation(method = { "onItemReceived", "whenItemHeld" }, at = @At(value = "INVOKE",
            target = FILLING_BY_SPOUT + "canItemBeFilled(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean amcore$canItemBeFilled(Level level, ItemStack stack, Operation<Boolean> original) {
        MachineTier tier = amcore$tier();
        if (!tier.isBase() && TieredRecipeLookup.canItemBeFilled(level, tier, stack)) {
            return true;
        }
        return original.call(level, stack);
    }

    @WrapOperation(method = { "onItemReceived", "whenItemHeld" }, at = @At(value = "INVOKE",
            target = FILLING_BY_SPOUT + "getRequiredAmountForItem(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/neoforged/neoforge/fluids/FluidStack;)I"))
    private int amcore$getRequiredAmountForItem(Level level, ItemStack stack, FluidStack fluid, Operation<Integer> original) {
        MachineTier tier = amcore$tier();
        if (!tier.isBase()) {
            Optional<RecipeHolder<TieredFillingRecipe>> recipe = TieredRecipeLookup.findFilling(level, tier, stack, fluid);
            if (recipe.isPresent()) {
                return recipe.get().value().getRequiredFluid().amount();
            }
        }
        return original.call(level, stack, fluid);
    }

    /** Mirrors {@code FillingBySpout.fillItem}: the fluid and the item are consumed here, the result returned. */
    @WrapOperation(method = "whenItemHeld", at = @At(value = "INVOKE",
            target = FILLING_BY_SPOUT + "fillItem(Lnet/minecraft/world/level/Level;ILnet/minecraft/world/item/ItemStack;Lnet/neoforged/neoforge/fluids/FluidStack;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack amcore$fillItem(Level level, int requiredAmount, ItemStack stack, FluidStack fluid, Operation<ItemStack> original) {
        MachineTier tier = amcore$tier();
        if (!tier.isBase()) {
            FluidStack toFill = fluid.copy();
            toFill.setAmount(requiredAmount);
            Optional<RecipeHolder<TieredFillingRecipe>> recipe = TieredRecipeLookup.findFillingWith(level, tier, stack, toFill);
            if (recipe.isPresent()) {
                List<ItemStack> results = recipe.get().value().rollResults(level.random);
                fluid.shrink(requiredAmount);
                stack.shrink(1);
                return results.isEmpty() ? ItemStack.EMPTY : results.getFirst();
            }
        }
        return original.call(level, requiredAmount, stack, fluid);
    }
}
