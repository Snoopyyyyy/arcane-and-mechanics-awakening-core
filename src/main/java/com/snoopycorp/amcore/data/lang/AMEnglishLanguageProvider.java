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
        add("fml.menu.mods.info.description." + AMCore.MODID, "Core mod of the Arcane & Mechanics modpack.");
        add("death.attack." + AMCore.MODID + ".molten", "%1$s melted in molten metal");
        add("death.attack." + AMCore.MODID + ".molten.player", "%1$s melted in molten metal while trying to escape %2$s");
    }
}
