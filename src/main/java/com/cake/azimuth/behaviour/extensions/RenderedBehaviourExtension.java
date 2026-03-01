package com.cake.azimuth.behaviour.extensions;

import com.cake.azimuth.behaviour.BehaviourExtension;
import com.cake.azimuth.behaviour.SuperBlockEntityBehaviour;
import com.cake.azimuth.behaviour.render.BlockEntityBehaviourRenderer;
import com.simibubi.create.foundation.blockEntity.CachedRenderBBBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Used to designate a block entity behaviour as one that includes additional block entity rendering.
 * This interface provides said renderer, as well as an interface to extend features such as render bounds.
 */
public interface RenderedBehaviourExtension extends BehaviourExtension {

    BehaviourRenderSupplier getRenderer();

    /**
     * Optional Flywheel visual for this behaviour. If null, no extra behaviour visual is attached.
     */
    default @Nullable BehaviourVisualFactory getVisualFactory() {
        return null;
    }

    /**
     * When true, the wrapper dispatches this behaviour's renderer even when Flywheel visualization is supported.
     * Defaults to true to keep BER active unless explicitly opted out.
     */
    default boolean rendersWhenVisualizationAvailable() {
        return true;
    }

    /**
     * Returns the additional AABB render bounds required for this block entity behaviour. By default, this is null, which will not expand the render bounds.
     * Note that adding this behaviour in a deferred manner may require a call to {@link CachedRenderBBBlockEntity#invalidateRenderBoundingBox()}
     * to update the client side bounding box.
     */
    default @Nullable AABB getRenderBoundingBox() {
        return null;
    }

    interface BehaviourRenderSupplier extends Supplier<Supplier<? extends BlockEntityBehaviourRenderer<?>>> {
    }

    interface BehaviourVisualFactory {
        @Nullable
        BehaviourVisual create(VisualizationContext context, SuperBlockEntityBehaviour behaviour, SmartBlockEntity blockEntity, AbstractBlockEntityVisual<?> parentVisual, float partialTick);
    }

    abstract class BehaviourVisual {
        protected final AbstractBlockEntityVisual<?> parentVisual;

        protected BehaviourVisual(final AbstractBlockEntityVisual<?> parentVisual) {
            this.parentVisual = parentVisual;
        }

        protected BlockPos getVisualPosition() {
            return parentVisual.getVisualPosition();
        }

        public void update(final float partialTick) {
        }

        public void updateLight(final float partialTick) {
        }

        public void collectCrumblingInstances(final Consumer<Instance> consumer) {
        }

        public abstract void delete();
    }

}
