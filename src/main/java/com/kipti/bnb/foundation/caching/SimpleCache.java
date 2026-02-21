package com.kipti.bnb.foundation.caching;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class SimpleCache<K, V> {

    private final Map<K, V> internalMap;
    private final int capacity;
    private final Predicate<V> validator;

    public SimpleCache(final int capacity, Predicate<V> validator) {
        this.capacity = capacity;

        // Thread-safe LRU Map
        this.internalMap = Collections.synchronizedMap(new LinkedHashMap<K, V>(capacity, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(final Map.Entry<K, V> eldest) {
                return size() > SimpleCache.this.capacity;
            }
        });
        this.validator = validator;
    }

    public void put(final K key, final V value) {
        internalMap.put(key, value);
    }

    /**
     * Get with a Validator.
     * Checks if the value is valid before returning it.
     * * @param key The key to look up.
     *
     * @param validator A function that returns true if the value is good, false if it is bad.
     * @return The value if it exists AND is valid; otherwise null.
     */
    public V get(final K key) {
        // We must synchronize to ensure atomic check-and-remove
        synchronized (internalMap) {
            final V value = internalMap.get(key);

            if (value == null) {
                return null;
            }

            // If the value fails the test (validator returns false)
            if (!validator.test(value)) {
                internalMap.remove(key); // Auto-cleanup
                return null;
            }

            return value;
        }
    }

    public void remove(final K key) {
        internalMap.remove(key);
    }

    public void clear() {
        internalMap.clear();
    }

    public int size() {
        return internalMap.size();
    }

    public void forEach(final BiConsumer<K, V> action) {
        synchronized (internalMap) {
            internalMap.forEach(action);
        }
    }
}