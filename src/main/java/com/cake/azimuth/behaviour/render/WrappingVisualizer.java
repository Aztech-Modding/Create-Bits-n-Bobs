package com.cake.azimuth.behaviour.render;

import com.cake.azimuth.behaviour.AzimuthSmartBlockEntityExtension;
import com.cake.azimuth.behaviour.SuperBlockEntityBehaviour;
import com.cake.azimuth.behaviour.extensions.RenderedBehaviourExtension;
import com.cake.azimuth.registration.VisualWrapperInterest;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import dev.engine_room.flywheel.api.visual.BlockEntityVisual;
import dev.engine_room.flywheel.api.visualization.BlockEntityVisualizer;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A wrapper around a {@link BlockEntityVisualizer} that adds support for rendering {@link RenderedBehaviourExtension}s.
 * This is only used when necessary, according to the {@link VisualWrapperInterest}.
 * The wrapper is reused when targeting a visual multiple times to avoid unnecessary allocations.
 */
public class WrappingVisualizer<T extends BlockEntity> implements BlockEntityVisualizer<T> {
    private static final HashMap<BlockEntityVisualizer<?>, WrappingVisualizer<?>> WRAPPERS_BY_DELEGATE = new HashMap<>();

    private final BlockEntityVisualizer<? super T> delegate;

    public WrappingVisualizer(final BlockEntityVisualizer<? super T> delegate) {
        this.delegate = delegate;
    }

    @SuppressWarnings("unchecked")
    public static <T extends BlockEntity> BlockEntityVisualizer<? super T> getWrapping(@Nullable final BlockEntityVisualizer<? super T> delegate) {
        if (delegate == null || delegate instanceof WrappingVisualizer<?>) {
            return delegate;
        }
        final WrappingVisualizer<?> visualizer = WRAPPERS_BY_DELEGATE.computeIfAbsent(delegate, WrappingVisualizer::new);
        if (visualizer.delegate != delegate) {
            throw new IllegalStateException("Inconsistent delegate mapping for " + delegate);
        }
        return (BlockEntityVisualizer<? super T>) visualizer;
    }

    @Override
    public BlockEntityVisual<? super T> createVisual(final VisualizationContext ctx, final T blockEntity, final float partialTick) {
        final BlockEntityVisual<? super T> delegateVisual = delegate.createVisual(ctx, blockEntity, partialTick);
        if (!(blockEntity instanceof final SmartBlockEntity smartBe) || !(smartBe instanceof final AzimuthSmartBlockEntityExtension azimuthBE)) {
            return delegateVisual;
        }
        if (!(delegateVisual instanceof final AbstractBlockEntityVisual<?> parentVisual)) {
            return delegateVisual;
        }

        final ArrayList<RenderedBehaviourExtension.BehaviourVisual> behaviourVisuals = new ArrayList<>();
        for (final RenderedBehaviourExtension behaviour : azimuthBE.azimuth$getRenderedExtensionCache()) {
            final RenderedBehaviourExtension.BehaviourVisualFactory factory = behaviour.getVisualFactory();
            if (factory == null || !(behaviour instanceof final SuperBlockEntityBehaviour superBehaviour)) {
                continue;
            }
            final RenderedBehaviourExtension.BehaviourVisual behaviourVisual = factory.create(ctx, superBehaviour, smartBe, parentVisual, partialTick);
            if (behaviourVisual != null) {
                behaviourVisuals.add(behaviourVisual);
            }
        }

        if (behaviourVisuals.isEmpty()) {
            return delegateVisual;
        }

        return new WrappedVisual<>(delegateVisual, behaviourVisuals);
    }

    @Override
    public boolean skipVanillaRender(final T blockEntity) {
        if (!(blockEntity instanceof final AzimuthSmartBlockEntityExtension azimuthBE)) {
            return delegate.skipVanillaRender(blockEntity);
        }

        for (final RenderedBehaviourExtension behaviour : azimuthBE.azimuth$getRenderedExtensionCache()) {
            if (behaviour.rendersWhenVisualizationAvailable())
                return false;
        }

        return delegate.skipVanillaRender(blockEntity);
    }

}
