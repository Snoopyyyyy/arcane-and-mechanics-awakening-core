package com.snoopycorp.amcore.data.lang;

import com.snoopycorp.amcore.AMCore;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class AMEnglishLanguageProvider extends LanguageProvider {
    public AMEnglishLanguageProvider(PackOutput output) {
        super(output, AMCore.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add("fml.menu.mods.info.description." + AMCore.MODID, "Core mod of the Arcane & Mechanics: Awakening modpack.");
    }
}
