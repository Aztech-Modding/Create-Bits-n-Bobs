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
import java.util.function.Function;
import java.util.function.Supplier;

public class LevelSafeStorage<T> {

    public static final List<LevelSafeStorage<?>> ALL_STORAGES = new CopyOnWriteArrayList<>();
    private final Supplier<T> storageFactory;
    private final Function<Level, T> levelAwareStorageFactory;
    private final Map<ResourceKey<Level>, T> storageByLevel;

    public LevelSafeStorage(final Supplier<T> storageFactory) {
        ALL_STORAGES.add(this);
        this.storageFactory = storageFactory;
        this.levelAwareStorageFactory = null;
        this.storageByLevel = new ConcurrentHashMap<>();
    }

    public LevelSafeStorage(final Function<Level, T> levelAwareStorageFactory) {
        ALL_STORAGES.add(this);
        this.storageFactory = null;
        this.levelAwareStorageFactory = levelAwareStorageFactory;
        this.storageByLevel = new ConcurrentHashMap<>();
    }

    public T getForLevel(final Level level) {
        final ResourceKey<Level> key = level.dimension();
        return storageByLevel.computeIfAbsent(key, ignored -> {
            if (levelAwareStorageFactory != null) {
                return levelAwareStorageFactory.apply(level);
            }
            return storageFactory.get();
        });
    }

    public void clearForLevel(final Level level) {
        storageByLevel.remove(level.dimension());
    }

    @EventBusSubscriber
    public static class StorageInvalidationEvents {
        @SubscribeEvent
        public static void onLevelUnload(final LevelEvent.Unload event) {
            if (!event.getLevel().isClientSide() && event.getLevel() instanceof final Level level) {
                for (final LevelSafeStorage<?> storage : ALL_STORAGES) {
                    storage.clearForLevel(level);
                }
            }
        }
    }
}

