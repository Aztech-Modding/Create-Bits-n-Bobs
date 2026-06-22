package com.kipti.bnb.mixin.truss_pipe;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = {"com.simibubi.create.content.fluids.pipes.FluidPipeBlockEntity$StandardPipeFluidTransportBehaviour"})
public class FluidTransportBehaviourMixin {

    @ModifyReturnValue(method = "getRenderedRimAttachment", at = @At("RETURN"))
    public FluidTransportBehaviour.AttachmentTypes bnb$getRenderedRimAttachmentHandlingTrussPipe(final FluidTransportBehaviour.AttachmentTypes original,
                                                                                                 @Local(argsOnly = true) final BlockAndTintGetter world,
                                                                                                 @Local(argsOnly = true) final BlockPos pos,
                                                                                                 @Local(argsOnly = true) final BlockState state,
                                                                                                 @Local(argsOnly = true) final Direction direction) {
//        if (original == FluidTransportBehaviour.AttachmentTypes.NONE) return original;
//
//        final BlockPos offsetPos = pos.relative(direction);
//        final BlockState facingState = world.getBlockState(offsetPos);
//
//        if (facingState.getBlock() instanceof TrussFluidPipeBlock
//                && facingState.getValue(TrussFluidPipeBlock.AXIS) == direction.getAxis())
//            return original == FluidTransportBehaviour.AttachmentTypes.DETAILED_CONNECTION ? FluidTransportBehaviour.AttachmentTypes.CONNECTION : original;
        return original;
    }

}
