package com.kipti.bnb.content.decoration.light.headlamp.rendering.pipeline.block_entity;

import com.kipti.bnb.CreateBitsnBobs;
import com.kipti.bnb.content.decoration.light.headlamp.HeadlampBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public final class HeadlampRenderCache {

    private static final int MAX_QUAD_CACHE_ENTRIES = 512;
    private static final int MAX_TRANSFORM_CACHE_ENTRIES = 64;

    private static final ResourceLocation HEADLIGHT_TEXTURE = CreateBitsnBobs.asResource("block/headlight/headlight");
    private static final ResourceLocation HEADLIGHT_OFF_TEXTURE = CreateBitsnBobs.asResource("block/headlight/headlight_off");

    private static final LruCache<QuadCacheKey, List<BakedQuad>> QUAD_CACHE = new LruCache<>(MAX_QUAD_CACHE_ENTRIES);
    private static final LruCache<TransformKey, Matrix4f> TRANSFORM_CACHE = new LruCache<>(MAX_TRANSFORM_CACHE_ENTRIES);
    private static final Map<SpriteKey, TextureAtlasSprite> SPRITE_CACHE = new java.util.concurrent.ConcurrentHashMap<>();

    private HeadlampRenderCache() {
    }

    public static void clearCaches() {
        QUAD_CACHE.clear();
        TRANSFORM_CACHE.clear();
        SPRITE_CACHE.clear();
    }

    public static List<BakedQuad> getOrCreateQuads(final QuadCacheKey key, final Supplier<List<BakedQuad>> builder) {
        return QUAD_CACHE.computeIfAbsent(key, () -> List.copyOf(builder.get()));
    }

    public static Matrix4f getTransform(final Direction facing, final HeadlampBlockEntity.HeadlampPlacement placement) {
        final TransformKey key = new TransformKey(facing, placement.ordinal());
        return TRANSFORM_CACHE.computeIfAbsent(key, () -> new Matrix4f()
                .translation(0.5f, 0.5f, 0.5f)
                .rotate(facing.getRotation())
                .translate(-0.5f, -0.5f, -0.5f)
                .translate((float) placement.horizontalAlignment().getOffset(), 0.0f, (float) placement.verticalAlignment().getOffset()));
    }

    public static TextureAtlasSprite getTintedSprite(final TextureAtlasSprite oldSprite, @Nullable final DyeColor color) {
        if (color == null) {
            return oldSprite;
        }
        final ResourceLocation spriteName = oldSprite.contents().name();
        if (HEADLIGHT_TEXTURE.equals(spriteName)) {
            return oldSprite;
        }
        final boolean isOffTexture = HEADLIGHT_OFF_TEXTURE.equals(spriteName);
        final ResourceLocation newTexture = CreateBitsnBobs.asResource(
                "block/headlight/headlight_" + (isOffTexture ? "off" : "on") + "_" + color.getName()
        );
        return SPRITE_CACHE.computeIfAbsent(new SpriteKey(newTexture), key ->
                Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(newTexture)
        );
    }

    public record QuadCacheKey(
            Direction facing,
            int placementIndex,
            int placementValue,
            boolean onState,
            Direction side,
            RenderType renderType
    ) {
    }

    private record TransformKey(Direction facing, int placementIndex) {
    }

    private record SpriteKey(ResourceLocation texture) {
    }

    private static final class LruCache<K, V> {
        private final Map<K, V> map;

        private LruCache(final int maxEntries) {
            this.map = new LinkedHashMap<>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(final Map.Entry<K, V> eldest) {
                    return size() > maxEntries;
                }
            };
        }

        public synchronized V get(final K key) {
            return map.get(key);
        }

        public synchronized V computeIfAbsent(final K key, final Supplier<V> valueSupplier) {
            V value = map.get(key);
            if (value != null) {
                return value;
            }
            value = Objects.requireNonNull(valueSupplier.get(), "value");
            map.put(Objects.requireNonNull(key, "key"), value);
            return value;
        }

        public synchronized void put(final K key, final V value) {
            map.put(Objects.requireNonNull(key, "key"), Objects.requireNonNull(value, "value"));
        }

        public synchronized void clear() {
            map.clear();
        }
    }
}
