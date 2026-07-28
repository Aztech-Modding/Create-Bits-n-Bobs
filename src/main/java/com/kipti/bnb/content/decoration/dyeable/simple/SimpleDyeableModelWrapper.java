package com.kipti.bnb.content.decoration.dyeable.simple;

import com.simibubi.create.foundation.model.BakedModelWrapperWithData;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import net.createmod.catnip.render.SpriteShiftEntry;
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
import java.util.Map;

public class SimpleDyeableModelWrapper extends BakedModelWrapperWithData {

    final Map<DyeColor, SpriteShiftEntry>[] spriteShiftEntry;

    @SafeVarargs
    public SimpleDyeableModelWrapper(final BakedModel originalModel, final Map<DyeColor, SpriteShiftEntry>... spriteShiftEntry) {
        super(originalModel);
        this.spriteShiftEntry = spriteShiftEntry;
    }

    /**
     * Factory for model-wrapper lambdas, intended for use with {@code CreateRegistrate#blockModel}.
     * Keeping the {@link BakedModel}-typed lambda here (a client-only class) instead of at the call
     * site ensures classes that get linked on the dedicated server never reference client-only types
     * in their method signatures.
     */
    @SafeVarargs
    public static NonNullFunction<BakedModel, ? extends BakedModel> wrap(final Map<DyeColor, SpriteShiftEntry>... spriteShiftEntry) {
        return model -> new SimpleDyeableModelWrapper(model, spriteShiftEntry);
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
        final SpriteShiftEntry[] entriesForColor = new SpriteShiftEntry[spriteShiftEntry.length];
        for (int i = 0; i < spriteShiftEntry.length; i++) {
            entriesForColor[i] = spriteShiftEntry[i].get(color);
        }
        return SimpleDyeableModelHelper.shiftSprites(quads, color, entriesForColor);
    }

}
