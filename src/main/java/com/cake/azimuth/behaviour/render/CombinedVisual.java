package com.cake.azimuth.behaviour.render;

import com.cake.azimuth.behaviour.extensions.RenderedBehaviourExtension;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.task.Plan;
import dev.engine_room.flywheel.api.visual.*;
import dev.engine_room.flywheel.lib.task.RunnablePlan;
import dev.engine_room.flywheel.lib.task.UnitPlan;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/**
 * Do i like this? no.
 * But, this means i am able to make a visual with behaviours, and i dont need to create some anonymous class bs
 */
class CombinedVisual<T extends BlockEntity>
        implements BlockEntityVisual<T>, DynamicVisual, TickableVisual, LightUpdatedVisual, ShaderLightVisual {
    private final BlockEntityVisual<? super T> delegateVisual;
    private final @Nullable DynamicVisual delegateDynamic;
    private final @Nullable TickableVisual delegateTickle;
    private final @Nullable LightUpdatedVisual delegateLightUpdated;
    private final @Nullable SectionTrackedVisual delegateSectionTracked;
    private final List<RenderedBehaviourExtension.BehaviourVisual> behaviourVisuals;

    CombinedVisual(final BlockEntityVisual<? super T> delegateVisual, final List<RenderedBehaviourExtension.BehaviourVisual> behaviourVisuals) {
        this.delegateVisual = delegateVisual;
        this.behaviourVisuals = behaviourVisuals;
        this.delegateDynamic = delegateVisual instanceof final DynamicVisual dynamic ? dynamic : null;
        this.delegateTickle = delegateVisual instanceof final TickableVisual tickle ? tickle : null;
        this.delegateLightUpdated = delegateVisual instanceof final LightUpdatedVisual lightUpdated ? lightUpdated : null;
        this.delegateSectionTracked = delegateVisual instanceof final SectionTrackedVisual sectionTracked ? sectionTracked : null;
    }

    @Override
    public void update(final float partialTick) {
        delegateVisual.update(partialTick);
        for (final RenderedBehaviourExtension.BehaviourVisual behaviourVisual : behaviourVisuals) {
            behaviourVisual.update(partialTick);
        }
    }

    @Override
    public void delete() {
        for (final RenderedBehaviourExtension.BehaviourVisual behaviourVisual : behaviourVisuals) {
            behaviourVisual.delete();
        }
        delegateVisual.delete();
    }

    @Override
    public void collectCrumblingInstances(final Consumer<@Nullable Instance> consumer) {
        delegateVisual.collectCrumblingInstances(consumer);
        for (final RenderedBehaviourExtension.BehaviourVisual behaviourVisual : behaviourVisuals) {
            behaviourVisual.collectCrumblingInstances(consumer);
        }
    }

    @Override
    public Plan<DynamicVisual.Context> planFrame() {
        final Plan<DynamicVisual.Context> self = RunnablePlan.of(ctx -> {
            for (final RenderedBehaviourExtension.BehaviourVisual behaviourVisual : behaviourVisuals) {
                behaviourVisual.update(ctx.partialTick());
            }
        });
        return delegateDynamic == null ? self : self.and(delegateDynamic.planFrame());
    }

    @Override
    public Plan<TickableVisual.Context> planTick() {
        return delegateTickle == null ? UnitPlan.of() : delegateTickle.planTick();
    }

    @Override
    public void setSectionCollector(final SectionCollector collector) {
        if (delegateSectionTracked != null) {
            delegateSectionTracked.setSectionCollector(collector);
        }
    }

    @Override
    public void updateLight(final float partialTick) {
        if (delegateLightUpdated != null) {
            delegateLightUpdated.updateLight(partialTick);
        }
        for (final RenderedBehaviourExtension.BehaviourVisual behaviourVisual : behaviourVisuals) {
            behaviourVisual.updateLight(partialTick);
        }
    }
}
