package com.kipti.bnb.foundation.caching;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

public class LevelSimpleCache<K, V> {

    /**
     * Caches for invalidation on level unload, and tick expiration
     */
    public static final List<LevelSimpleCache<?, ?>> ALL_CACHES = new CopyOnWriteArrayList<>();
    private final Supplier<SimpleCache<K, V>> cacheFactory;
    private final Map<ResourceKey<Level>, SimpleCache<K, V>> cacheByLevel;

    public LevelSimpleCache(final Supplier<SimpleCache<K, V>> cacheFactory) {
        ALL_CACHES.add(this);
        this.cacheFactory = cacheFactory;
        this.cacheByLevel = new ConcurrentHashMap<>();
    }

    public SimpleCache<K, V> getCacheForLevel(final Level level) {
        final ResourceKey<Level> key = level.dimension();
        return cacheByLevel.computeIfAbsent(key, ignored -> cacheFactory.get());
    }

    public void clearForLevel(final Level level) {
        cacheByLevel.remove(level.dimension());
    }

    @EventBusSubscriber
    public static class CacheInvalidationEvents {
        @SubscribeEvent
        public static void onLevelUnload(final LevelEvent.Unload event) {
            if (!event.getLevel().isClientSide() && event.getLevel() instanceof final Level level) {
                for (final LevelSimpleCache<?, ?> cache : ALL_CACHES) {
                    cache.clearForLevel(level);
                }
            }
        }
    }


}
