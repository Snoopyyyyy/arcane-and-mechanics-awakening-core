package com.snoopycorp.amcore.data.lang;

import com.snoopycorp.amcore.AMCore;
import com.snoopycorp.amcore.hotstuff.HotState;
import com.snoopycorp.amcore.hotstuff.HotStuffItem;
import com.snoopycorp.amcore.machinetier.MachineTier;
import com.snoopycorp.amcore.machinetier.recipe.AMRecipeTypes;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class AMEnglishLanguageProvider extends LanguageProvider {
    public AMEnglishLanguageProvider(PackOutput output) {
        super(output, AMCore.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add("fml.menu.mods.info.description." + AMCore.MODID, "Core mod of the Arcane & Mechanics modpack.");
        add("death.attack." + AMCore.MODID + ".molten", "%1$s melted in molten metal");
        add("death.attack." + AMCore.MODID + ".molten.player", "%1$s melted in molten metal while trying to escape %2$s");
        add("death.attack." + AMCore.MODID + ".hot_touch", "%1$s burned their hands");
        add("death.attack." + AMCore.MODID + ".hot_touch.player", "%1$s burned their hands while trying to escape %2$s");
        add(HotStuffItem.NAME_KEY, "%s (%s)");
        add(HotStuffItem.TOOLTIP_KEY, "%s: %s°");
        add(HotState.WARM.getTranslationKey(), "Warm");
        add(HotState.HOT.getTranslationKey(), "Hot");
        add(HotState.SCORCHING.getTranslationKey(), "Scorching");

        add(MachineTier.TOOLTIP_KEY, "Tier: %s");
        add(MachineTier.ASSEMBLY_KEY, "%s (%s+)");
        add(MachineTier.ADVANCED.getTranslationKey(), "Advanced");
        add(MachineTier.ELITE.getTranslationKey(), "Elite");
        add(MachineTier.ULTIMATE.getTranslationKey(), "Ultimate");
        for (AMRecipeTypes type : AMRecipeTypes.values()) {
            String process = switch (type.process()) {
                case PRESSING -> "Pressing";
                case COMPACTING -> "Compacting";
                case MIXING -> "Mixing";
                case CUTTING -> "Sawing";
                case DEPLOYING -> "Deploying";
                case FILLING -> "Filling";
            };
            String tier = switch (type.tier()) {
                case ADVANCED -> "Advanced";
                case ELITE -> "Elite";
                default -> "Ultimate";
            };
            add(AMCore.MODID + ".recipe." + type.getId().getPath(), tier + " " + process);
        }
    }
}
