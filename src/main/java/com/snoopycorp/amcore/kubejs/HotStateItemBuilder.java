package com.snoopycorp.amcore.kubejs;

import com.snoopycorp.amcore.hotstuff.HotState;
import com.snoopycorp.amcore.hotstuff.HotStuffItem;

import dev.latvian.mods.kubejs.client.LangKubeEvent;
import dev.latvian.mods.kubejs.core.ItemKJS;
import dev.latvian.mods.kubejs.generator.KubeAssetGenerator;
import dev.latvian.mods.kubejs.item.ItemBuilder;
import dev.latvian.mods.kubejs.script.ConsoleJS;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

/**
 * Item of one heat state of an {@code amcore:hot_stuff} builder. Every item option set in the script is read
 * from the parent builder, so all the states of one builder share them.
 */
public class HotStateItemBuilder extends ItemBuilder {
    private final HotStuffBuilder parent;
    private final ResourceLocation baseItem;
    private final HotState state;
    private final HeatBuilder.StateSettings settings;

    HotStateItemBuilder(HotStuffBuilder parent, ResourceLocation baseItem, HotState state, HeatBuilder.StateSettings settings) {
        super(parent.newID(state.getSerializedName() + "_", ""));
        this.parent = parent;
        this.baseItem = baseItem;
        this.state = state;
        this.settings = settings;

        sourceLine = parent.sourceLine;
        burnTime = parent.burnTime;
        tint = parent.tint;
        defaultTags.addAll(parent.defaultTags);
        defaultTags.add(HotStuffItem.HOT_ITEMS.location());

        if (parent.hasCustomModel()) {
            modelGenerator = parent.modelGenerator;
            parentModel = parent.parentModel;
            textures.putAll(parent.textures);
            baseTexture = parent.baseTexture;
        }
    }

    @Override
    public Item createObject() {
        if (!BuiltInRegistries.ITEM.containsKey(baseItem)) {
            ConsoleJS.STARTUP.warn("Hot item '" + id + "': base item '" + baseItem + "' doesn't exist.");
        }

        return new HotStuffItem(parent.createItemProperties(), state, settings.temperature(), baseItem, settings.color());
    }

    @Override
    public Item transformObject(Item obj) {
        parent.displayName(parent.displayName, parent.formattedDisplayName);
        ((ItemKJS) obj).kjs$setItemBehavior(parent.behavior);
        return obj;
    }

    @Override
    public void generateAssets(KubeAssetGenerator generator) {
        if (parent.hasCustomModel()) {
            super.generateAssets(generator);
        } else {
            generator.itemModel(id, model -> model.parent(baseItem.withPrefix("item/")));
        }
    }

    /**
     * Without a name set by the script, the item has no translation so that its name is built from the base item.
     */
    @Override
    public void generateLang(LangKubeEvent lang) {
        if (parent.displayName != null) {
            lang.add(id.getNamespace(), getBuilderTranslationKey(), parent.displayName.getString());
        }
    }
}
