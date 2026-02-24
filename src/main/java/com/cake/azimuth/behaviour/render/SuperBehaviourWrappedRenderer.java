package com.cake.azimuth.behaviour.render;

import com.cake.azimuth.behaviour.AzimuthSmartBlockEntityExtension;
import com.cake.azimuth.behaviour.SuperBlockEntityBehaviour;
import com.cake.azimuth.behaviour.extensions.RenderedBehaviourExtension;
import com.cake.azimuth.registration.RenderedBehaviourWrapPlan;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class SuperBehaviourWrappedRenderer<T extends BlockEntity> implements BlockEntityRenderer<T> {
    private final BlockEntityRenderer<T> delegate;
    private final RenderedBehaviourWrapPlan wrapPlan;

    public SuperBehaviourWrappedRenderer(final BlockEntityRenderer<T> delegate, final BlockEntityType<?> blockEntityType, final RenderedBehaviourWrapPlan wrapPlan) {
        this.delegate = delegate;
        this.wrapPlan = wrapPlan;
    }

    public static <T extends BlockEntity> BlockEntityRendererProvider<T> wrapProvider(final BlockEntityType<? extends T> type,
                                                                                      final BlockEntityRendererProvider<T> provider,
                                                                                      final RenderedBehaviourWrapPlan wrapPlan) {
        if (provider instanceof WrappedRendererProvider<?>) {
            return provider;
        }
        return new WrappedRendererProvider<>(provider, type, wrapPlan);
    }

    @Override
    public void render(final @NotNull T blockEntity, final float partialTicks, final @NotNull PoseStack poseStack, final @NotNull MultiBufferSource bufferSource, final int light, final int overlay) {
        delegate.render(blockEntity, partialTicks, poseStack, bufferSource, light, overlay);
        if (!wrapPlan.wrapRenderer()) {
            return;
        }
        if (!(blockEntity instanceof final com.simibubi.create.foundation.blockEntity.SmartBlockEntity smartBe) || !(smartBe instanceof final AzimuthSmartBlockEntityExtension azimuthBE)) {
            return;
        }
        final boolean visualizationActive = blockEntity.getLevel() != null && VisualizationManager.supportsVisualization(blockEntity.getLevel());
        for (final RenderedBehaviourExtension behaviour : azimuthBE.azimuth$getRenderedExtensionCache()) {
            final boolean keepRendererActive = behaviour.shouldAlwaysActivateRenderer();
            if (visualizationActive && !keepRendererActive) {
                continue;
            }

            behaviour.getRenderer().get().get().castRenderSafe(
                    (SuperBlockEntityBehaviour) behaviour,
                    smartBe,
                    partialTicks,
                    poseStack,
                    bufferSource,
                    light,
                    overlay
            );
        }
    }

    @Override
    public boolean shouldRenderOffScreen(final @NotNull T blockEntity) {
        return delegate.shouldRenderOffScreen(blockEntity);
    }

    @Override
    public int getViewDistance() {
        return delegate.getViewDistance();
    }

    @Override
    public boolean shouldRender(final @NotNull T blockEntity, final @NotNull Vec3 cameraPos) {
        return delegate.shouldRender(blockEntity, cameraPos);
    }

    private record WrappedRendererProvider<T extends BlockEntity>(BlockEntityRendererProvider<T> delegate,
                                                                  BlockEntityType<? extends T> type,
                                                                  RenderedBehaviourWrapPlan wrapPlan) implements BlockEntityRendererProvider<T> {
        @Override
        public @NotNull BlockEntityRenderer<T> create(final @NotNull Context context) {
            return new SuperBehaviourWrappedRenderer<>(delegate.create(context), type, wrapPlan);
        }
    }
}
