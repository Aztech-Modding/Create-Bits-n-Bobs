package com.kipti.bnb.mixin.azimuth;

import com.cake.azimuth.behaviour.AzimuthSmartBlockEntityExtension;
import com.cake.azimuth.behaviour.extensions.RenderedBehaviourExtension;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import dev.engine_room.flywheel.lib.visualization.VisualizationHelper;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VisualizationHelper.class)
public class VisualizationHelperMixin {
    @Inject(method = "skipVanillaRender(Lnet/minecraft/world/level/block/entity/BlockEntity;)Z", at = @At("HEAD"), cancellable = true)
    private static void azimuth$allowVanillaRenderForForcedBehaviours(final BlockEntity blockEntity, final CallbackInfoReturnable<Boolean> cir) {
        if (blockEntity instanceof final SmartBlockEntity smartBe && smartBe instanceof final AzimuthSmartBlockEntityExtension azimuthBE) {
            for (final RenderedBehaviourExtension behaviour : azimuthBE.azimuth$getRenderedExtensionCache()) {
                if (behaviour.shouldAlwaysActivateRenderer()) {
                    cir.setReturnValue(false);
                    return;
                }
            }
        }
    }
}
