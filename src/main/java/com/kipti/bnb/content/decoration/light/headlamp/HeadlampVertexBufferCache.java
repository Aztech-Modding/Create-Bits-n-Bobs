package com.kipti.bnb.content.decoration.light.headlamp;

import com.mojang.blaze3d.vertex.*;
import net.createmod.catnip.render.SuperBufferFactory;
import net.createmod.catnip.render.SuperByteBuffer;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A time-expiring cache that holds up to {@value #MAX_ENTRIES} headlamp state {@link SuperByteBuffer}s.
 * <p>
 * Each entry is keyed by the render state {@code long} from {@link HeadlampBlockEntity#getRenderStateAsLong()}.
 * Buffers are rendered facing up with no rotational or positional data baked in, so they can be reused
 * as generic instances between headlamps facing different directions.
 * <p>
 * Entries that have not been accessed within {@value #EXPIRY_TICKS} ticks are evicted.
 * All public methods are thread-safe via synchronization on the internal map.
 */
public final class HeadlampVertexBufferCache {

    private static final int MAX_ENTRIES = 64;
    private static final int EXPIRY_TICKS = 20;

    private static final Map<Long, CacheEntry> CACHE = new LinkedHashMap<>(16, 0.75f, true);

    private HeadlampVertexBufferCache() {
    }

    /**
     * Retrieves a cached {@link SuperByteBuffer} for the given render state, creating one via the
     * provided builder if absent or expired. The builder receives a {@link BufferBuilder} and should
     * emit quads in {@link VertexFormat.Mode#QUADS} with {@link DefaultVertexFormat#BLOCK} format.
     *
     * @param renderState the packed render state from {@link HeadlampBlockEntity#getRenderStateAsLong()}
     * @param builder     a consumer that populates the {@link BufferBuilder} with headlamp geometry
     * @return the cached buffer, or {@code null} if the builder produced no geometry
     */
    public static @Nullable SuperByteBuffer getOrCreate(final long renderState, final Consumer<BufferBuilder> builder) {
        //TEMP DEBUG JUST CLEAR CACHE ALWAYS
        CACHE.entrySet().removeIf(entry -> true);

        synchronized (CACHE) {
            final CacheEntry existing = CACHE.get(renderState);
            if (existing != null) {
                existing.lastAccessTick = currentTick();
                return existing.buffer;
            }
        }

        // Build outside the lock to avoid holding it during mesh construction
        final SuperByteBuffer buffer = buildBuffer(builder);

        synchronized (CACHE) {
            // Double-check; another thread may have inserted while we were building
            final CacheEntry raceEntry = CACHE.get(renderState);
            if (raceEntry != null) {
                raceEntry.lastAccessTick = currentTick();
                return raceEntry.buffer;
            }

            if (buffer != null) {
                CACHE.put(renderState, new CacheEntry(buffer, currentTick()));
                evictExcessEntries();
            }
            return buffer;
        }
    }

    /**
     * Evicts all entries that have not been accessed within {@link #EXPIRY_TICKS} ticks.
     * Should be called once per tick from the client tick event.
     */
    public static void tick() {
        synchronized (CACHE) {
            final long now = currentTick();
            CACHE.entrySet().removeIf(entry -> now - entry.getValue().lastAccessTick > EXPIRY_TICKS);
        }
    }

    /**
     * Clears all cached vertex buffers. Called on renderer invalidation.
     */
    public static void clear() {
        synchronized (CACHE) {
            CACHE.clear();
        }
    }

    /**
     * Returns the current number of cached entries. Primarily for diagnostics.
     */
    public static int size() {
        synchronized (CACHE) {
            return CACHE.size();
        }
    }

    private static @Nullable SuperByteBuffer buildBuffer(final Consumer<BufferBuilder> builder) {
        try (final ByteBufferBuilder byteBuffer = new ByteBufferBuilder(256)) {
            final BufferBuilder bufferBuilder = new BufferBuilder(byteBuffer, VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
            builder.accept(bufferBuilder);
            final MeshData meshData = bufferBuilder.build();
            if (meshData == null) {
                return null;
            }
            return SuperBufferFactory.getInstance().create(meshData);
        }
    }

    private static void evictExcessEntries() {
        final int excess = CACHE.size() - MAX_ENTRIES;
        if (excess <= 0) {
            return;
        }
        final var iterator = CACHE.keySet().iterator();
        for (int i = 0; i < excess && iterator.hasNext(); i++) {
            iterator.next();
            iterator.remove();
        }
    }

    private static long currentTick() {
        return net.createmod.catnip.animation.AnimationTickHolder.getTicks();
    }

    private static final class CacheEntry {
        final @Nullable SuperByteBuffer buffer;
        volatile long lastAccessTick;

        CacheEntry(final @Nullable SuperByteBuffer buffer, final long lastAccessTick) {
            this.buffer = buffer;
            this.lastAccessTick = lastAccessTick;
        }
    }
}
