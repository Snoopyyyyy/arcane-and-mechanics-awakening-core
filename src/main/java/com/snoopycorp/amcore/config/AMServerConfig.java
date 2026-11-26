package com.snoopycorp.amcore.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Server config, synced to clients by NeoForge so both sides agree on machine speeds and tank sizes.
 */
public final class AMServerConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.DoubleValue ADVANCED_SPEED_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue ELITE_SPEED_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue ULTIMATE_SPEED_MULTIPLIER;
    public static final ModConfigSpec.IntValue ADVANCED_SPOUT_CAPACITY;
    public static final ModConfigSpec.IntValue ELITE_SPOUT_CAPACITY;
    public static final ModConfigSpec.IntValue ULTIMATE_SPOUT_CAPACITY;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("Create machine tiers.",
                "A machine's tier comes from the block tags amcore:machine_tiers/advanced, /elite and /ultimate.",
                "Machines outside these tags are at the base tier and keep Create's behaviour.").push("machine_tiers");

        ADVANCED_SPEED_MULTIPLIER = builder
                .comment("Processing speed multiplier of advanced machines, on every recipe they run. 1 disables the bonus.")
                .defineInRange("advancedSpeedMultiplier", 1.5D, 1.0D, 16.0D);
        ELITE_SPEED_MULTIPLIER = builder
                .comment("Processing speed multiplier of elite machines.")
                .defineInRange("eliteSpeedMultiplier", 2.0D, 1.0D, 16.0D);
        ULTIMATE_SPEED_MULTIPLIER = builder
                .comment("Processing speed multiplier of ultimate machines.")
                .defineInRange("ultimateSpeedMultiplier", 3.0D, 1.0D, 16.0D);

        ADVANCED_SPOUT_CAPACITY = builder
                .comment("Tank capacity of advanced spouts, in mB (a base spout holds 1000 mB). Applied when the spout is loaded.")
                .defineInRange("advancedSpoutCapacity", 4000, 1000, 1_000_000);
        ELITE_SPOUT_CAPACITY = builder
                .comment("Tank capacity of elite spouts, in mB.")
                .defineInRange("eliteSpoutCapacity", 8000, 1000, 1_000_000);
        ULTIMATE_SPOUT_CAPACITY = builder
                .comment("Tank capacity of ultimate spouts, in mB.")
                .defineInRange("ultimateSpoutCapacity", 16000, 1000, 1_000_000);

        builder.pop();
        SPEC = builder.build();
    }

    private AMServerConfig() {
    }

    /** Value of a setting, or its default while the server config is not loaded (main menu, early loading). */
    public static double get(ModConfigSpec.DoubleValue value) {
        return SPEC.isLoaded() ? value.get() : value.getDefault();
    }

    public static int get(ModConfigSpec.IntValue value) {
        return SPEC.isLoaded() ? value.get() : value.getDefault();
    }
}
