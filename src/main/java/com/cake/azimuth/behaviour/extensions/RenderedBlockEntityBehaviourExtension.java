package com.cake.azimuth.behaviour.extensions;

import com.cake.azimuth.behaviour.IBehaviourExtension;
import com.cake.azimuth.behaviour.render.BlockEntityBehaviourRenderer;
import com.simibubi.create.foundation.blockEntity.CachedRenderBBBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * Used to designate a block entity behaviour as one that includes additional block entity rendering.
 * This interface provides said renderer, as well as an interface to extend features such as render bounds.
 */
public interface RenderedBlockEntityBehaviourExtension<T extends SmartBlockEntity> extends IBehaviourExtension {

    Supplier<Supplier<BlockEntityBehaviourRenderer<T>>> getRenderer();

    /**
     * Returns the additional AABB render bounds required for this block entity behaviour. By default, this is null, which will not expand the render bounds.
     * Note that adding this behaviour in a deferred manner may require a call to {@link CachedRenderBBBlockEntity#invalidateRenderBoundingBox()}
     * to update the client side bounding box.
     */
    default @Nullable AABB getRenderBoundingBox() {
        return null;
    }

}
