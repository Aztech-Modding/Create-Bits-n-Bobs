package com.kipti.bnb.foundation.caching;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class SimpleCache<K, V> {

    private final Map<K, V> internalMap;
    private final int capacity;
    private final Predicate<V> validator;

    public SimpleCache(final int capacity, final Predicate<V> validator) {
        this.capacity = capacity;

        this.internalMap = new LinkedHashMap<>(capacity, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(final Map.Entry<K, V> eldest) {
                return size() > SimpleCache.this.capacity;
            }
        };
        this.validator = validator;
    }

    public void put(final K key, final V value) {
        synchronized (internalMap) {
            internalMap.put(key, value);
        }
    }

    /**
     * Get with a Validator.
     * Checks if the value is valid before returning it.
     *
     * @param key The key to look up.
     * @return The value if it exists AND is valid; otherwise null.
     */
    public V get(final K key) {
        synchronized (internalMap) {
            final V value = internalMap.get(key);

            if (value == null) {
                return null;
            }

            if (!validator.test(value)) {
                internalMap.remove(key);
                return null;
            }

            return value;
        }
    }

    public void remove(final K key) {
        synchronized (internalMap) {
            internalMap.remove(key);
        }
    }

    public void clear() {
        synchronized (internalMap) {
            internalMap.clear();
        }
    }

    public int size() {
        synchronized (internalMap) {
            return internalMap.size();
        }
    }

    public void forEach(final BiConsumer<K, V> action) {
        synchronized (internalMap) {
            internalMap.forEach(action);
        }
    }
}
