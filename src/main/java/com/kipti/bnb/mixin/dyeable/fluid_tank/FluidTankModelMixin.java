package com.kipti.bnb.mixin.dyeable.fluid_tank;

import com.cake.azimuth.utility.client.model.QuadTransformer;
import com.kipti.bnb.content.decoration.dyeable.DyeableTransitionHelper;
import com.kipti.bnb.content.decoration.dyeable.tanks.DyeableTankBehaviour;
import com.kipti.bnb.registry.client.BnbSpriteShifts;
import com.simibubi.create.content.fluids.tank.FluidTankModel;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
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
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Shifts fluid tank quad UVs to dyed texture variants when the tank has a dye color applied.
 */
@Mixin(FluidTankModel.class)
public class FluidTankModelMixin {

    @Unique
    private static final ModelProperty<DyeColor> BNB_TANK_DYE_COLOR = new ModelProperty<>();

    @Inject(method = "gatherModelData", at = @At("TAIL"))
    private void bnb$gatherDyeColor(
            final ModelData.Builder builder,
            final BlockAndTintGetter world,
            final BlockPos pos,
            final BlockState state,
            final ModelData blockEntityData,
            final CallbackInfoReturnable<ModelData.Builder> cir
    ) {
        final DyeableTankBehaviour behaviour = BlockEntityBehaviour.get(
                world,
                pos,
                DyeableTankBehaviour.TYPE
        );
        DyeColor color = null;
        if (behaviour != null) {
            color = behaviour.getDisplayedColor();
        }
        if (color == null) {
            color = DyeableTransitionHelper.getPendingPlacementColor(world, pos);
        }
        if (color != null) {
            builder.with(BNB_TANK_DYE_COLOR, color);
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
        if (!data.has(BNB_TANK_DYE_COLOR)) return;
        final DyeColor color = data.get(BNB_TANK_DYE_COLOR);
        if (color == null) return;

        cir.setReturnValue(QuadTransformer.shiftSprites(cir.getReturnValue(), quad -> bnb$findShiftEntry(quad, color)));
    }

    @Unique
    private static SpriteShiftEntry bnb$findShiftEntry(final BakedQuad quad, final DyeColor color) {
        SpriteShiftEntry entry;

        entry = BnbSpriteShifts.DYED_FLUID_TANK_CONNECTED.get(color);
        if (entry != null && QuadTransformer.uvWithinSprite(quad, entry.getOriginal())) return entry;

        entry = BnbSpriteShifts.DYED_FLUID_TANK_TOP_CONNECTED.get(color);
        if (entry != null && QuadTransformer.uvWithinSprite(quad, entry.getOriginal())) return entry;

        entry = BnbSpriteShifts.DYED_FLUID_TANK_INNER_CONNECTED.get(color);
        if (entry != null && QuadTransformer.uvWithinSprite(quad, entry.getOriginal())) return entry;

        entry = BnbSpriteShifts.DYED_FLUID_TANK_WINDOW.get(color);
        if (entry != null && QuadTransformer.uvWithinSprite(quad, entry.getOriginal())) return entry;

        entry = BnbSpriteShifts.DYED_FLUID_TANK_WINDOW_SINGLE.get(color);
        if (entry != null && QuadTransformer.uvWithinSprite(quad, entry.getOriginal())) return entry;

        return null;
    }
}
