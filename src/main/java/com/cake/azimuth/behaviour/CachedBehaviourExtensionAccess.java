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

    public CachedBehaviourExtensionAccess(Class<T> type, Supplier<Object> blockEntitySupplier, Predicate<SuperBlockEntityBehaviour> filter) {
        this.type = type;
        this.blockEntitySupplier = blockEntitySupplier;
        this.filter = filter;
    }

    public T[] get() {
        if (behaviourCache != null) {
            return behaviourCache;
        }

        Object blockEntity = blockEntitySupplier.get();
        if (!(blockEntity instanceof AzimuthSmartBlockEntityExtension azebe)) {
            // Create a safe empty array of type T
            return (T[]) Array.newInstance(type, 0);
        }

        azebe.azimuth$addCacheClearListener(() -> behaviourCache = null);

        List<T> behaviours = azebe.azimuth$searchExtensionBehaviours(filter);

        // Use Array.newInstance to create a physically correct T[] array
        T[] array = (T[]) Array.newInstance(type, behaviours.size());
        behaviourCache = behaviours.toArray(array);

        return behaviourCache;
    }
}