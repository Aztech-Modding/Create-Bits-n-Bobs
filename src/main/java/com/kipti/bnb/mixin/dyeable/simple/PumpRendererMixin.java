package com.kipti.bnb.mixin.dyeable.simple;

import com.kipti.bnb.content.decoration.dyeable.simple.SimpleDyeablePartialHelper;
import com.kipti.bnb.registry.client.BnbSpriteShifts;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.fluids.pump.PumpBlockEntity;
import com.simibubi.create.content.fluids.pump.PumpRenderer;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PumpRenderer.class)
public class PumpRendererMixin {

    @WrapOperation(
            method = "getRotatedModel(Lcom/simibubi/create/content/fluids/pump/PumpBlockEntity;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/createmod/catnip/render/SuperByteBuffer;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/createmod/catnip/render/CachedBuffers;partialFacing(Ldev/engine_room/flywheel/lib/model/baked/PartialModel;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/createmod/catnip/render/SuperByteBuffer;"
            )
    )
    private SuperByteBuffer bnb$applyPumpCogDye(
            final PartialModel partial,
            final BlockState referenceState,
            final Operation<SuperByteBuffer> original,
            final PumpBlockEntity be,
            final BlockState state
    ) {
        return SimpleDyeablePartialHelper.apply(
                original.call(partial, referenceState),
                SimpleDyeablePartialHelper.getColor(be),
                BnbSpriteShifts.DYED_PUMP
        );
    }

}
