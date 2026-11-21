package com.snoopycorp.amcore.kubejs;

import com.snoopycorp.amcore.damage.AMDamageTypes;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;

public class BurningLiquidBlock extends LiquidBlock {
    private final float burnSeconds;
    private final float burnDamage;

    public BurningLiquidBlock(FlowingFluid fluid, Properties properties, float burnSeconds, float burnDamage) {
        super(fluid, properties);
        this.burnSeconds = burnSeconds;
        this.burnDamage = burnDamage;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide() || entity.fireImmune() || entity.getFluidTypeHeight(fluid.getFluidType()) <= 0) {
            return;
        }
        entity.igniteForSeconds(burnSeconds);
        if (entity.hurt(entity.damageSources().source(AMDamageTypes.MOLTEN), burnDamage)) {
            entity.playSound(SoundEvents.GENERIC_BURN, 0.4F, 2.0F + level.getRandom().nextFloat() * 0.4F);
        }
    }
}
