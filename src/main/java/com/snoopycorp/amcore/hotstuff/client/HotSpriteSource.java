package com.snoopycorp.amcore.hotstuff.client;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.serialization.MapCodec;
import com.snoopycorp.amcore.AMCore;
import com.snoopycorp.amcore.hotstuff.HotState;
import com.snoopycorp.amcore.hotstuff.HotStuffItem;

import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.FastColor;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;

/**
 * Block atlas source adding, for every hot item, a heated copy of each texture of its model. {@link HotItemModel}
 * then draws the item with these copies.
 *
 * <p>Flat item textures glow from the lower edge of their silhouette, higher and brighter as the item gets hotter,
 * with a fire palette (or a palette built from the script color) following the shading of the texture. Textures of
 * other models (blocks, 3D items) are blended toward the item color, since their lower edge has no meaning.
 */
public final class HotSpriteSource implements SpriteSource {
    public static final ResourceLocation ID = AMCore.id("hot_stuff");
    public static final HotSpriteSource INSTANCE = new HotSpriteSource();
    public static final SpriteSourceType TYPE = new SpriteSourceType(MapCodec.unit(INSTANCE));

    private static final FileToIdConverter MODEL_ID_CONVERTER = FileToIdConverter.json("models");
    private static final int MAX_DEPTH = 32;
    /** Fire palette used when the script gives no color, from deep red to white. */
    private static final Palette FIRE = new Palette(
            new float[]{0F, 0.15F, 0.3F, 0.45F, 0.6F, 0.72F, 0.85F, 0.95F, 1F},
            new int[]{0x7A1406, 0xFF2A12, 0xFF5F1B, 0xFF7720, 0xFFA34B, 0xFFBA61, 0xFEE18F, 0xFFF6DA, 0xFFFFFF});

    private HotSpriteSource() {
    }

    /**
     * Colors of the glow along a position from 0 (darkest) to 1 (white hot).
     */
    private record Palette(float[] positions, int[] colors) {
        /**
         * Palette going from a dark shade of {@code color} through {@code color} up to white.
         */
        static Palette of(int color) {
            return new Palette(new float[]{0F, 0.45F, 0.8F, 1F}, new int[]{darken(color), color, lerp(color, 0xFFF6DA, 0.6F), 0xFFFFFF});
        }

        int sample(float position) {
            for (int i = 1; i < positions.length; i++) {
                if (position <= positions[i]) {
                    return lerp(colors[i - 1], colors[i], (position - positions[i - 1]) / (positions[i] - positions[i - 1]));
                }
            }

            return colors[colors.length - 1];
        }
    }

    /**
     * How a texture is heated.
     */
    public enum Style {
        /** Fire glow rising from the lower edge of the silhouette, for flat item textures. */
        GLOW,
        /** Whole texture blended toward the item color. */
        TINT
    }

    /**
     * Id of the heated copy of {@code texture}, for the given state and color set by the script ({@code null} when
     * the script gives none).
     */
    public static ResourceLocation heatedSprite(ResourceLocation texture, Style style, HotState state, @Nullable Integer customColor) {
        String color = customColor == null ? "default" : "%06x".formatted(customColor & 0xFFFFFF);
        return AMCore.id("hot/%s/%s_%s/%s/%s".formatted(style.name().toLowerCase(Locale.ROOT), state.getSerializedName(), color,
                texture.getNamespace(), texture.getPath()));
    }

    @Override
    public void run(ResourceManager resourceManager, Output output) {
        Set<ResourceLocation> added = new HashSet<>();

        for (Item item : BuiltInRegistries.ITEM) {
            if (!(item instanceof HotStuffItem hot)) {
                continue;
            }

            HotState state = hot.getState();
            Integer customColor = hot.getCustomColor();
            int color = hot.getColor();
            ModelTextures model = collectTextures(resourceManager, BuiltInRegistries.ITEM.getKey(item).withPrefix("item/"));
            Style style = model.flat() ? Style.GLOW : Style.TINT;

            for (ResourceLocation texture : model.textures()) {
                ResourceLocation sprite = heatedSprite(texture, style, state, customColor);

                if (!added.add(sprite)) {
                    continue;
                }

                resourceManager.getResource(TEXTURE_ID_CONVERTER.idToFile(texture)).ifPresent(resource -> output.add(sprite, loader -> {
                    SpriteContents contents = loader.loadSprite(sprite, resource);

                    if (contents != null) {
                        heat(contents, style, state, customColor, color);
                    }

                    return contents;
                }));
            }
        }
    }

    @Override
    public SpriteSourceType type() {
        return TYPE;
    }

    private static void heat(SpriteContents contents, Style style, HotState state, @Nullable Integer customColor, int color) {
        NativeImage image = contents.getOriginalImage();

        if (style == Style.TINT) {
            tint(image, color, state.getBlendStrength());
            return;
        }

        Palette palette = customColor == null ? FIRE : Palette.of(customColor);
        // Each frame of an animated texture is heated on its own.
        int width = contents.width();
        int height = contents.height();

        for (int frameY = 0; frameY + height <= image.getHeight(); frameY += height) {
            for (int frameX = 0; frameX + width <= image.getWidth(); frameX += width) {
                glow(image, frameX, frameY, width, height, state, palette);
            }
        }
    }

    private static void tint(NativeImage image, int color, float strength) {
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = image.getPixelRGBA(x, y);

                if (FastColor.ABGR32.alpha(pixel) != 0) {
                    image.setPixelRGBA(x, y, withAlpha(lerp(rgb(pixel), color, strength), pixel));
                }
            }
        }
    }

    /**
     * Turns the lower part of the silhouette into fire. The glow rises from the lowest opaque pixel of each column
     * over the coverage of the state and fades smoothly into the original texture. Along the palette, pixels go
     * further as they are lower and lighter: shadows stay deep red while highlights reach yellow or white. The outer
     * rim of the silhouette is heated less, so that it keeps outlining the item.
     */
    private static void glow(NativeImage image, int x0, int y0, int width, int height, HotState state, Palette palette) {
        int[] bottom = new int[width];
        int top = height;
        int lowest = -1;
        float darkest = 1;
        float lightest = 0;

        for (int x = 0; x < width; x++) {
            bottom[x] = -1;

            for (int y = 0; y < height; y++) {
                int pixel = image.getPixelRGBA(x0 + x, y0 + y);

                if (FastColor.ABGR32.alpha(pixel) != 0) {
                    bottom[x] = y;
                    top = Math.min(top, y);
                    lowest = Math.max(lowest, y);
                    float luminance = luminance(pixel);
                    darkest = Math.min(darkest, luminance);
                    lightest = Math.max(lightest, luminance);
                }
            }
        }

        if (lowest < 0) {
            return;
        }

        float silhouette = lowest - top + 1;
        float coverage = state.getGlowCoverage();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y <= bottom[x]; y++) {
                int pixel = image.getPixelRGBA(x0 + x, y0 + y);

                if (FastColor.ABGR32.alpha(pixel) == 0) {
                    continue;
                }

                float depth = 1 - (bottom[x] - y) / silhouette;
                float heat = Mth.clamp((depth - (1 - coverage)) / coverage, 0, 1);

                if (heat <= 0) {
                    continue;
                }

                float shade = lightest > darkest ? (luminance(pixel) - darkest) / (lightest - darkest) : 0.5F;
                boolean rim = isRim(image, x0, y0, width, height, x, y);
                float position = Mth.clamp(heat * state.getGlowIntensity() * (rim ? 0.3F + 0.7F * shade : 0.45F + 0.55F * shade), 0, 1);
                float strength = smoothstep(heat / state.getGlowFade()) * (0.55F + 0.45F * heat) * (rim ? 0.5F + 0.5F * shade : 1);
                image.setPixelRGBA(x0 + x, y0 + y, withAlpha(lerp(rgb(pixel), palette.sample(position), strength), pixel));
            }
        }
    }

    /**
     * Whether the pixel at ({@code x}, {@code y}) of the frame touches a transparent pixel or the frame border.
     */
    private static boolean isRim(NativeImage image, int x0, int y0, int width, int height, int x, int y) {
        return x == 0 || y == 0 || x == width - 1 || y == height - 1
                || FastColor.ABGR32.alpha(image.getPixelRGBA(x0 + x - 1, y0 + y)) == 0
                || FastColor.ABGR32.alpha(image.getPixelRGBA(x0 + x + 1, y0 + y)) == 0
                || FastColor.ABGR32.alpha(image.getPixelRGBA(x0 + x, y0 + y - 1)) == 0
                || FastColor.ABGR32.alpha(image.getPixelRGBA(x0 + x, y0 + y + 1)) == 0;
    }

    private static float smoothstep(float value) {
        float v = Mth.clamp(value, 0, 1);
        return v * v * (3 - 2 * v);
    }

    /**
     * Darker and redder shade of {@code color}, for the shadows of the fire.
     */
    private static int darken(int color) {
        return (int) (((color >> 16) & 0xFF) * 0.55F) << 16 | (int) (((color >> 8) & 0xFF) * 0.3F) << 8 | (int) ((color & 0xFF) * 0.3F);
    }

    private static float luminance(int abgr) {
        return (FastColor.ABGR32.red(abgr) * 0.299F + FastColor.ABGR32.green(abgr) * 0.587F + FastColor.ABGR32.blue(abgr) * 0.114F) / 255;
    }

    /**
     * RGB color of an ABGR pixel.
     */
    private static int rgb(int abgr) {
        return FastColor.ABGR32.red(abgr) << 16 | FastColor.ABGR32.green(abgr) << 8 | FastColor.ABGR32.blue(abgr);
    }

    /**
     * ABGR pixel of an RGB color, with the transparency of {@code abgr}.
     */
    private static int withAlpha(int rgb, int abgr) {
        return FastColor.ABGR32.color(FastColor.ABGR32.alpha(abgr), rgb & 0xFF, (rgb >> 8) & 0xFF, (rgb >> 16) & 0xFF);
    }

    private static int lerp(int from, int to, float delta) {
        int red = Math.round(Mth.lerp(delta, (from >> 16) & 0xFF, (to >> 16) & 0xFF));
        int green = Math.round(Mth.lerp(delta, (from >> 8) & 0xFF, (to >> 8) & 0xFF));
        int blue = Math.round(Mth.lerp(delta, from & 0xFF, to & 0xFF));
        return red << 16 | green << 8 | blue;
    }

    /**
     * Textures used by a model and its parents, read from the model files, and whether the model is a flat item
     * model (built from {@code item/generated}).
     */
    private static ModelTextures collectTextures(ResourceManager resourceManager, ResourceLocation model) {
        Map<String, String> textures = new HashMap<>();
        ResourceLocation current = model;
        boolean flat = false;

        for (int depth = 0; current != null && depth < MAX_DEPTH; depth++) {
            if (current.getPath().equals("item/generated") || current.getPath().equals("builtin/generated")) {
                flat = true;
            }

            JsonObject json = readModel(resourceManager, current);

            if (json == null) {
                break;
            }

            if (json.get("textures") instanceof JsonObject map) {
                for (Map.Entry<String, JsonElement> entry : map.entrySet()) {
                    if (entry.getValue().isJsonPrimitive()) {
                        textures.putIfAbsent(entry.getKey(), entry.getValue().getAsString());
                    }
                }
            }

            current = json.has("parent") ? ResourceLocation.tryParse(GsonHelper.getAsString(json, "parent")) : null;
        }

        Set<ResourceLocation> result = new LinkedHashSet<>();

        for (String value : textures.values()) {
            for (int depth = 0; value != null && value.startsWith("#") && depth < MAX_DEPTH; depth++) {
                value = textures.get(value.substring(1));
            }

            if (value != null && !value.startsWith("#")) {
                ResourceLocation texture = ResourceLocation.tryParse(value);

                if (texture != null) {
                    result.add(texture);
                }
            }
        }

        return new ModelTextures(result, flat);
    }

    @Nullable
    private static JsonObject readModel(ResourceManager resourceManager, ResourceLocation model) {
        Resource resource = resourceManager.getResource(MODEL_ID_CONVERTER.idToFile(model)).orElse(null);

        if (resource == null) {
            return null;
        }

        try (Reader reader = resource.openAsReader()) {
            return GsonHelper.parse(reader);
        } catch (IOException | RuntimeException e) {
            AMCore.LOGGER.warn("Can't read model {} of a hot item: {}", model, e.toString());
            return null;
        }
    }

    private record ModelTextures(Set<ResourceLocation> textures, boolean flat) {
    }
}
