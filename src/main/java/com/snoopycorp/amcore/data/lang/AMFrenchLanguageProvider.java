package com.snoopycorp.amcore.data.lang;

import com.snoopycorp.amcore.AMCore;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class AMFrenchLanguageProvider extends LanguageProvider {
    public AMFrenchLanguageProvider(PackOutput output) {
        super(output, AMCore.MODID, "fr_fr");
    }

    @Override
    protected void addTranslations() {
        add("fml.menu.mods.info.description." + AMCore.MODID, "Mod central du modpack Arcane & Mechanics: Awakening.");
    }
}
