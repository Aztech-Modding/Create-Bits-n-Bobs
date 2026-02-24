package com.kipti.bnb.mixin.azimuth;

import com.cake.azimuth.behaviour.render.WrappedVisualizer;
import com.cake.azimuth.registration.RenderedBehaviourInterest;
import com.cake.azimuth.registration.RenderedBehaviourWrapPlan;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.engine_room.flywheel.api.internal.FlwApiLink;
import dev.engine_room.flywheel.api.visualization.BlockEntityVisualizer;
import dev.engine_room.flywheel.api.visualization.VisualizerRegistry;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(VisualizerRegistry.class)
public class VisualizerRegistryMixin {

    @WrapOperation(
            method = "setVisualizer(Lnet/minecraft/world/level/block/entity/BlockEntityType;Ldev/engine_room/flywheel/api/visualization/BlockEntityVisualizer;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Ldev/engine_room/flywheel/api/internal/FlwApiLink;setVisualizer(Lnet/minecraft/world/level/block/entity/BlockEntityType;Ldev/engine_room/flywheel/api/visualization/BlockEntityVisualizer;)V"
            )
    )
    private static <T extends BlockEntity> void azimuth$wrapInterestedVisualizer(final FlwApiLink instance, final BlockEntityType<T> type, final BlockEntityVisualizer<? super T> visualizer, final Operation<Void> original) {
        final RenderedBehaviourWrapPlan plan = RenderedBehaviourInterest.getPlan(type);
        if (plan == null || !plan.wrapVisual()) {
            original.call(instance, type, visualizer);
            return;
        }
        final BlockEntityVisualizer<? super T> wrapped = WrappedVisualizer.wrap(type, visualizer, plan);
        original.call(instance, type, wrapped);
    }
}
