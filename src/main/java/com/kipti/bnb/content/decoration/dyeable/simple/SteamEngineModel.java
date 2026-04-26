package com.kipti.bnb.content.decoration.dyeable.simple;

import com.kipti.bnb.registry.client.BnbSpriteShifts;
import com.simibubi.create.foundation.model.BakedModelWrapperWithData;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SteamEngineModel extends BakedModelWrapperWithData {

    public SteamEngineModel(final BakedModel originalModel) {
        super(originalModel);
    }

    @Override
    protected ModelData.Builder gatherModelData(
            final ModelData.Builder builder,
            final BlockAndTintGetter world,
            final BlockPos pos,
            final BlockState state,
            final ModelData blockEntityData
    ) {
        SimpleDyeableModelHelper.putDyeColor(builder, world, pos);
        return builder;
    }

    @Override
    public List<BakedQuad> getQuads(
            @Nullable final BlockState state,
            @Nullable final Direction side,
            final RandomSource rand,
            final ModelData data,
            @Nullable final RenderType renderType
    ) {
        final List<BakedQuad> quads = super.getQuads(state, side, rand, data, renderType);
        final DyeColor color = SimpleDyeableModelHelper.getDyeColor(data);
        if (color == null) {
            return quads;
        }
        return SimpleDyeableModelHelper.shiftSprites(quads, color, BnbSpriteShifts.DYED_STEAM_ENGINE.get(color));
    }

}
