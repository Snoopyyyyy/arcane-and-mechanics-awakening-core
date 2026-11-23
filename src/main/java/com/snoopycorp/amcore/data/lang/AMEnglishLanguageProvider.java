package com.snoopycorp.amcore.data.lang;

import com.snoopycorp.amcore.AMCore;
import com.snoopycorp.amcore.hotstuff.HotState;
import com.snoopycorp.amcore.hotstuff.HotStuffItem;

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
    }
}
