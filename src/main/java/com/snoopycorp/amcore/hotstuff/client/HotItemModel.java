package com.snoopycorp.amcore.hotstuff.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.snoopycorp.amcore.hotstuff.HotStuffItem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.FastColor;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.IQuadTransformer;
import net.neoforged.neoforge.client.model.QuadTransformers;
import net.neoforged.neoforge.client.model.data.ModelData;

/**
 * Model of a hot item: the model of the item, drawn with the heated textures made by {@link HotSpriteSource} and
 * lit by its heat state. A texture without a heated copy is tinted by multiplying it by the color instead.
 */
public class HotItemModel extends BakedModelWrapper<BakedModel> {
    private static final int SIDES = Direction.values().length + 1;

    private final HotStuffItem item;
    private final int color;
    private final IQuadTransformer emissivity;
    private final CachedQuads[] cache = new CachedQuads[SIDES];
    private final Map<BakedModel, HotItemModel> variants = Collections.synchronizedMap(new IdentityHashMap<>());
    private final List<BakedModel> selfPasses = List.of(this);
    private final ItemOverrides overrides = new ItemOverrides() {
        @Override
        public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
            return heat(originalModel.getOverrides().resolve(originalModel, stack, level, entity, seed));
        }
    };

    public HotItemModel(BakedModel originalModel, HotStuffItem item) {
        this(originalModel, item, QuadTransformers.settingEmissivity(item.getState().getEmissivity()));
    }

    private HotItemModel(BakedModel originalModel, HotStuffItem item, IQuadTransformer emissivity) {
        super(originalModel);
        this.item = item;
        this.color = item.getColor();
        this.emissivity = emissivity;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
        return heat(side, super.getQuads(state, side, rand));
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData extraData, @Nullable RenderType renderType) {
        return heat(side, super.getQuads(state, side, rand, extraData, renderType));
    }

    @Override
    public ItemOverrides getOverrides() {
        return overrides;
    }

    @Override
    public BakedModel applyTransform(ItemDisplayContext cameraTransformType, PoseStack poseStack, boolean applyLeftHandTransform) {
        return heat(super.applyTransform(cameraTransformType, poseStack, applyLeftHandTransform));
    }

    @Override
    public List<BakedModel> getRenderPasses(ItemStack itemStack, boolean fabulous) {
        List<BakedModel> passes = super.getRenderPasses(itemStack, fabulous);

        if (passes.size() == 1 && passes.getFirst() == originalModel) {
            return selfPasses;
        }

        return passes.stream().map(this::heat).toList();
    }

    /**
     * The given model drawn hot: this model for the wrapped one, a hot copy for any other one.
     */
    @Nullable
    private BakedModel heat(@Nullable BakedModel model) {
        if (model == null || model == this || model instanceof HotItemModel) {
            return model;
        }

        if (model == originalModel) {
            return this;
        }

        return variants.computeIfAbsent(model, m -> new HotItemModel(m, item, emissivity));
    }

    private List<BakedQuad> heat(@Nullable Direction side, List<BakedQuad> quads) {
        int index = side == null ? SIDES - 1 : side.ordinal();
        CachedQuads cached = cache[index];

        if (cached != null && cached.source() == quads) {
            return cached.result();
        }

        List<BakedQuad> result = new ArrayList<>(quads.size());

        for (BakedQuad quad : quads) {
            result.add(heat(quad));
        }

        result = List.copyOf(result);
        cache[index] = new CachedQuads(quads, result);
        return result;
    }

    private BakedQuad heat(BakedQuad quad) {
        int[] vertices = quad.getVertices().clone();
        TextureAtlasSprite sprite = quad.getSprite();
        TextureAtlasSprite heated = findSprite(sprite);

        for (int i = 0; i < 4; i++) {
            int offset = i * IQuadTransformer.STRIDE;

            if (heated != null) {
                float u = Float.intBitsToFloat(vertices[offset + IQuadTransformer.UV0]);
                float v = Float.intBitsToFloat(vertices[offset + IQuadTransformer.UV0 + 1]);
                vertices[offset + IQuadTransformer.UV0] = Float.floatToRawIntBits(heated.getU(sprite.getUOffset(u)));
                vertices[offset + IQuadTransformer.UV0 + 1] = Float.floatToRawIntBits(heated.getV(sprite.getVOffset(v)));
            } else {
                vertices[offset + IQuadTransformer.COLOR] = multiply(vertices[offset + IQuadTransformer.COLOR]);
            }
        }

        BakedQuad hot = new BakedQuad(vertices, quad.getTintIndex(), quad.getDirection(), heated != null ? heated : sprite, quad.isShade(), quad.hasAmbientOcclusion());
        emissivity.processInPlace(hot);
        return hot;
    }

    @Nullable
    private TextureAtlasSprite findSprite(TextureAtlasSprite sprite) {
        if (!sprite.atlasLocation().equals(InventoryMenu.BLOCK_ATLAS)) {
            return null;
        }

        var atlas = Minecraft.getInstance().getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS);

        for (HotSpriteSource.Style style : HotSpriteSource.Style.values()) {
            TextureAtlasSprite heated = atlas.getSprite(HotSpriteSource.heatedSprite(sprite.contents().name(), style, item.getState(), item.getCustomColor()));

            if (!heated.contents().name().equals(MissingTextureAtlasSprite.getLocation())) {
                return heated;
            }
        }

        return null;
    }

    private int multiply(int abgr) {
        return FastColor.ABGR32.color(FastColor.ABGR32.alpha(abgr),
                FastColor.ABGR32.blue(abgr) * (color & 0xFF) / 255,
                FastColor.ABGR32.green(abgr) * ((color >> 8) & 0xFF) / 255,
                FastColor.ABGR32.red(abgr) * ((color >> 16) & 0xFF) / 255);
    }

    private record CachedQuads(List<BakedQuad> source, List<BakedQuad> result) {
    }
}
