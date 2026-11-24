package com.snoopycorp.amcore.hotstuff.client;

import java.util.Map;

import com.snoopycorp.amcore.AMCore;
import com.snoopycorp.amcore.hotstuff.HotStuffItem;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterItemDecorationsEvent;
import net.neoforged.neoforge.client.event.RegisterSpriteSourceTypesEvent;

/**
 * Client setup of hot items rendering.
 */
@EventBusSubscriber(modid = AMCore.MODID, value = Dist.CLIENT)
public final class HotStuffClient {
    private HotStuffClient() {
    }

    @SubscribeEvent
    static void registerSpriteSourceTypes(RegisterSpriteSourceTypesEvent event) {
        event.register(HotSpriteSource.ID, HotSpriteSource.TYPE);
    }

    @SubscribeEvent
    static void registerItemDecorations(RegisterItemDecorationsEvent event) {
        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof HotStuffItem) {
                event.register(item, ThermometerDecorator.INSTANCE);
            }
        }
    }

    @SubscribeEvent
    static void modifyBakingResult(ModelEvent.ModifyBakingResult event) {
        Map<ModelResourceLocation, BakedModel> models = event.getModels();

        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof HotStuffItem hot) {
                ModelResourceLocation id = ModelResourceLocation.inventory(BuiltInRegistries.ITEM.getKey(item));
                BakedModel model = models.get(id);

                if (model != null) {
                    models.put(id, new HotItemModel(model, hot));
                }
            }
        }
    }
}
