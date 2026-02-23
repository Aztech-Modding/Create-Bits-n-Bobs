package com.kipti.bnb.mixin.azimuth;

import com.cake.azimuth.behaviour.AzimuthSmartBlockEntityExtension;
import com.cake.azimuth.behaviour.SuperBlockEntityBehaviour;
import com.cake.azimuth.behaviour.extensions.RenderedBehaviourExtension;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SafeBlockEntityRenderer.class)
public class SafeBlockEntityRendererMixin<T extends BlockEntity> {

    @Inject(method = "render", at = @At("TAIL"))
    protected void azimuth$renderBehaviours(T blockEntity, float partialTicks, PoseStack ms, MultiBufferSource bufferSource, int light, int overlay, CallbackInfo ci) {
        if (blockEntity instanceof SmartBlockEntity smartBe && smartBe instanceof AzimuthSmartBlockEntityExtension azimuthBE) {
            for (RenderedBehaviourExtension behaviour : azimuthBE.azimuth$getRenderedExtensionCache()) {
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
