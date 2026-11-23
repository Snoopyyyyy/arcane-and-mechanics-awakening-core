package com.snoopycorp.amcore.data.lang;

import com.snoopycorp.amcore.AMCore;
import com.snoopycorp.amcore.hotstuff.HotState;
import com.snoopycorp.amcore.hotstuff.HotStuffItem;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class AMFrenchLanguageProvider extends LanguageProvider {
    public AMFrenchLanguageProvider(PackOutput output) {
        super(output, AMCore.MODID, "fr_fr");
    }

    @Override
    protected void addTranslations() {
        add("fml.menu.mods.info.description." + AMCore.MODID, "Mod central du modpack Arcane & Mechanics.");
        add("death.attack." + AMCore.MODID + ".molten", "%1$s a fondu dans du métal en fusion");
        add("death.attack." + AMCore.MODID + ".molten.player", "%1$s a fondu dans du métal en fusion en essayant d'échapper à %2$s");
        add("death.attack." + AMCore.MODID + ".hot_touch", "%1$s s'est brûlé les mains");
        add("death.attack." + AMCore.MODID + ".hot_touch.player", "%1$s s'est brûlé les mains en essayant d'échapper à %2$s");
        add(HotStuffItem.NAME_KEY, "%s (%s)");
        add(HotStuffItem.TOOLTIP_KEY, "%s : %s°");
        add(HotState.WARM.getTranslationKey(), "Tiède");
        add(HotState.HOT.getTranslationKey(), "Chaud");
        add(HotState.SCORCHING.getTranslationKey(), "Brûlant");
    }
}
