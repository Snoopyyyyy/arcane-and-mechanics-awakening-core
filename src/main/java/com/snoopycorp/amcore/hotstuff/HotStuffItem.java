package com.snoopycorp.amcore.hotstuff;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.snoopycorp.amcore.AMCore;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * A heated copy of another item, in a fixed heat state. A hot item never changes state on its own.
 */
public class HotStuffItem extends Item {
    public static final TagKey<Item> HOT_ITEMS = TagKey.create(Registries.ITEM, AMCore.id("hot_items"));
    /** Name of a hot item: base item name, then heat state. */
    public static final String NAME_KEY = AMCore.MODID + ".heat.name";
    /** Tooltip line of a hot item: heat state, then temperature. */
    public static final String TOOLTIP_KEY = AMCore.MODID + ".heat.tooltip";

    private final HotState state;
    private final int temperature;
    private final ResourceLocation baseItem;
    @Nullable
    private final Integer color;
    @Nullable
    private ItemStack baseStack;

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
     * RGB heat color of this item: the color given by the script, or the default color of its state.
     */
    public int getColor() {
        return color != null ? color : state.getDefaultColor();
    }

    /**
     * RGB heat color given by the script, or {@code null} when the item uses the default fire colors.
     */
    @Nullable
    public Integer getCustomColor() {
        return color;
    }

    private ItemStack getBaseStack() {
        if (baseStack == null) {
            baseStack = BuiltInRegistries.ITEM.get(baseItem).getDefaultInstance();
        }

        return baseStack;
    }

    /**
     * Base item name followed by the heat state, e.g. "Iron Ingot (Scorching)". A name given to this item
     * (a translation of its own key, or a name set by a script) takes priority.
     */
    @Override
    public Component getName(ItemStack stack) {
        Component name = super.getName(stack);

        if (name.getContents() instanceof TranslatableContents contents
                && contents.getKey().equals(getDescriptionId(stack))
                && !Language.getInstance().has(contents.getKey())) {
            return Component.translatable(NAME_KEY, getBaseStack().getHoverName(), state.getDisplayName());
        }

        return name;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(TOOLTIP_KEY, state.getDisplayName(), temperature).withStyle(state.getTextColor()));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
