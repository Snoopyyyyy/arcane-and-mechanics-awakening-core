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
    WARM(0xFF5F1B, 0.7F, 0.7F, 1F, 0.3F, 5, 0xFFD23F, ChatFormatting.YELLOW),
    HOT(0xFF7720, 0.95F, 1F, 1F, 0.5F, 10, 0xFF8A1A, ChatFormatting.GOLD),
    SCORCHING(0xFFA34B, 1F, 1.4F, 0.9F, 0.65F, 15, 0xE8251A, ChatFormatting.RED);

    private final String name;
    private final int defaultColor;
    private final float glowCoverage;
    private final float glowIntensity;
    private final float glowFade;
    private final float blendStrength;
    private final int emissivity;
    private final int iconColor;
    private final ChatFormatting textColor;

    HotState(int defaultColor, float glowCoverage, float glowIntensity, float glowFade, float blendStrength, int emissivity, int iconColor, ChatFormatting textColor) {
        this.name = name().toLowerCase(Locale.ROOT);
        this.defaultColor = defaultColor;
        this.glowCoverage = glowCoverage;
        this.glowIntensity = glowIntensity;
        this.glowFade = glowFade;
        this.blendStrength = blendStrength;
        this.emissivity = emissivity;
        this.iconColor = iconColor;
        this.textColor = textColor;
    }

    /**
     * RGB heat color used when the script does not give one for this state. It tints the textures of non flat
     * models.
     */
    public int getDefaultColor() {
        return defaultColor;
    }

    /**
     * Part of the item height, from its lower edge, that the glow of flat item textures reaches (0 to 1).
     */
    public float getGlowCoverage() {
        return glowCoverage;
    }

    /**
     * How far along the fire palette the hottest pixels go: about 1 reaches white on the lightest pixels.
     */
    public float getGlowIntensity() {
        return glowIntensity;
    }

    /**
     * Part of the glow over which it fades into the original texture (0 to 1): higher is smoother.
     */
    public float getGlowFade() {
        return glowFade;
    }

    /**
     * How far textures of non flat models are blended toward the heat color, from 0 (unchanged) to 1 (plain color).
     */
    public float getBlendStrength() {
        return blendStrength;
    }

    /**
     * Light level the item is rendered with, from 0 to 15, whatever the light around it.
     */
    public int getEmissivity() {
        return emissivity;
    }

    /**
     * RGB color of the thermometer drawn over the item in inventories.
     */
    public int getIconColor() {
        return iconColor;
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
