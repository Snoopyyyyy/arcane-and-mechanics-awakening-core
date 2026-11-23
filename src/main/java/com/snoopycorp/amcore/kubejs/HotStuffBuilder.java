package com.snoopycorp.amcore.kubejs;

import java.util.function.Consumer;

import com.snoopycorp.amcore.hotstuff.HotStuffItem;

import dev.latvian.mods.kubejs.client.LangKubeEvent;
import dev.latvian.mods.kubejs.generator.KubeAssetGenerator;
import dev.latvian.mods.kubejs.item.ItemBuilder;
import dev.latvian.mods.kubejs.registry.AdditionalObjectRegistry;
import dev.latvian.mods.kubejs.registry.RegistryObjectStorage;
import dev.latvian.mods.kubejs.script.ConsoleJS;
import dev.latvian.mods.kubejs.typings.Info;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;

/**
 * Builder of the {@code amcore:hot_stuff} item type. It registers nothing under its own id: it creates one
 * {@link HotStuffItem} per declared heat state, named {@code <state>_<name>}.
 */
public class HotStuffBuilder extends ItemBuilder {
    public final transient HeatBuilder heat = new HeatBuilder();

    public HotStuffBuilder(ResourceLocation id) {
        super(id);
        dummyBuilder = true;
    }

    @Info("""
            Sets the heated item and its heat states, e.g.
            `.heat(h => h.from('minecraft:iron_ingot').warm(100).hot(500, 0xFF8800).scorching(1000, '#FFEEDD'))`.

            Each declared state creates an item `<state>_<name>`. The color of a state is optional.
            """)
    public HotStuffBuilder heat(Consumer<HeatBuilder> settings) {
        settings.accept(heat);
        return this;
    }

    /**
     * True when the script customized the model or the texture, which then replaces the base item model.
     */
    boolean hasCustomModel() {
        return modelGenerator != null || parentModel != null || !textures.isEmpty() || !baseTexture.equals(id.withPrefix("item/").toString());
    }

    @Override
    public void createAdditionalObjects(AdditionalObjectRegistry registry) {
        // KubeJS still lists a dummy builder (creative tab, assets, lang, tags) and would read its missing item.
        RegistryObjectStorage.ITEM.objects.remove(id, this);
        RegistryObjectStorage.ALL_BUILDERS.remove(this);

        var errors = heat.validate();

        if (!errors.isEmpty()) {
            for (var error : errors) {
                logError("Invalid hot item '" + id + "': " + error + ". No item is created for it.");
            }

            return;
        }

        for (var entry : heat.states().entrySet()) {
            var state = new HotStateItemBuilder(this, heat.baseItem(), entry.getKey(), entry.getValue());

            try {
                registry.add(Registries.ITEM, state);
            } catch (IllegalArgumentException ex) {
                logError("Can't create hot item '" + state.id + "': " + ex.getMessage());
            }
        }

        defaultTags.clear();
    }

    private void logError(String message) {
        ConsoleJS.STARTUP.error(message, sourceLine, null, null);
    }

    @Override
    public void generateAssets(KubeAssetGenerator generator) {
    }

    @Override
    public void generateLang(LangKubeEvent lang) {
    }
}
