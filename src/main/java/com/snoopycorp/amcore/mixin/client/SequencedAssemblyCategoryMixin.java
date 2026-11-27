package com.snoopycorp.amcore.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.compat.jei.category.SequencedAssemblyCategory;
import com.simibubi.create.compat.jei.category.sequencedAssembly.SequencedAssemblySubCategory;
import com.simibubi.create.content.processing.sequenced.SequencedRecipe;
import com.snoopycorp.amcore.machinetier.compat.jei.TierMachineDisplay;
import com.snoopycorp.amcore.machinetier.recipe.TieredRecipe;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Sequenced assembly: a tiered step draws the machine of its tier; Create's steps keep Create's machines.
 */
@Mixin(SequencedAssemblyCategory.class)
public abstract class SequencedAssemblyCategoryMixin {
    @WrapOperation(method = "draw(Lcom/simibubi/create/content/processing/sequenced/SequencedAssemblyRecipe;Lmezz/jei/api/gui/ingredient/IRecipeSlotsView;Lnet/minecraft/client/gui/GuiGraphics;DD)V",
            at = @At(value = "INVOKE",
                    target = "Lcom/simibubi/create/compat/jei/category/sequencedAssembly/SequencedAssemblySubCategory;draw(Lcom/simibubi/create/content/processing/sequenced/SequencedRecipe;Lnet/minecraft/client/gui/GuiGraphics;DDI)V"))
    private void amcore$drawStepWithTierMachine(SequencedAssemblySubCategory subCategory, SequencedRecipe<?> step,
            GuiGraphics graphics, double mouseX, double mouseY, int index, Operation<Void> original) {
        if (!(step.getRecipe() instanceof TieredRecipe tiered)) {
            original.call(subCategory, step, graphics, mouseX, mouseY, index);
            return;
        }
        var previous = TierMachineDisplay.push(tiered.tierType());
        try {
            original.call(subCategory, step, graphics, mouseX, mouseY, index);
        } finally {
            TierMachineDisplay.restore(previous);
        }
    }
}
