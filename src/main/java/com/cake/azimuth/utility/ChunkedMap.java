package com.cake.azimuth.utility;

import net.minecraft.world.level.ChunkPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Map of ChunkPos to objects, an object may exist in multiple chunks at one time
 */
public class ChunkedMap<T extends ChunkedMap.IChunkedObject> {

    private final Map<ChunkPos, List<T>> map = new HashMap<>();

    public void add(final T object) {
        for (final ChunkPos chunk : object.getChunks()) {
            map.computeIfAbsent(chunk, c -> new ArrayList<>()).add(object);
        }
    }

    public List<T> get(final ChunkPos chunk) {
        return map.getOrDefault(chunk, List.of());
    }

    public void remove(final T object) {
        for (final ChunkPos chunk : object.getChunks()) {
            final List<T> list = map.get(chunk);
            if (list != null) {
                list.remove(object);
                if (list.isEmpty()) {
                    map.remove(chunk);
                }
            }
        }
    }

    public interface IChunkedObject {
        /**
         * Returns the chunks this object is in. THIS MUST BE FINAL
         */
        ChunkPos[] getChunks();
    }
}
