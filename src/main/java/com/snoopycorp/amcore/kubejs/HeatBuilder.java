package com.snoopycorp.amcore.kubejs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.snoopycorp.amcore.hotstuff.HotState;

import dev.latvian.mods.kubejs.color.KubeColor;
import dev.latvian.mods.kubejs.typings.Info;

import net.minecraft.resources.ResourceLocation;

/**
 * Heat settings of an {@code amcore:hot_stuff} item, set through {@code .heat(h => ...)}.
 */
public class HeatBuilder {
    public record StateSettings(int temperature, @Nullable Integer color) {
    }

    @Nullable
    private ResourceLocation baseItem;
    private final Map<HotState, StateSettings> states = new EnumMap<>(HotState.class);

    @Info("Sets the item that is heated. Its model and name are used by every heat state. Required.")
    public HeatBuilder from(ResourceLocation item) {
        baseItem = item;
        return this;
    }

    @Info("Declares the warm state with its temperature, which creates the item `warm_<name>`.")
    public HeatBuilder warm(int temperature) {
        return state(HotState.WARM, temperature, null);
    }

    @Info("Declares the warm state with its temperature and tint color, which creates the item `warm_<name>`.")
    public HeatBuilder warm(int temperature, KubeColor color) {
        return state(HotState.WARM, temperature, color);
    }

    @Info("Declares the hot state with its temperature, which creates the item `hot_<name>`.")
    public HeatBuilder hot(int temperature) {
        return state(HotState.HOT, temperature, null);
    }

    @Info("Declares the hot state with its temperature and tint color, which creates the item `hot_<name>`.")
    public HeatBuilder hot(int temperature, KubeColor color) {
        return state(HotState.HOT, temperature, color);
    }

    @Info("Declares the scorching state with its temperature, which creates the item `scorching_<name>`.")
    public HeatBuilder scorching(int temperature) {
        return state(HotState.SCORCHING, temperature, null);
    }

    @Info("Declares the scorching state with its temperature and tint color, which creates the item `scorching_<name>`.")
    public HeatBuilder scorching(int temperature, KubeColor color) {
        return state(HotState.SCORCHING, temperature, color);
    }

    private HeatBuilder state(HotState state, int temperature, @Nullable KubeColor color) {
        states.put(state, new StateSettings(temperature, color == null ? null : color.kjs$getRGB()));
        return this;
    }

    @Nullable
    ResourceLocation baseItem() {
        return baseItem;
    }

    /**
     * Declared states, from the coolest to the hottest.
     */
    Map<HotState, StateSettings> states() {
        return Collections.unmodifiableMap(states);
    }

    /**
     * Problems in the settings, empty when they are valid.
     */
    List<String> validate() {
        List<String> errors = new ArrayList<>();

        if (baseItem == null) {
            errors.add("no base item, call .from('<item id>')");
        }

        if (states.isEmpty()) {
            errors.add("no heat state, call at least one of .warm(...), .hot(...) or .scorching(...)");
        }

        HotState previous = null;

        for (var entry : states.entrySet()) {
            int temperature = entry.getValue().temperature();

            if (temperature <= 0) {
                errors.add("the %s temperature must be greater than 0 (got %d)".formatted(entry.getKey().getSerializedName(), temperature));
            }

            if (previous != null && temperature <= states.get(previous).temperature()) {
                errors.add("the %s temperature (%d) must be greater than the %s temperature (%d)".formatted(
                        entry.getKey().getSerializedName(), temperature, previous.getSerializedName(), states.get(previous).temperature()));
            }

            previous = entry.getKey();
        }

        return errors;
    }
}
