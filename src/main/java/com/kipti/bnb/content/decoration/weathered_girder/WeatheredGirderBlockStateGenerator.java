package com.kipti.bnb.content.decoration.weathered_girder;

import com.simibubi.create.content.decoration.girder.GirderBlock;
import com.simibubi.create.content.decoration.girder.GirderEncasedShaftBlock;
import com.simibubi.create.foundation.data.AssetLookup;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import net.createmod.catnip.data.Iterate;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.MultiPartBlockStateBuilder;

/**
 * My famous, patented, and trademarked block state generator for the weathered girder block.
 * (copy-pasted from the original girder block state generator)
 */
public class WeatheredGirderBlockStateGenerator {

    public static void blockStateWithShaft(DataGenContext<Block, WeatheredGirderEncasedShaftBlock> c,
                                           RegistrateBlockstateProvider p) {
        MultiPartBlockStateBuilder builder = p.getMultipartBuilder(c.get());

        builder.part()
                .modelFile(AssetLookup.partialBaseModel(c, p))
                .rotationY(0)
                .addModel()
                .condition(GirderEncasedShaftBlock.HORIZONTAL_AXIS, Direction.Axis.Z)
                .end();

        builder.part()
                .modelFile(AssetLookup.partialBaseModel(c, p))
                .rotationY(90)
                .addModel()
                .condition(GirderEncasedShaftBlock.HORIZONTAL_AXIS, Direction.Axis.X)
                .end();

        builder.part()
                .modelFile(AssetLookup.partialBaseModel(c, p, "top"))
                .addModel()
                .condition(GirderEncasedShaftBlock.TOP, true)
                .end();

        builder.part()
                .modelFile(AssetLookup.partialBaseModel(c, p, "bottom"))
                .addModel()
                .condition(GirderEncasedShaftBlock.BOTTOM, true)
                .end();

    }

    public static void blockState(DataGenContext<Block, WeatheredGirderBlock> c, RegistrateBlockstateProvider p) {
        MultiPartBlockStateBuilder builder = p.getMultipartBuilder(c.get());

        builder.part()
                .modelFile(AssetLookup.partialBaseModel(c, p, "pole"))
                .addModel()
                .condition(GirderBlock.X, false)
                .condition(GirderBlock.Z, false)
                .end();

        builder.part()
                .modelFile(AssetLookup.partialBaseModel(c, p, "x"))
                .addModel()
                .condition(GirderBlock.X, true)
                .end();

        builder.part()
                .modelFile(AssetLookup.partialBaseModel(c, p, "z"))
                .addModel()
                .condition(GirderBlock.Z, true)
                .end();

        for (boolean x : Iterate.trueAndFalse)
            builder.part()
                    .modelFile(AssetLookup.partialBaseModel(c, p, "top"))
                    .addModel()
                    .condition(GirderBlock.TOP, true)
                    .condition(GirderBlock.X, x)
                    .condition(GirderBlock.Z, !x)
                    .end()
                    .part()
                    .modelFile(AssetLookup.partialBaseModel(c, p, "bottom"))
                    .addModel()
                    .condition(GirderBlock.BOTTOM, true)
                    .condition(GirderBlock.X, x)
                    .condition(GirderBlock.Z, !x)
                    .end();

        builder.part()
                .modelFile(AssetLookup.partialBaseModel(c, p, "cross"))
                .addModel()
                .condition(GirderBlock.X, true)
                .condition(GirderBlock.Z, true)
                .end();

    }

}

