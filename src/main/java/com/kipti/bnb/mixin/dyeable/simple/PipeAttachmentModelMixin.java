package com.kipti.bnb.mixin.dyeable.simple;

import com.kipti.bnb.content.decoration.dyeable.simple.SimpleDyeableModelHelper;
import com.kipti.bnb.registry.client.BnbSpriteShifts;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.fluids.PipeAttachmentModel;
import net.createmod.catnip.render.SpriteShiftEntry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(PipeAttachmentModel.class)
public class PipeAttachmentModelMixin {

    @Inject(method = "gatherModelData", at = @At("TAIL"))
    private void bnb$gatherSimpleDyeColor(
            final ModelData.Builder builder,
            final BlockAndTintGetter world,
            final BlockPos pos,
            final BlockState state,
            final ModelData blockEntityData,
            final CallbackInfoReturnable<ModelData.Builder> cir
    ) {
        SimpleDyeableModelHelper.putDyeColor(builder, world, pos);
    }

    @Inject(method = "getQuads", at = @At("RETURN"), cancellable = true)
    private void bnb$applySimpleDyeSpriteShift(
            final BlockState state,
            final Direction side,
            final RandomSource rand,
            final ModelData data,
            final RenderType renderType,
            final CallbackInfoReturnable<List<BakedQuad>> cir
    ) {
        final DyeColor color = SimpleDyeableModelHelper.getDyeColor(data);
        if (color == null) {
            return;
        }
        cir.setReturnValue(SimpleDyeableModelHelper.shiftSprites(
                cir.getReturnValue(),
                color,
                bnb$getShiftEntries(state, color)
        ));
    }

    @Unique
    private static SpriteShiftEntry[] bnb$getShiftEntries(final BlockState state, final DyeColor color) {
        if (state == null) {
            return new SpriteShiftEntry[0];
        }
        if (AllBlocks.MECHANICAL_PUMP.has(state)) {
            return new SpriteShiftEntry[]{
                    BnbSpriteShifts.DYED_PUMP.get(color)
            };
        }
        if (AllBlocks.SMART_FLUID_PIPE.has(state)) {
            return new SpriteShiftEntry[]{
                    BnbSpriteShifts.DYED_SMART_PIPE_1.get(color),
                    BnbSpriteShifts.DYED_SMART_PIPE_2.get(color),
                    BnbSpriteShifts.DYED_PIPES.get(color)
            };
        }
        if (AllBlocks.FLUID_VALVE.has(state)) {
            return new SpriteShiftEntry[]{
                    BnbSpriteShifts.DYED_VALVE_CLOSED.get(color),
                    BnbSpriteShifts.DYED_VALVE_OPEN.get(color),
                    BnbSpriteShifts.DYED_FLUID_VALVE.get(color)
            };
        }
        return new SpriteShiftEntry[0];
    }

}
