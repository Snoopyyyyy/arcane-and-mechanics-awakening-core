package com.snoopycorp.amcore.machinetier.client;

import java.util.List;

import com.snoopycorp.amcore.AMCore;
import com.snoopycorp.amcore.machinetier.MachineTier;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/**
 * "Tier: ..." line on the items of tiered machines, right under the name.
 */
@EventBusSubscriber(modid = AMCore.MODID, value = Dist.CLIENT)
public final class MachineTierTooltip {
    private MachineTierTooltip() {
    }

    @SubscribeEvent
    static void onItemTooltip(ItemTooltipEvent event) {
        if (!(event.getItemStack().getItem() instanceof BlockItem blockItem)) {
            return;
        }
        MachineTier tier = MachineTier.of(blockItem.getBlock());
        if (tier.isBase()) {
            return;
        }
        List<Component> tooltip = event.getToolTip();
        Component line = Component.translatable(MachineTier.TOOLTIP_KEY, tier.getDisplayName()).withStyle(ChatFormatting.GRAY);
        tooltip.add(Math.min(1, tooltip.size()), line);
    }
}
