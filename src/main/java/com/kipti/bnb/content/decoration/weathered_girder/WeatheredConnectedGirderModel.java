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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Back at it again with another mirror of the original class.
 */
public class WeatheredConnectedGirderModel extends CTModel {

    protected static final ModelProperty<ConnectionData> CONNECTION_PROPERTY = new ModelProperty<>();

    public WeatheredConnectedGirderModel(BakedModel originalModel) {
        super(originalModel, new WeatheredGirderCTBehaviour());
    }

    @Override
    protected ModelData.Builder gatherModelData(ModelData.Builder builder, BlockAndTintGetter world, BlockPos pos, BlockState state,
                                                ModelData blockEntityData) {
        super.gatherModelData(builder, world, pos, state, blockEntityData);
        ConnectionData connectionData = new ConnectionData();
        for (Direction d : Iterate.horizontalDirections)
            connectionData.setConnected(d, WeatheredGirderBlock.isConnected(world, pos, state, d));
        return builder.with(CONNECTION_PROPERTY, connectionData);
    }

    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand, ModelData extraData, RenderType renderType) {
        List<BakedQuad> superQuads = super.getQuads(state, side, rand, extraData, renderType);
        if (side != null || !extraData.has(CONNECTION_PROPERTY))
            return superQuads;
        List<BakedQuad> quads = new ArrayList<>(superQuads);
        ConnectionData data = extraData.get(CONNECTION_PROPERTY);
        for (Direction d : Iterate.horizontalDirections)
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

        void setConnected(Direction face, boolean connected) {
            connectedFaces[face.get2DDataValue()] = connected;
        }

        boolean isConnected(Direction face) {
            return connectedFaces[face.get2DDataValue()];
        }
    }

}


