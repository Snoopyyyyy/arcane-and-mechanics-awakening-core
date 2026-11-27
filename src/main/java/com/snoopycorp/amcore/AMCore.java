package com.snoopycorp.amcore;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.snoopycorp.amcore.config.AMConfig;
import com.snoopycorp.amcore.config.AMServerConfig;
import com.snoopycorp.amcore.machinetier.recipe.AMRecipeTypes;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(AMCore.MODID)
public class AMCore {
    public static final String MODID = "amcore";
    public static final Logger LOGGER = LogUtils.getLogger();

    public AMCore(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, AMConfig.SPEC);
        modContainer.registerConfig(ModConfig.Type.SERVER, AMServerConfig.SPEC);
        AMRecipeTypes.register(modEventBus);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
