package com.kipti.bnb.mixin.azimuth;

import com.cake.azimuth.behaviour.render.SuperBehaviourWrappedRenderer;
import com.cake.azimuth.registration.RenderedBehaviourInterest;
import com.cake.azimuth.registration.RenderedBehaviourWrapPlan;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockEntityRenderers.class)
public class BlockEntityRenderersMixin {
    @SuppressWarnings("unchecked")
    @WrapOperation(
            method = "register",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"
            )
    )
    private static Object azimuth$wrapInterestedRendererProvider(final java.util.Map<Object, Object> instance, final Object key, final Object value, final Operation<Object> original,
                                                                 final BlockEntityType<? extends BlockEntity> type, final BlockEntityRendererProvider<BlockEntity> renderProvider) {
        final RenderedBehaviourWrapPlan plan = RenderedBehaviourInterest.getPlan(type);
        if (plan == null || !plan.wrapRenderer()) {
            return original.call(instance, key, value);
        }
        final BlockEntityRendererProvider<BlockEntity> wrapped = SuperBehaviourWrappedRenderer.wrapProvider(type, renderProvider, plan);
        return original.call(instance, key, wrapped);
    }
}
