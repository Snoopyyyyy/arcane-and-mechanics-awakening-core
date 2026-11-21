package com.snoopycorp.amcore.data;

import com.snoopycorp.amcore.AMCore;
import com.snoopycorp.amcore.data.lang.*;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = AMCore.MODID)
public final class AMCoreData {
    private AMCoreData() {
    }

    @SubscribeEvent
    static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();

        // Language providers
        generator.addProvider(event.includeClient(), new AMEnglishLanguageProvider(output));
        generator.addProvider(event.includeClient(), new AMFrenchLanguageProvider(output));
    }
}
