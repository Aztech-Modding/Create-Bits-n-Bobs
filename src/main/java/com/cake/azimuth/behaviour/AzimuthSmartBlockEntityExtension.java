package com.cake.azimuth.behaviour;

import com.cake.azimuth.behaviour.extensions.ItemRequirementBehaviourExtension;
import com.cake.azimuth.behaviour.extensions.RenderedBehaviourExtension;

import java.util.List;
import java.util.function.Predicate;

/**
 * An interface resulting from azimuth's mixins to {@link com.simibubi.create.foundation.blockEntity.SmartBlockEntity}.
 * Shouldn't be necessary to use, and should be considered internal, but if necessary, allows for clearing the rendered block entity behaviour cache if the behaviours have been changed on the fly.
 * By default, the rendered block entity behaviour cache is lazily constructed, so non-deferred {@link RenderedBehaviourExtension}s should be completely fine.
 */
public interface AzimuthSmartBlockEntityExtension {

    /**
     * Updates the quick-access cache for performance adjacent block entity behaviours.
     * Only necessary for deferred behaviours.
     * Shouldn't really be used, just here whenever I get round to auto invalidation.
     */
    void azimuth$updateBehaviourExtensionCache();

    SuperBlockEntityBehaviour[] azimuth$getSuperBehaviours();

    SuperBlockEntityBehaviour[] azimuth$searchSuperBehaviours();

    <T> List<T> azimuth$searchExtensionBehaviours(Predicate<SuperBlockEntityBehaviour> filter);

    void azimuth$addCacheClearListener(Runnable cacheClearListener);

    //Non-integrated caches
    ItemRequirementBehaviourExtension[] azimuth$getItemRequirementExtensionCache();

    RenderedBehaviourExtension[] azimuth$getRenderedExtensionCache();

}
