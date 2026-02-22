package com.cake.azimuth.behaviour;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class CachedBehaviourExtensionAccess<T> {

    private Object[] behaviourCache;
    private final Supplier<Object> blockEntitySupplier;
    private final Predicate<SuperBlockEntityBehaviour> filter;

    public CachedBehaviourExtensionAccess(Supplier<Object> blockEntitySupplier, Predicate<SuperBlockEntityBehaviour> filter) {
        this.blockEntitySupplier = blockEntitySupplier;
        this.filter = filter;
    }

    public T[] get() {
        if (behaviourCache != null)
            return (T[]) behaviourCache;

        Object blockEntity = blockEntitySupplier.get();
        if (!(blockEntity instanceof AzimuthSmartBlockEntityExtension azebe)) {
            return (T[]) new IBehaviourExtension[0];
        }

        azebe.azimuth$addCacheClearListener(() -> behaviourCache = null);

        List<T> behaviours = azebe.azimuth$getExtensionBehavioursCache(filter);
        behaviourCache = behaviours.toArray(new Object[0]);
        return (T[]) behaviourCache;
    }

}
