package com.kipti.bnb.mixin.dyeable.fluid_tank;

import com.kipti.bnb.content.decoration.dyeable.DyeableTransitionHelper;
import com.kipti.bnb.content.decoration.dyeable.fluid_tank.DyeableFluidTankBehaviour;
import com.kipti.bnb.registry.client.BnbSpriteShifts;
import com.simibubi.create.content.fluids.tank.FluidTankModel;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.model.BakedQuadHelper;
import net.createmod.catnip.render.SpriteShiftEntry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
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

import java.util.ArrayList;
import java.util.Arrays;
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
        final DyeableFluidTankBehaviour behaviour = BlockEntityBehaviour.get(
                world,
                pos,
                DyeableFluidTankBehaviour.TYPE
        );
        DyeColor color = null;
        if (behaviour != null) {
            color = behaviour.getColor();
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

        final List<BakedQuad> originalQuads = cir.getReturnValue();
        final List<BakedQuad> shiftedQuads = new ArrayList<>(originalQuads.size());

        for (final BakedQuad quad : originalQuads) {
            shiftedQuads.add(bnb$shiftQuad(quad, color));
        }

        cir.setReturnValue(shiftedQuads);
    }

    @Unique
    private static BakedQuad bnb$shiftQuad(final BakedQuad quad, final DyeColor color) {
        final SpriteShiftEntry shiftEntry = bnb$findShiftEntry(quad, color);
        if (shiftEntry == null) return quad;

        final int[] originalVertexData = quad.getVertices();
        final int[] vertexData = Arrays.copyOf(originalVertexData, originalVertexData.length);
        final int vertexCount = vertexData.length / BakedQuadHelper.VERTEX_STRIDE;
        for (int vertex = 0; vertex < vertexCount; vertex++) {
            final float u = BakedQuadHelper.getU(vertexData, vertex);
            final float v = BakedQuadHelper.getV(vertexData, vertex);
            BakedQuadHelper.setU(vertexData, vertex, shiftEntry.getTargetU(u));
            BakedQuadHelper.setV(vertexData, vertex, shiftEntry.getTargetV(v));
        }

        return new BakedQuad(
                vertexData,
                quad.getTintIndex(),
                quad.getDirection(),
                shiftEntry.getTarget(),
                quad.isShade()
        );
    }

    @Unique
    private static SpriteShiftEntry bnb$findShiftEntry(final BakedQuad quad, final DyeColor color) {
        final int[] vertexData = quad.getVertices();
        final float u = BakedQuadHelper.getU(vertexData, 0);
        final float v = BakedQuadHelper.getV(vertexData, 0);

        SpriteShiftEntry entry;

        entry = BnbSpriteShifts.DYED_FLUID_TANK_CONNECTED.get(color);
        if (entry != null && bnb$uvWithinSprite(u, v, entry.getOriginal())) return entry;

        entry = BnbSpriteShifts.DYED_FLUID_TANK_TOP_CONNECTED.get(color);
        if (entry != null && bnb$uvWithinSprite(u, v, entry.getOriginal())) return entry;

        entry = BnbSpriteShifts.DYED_FLUID_TANK_INNER_CONNECTED.get(color);
        if (entry != null && bnb$uvWithinSprite(u, v, entry.getOriginal())) return entry;

        entry = BnbSpriteShifts.DYED_FLUID_TANK_WINDOW.get(color);
        if (entry != null && bnb$uvWithinSprite(u, v, entry.getOriginal())) return entry;

        entry = BnbSpriteShifts.DYED_FLUID_TANK_WINDOW_SINGLE.get(color);
        if (entry != null && bnb$uvWithinSprite(u, v, entry.getOriginal())) return entry;

        return null;
    }

    @Unique
    private static boolean bnb$uvWithinSprite(final float u, final float v, final TextureAtlasSprite sprite) {
        return u >= sprite.getU0() && u <= sprite.getU1()
                && v >= sprite.getV0() && v <= sprite.getV1();
    }
}
