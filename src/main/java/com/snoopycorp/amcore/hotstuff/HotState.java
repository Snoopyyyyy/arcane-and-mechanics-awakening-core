package com.snoopycorp.amcore.hotstuff;

import java.util.Locale;

import com.snoopycorp.amcore.AMCore;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

/**
 * Heat states a hot item can be in, from the coolest to the hottest.
 */
public enum HotState implements StringRepresentable {
    WARM(0xFFB347, ChatFormatting.YELLOW),
    HOT(0xFF7A1A, ChatFormatting.GOLD),
    SCORCHING(0xFFE4D0, ChatFormatting.RED);

    private final String name;
    private final int defaultColor;
    private final ChatFormatting textColor;

    HotState(int defaultColor, ChatFormatting textColor) {
        this.name = name().toLowerCase(Locale.ROOT);
        this.defaultColor = defaultColor;
        this.textColor = textColor;
    }

    /**
     * RGB tint used when the script does not give a color for this state.
     */
    public int getDefaultColor() {
        return defaultColor;
    }

    /**
     * Color of the state in tooltips.
     */
    public ChatFormatting getTextColor() {
        return textColor;
    }

    public String getTranslationKey() {
        return AMCore.MODID + ".heat.state." + name;
    }

    public Component getDisplayName() {
        return Component.translatable(getTranslationKey());
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
