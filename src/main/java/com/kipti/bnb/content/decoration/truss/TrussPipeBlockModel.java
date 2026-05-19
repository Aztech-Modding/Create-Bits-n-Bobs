package com.kipti.bnb.content.decoration.truss;

import com.kipti.bnb.registry.client.BnbPartialModels;
import com.simibubi.create.content.fluids.PipeAttachmentModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

import java.util.ArrayList;
import java.util.List;

import static com.kipti.bnb.content.decoration.truss.TrussBlockModel.ALTERNATING;
import static com.kipti.bnb.content.decoration.truss.TrussBlockModel.ALTERNATING_AXIS;

public class TrussPipeBlockModel extends PipeAttachmentModel {

    public TrussPipeBlockModel(final BakedModel template, final boolean ao) {
        super(template, ao);
    }

    public static BakedModel withTrussAO(final BakedModel template) {
        return new TrussPipeBlockModel(template, true);
    }

    @Override
    protected ModelData.Builder gatherModelData(final ModelData.Builder builder,
                                                final BlockAndTintGetter world,
                                                final BlockPos pos,
                                                final BlockState state,
                                                final ModelData blockEntityData) {
        return super.gatherModelData(builder, world, pos, state, blockEntityData)
                .with(ALTERNATING, pos.get(state.getValue(TrussBlock.AXIS)) % 2 == 0)
                .with(ALTERNATING_AXIS, state.getValue(TrussBlock.AXIS));
    }

    @Override
    public List<BakedQuad> getQuads(final BlockState state,
                                    final Direction side,
                                    final RandomSource rand,
                                    final ModelData data,
                                    final RenderType renderType) {
        final List<BakedQuad> quads = new ArrayList<>(super.getQuads(state, side, rand, data, renderType));

        List<BakedQuad> trussQuads = BnbPartialModels.INDUSTRIAL_TRUSS.get().getQuads(
                state,
                side,
                rand,
                data,
                renderType
        );
        final Direction.Axis axis = state.getValue(TrussBlock.AXIS);
        trussQuads = TrussBlockModel.alternateQuads(data, trussQuads, axis);
        quads.addAll(trussQuads);

        return quads;
    }

}
