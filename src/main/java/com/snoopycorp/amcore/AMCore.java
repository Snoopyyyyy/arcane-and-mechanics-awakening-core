package com.snoopycorp.amcore;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(AMCore.MODID)
public class AMCore {
    public static final String MODID = "amcore";
    public static final Logger LOGGER = LogUtils.getLogger();

    public AMCore(IEventBus modEventBus, ModContainer modContainer) {
    }
}
