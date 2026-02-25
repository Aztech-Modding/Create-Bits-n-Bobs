package com.cake.azimuth.behaviour;

import org.jetbrains.annotations.ApiStatus;

import java.util.function.Supplier;

@ApiStatus.Internal
public class CachedSuperBehaviourAccess {
    private final Supplier<Object> blockEntitySupplier;

    private SuperBlockEntityBehaviour[] behaviourCache;

    public CachedSuperBehaviourAccess(final Supplier<Object> blockEntitySupplier) {
        this.blockEntitySupplier = blockEntitySupplier;
    }

    public SuperBlockEntityBehaviour[] get() {
        if (behaviourCache != null) {
            return behaviourCache;
        }

        final Object blockEntity = blockEntitySupplier.get();
        if (!(blockEntity instanceof final AzimuthSmartBlockEntityExtension asbee)) {
            // Create a safe empty array of type T
            return new SuperBlockEntityBehaviour[0];
        }

        asbee.azimuth$addCacheClearListener(() -> behaviourCache = null);

        behaviourCache = asbee.azimuth$searchSuperBehaviours();
        return behaviourCache;
    }
}