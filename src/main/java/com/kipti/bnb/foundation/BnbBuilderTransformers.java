package com.kipti.bnb.foundation;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.content.kinetics.simpleRelays.encased.EncasedCogwheelBlock;
import com.simibubi.create.content.kinetics.simpleRelays.encased.EncasedShaftBlock;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Supplier;

import static com.simibubi.create.foundation.data.BlockStateGen.axisBlock;

public class BnbBuilderTransformers {

    public static <B extends EncasedShaftBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> encasedShaftWithoutCT(final String casing) {
        return builder -> encasedBase(builder, AllBlocks.SHAFT::get)
                .blockstate((c, p) -> axisBlock(c, p, blockState -> p.models()
                        .getExistingFile(p.modLoc("block/encased_shaft/block_" + casing)), true))
                .item()
                .model(AssetLookup.customBlockItemModel("encased_shaft", "item_" + casing))
                .build();
    }


    public static <B extends EncasedCogwheelBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> encasedCogwheelWithoutCT(final String casing) {
        return b -> encasedCogwheelBaseWithoutCT(b, casing, AllBlocks.COGWHEEL::get, false);
    }

    public static <B extends EncasedCogwheelBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> encasedLargeCogwheelWithoutCT(final String casing) {
        return b -> encasedCogwheelBaseWithoutCT(b, casing, AllBlocks.LARGE_COGWHEEL::get, true);
    }

    private static <B extends EncasedCogwheelBlock, P> BlockBuilder<B, P> encasedCogwheelBaseWithoutCT(final BlockBuilder<B, P> b,
                                                                                                       final String casing, final Supplier<ItemLike> drop, final boolean large) {
        final String encasedSuffix = "_encased_cogwheel_side" + (large ? "_connected" : "");
        final String blockFolder = large ? "encased_large_cogwheel" : "encased_cogwheel";
        return encasedBase(b, drop).addLayer(() -> RenderType::cutoutMipped)
                .blockstate((c, p) -> axisBlock(c, p, blockState -> {
                    final String suffix = (blockState.getValue(EncasedCogwheelBlock.TOP_SHAFT) ? "_top" : "")
                            + (blockState.getValue(EncasedCogwheelBlock.BOTTOM_SHAFT) ? "_bottom" : "");
                    final String modelName = c.getName() + suffix;
                    return p.models()
                            .withExistingParent(modelName, p.modLoc("block/" + blockFolder + "/block" + suffix))
                            .texture("casing", p.modLoc("block/" + casing + "_block"))
                            .texture("particle", p.modLoc("block/" + casing + "_block"))
                            .texture("4", p.modLoc("block/" + casing + "_gearbox"))
                            .texture("1", p.modLoc("block/" + casing + "_block"))
                            .texture("side", p.modLoc("block/" + casing + encasedSuffix));
                }, false))
                .item()
                .model((c, p) -> p.withExistingParent(c.getName(), p.modLoc("block/" + blockFolder + "/item"))
                        .texture("casing", p.modLoc("block/" + casing + "_block"))
                        .texture("particle", p.modLoc("block/" + casing + "_block"))
                        .texture("1", p.modLoc("block/" + casing + "_block"))
                        .texture("side", p.modLoc("block/" + casing + encasedSuffix)))
                .build();
    }

    private static <B extends RotatedPillarKineticBlock, P> BlockBuilder<B, P> encasedBase(final BlockBuilder<B, P> b,
                                                                                           final Supplier<ItemLike> drop) {
        return b.initialProperties(SharedProperties::stone)
                .properties(BlockBehaviour.Properties::noOcclusion)
                .loot((p, lb) -> p.dropOther(lb, drop.get()));
    }

}
