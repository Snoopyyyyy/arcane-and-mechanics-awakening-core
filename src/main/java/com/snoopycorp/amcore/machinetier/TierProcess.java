package com.snoopycorp.amcore.machinetier;

import java.util.Locale;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

/**
 * A Create process that has tiered recipe types, with the Create block tag that lists its machines.
 */
public enum TierProcess {
    PRESSING("presses", "mechanical_press"),
    COMPACTING("presses", "mechanical_press"),
    MIXING("mixers", "mechanical_mixer"),
    CUTTING("saws", "mechanical_saw"),
    DEPLOYING("deployers", "deployer"),
    FILLING("spouts", "spout");

    private final TagKey<Block> machineTag;
    private final ResourceLocation baseMachine;

    TierProcess(String machineTag, String baseMachine) {
        this.machineTag = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("create", machineTag));
        this.baseMachine = ResourceLocation.fromNamespaceAndPath("create", baseMachine);
    }

    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    /** Create's own recipe type for this process, e.g. {@code create:pressing}. */
    public ResourceLocation createTypeId() {
        return ResourceLocation.fromNamespaceAndPath("create", getSerializedName());
    }

    /** Create block tag holding every machine of this process, e.g. {@code create:presses}. */
    public TagKey<Block> machineTag() {
        return machineTag;
    }

    /** Create's base machine for this process, e.g. {@code create:mechanical_press}. */
    public ResourceLocation baseMachine() {
        return baseMachine;
    }
}
