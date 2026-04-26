package com.kipti.bnb.mixin.dyeable.simple;

import com.kipti.bnb.content.decoration.dyeable.simple.SimpleDyeablePartialHelper;
import com.kipti.bnb.registry.client.BnbSpriteShifts;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.fluids.pipes.valve.FluidValveBlockEntity;
import com.simibubi.create.content.fluids.pipes.valve.FluidValveRenderer;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FluidValveRenderer.class)
public class FluidValveRendererMixin {

    @WrapOperation(
            method = "renderSafe(Lcom/simibubi/create/content/fluids/pipes/valve/FluidValveBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/createmod/catnip/render/CachedBuffers;partial(Ldev/engine_room/flywheel/lib/model/baked/PartialModel;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/createmod/catnip/render/SuperByteBuffer;"
            )
    )
    private SuperByteBuffer bnb$applyFluidValvePointerDye(
            final PartialModel partial,
            final BlockState referenceState,
            final Operation<SuperByteBuffer> original,
            final FluidValveBlockEntity be,
            final float partialTicks,
            final PoseStack ms,
            final MultiBufferSource buffer,
            final int light,
            final int overlay
    ) {
        return SimpleDyeablePartialHelper.apply(
                original.call(partial, referenceState),
                SimpleDyeablePartialHelper.getColor(be),
                BnbSpriteShifts.DYED_FLUID_VALVE
        );
    }

}
