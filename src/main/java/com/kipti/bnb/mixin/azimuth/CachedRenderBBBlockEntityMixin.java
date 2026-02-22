package com.kipti.bnb.mixin.azimuth;

import com.cake.azimuth.behaviour.CachedBehaviourExtensionAccess;
import com.cake.azimuth.behaviour.extensions.RenderedBlockEntityBehaviourExtension;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.foundation.blockEntity.CachedRenderBBBlockEntity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CachedRenderBBBlockEntity.class)
public class CachedRenderBBBlockEntityMixin {

    @Unique
    private final CachedBehaviourExtensionAccess<RenderedBlockEntityBehaviourExtension<?>> azimuth$renderedBehaviourCacheAccess =
            new CachedBehaviourExtensionAccess<>(() -> this, (e) -> e instanceof RenderedBlockEntityBehaviourExtension<?>);

    @WrapOperation(method = "getRenderBoundingBox", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/foundation/blockEntity/CachedRenderBBBlockEntity;createRenderBoundingBox()Lnet/minecraft/world/phys/AABB;"))
    private AABB azimuth$includeAdditionalRenderBounds(CachedRenderBBBlockEntity instance, Operation<AABB> original) {
        AABB originalBox = original.call(instance);
        for (RenderedBlockEntityBehaviourExtension<?> behaviour : azimuth$renderedBehaviourCacheAccess.get()) {
            AABB renderBoundingBox = behaviour.getRenderBoundingBox();
            if (renderBoundingBox != null)
                originalBox = originalBox.minmax(renderBoundingBox);
        }
        return originalBox;
    }

}
