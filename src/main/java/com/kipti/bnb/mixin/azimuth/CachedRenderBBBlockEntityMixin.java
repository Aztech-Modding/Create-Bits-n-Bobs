package com.kipti.bnb.mixin.azimuth;

import com.cake.azimuth.behaviour.AzimuthSmartBlockEntityExtension;
import com.cake.azimuth.behaviour.extensions.RenderedBehaviourExtension;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.foundation.blockEntity.CachedRenderBBBlockEntity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CachedRenderBBBlockEntity.class)
public class CachedRenderBBBlockEntityMixin {

    @WrapOperation(method = "getRenderBoundingBox", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/foundation/blockEntity/CachedRenderBBBlockEntity;createRenderBoundingBox()Lnet/minecraft/world/phys/AABB;"))
    private AABB azimuth$includeAdditionalRenderBounds(final CachedRenderBBBlockEntity instance, final Operation<AABB> original) {
        AABB originalBox = original.call(instance);
        if (this instanceof final AzimuthSmartBlockEntityExtension asbee) {
            for (final RenderedBehaviourExtension behaviour : asbee.azimuth$getRenderedExtensionCache()) {
                final AABB renderBoundingBox = behaviour.getRenderBoundingBox();
                if (renderBoundingBox != null)
                    originalBox = originalBox.minmax(renderBoundingBox);
            }
        }
        return originalBox;
    }

}
