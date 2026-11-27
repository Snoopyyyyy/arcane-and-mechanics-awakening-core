package com.snoopycorp.amcore.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.snoopycorp.amcore.machinetier.compat.jei.TierMachineDisplay;

import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

/**
 * Tells the animated machines which category is drawing, so amcore's categories draw their tier machine.
 */
@Mixin(CreateRecipeCategory.class)
public abstract class CreateRecipeCategoryMixin<T extends Recipe<?>> {
    @Shadow
    @Final
    protected RecipeType<RecipeHolder<T>> type;

    @Inject(method = "draw(Lnet/minecraft/world/item/crafting/RecipeHolder;Lmezz/jei/api/gui/ingredient/IRecipeSlotsView;Lnet/minecraft/client/gui/GuiGraphics;DD)V",
            at = @At("HEAD"))
    private void amcore$beginTierDraw(RecipeHolder<T> recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX,
            double mouseY, CallbackInfo ci) {
        TierMachineDisplay.begin(type.getUid());
    }

    @Inject(method = "draw(Lnet/minecraft/world/item/crafting/RecipeHolder;Lmezz/jei/api/gui/ingredient/IRecipeSlotsView;Lnet/minecraft/client/gui/GuiGraphics;DD)V",
            at = @At("RETURN"))
    private void amcore$endTierDraw(RecipeHolder<T> recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX,
            double mouseY, CallbackInfo ci) {
        TierMachineDisplay.end();
    }
}
