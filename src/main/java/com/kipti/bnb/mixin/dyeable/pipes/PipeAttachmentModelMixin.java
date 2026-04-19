package com.kipti.bnb.mixin.dyeable.pipes;

import com.kipti.bnb.content.decoration.dyeable.pipes.DyeablePipeBehaviour;
import com.kipti.bnb.content.decoration.dyeable.pipes.DyeablePipeModelHelper;
import com.simibubi.create.content.fluids.PipeAttachmentModel;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(PipeAttachmentModel.class)
public class PipeAttachmentModelMixin {

    @Inject(method = "gatherModelData", at = @At("TAIL"))
    private void bnb$gatherDyeColor(
            final ModelData.Builder builder,
            final BlockAndTintGetter world,
            final BlockPos pos,
            final BlockState state,
            final ModelData blockEntityData,
            final CallbackInfoReturnable<ModelData.Builder> cir
    ) {
        final DyeablePipeBehaviour behaviour = BlockEntityBehaviour.get(world, pos, DyeablePipeBehaviour.TYPE);
        if (behaviour != null && behaviour.getColor() != null) {
            builder.with(DyeablePipeModelHelper.PIPE_DYE_COLOR, behaviour.getColor());
        }
    }

    @Inject(method = "getQuads", at = @At("RETURN"), cancellable = true)
    private void bnb$applyDyeSpriteShift(
            final BlockState state,
            final Direction side,
            final RandomSource rand,
            final ModelData data,
            final RenderType renderType,
            final CallbackInfoReturnable<List<BakedQuad>> cir
    ) {
        final var color = DyeablePipeModelHelper.getDyeColor(data);
        if (color == null) {
            return;
        }
        cir.setReturnValue(DyeablePipeModelHelper.shiftQuads(cir.getReturnValue(), color));
    }

}
