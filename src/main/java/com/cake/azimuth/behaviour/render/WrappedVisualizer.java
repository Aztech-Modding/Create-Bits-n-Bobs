package com.cake.azimuth.behaviour.render;

import com.cake.azimuth.behaviour.AzimuthSmartBlockEntityExtension;
import com.cake.azimuth.behaviour.SuperBlockEntityBehaviour;
import com.cake.azimuth.behaviour.extensions.RenderedBehaviourExtension;
import com.cake.azimuth.registration.RenderedBehaviourWrapPlan;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import dev.engine_room.flywheel.api.visual.BlockEntityVisual;
import dev.engine_room.flywheel.api.visualization.BlockEntityVisualizer;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class WrappedVisualizer<T extends BlockEntity> implements BlockEntityVisualizer<T> {
    private final BlockEntityVisualizer<? super T> delegate;
    private final RenderedBehaviourWrapPlan wrapPlan;

    public WrappedVisualizer(final BlockEntityType<T> type,
                             final BlockEntityVisualizer<? super T> delegate,
                             final RenderedBehaviourWrapPlan wrapPlan) {
        this.delegate = delegate;
        this.wrapPlan = wrapPlan;
    }

    public static <T extends BlockEntity> BlockEntityVisualizer<? super T> wrap(final BlockEntityType<T> type,
                                                                                @Nullable final BlockEntityVisualizer<? super T> delegate,
                                                                                final RenderedBehaviourWrapPlan wrapPlan) {
        if (delegate == null || delegate instanceof WrappedVisualizer<?>) {
            return delegate;
        }
        return new WrappedVisualizer<>(type, delegate, wrapPlan);
    }

    @Override
    public BlockEntityVisual<? super T> createVisual(final VisualizationContext ctx, final T blockEntity, final float partialTick) {
        final BlockEntityVisual<? super T> delegateVisual = delegate.createVisual(ctx, blockEntity, partialTick);
        if (!wrapPlan.wrapVisual()) {
            return delegateVisual;
        }
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

        return new CombinedVisual<>(delegateVisual, behaviourVisuals);
    }

    @Override
    public boolean skipVanillaRender(final T blockEntity) {
        if (!(blockEntity instanceof final AzimuthSmartBlockEntityExtension azimuthBE)) {
            return delegate.skipVanillaRender(blockEntity);
        }

        for (final RenderedBehaviourExtension behaviour : azimuthBE.azimuth$getRenderedExtensionCache()) {
            if (behaviour.shouldAlwaysActivateRenderer())
                return false;
        }

        return delegate.skipVanillaRender(blockEntity);
    }

}
