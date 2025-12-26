package com.kipti.bnb.mixin;

import com.kipti.bnb.content.weathered_girder.WeatheredGirderBlock;
import com.simibubi.create.content.decoration.girder.GirderCTBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GirderCTBehaviour.class)
public class GirderCTBehaviourMixin {

    @Inject(method = "connectsTo", at = @At("RETURN"), cancellable = true)
    private void bnb$connectsToAdditionalGirderTypes(final BlockState state, final BlockState other, final BlockAndTintGetter reader, final BlockPos pos, final BlockPos otherPos, final Direction face, final CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue())
            return;
        if (other.getBlock() != state.getBlock() && !(other.getBlock() instanceof WeatheredGirderBlock))
            return;
        cir.setReturnValue(true);
    }

}
