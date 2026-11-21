package com.snoopycorp.amcore.kubejs;

import dev.latvian.mods.kubejs.fluid.FluidBlockBuilder;
import dev.latvian.mods.kubejs.fluid.ThickFluidBuilder;
import dev.latvian.mods.kubejs.generator.KubeAssetGenerator;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.pathfinder.PathType;

public class MoltenFluidBuilder extends ThickFluidBuilder {
    private static final ResourceLocation STILL_TEXTURE = ResourceLocation.fromNamespaceAndPath("mysticalagradditions", "block/molten_still");
    private static final ResourceLocation FLOWING_TEXTURE = ResourceLocation.fromNamespaceAndPath("mysticalagradditions", "block/molten_flowing");
    private static final ResourceLocation BUCKET_MODEL = ResourceLocation.fromNamespaceAndPath("neoforge", "item/bucket_drip");

    private int lightLevel = 15;
    private float burnSeconds = 10;
    private float burnDamage = 4;

    public MoltenFluidBuilder(ResourceLocation id) {
        super(id);
        block = new FluidBlockBuilder(this) {
            @Override
            public Block createObject() {
                MoltenFluidBuilder molten = MoltenFluidBuilder.this;
                return new BurningLiquidBlock(fluidBuilder.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.LAVA)
                        .noLootTable()
                        .lightLevel(state -> molten.lightLevel), molten.burnSeconds, molten.burnDamage);
            }
        };
        fluidType.stillTexture(STILL_TEXTURE)
                .flowingTexture(FLOWING_TEXTURE)
                .temperature(1300)
                .lightLevel(lightLevel)
                .density(2000)
                .viscosity(10000)
                .motionScale(0.0023333333333333335D)
                .canExtinguish(false)
                .canConvertToSource(false)
                .canHydrate(false)
                .pathType(PathType.LAVA);
        slopeFindDistance(2);
        levelDecreasePerBlock(2);
        tickRate(30);
    }

    public MoltenFluidBuilder lightLevel(int lightLevel) {
        this.lightLevel = lightLevel;
        fluidType.lightLevel(lightLevel);
        return this;
    }

    public MoltenFluidBuilder burnSeconds(float burnSeconds) {
        this.burnSeconds = burnSeconds;
        return this;
    }

    public MoltenFluidBuilder burnDamage(float burnDamage) {
        this.burnDamage = burnDamage;
        return this;
    }

    @Override
    public void generateAssets(KubeAssetGenerator generator) {
        super.generateAssets(generator);
        if (bucketItem != null) {
            generator.itemModel(bucketItem.id, model -> {
                model.parent(BUCKET_MODEL);
                model.custom(json -> {
                    json.addProperty("loader", "neoforge:fluid_container");
                    json.addProperty("fluid", id.toString());
                });
            });
        }
    }
}
