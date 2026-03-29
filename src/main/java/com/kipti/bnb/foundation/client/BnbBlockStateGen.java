package com.kipti.bnb.foundation.client;

import com.kipti.bnb.content.decoration.truss.AlternatingTrussBlock;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;

import java.util.function.Function;

public class BnbBlockStateGen {

    public static final int DEFAULT_ANGLE_OFFSET = 180;

    public static <T extends Block> void directionalUvLockBlock(final DataGenContext<Block, T> ctx,
                                                                final RegistrateBlockstateProvider prov,
                                                                final Function<BlockState, ModelFile> modelFunc) {
        prov.getVariantBuilder(ctx.get())
                .forAllStates(state -> {
                    final Direction dir = state.getValue(BlockStateProperties.FACING);
                    return ConfiguredModel.builder()
                            .modelFile(modelFunc.apply(state))
                            .rotationX(dir == Direction.DOWN ? 180 : dir.getAxis().isHorizontal() ? 90 : 0)
                            .rotationY(dir.getAxis().isVertical() ? 0 : (((int) dir.toYRot()) + DEFAULT_ANGLE_OFFSET) % 360)
                            .uvLock(true)
                            .build();
                });
    }

    public static <T extends Block> void directionalBlockIgnoresWaterlogged(final DataGenContext<Block, T> ctx,
                                                                            final RegistrateBlockstateProvider prov,
                                                                            final Function<BlockState, ModelFile> modelFunc,
                                                                            final boolean uvLock) {
        prov.getVariantBuilder(ctx.getEntry())
                .forAllStatesExcept(
                        state -> {
                            final Direction dir = state.getValue(BlockStateProperties.FACING);
                            return ConfiguredModel.builder()
                                    .modelFile(modelFunc.apply(state))
                                    .rotationX(dir == Direction.DOWN ? 180
                                                       : dir.getAxis()
                                            .isHorizontal() ? 90 : 0)
                                    .rotationY(dir.getAxis()
                                                       .isVertical() ? 0 : (((int) dir.toYRot()) + 180) % 360)
                                    .uvLock(uvLock)
                                    .build();
                        }, BlockStateProperties.WATERLOGGED
                );
    }

    public static <T extends Block> void axisModel(final DataGenContext<Block, T> ctx,
                                                   final RegistrateBlockstateProvider prov) {
        prov.getVariantBuilder(ctx.get())
                .forAllStates(state -> {
                    final Direction dir = Direction.fromAxisAndDirection(
                            state.getValue(RotatedPillarBlock.AXIS),
                            Direction.AxisDirection.POSITIVE
                    );
                    return ConfiguredModel.builder()
                            .modelFile(prov.models().getExistingFile(ctx.getId()))
                            .rotationX(dir == Direction.DOWN ? 180 : dir.getAxis().isHorizontal() ? 90 : 0)
                            .rotationY(dir.getAxis().isVertical() ? 0 : (((int) dir.toYRot()) + DEFAULT_ANGLE_OFFSET) % 360)
                            .build();
                });
    }

    public static <T extends AlternatingTrussBlock> void alternatingTrussModel(final DataGenContext<Block, T> ctx,
                                                                               final RegistrateBlockstateProvider prov) {
        prov.getVariantBuilder(ctx.get())
                .forAllStates(state -> {
                    final Direction dir = Direction.fromAxisAndDirection(
                            state.getValue(RotatedPillarBlock.AXIS),
                            Direction.AxisDirection.POSITIVE
                    );
                    return ConfiguredModel.builder()
                            .modelFile(prov.models().getExistingFile(
                                    prov.modLoc("block/industrial_truss/industrial_truss" + (state.getValue(
                                            AlternatingTrussBlock.ALTERNATING) ? "_alternating" : ""))
                            ))
                            .rotationX(dir == Direction.DOWN ? 180 : dir.getAxis().isHorizontal() ? 90 : 0)
                            .rotationY(dir.getAxis().isVertical() ? 0 : (((int) dir.toYRot()) + DEFAULT_ANGLE_OFFSET) % 360)
                            .build();
                });
    }

    public static <T extends Block> void directionalMixedUvLockBlock(final DataGenContext<Block, T> ctx,
                                                                     final RegistrateBlockstateProvider prov,
                                                                     final ModelFile uvlockModel,
                                                                     final ModelFile uvunlockModel) {
        for (final Direction dir : Direction.values()) {//Specifically not using forAllStates cause uh
            prov.getMultipartBuilder(ctx.get())
                    .part()
                    .modelFile(uvlockModel)
                    .rotationX(dir == Direction.DOWN ? 180 : dir.getAxis().isHorizontal() ? 90 : 0)
                    .rotationY(dir.getAxis().isVertical() ? 0 : (((int) dir.toYRot()) + BnbBlockStateGen.DEFAULT_ANGLE_OFFSET) % 360)
                    .uvLock(true)
                    .addModel()
                    .condition(BlockStateProperties.FACING, dir)
                    .end()
                    .part()
                    .modelFile(uvunlockModel)
                    .rotationX(dir == Direction.DOWN ? 180 : dir.getAxis().isHorizontal() ? 90 : 0)
                    .rotationY(dir.getAxis().isVertical() ? 0 : (((int) dir.toYRot()) + BnbBlockStateGen.DEFAULT_ANGLE_OFFSET) % 360)
                    .uvLock(false)
                    .addModel()
                    .condition(BlockStateProperties.FACING, dir)
                    .end();
        }
    }
}

