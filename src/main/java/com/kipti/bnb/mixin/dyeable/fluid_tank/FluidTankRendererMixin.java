package com.kipti.bnb.mixin.dyeable.fluid_tank;

import com.kipti.bnb.content.decoration.dyeable.simple.SimpleDyeablePartialHelper;
import com.kipti.bnb.content.decoration.dyeable.tanks.DyeableTankBehaviour;
import com.kipti.bnb.registry.client.BnbSpriteShifts;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.content.fluids.tank.FluidTankRenderer;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FluidTankRenderer.class)
public class FluidTankRendererMixin {

    @WrapOperation(
            method = "renderAsBoiler",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/createmod/catnip/render/CachedBuffers;partial(Ldev/engine_room/flywheel/lib/model/baked/PartialModel;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/createmod/catnip/render/SuperByteBuffer;",
                    ordinal = 0
            )
    )
    private SuperByteBuffer bnb$applyBoilerGaugeDye(
            final PartialModel partial,
            final BlockState referenceState,
            final Operation<SuperByteBuffer> original,
            final FluidTankBlockEntity be,
            final float partialTicks,
            final PoseStack ms,
            final MultiBufferSource buffer,
            final int light,
            final int overlay
    ) {
        return SimpleDyeablePartialHelper.apply(
                original.call(partial, referenceState),
                bnb$getDisplayedTankColor(be),
                BnbSpriteShifts.DYED_BOILER_GAUGE
        );
    }

    @Unique
    @Nullable
    private static DyeColor bnb$getDisplayedTankColor(final FluidTankBlockEntity be) {
        final DyeableTankBehaviour behaviour = BlockEntityBehaviour.get(be, DyeableTankBehaviour.TYPE);
        if (behaviour == null) {
            return null;
        }

        return behaviour.getDisplayedColor();
    }

}
