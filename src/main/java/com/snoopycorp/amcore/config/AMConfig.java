package com.snoopycorp.amcore.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class AMConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.IntValue HOT_STUFF_DAMAGE_INTERVAL;
    public static final ModConfigSpec.IntValue HOT_STUFF_MIN_TEMPERATURE;
    public static final ModConfigSpec.DoubleValue HOT_STUFF_DEGREES_PER_DAMAGE;
    public static final ModConfigSpec.IntValue HOT_STUFF_IGNITE_TEMPERATURE;
    public static final ModConfigSpec.DoubleValue HOT_STUFF_IGNITE_SECONDS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("Damage dealt by hot items carried in a player's inventory.",
                "Only the hottest item of the inventory is taken into account.").push("hot_stuff");

        HOT_STUFF_DAMAGE_INTERVAL = builder
                .comment("Ticks between two burns (20 = 1 second).")
                .defineInRange("damageInterval", 20, 1, 1200);
        HOT_STUFF_MIN_TEMPERATURE = builder
                .comment("Temperature below which a hot item deals no damage.")
                .defineInRange("minTemperature", 100, 0, Integer.MAX_VALUE);
        HOT_STUFF_DEGREES_PER_DAMAGE = builder
                .comment("Degrees per point of damage (2 points = 1 heart).")
                .defineInRange("degreesPerDamage", 250.0D, 1.0D, Double.MAX_VALUE);
        HOT_STUFF_IGNITE_TEMPERATURE = builder
                .comment("Temperature from which the player is set on fire.")
                .defineInRange("igniteTemperature", 1000, 0, Integer.MAX_VALUE);
        HOT_STUFF_IGNITE_SECONDS = builder
                .comment("Fire duration, in seconds.", "0 disables setting the player on fire.")
                .defineInRange("igniteSeconds", 3.0D, 0.0D, 60.0D);

        builder.pop();
        SPEC = builder.build();
    }

    private AMConfig() {
    }
}
