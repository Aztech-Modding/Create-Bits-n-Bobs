package com.kipti.bnb.mixin.azimuth;

import com.cake.azimuth.behaviour.AzimuthSmartBlockEntityExtension;
import com.cake.azimuth.behaviour.SuperBlockEntityBehaviour;
import com.cake.azimuth.behaviour.extensions.RenderedBehaviourExtension;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SafeBlockEntityRenderer.class)
public class SafeBlockEntityRendererMixin<T extends BlockEntity> {

    @Inject(method = "render", at = @At("TAIL"))
    protected void azimuth$renderBehaviours(final T blockEntity, final float partialTicks, final PoseStack ms, final MultiBufferSource bufferSource, final int light, final int overlay, final CallbackInfo ci) {
        if (blockEntity instanceof final SmartBlockEntity smartBe && smartBe instanceof final AzimuthSmartBlockEntityExtension azimuthBE) {
            final boolean visualizationActive = smartBe.getLevel() != null && VisualizationManager.supportsVisualization(smartBe.getLevel());
            for (final RenderedBehaviourExtension behaviour : azimuthBE.azimuth$getRenderedExtensionCache()) {
                if (visualizationActive && !behaviour.rendersWhenVisualizationAvailable()) {
                    continue;
                }
                behaviour.getRenderer().get().get().castRenderSafe(
                        (SuperBlockEntityBehaviour) behaviour,
                        (SmartBlockEntity) blockEntity,
                        partialTicks,
                        ms,
                        bufferSource,
                        light,
                        overlay
                );
            }
        }

    }

}
