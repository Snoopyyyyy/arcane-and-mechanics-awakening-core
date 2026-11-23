package com.snoopycorp.amcore.hotstuff;

import java.util.Locale;

import net.minecraft.util.StringRepresentable;

/**
 * Heat states a hot item can be in, from the coolest to the hottest.
 */
public enum HotState implements StringRepresentable {
    WARM(0xFFB347),
    HOT(0xFF7A1A),
    SCORCHING(0xFFE4D0);

    private final String name;
    private final int defaultColor;

    HotState(int defaultColor) {
        this.name = name().toLowerCase(Locale.ROOT);
        this.defaultColor = defaultColor;
    }

    /**
     * RGB tint used when the script does not give a color for this state.
     */
    public int getDefaultColor() {
        return defaultColor;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
