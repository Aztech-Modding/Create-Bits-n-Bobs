package com.kipti.bnb.content.decoration.weathered_girder;

import com.kipti.bnb.registry.client.BnbPartialModels;
import com.simibubi.create.foundation.block.connected.CTModel;
import net.createmod.catnip.data.Iterate;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Back at it again with another mirror of the original class.
 */
public class WeatheredConnectedGirderModel extends CTModel {

    protected static final ModelProperty<ConnectionData> CONNECTION_PROPERTY = new ModelProperty<>();

    public WeatheredConnectedGirderModel(final BakedModel originalModel) {
        super(originalModel, new WeatheredGirderCTBehaviour());
    }

    @Override
    protected ModelData.Builder gatherModelData(final ModelData.Builder builder, final BlockAndTintGetter world, final BlockPos pos, final BlockState state,
                                                final ModelData blockEntityData) {
        super.gatherModelData(builder, world, pos, state, blockEntityData);
        final ConnectionData connectionData = new ConnectionData();
        for (final Direction d : Iterate.horizontalDirections)
            connectionData.setConnected(d, WeatheredGirderBlock.isConnected(world, pos, state, d));
        return builder.with(CONNECTION_PROPERTY, connectionData);
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(final BlockState state, final Direction side, final RandomSource rand, final ModelData extraData, final RenderType renderType) {
        final List<BakedQuad> superQuads = super.getQuads(state, side, rand, extraData, renderType);
        if (side != null || !extraData.has(CONNECTION_PROPERTY))
            return superQuads;
        final List<BakedQuad> quads = new ArrayList<>(superQuads);
        final ConnectionData data = extraData.get(CONNECTION_PROPERTY);
        for (final Direction d : Iterate.horizontalDirections)
            if (data.isConnected(d))
                quads.addAll(BnbPartialModels.WEATHERED_METAL_GIRDER_BRACKETS.get(d)
                        .get()
                        .getQuads(state, side, rand, extraData, renderType));
        return quads;
    }

    private static class ConnectionData {
        boolean[] connectedFaces;

        public ConnectionData() {
            connectedFaces = new boolean[4];
            Arrays.fill(connectedFaces, false);
        }

        void setConnected(final Direction face, final boolean connected) {
            connectedFaces[face.get2DDataValue()] = connected;
        }

        boolean isConnected(final Direction face) {
            return connectedFaces[face.get2DDataValue()];
        }
    }

}


