package com.cake.azimuth.behaviour;

import java.util.function.Supplier;

public class CachedSuperBehaviourAccess {
    private final Supplier<Object> blockEntitySupplier;

    private SuperBlockEntityBehaviour[] behaviourCache;

    public CachedSuperBehaviourAccess(Supplier<Object> blockEntitySupplier) {
        this.blockEntitySupplier = blockEntitySupplier;
    }

    public SuperBlockEntityBehaviour[] get() {
        if (behaviourCache != null) {
            return behaviourCache;
        }

        Object blockEntity = blockEntitySupplier.get();
        if (!(blockEntity instanceof AzimuthSmartBlockEntityExtension azebe)) {
            // Create a safe empty array of type T
            return new SuperBlockEntityBehaviour[0];
        }

        azebe.azimuth$addCacheClearListener(() -> behaviourCache = null);

        behaviourCache = azebe.azimuth$searchSuperBehaviours();
        return behaviourCache;
    }
}