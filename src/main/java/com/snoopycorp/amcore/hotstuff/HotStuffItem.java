package com.snoopycorp.amcore.hotstuff;

import org.jetbrains.annotations.Nullable;

import com.snoopycorp.amcore.AMCore;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/**
 * A heated copy of another item, in a fixed heat state. A hot item never changes state on its own.
 */
public class HotStuffItem extends Item {
    public static final TagKey<Item> HOT_ITEMS = TagKey.create(Registries.ITEM, AMCore.id("hot_items"));

    private final HotState state;
    private final int temperature;
    private final ResourceLocation baseItem;
    @Nullable
    private final Integer color;

    public HotStuffItem(Properties properties, HotState state, int temperature, ResourceLocation baseItem, @Nullable Integer color) {
        super(properties);
        this.state = state;
        this.temperature = temperature;
        this.baseItem = baseItem;
        this.color = color;
    }

    public HotState getState() {
        return state;
    }

    public int getTemperature() {
        return temperature;
    }

    /**
     * Id of the item this hot item is a heated copy of.
     */
    public ResourceLocation getBaseItem() {
        return baseItem;
    }

    /**
     * RGB tint of this item: the color given by the script, or the default color of its state.
     */
    public int getColor() {
        return color != null ? color : state.getDefaultColor();
    }
}
