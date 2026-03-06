package com.kipti.bnb.mixin.dyeable_pipes;

import com.kipti.bnb.content.dyeable_pipes.DyeablePipeBehaviour;
import com.kipti.bnb.registry.client.BnbSpriteShifts;
import com.simibubi.create.content.fluids.PipeAttachmentModel;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.model.BakedQuadHelper;
import net.createmod.catnip.render.SpriteShiftEntry;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Mixin(PipeAttachmentModel.class)
public class PipeAttachmentModelMixin {

    @Unique
    private static final ModelProperty<DyeColor> BNB_PIPE_DYE_COLOR = new ModelProperty<>();

    @Inject(method = "gatherModelData", at = @At("TAIL"))
    private void bnb$gatherDyeColor(
            final ModelDataMap.Builder builder,
            final BlockAndTintGetter world,
            final BlockPos pos,
            final BlockState state,
            final IModelData blockEntityData,
            final CallbackInfo ci
    ) {
        final DyeablePipeBehaviour behaviour = BlockEntityBehaviour.get(world, pos, DyeablePipeBehaviour.TYPE);
        if (behaviour != null && behaviour.getColor() != null) {
            builder.withInitial(BNB_PIPE_DYE_COLOR, behaviour.getColor());
        }
    }

    @Inject(method = "getQuads", at = @At("RETURN"), cancellable = true)
    private void bnb$applyDyeSpriteShift(
            final BlockState state,
            final Direction side,
            final Random rand,
            final IModelData data,
            final CallbackInfoReturnable<List<BakedQuad>> cir
    ) {
        if (!data.hasProperty(BNB_PIPE_DYE_COLOR)) return;
        final DyeColor color = data.getData(BNB_PIPE_DYE_COLOR);
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
        final SpriteShiftEntry shiftEntry = bnb$findShiftEntry(quad.getSprite(), color);
        if (shiftEntry == null) return quad;

        final int[] vertexData = quad.getVertices().clone();
        final int vertexCount = vertexData.length / BakedQuadHelper.VERTEX_STRIDE;
        for (int vertex = 0; vertex < vertexCount; vertex++) {
            final float u = BakedQuadHelper.getU(vertexData, vertex);
            final float v = BakedQuadHelper.getV(vertexData, vertex);
            BakedQuadHelper.setU(vertexData, vertex, shiftEntry.getTargetU(u));
            BakedQuadHelper.setV(vertexData, vertex, shiftEntry.getTargetV(v));
        }

        return new BakedQuad(vertexData, quad.getTintIndex(), quad.getDirection(), shiftEntry.getTarget(), quad.isShade());
    }

    @Unique
    private static SpriteShiftEntry bnb$findShiftEntry(final TextureAtlasSprite sprite, final DyeColor color) {
        final SpriteShiftEntry pipesEntry = BnbSpriteShifts.DYED_PIPES.get(color);
        if (pipesEntry != null && pipesEntry.getOriginal() != null && pipesEntry.getOriginal() == sprite) {
            return pipesEntry;
        }

        final SpriteShiftEntry connectedEntry = BnbSpriteShifts.DYED_PIPES_CONNECTED.get(color);
        if (connectedEntry != null && connectedEntry.getOriginal() != null && connectedEntry.getOriginal() == sprite) {
            return connectedEntry;
        }

        return null;
    }

}
