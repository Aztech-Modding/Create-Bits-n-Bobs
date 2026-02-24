package com.cake.azimuth.behaviour;

import java.lang.reflect.Array;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class CachedBehaviourExtensionAccess<T extends BehaviourExtension> {
    private final Class<T> type;
    private final Supplier<Object> blockEntitySupplier;
    private final Predicate<SuperBlockEntityBehaviour> filter;

    private T[] behaviourCache;

    public CachedBehaviourExtensionAccess(final Class<T> type, final Supplier<Object> blockEntitySupplier, final Predicate<SuperBlockEntityBehaviour> filter) {
        this.type = type;
        this.blockEntitySupplier = blockEntitySupplier;
        this.filter = filter;
    }

    public T[] get() {
        if (behaviourCache != null) {
            return behaviourCache;
        }

        final Object blockEntity = blockEntitySupplier.get();
        if (!(blockEntity instanceof final AzimuthSmartBlockEntityExtension azebe)) {
            // Create a safe empty array of type T
            return (T[]) Array.newInstance(type, 0);
        }

        azebe.azimuth$addCacheClearListener(() -> behaviourCache = null);

        final List<T> behaviours = azebe.azimuth$searchExtensionBehaviours(filter);

        // Use Array.newInstance to create a physically correct T[] array
        final T[] array = (T[]) Array.newInstance(type, behaviours.size());
        behaviourCache = behaviours.toArray(array);

        return behaviourCache;
    }
}