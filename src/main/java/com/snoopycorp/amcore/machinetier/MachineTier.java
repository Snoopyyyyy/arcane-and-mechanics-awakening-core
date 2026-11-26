package com.snoopycorp.amcore.machinetier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.snoopycorp.amcore.AMCore;
import com.snoopycorp.amcore.config.AMServerConfig;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Tier of a Create machine, read from the block tags {@code amcore:machine_tiers/<tier>}. A block in no tag is at the
 * base tier; a block in several tags gets the highest one.
 */
public enum MachineTier {
    BASE,
    ADVANCED,
    ELITE,
    ULTIMATE;

    public static final String TOOLTIP_KEY = AMCore.MODID + ".machine_tier.tooltip";
    public static final String ASSEMBLY_KEY = AMCore.MODID + ".machine_tier.assembly";

    private static final MachineTier[] DESCENDING = { ULTIMATE, ELITE, ADVANCED };

    private final TagKey<Block> tag;

    MachineTier() {
        this.tag = ordinal() == 0 ? null
                : TagKey.create(Registries.BLOCK, AMCore.id("machine_tiers/" + name().toLowerCase(Locale.ROOT)));
    }

    public static MachineTier of(BlockState state) {
        for (MachineTier tier : DESCENDING) {
            if (state.is(tier.tag)) {
                return tier;
            }
        }
        return BASE;
    }

    public static MachineTier of(Block block) {
        return of(block.defaultBlockState());
    }

    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public TagKey<Block> tag() {
        return tag;
    }

    public boolean isBase() {
        return this == BASE;
    }

    /** Tiers whose recipes this tier runs, from this tier down to {@link #ADVANCED}. Empty for the base tier. */
    public List<MachineTier> allowedDescending() {
        List<MachineTier> tiers = new ArrayList<>();
        for (int i = ordinal(); i >= 1; i--) {
            tiers.add(values()[i]);
        }
        return tiers;
    }

    public String getTranslationKey() {
        return AMCore.MODID + ".machine_tier." + getSerializedName();
    }

    public Component getDisplayName() {
        return Component.translatable(getTranslationKey());
    }

    public double speedMultiplier() {
        return switch (this) {
            case BASE -> 1.0D;
            case ADVANCED -> AMServerConfig.get(AMServerConfig.ADVANCED_SPEED_MULTIPLIER);
            case ELITE -> AMServerConfig.get(AMServerConfig.ELITE_SPEED_MULTIPLIER);
            case ULTIMATE -> AMServerConfig.get(AMServerConfig.ULTIMATE_SPEED_MULTIPLIER);
        };
    }

    /** Spout tank capacity in mB, or {@code base} for the base tier. */
    public int spoutCapacity(int base) {
        return switch (this) {
            case BASE -> base;
            case ADVANCED -> AMServerConfig.get(AMServerConfig.ADVANCED_SPOUT_CAPACITY);
            case ELITE -> AMServerConfig.get(AMServerConfig.ELITE_SPOUT_CAPACITY);
            case ULTIMATE -> AMServerConfig.get(AMServerConfig.ULTIMATE_SPOUT_CAPACITY);
        };
    }

    /**
     * Machines of a process whose tier is at least {@code min}, lowest tier first: blocks of the tier tags that are
     * also in Create's machine tag of the process.
     */
    public static List<Block> machines(TierProcess process, MachineTier min) {
        List<Block> blocks = new ArrayList<>();
        for (MachineTier tier : DESCENDING) {
            if (tier.ordinal() < min.ordinal()) {
                continue;
            }
            for (Holder<Block> holder : BuiltInRegistries.BLOCK.getTagOrEmpty(tier.tag)) {
                Block block = holder.value();
                if (holder.is(process.machineTag()) && of(block) == tier && !blocks.contains(block)) {
                    blocks.add(block);
                }
            }
        }
        Collections.reverse(blocks);
        return blocks;
    }
}
