package com.snoopycorp.amcore.hotstuff;

import org.jetbrains.annotations.Nullable;

import com.snoopycorp.amcore.AMCore;
import com.snoopycorp.amcore.config.AMConfig;
import com.snoopycorp.amcore.damage.AMDamageTypes;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Burns players who carry hot items. Only the hottest item of the inventory is taken into account.
 */
@EventBusSubscriber(modid = AMCore.MODID)
public final class HotStuffDamage {
    private HotStuffDamage() {
    }

    @SubscribeEvent
    static void playerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        if (player.level().isClientSide() || !player.isAlive() || player.tickCount % AMConfig.HOT_STUFF_DAMAGE_INTERVAL.get() != 0) {
            return;
        }

        if (player.isCreative() || player.isSpectator() || player.fireImmune() || player.hasEffect(MobEffects.FIRE_RESISTANCE)) {
            return;
        }

        HotStuffItem hottest = findHottest(player);

        if (hottest == null || hottest.getTemperature() < AMConfig.HOT_STUFF_MIN_TEMPERATURE.get()) {
            return;
        }

        int temperature = hottest.getTemperature();
        float damage = (float) (temperature / AMConfig.HOT_STUFF_DEGREES_PER_DAMAGE.get());
        player.hurt(player.damageSources().source(AMDamageTypes.HOT_TOUCH), damage);

        double igniteSeconds = AMConfig.HOT_STUFF_IGNITE_SECONDS.get();

        if (igniteSeconds > 0 && temperature >= AMConfig.HOT_STUFF_IGNITE_TEMPERATURE.get()) {
            player.igniteForSeconds((float) igniteSeconds);
        }
    }

    /**
     * Hottest item among the whole inventory (main, armor and offhand slots) and the item held by the cursor.
     */
    @Nullable
    private static HotStuffItem findHottest(Player player) {
        HotStuffItem hottest = hotter(null, player.containerMenu.getCarried());
        var inventory = player.getInventory();

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            hottest = hotter(hottest, inventory.getItem(i));
        }

        return hottest;
    }

    @Nullable
    private static HotStuffItem hotter(@Nullable HotStuffItem hottest, ItemStack stack) {
        if (stack.getItem() instanceof HotStuffItem item && (hottest == null || item.getTemperature() > hottest.getTemperature())) {
            return item;
        }

        return hottest;
    }
}
