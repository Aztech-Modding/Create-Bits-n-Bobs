package com.kipti.bnb.content.decoration.chair;

import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import net.createmod.catnip.data.Iterate;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import org.jetbrains.annotations.NotNull;

public class ChairBlockStateGen {

    public static @NotNull NonNullBiConsumer<DataGenContext<Block, ChairBlock>, RegistrateBlockstateProvider> dyedChair(final String colourName) {
        return (c, p) -> {
            final BlockModelBuilder chairBaseBlock = p.models().withExistingParent("block/chair/block_single_" + colourName, p.modLoc("block/chair/block_single"))
                    .texture("2", p.modLoc("block/chair/chair_" + colourName))
                    .ao(false);
            final BlockModelBuilder chairCornerBaseBlock = p.models().withExistingParent("block/chair/block_corner_" + colourName, p.modLoc("block/chair/block_corner"))
                    .texture("2", p.modLoc("block/chair/chair_" + colourName))
                    .ao(false);

            final BlockModelBuilder chairLeftArmrest = p.models().withExistingParent(
                            "block/chair/left_armrest_" + colourName,
                            p.modLoc("block/chair/left_armrest"))
                    .texture("2", p.modLoc("block/chair/chair_" + colourName))
                    .ao(false);

            final BlockModelBuilder chairRightArmrest = p.models().withExistingParent(
                            "block/chair/right_armrest_" + colourName,
                            p.modLoc("block/chair/right_armrest"))
                    .texture("2", p.modLoc("block/chair/chair_" + colourName))
                    .ao(false);

            final BlockModelBuilder chairBack = p.models().withExistingParent(
                            "block/chair/chair_back_" + colourName,
                            p.modLoc("block/chair/chair_back"))
                    .texture("2", p.modLoc("block/chair/chair_" + colourName))
                    .ao(false);
            final BlockModelBuilder chairBackFlat = p.models().withExistingParent(
                            "block/chair/chair_back_flat_" + colourName,
                            p.modLoc("block/chair/chair_back_flat"))
                    .texture("2", p.modLoc("block/chair/chair_" + colourName))
                    .ao(false);

            final BlockModelBuilder chairBackFlatTrimmedRight = p.models().withExistingParent(
                            "block/chair/chair_back_flat_trimmed_right_" + colourName,
                            p.modLoc("block/chair/chair_back_flat_trimmed_right"))
                    .texture("2", p.modLoc("block/chair/chair_" + colourName))
                    .ao(false);
            final BlockModelBuilder chairBackFlatTrimmedLeft = p.models().withExistingParent(
                            "block/chair/chair_back_flat_trimmed_left_" + colourName,
                            p.modLoc("block/chair/chair_back_flat_trimmed_left"))
                    .texture("2", p.modLoc("block/chair/chair_" + colourName))
                    .ao(false);

            for (final Direction direction : Iterate.horizontalDirections) {
                //Base seat model
                p.getMultipartBuilder(c.get())
                        .part()
                        .modelFile(chairBaseBlock)
                        .rotationY((int) (direction.toYRot() + 180) % 360)
                        .addModel()
                        .condition(ChairBlock.FACING, direction)
                        .condition(ChairBlock.CORNER, false)
                        .end();

                //- 'Confusion' fallback, keep the normal model since there will be no arms
                p.getMultipartBuilder(c.get())
                        .part()
                        .modelFile(chairBaseBlock)
                        .rotationY((int) (direction.toYRot() + 180) % 360)
                        .addModel()
                        .condition(ChairBlock.FACING, direction)
                        .condition(ChairBlock.CORNER, true)
                        .condition(ChairBlock.LEFT_ARM, false)
                        .condition(ChairBlock.RIGHT_ARM, false)
                        .condition(ChairBlock.INVERTED_CORNER, false)
                        .end();
                p.getMultipartBuilder(c.get())
                        .part()
                        .modelFile(chairBaseBlock)
                        .rotationY((int) (direction.toYRot() + 180) % 360)
                        .addModel()
                        .condition(ChairBlock.FACING, direction)
                        .condition(ChairBlock.CORNER, true)
                        .condition(ChairBlock.LEFT_ARM, true)
                        .condition(ChairBlock.RIGHT_ARM, true)
                        .condition(ChairBlock.INVERTED_CORNER, false)
                        .end();

                //- Confusion fallback but for inverted corner being true even if normal corner is true
                p.getMultipartBuilder(c.get())
                        .part()
                        .modelFile(chairBaseBlock)
                        .rotationY((int) (direction.toYRot() + 180) % 360)
                        .addModel()
                        .condition(ChairBlock.FACING, direction)
                        .condition(ChairBlock.CORNER, true)
                        .condition(ChairBlock.INVERTED_CORNER, true)
                        .end();

                // Corner base model
                p.getMultipartBuilder(c.get())
                        .part()
                        .modelFile(chairCornerBaseBlock)
                        .rotationY((int) (direction.toYRot() + 180) % 360)
                        .addModel()
                        .condition(ChairBlock.FACING, direction)
                        .condition(ChairBlock.CORNER, true)
                        .condition(ChairBlock.LEFT_ARM, false)
                        .condition(ChairBlock.RIGHT_ARM, true)
                        .condition(ChairBlock.INVERTED_CORNER, false)
                        .end();
                p.getMultipartBuilder(c.get())
                        .part()
                        .modelFile(chairCornerBaseBlock)
                        .rotationY((int) (direction.toYRot() + 180 - 90) % 360)
                        .addModel()
                        .condition(ChairBlock.FACING, direction)
                        .condition(ChairBlock.CORNER, true)
                        .condition(ChairBlock.LEFT_ARM, true)
                        .condition(ChairBlock.RIGHT_ARM, false)
                        .condition(ChairBlock.INVERTED_CORNER, false)
                        .end();

                //Armrests
                p.getMultipartBuilder(c.get())
                        .part()
                        .modelFile(chairLeftArmrest)
                        .rotationY((int) (direction.toYRot() + 180) % 360)
                        .addModel()
                        .condition(ChairBlock.FACING, direction)
                        .condition(ChairBlock.LEFT_ARM, true)
                        .condition(ChairBlock.CORNER, false)
                        .condition(ChairBlock.INVERTED_CORNER, false)
                        .end();
                p.getMultipartBuilder(c.get())
                        .part()
                        .modelFile(chairRightArmrest)
                        .rotationY((int) (direction.toYRot() + 180) % 360)
                        .addModel()
                        .condition(ChairBlock.FACING, direction)
                        .condition(ChairBlock.RIGHT_ARM, true)
                        .condition(ChairBlock.CORNER, false)
                        .condition(ChairBlock.INVERTED_CORNER, false)
                        .end();

                addNonFlatBackStates(c, p, direction, chairBack);

                //Block state gen nonsense to make EITHER flat back or forced flat back have the same model
                addBackStates(c,
                        p,
                        direction,
                        chairBack,
                        chairBackFlatTrimmedLeft,
                        chairBackFlatTrimmedRight,
                        chairBackFlat,
                        false, false);
                addBackStates(c,
                        p,
                        direction,
                        chairBack,
                        chairBackFlatTrimmedLeft,
                        chairBackFlatTrimmedRight,
                        chairBackFlat,
                        true, false);
                addBackStates(c,
                        p,
                        direction,
                        chairBack,
                        chairBackFlatTrimmedLeft,
                        chairBackFlatTrimmedRight,
                        chairBackFlat,
                        true, true);
            }
        };
    }

    private static void addNonFlatBackStates(DataGenContext<Block, ChairBlock> c, RegistrateBlockstateProvider p, Direction direction, BlockModelBuilder chairBack) {
        //Corner back
        p.getMultipartBuilder(c.get())
                .part()
                .modelFile(chairBack)
                .rotationY((int) (direction.toYRot() + 180 + 90) % 360)
                .addModel()
                .condition(ChairBlock.FACING, direction)
                .condition(ChairBlock.LEFT_ARM, false)
                .condition(ChairBlock.RIGHT_ARM, true)
                .condition(ChairBlock.CORNER, true)
                .condition(ChairBlock.BACK_FLAT, false)
                .condition(ChairBlock.FORCED_BACK_FLAT, false)
                .condition(ChairBlock.INVERTED_CORNER, false)
                .end();

        p.getMultipartBuilder(c.get())
                .part()
                .modelFile(chairBack)
                .rotationY((int) (direction.toYRot() + 180 - 90) % 360)
                .addModel()
                .condition(ChairBlock.FACING, direction)
                .condition(ChairBlock.RIGHT_ARM, false)
                .condition(ChairBlock.LEFT_ARM, true)
                .condition(ChairBlock.CORNER, true)
                .condition(ChairBlock.BACK_FLAT, false)
                .condition(ChairBlock.FORCED_BACK_FLAT, false)
                .condition(ChairBlock.INVERTED_CORNER, false)
                .end();

        //Flat component of corner back
        p.getMultipartBuilder(c.get())
                .part()
                .modelFile(chairBack)
                .rotationY((int) (direction.toYRot() + 180) % 360)
                .addModel()
                .condition(ChairBlock.FACING, direction)
                .condition(ChairBlock.LEFT_ARM, false)
                .condition(ChairBlock.RIGHT_ARM, true)
                .condition(ChairBlock.CORNER, true)
                .condition(ChairBlock.BACK_FLAT, false)
                .condition(ChairBlock.FORCED_BACK_FLAT, false)
                .condition(ChairBlock.INVERTED_CORNER, false)
                .end();

        p.getMultipartBuilder(c.get())
                .part()
                .modelFile(chairBack)
                .rotationY((int) (direction.toYRot() + 180) % 360)
                .addModel()
                .condition(ChairBlock.FACING, direction)
                .condition(ChairBlock.RIGHT_ARM, false)
                .condition(ChairBlock.LEFT_ARM, true)
                .condition(ChairBlock.CORNER, true)
                .condition(ChairBlock.BACK_FLAT, false)
                .condition(ChairBlock.FORCED_BACK_FLAT, false)
                .condition(ChairBlock.INVERTED_CORNER, false)
                .end();

        //Base back

        p.getMultipartBuilder(c.get())
                .part()
                .modelFile(chairBack)
                .rotationY((int) (direction.toYRot() + 180) % 360)
                .addModel()
                .condition(ChairBlock.FACING, direction)
                .condition(ChairBlock.BACK_FLAT, false)
                .condition(ChairBlock.FORCED_BACK_FLAT, false)
                .condition(ChairBlock.CORNER, false)
                .condition(ChairBlock.INVERTED_CORNER, false)
                .end();
    }

    private static void addBackStates(final DataGenContext<Block, ChairBlock> c,
                                      final RegistrateBlockstateProvider p,
                                      final Direction direction,
                                      final BlockModelBuilder chairBack,
                                      final BlockModelBuilder chairBackFlatTrimmedLeft,
                                      final BlockModelBuilder chairBackFlatTrimmedRight,
                                      final BlockModelBuilder chairBackFlat,
                                      final boolean forForcedFlatBack, final boolean secondaryActive) {
        final BooleanProperty activeBackProperty = forForcedFlatBack ? ChairBlock.BACK_FLAT : ChairBlock.FORCED_BACK_FLAT;
        final BooleanProperty inactiveBackProperty = forForcedFlatBack ? ChairBlock.FORCED_BACK_FLAT : ChairBlock.BACK_FLAT;

        //Corner back
        p.getMultipartBuilder(c.get())
                .part()
                .modelFile(chairBackFlatTrimmedLeft)
                .rotationY((int) (direction.toYRot() + 180 + 90) % 360)
                .addModel()
                .condition(ChairBlock.FACING, direction)
                .condition(activeBackProperty, true)
                .condition(inactiveBackProperty, secondaryActive)
                .condition(ChairBlock.CORNER, true)
                .condition(ChairBlock.LEFT_ARM, false)
                .condition(ChairBlock.RIGHT_ARM, true)
                .condition(ChairBlock.INVERTED_CORNER, false)
                .end();
        p.getMultipartBuilder(c.get())
                .part()
                .modelFile(chairBackFlatTrimmedRight)
                .rotationY((int) (direction.toYRot() + 180 - 90) % 360)
                .addModel()
                .condition(ChairBlock.FACING, direction)
                .condition(activeBackProperty, true)
                .condition(inactiveBackProperty, secondaryActive)
                .condition(ChairBlock.CORNER, true)
                .condition(ChairBlock.LEFT_ARM, true)
                .condition(ChairBlock.RIGHT_ARM, false)
                .condition(ChairBlock.INVERTED_CORNER, false)
                .end();

        //Base back

        p.getMultipartBuilder(c.get())
                .part()
                .modelFile(chairBackFlat)
                .rotationY((int) (direction.toYRot() + 180) % 360)
                .addModel()
                .condition(ChairBlock.FACING, direction)
                .condition(activeBackProperty, true)
                .condition(inactiveBackProperty, secondaryActive)
                .condition(ChairBlock.CORNER, false)
                .condition(ChairBlock.INVERTED_CORNER, false)
                .end();

        //Confusion states again
        p.getMultipartBuilder(c.get())
                .part()
                .modelFile(chairBackFlat)
                .rotationY((int) (direction.toYRot() + 180) % 360)
                .addModel()
                .condition(ChairBlock.FACING, direction)
                .condition(activeBackProperty, true)
                .condition(inactiveBackProperty, secondaryActive)
                .condition(ChairBlock.CORNER, true)
                .condition(ChairBlock.LEFT_ARM, false)
                .condition(ChairBlock.RIGHT_ARM, false)
                .condition(ChairBlock.INVERTED_CORNER, false)
                .end();
        p.getMultipartBuilder(c.get())
                .part()
                .modelFile(chairBackFlat)
                .rotationY((int) (direction.toYRot() + 180) % 360)
                .addModel()
                .condition(ChairBlock.FACING, direction)
                .condition(activeBackProperty, true)
                .condition(inactiveBackProperty, secondaryActive)
                .condition(ChairBlock.CORNER, true)
                .condition(ChairBlock.LEFT_ARM, true)
                .condition(ChairBlock.RIGHT_ARM, true)
                .condition(ChairBlock.INVERTED_CORNER, false)
                .end();

        //Corner trimmed states (avoiding z clipping)
        p.getMultipartBuilder(c.get())
                .part()
                .modelFile(chairBackFlatTrimmedRight)
                .rotationY((int) (direction.toYRot() + 180) % 360)
                .addModel()
                .condition(ChairBlock.FACING, direction)
                .condition(activeBackProperty, true)
                .condition(inactiveBackProperty, secondaryActive)
                .condition(ChairBlock.CORNER, true)
                .condition(ChairBlock.LEFT_ARM, false)
                .condition(ChairBlock.RIGHT_ARM, true)
                .condition(ChairBlock.INVERTED_CORNER, false)
                .end();

        p.getMultipartBuilder(c.get())
                .part()
                .modelFile(chairBackFlatTrimmedLeft)
                .rotationY((int) (direction.toYRot() + 180) % 360)
                .addModel()
                .condition(ChairBlock.FACING, direction)
                .condition(activeBackProperty, true)
                .condition(inactiveBackProperty, secondaryActive)
                .condition(ChairBlock.CORNER, true)
                .condition(ChairBlock.LEFT_ARM, true)
                .condition(ChairBlock.RIGHT_ARM, false)
                .condition(ChairBlock.INVERTED_CORNER, false)
                .end();
    }

}

