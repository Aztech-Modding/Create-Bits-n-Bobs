package com.kipti.bnb.foundation;

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

    private static final int DEFAULT_ANGLE_OFFSET = 180;

    public static <T extends Block> void directionalUvLockBlock(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov, Function<BlockState, ModelFile> modelFunc) {
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
                                                                            final RegistrateBlockstateProvider prov, final Function<BlockState, ModelFile> modelFunc, final boolean uvLock) {
        prov.getVariantBuilder(ctx.getEntry())
                .forAllStatesExcept(state -> {
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
                }, BlockStateProperties.WATERLOGGED);
    }

    public static <T extends Block> void axisModel(final DataGenContext<Block, T> ctx,
                                                   final RegistrateBlockstateProvider prov) {
        prov.getVariantBuilder(ctx.get())
                .forAllStates(state -> {
                    final Direction dir = Direction.fromAxisAndDirection(state.getValue(RotatedPillarBlock.AXIS), Direction.AxisDirection.POSITIVE);
                    return ConfiguredModel.builder()
                            .modelFile(prov.models().getExistingFile(ctx.getId()))
                            .rotationX(dir == Direction.DOWN ? 180 : dir.getAxis().isHorizontal() ? 90 : 0)
                            .rotationY(dir.getAxis().isVertical() ? 0 : (((int) dir.toYRot()) + DEFAULT_ANGLE_OFFSET) % 360)
                            .build();
                });
    }

    public static <T extends AlternatingTrussBlock> void alternatingTrussModel(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov) {
        prov.getVariantBuilder(ctx.get())
                .forAllStates(state -> {
                    final Direction dir = Direction.fromAxisAndDirection(state.getValue(RotatedPillarBlock.AXIS), Direction.AxisDirection.POSITIVE);
                    return ConfiguredModel.builder()
                            .modelFile(prov.models().getExistingFile(
                                    prov.modLoc(ctx.getId().getPath() + (state.getValue(AlternatingTrussBlock.ALTERNATING) ? "_alternating" : ""))
                            ))
                            .rotationX(dir == Direction.DOWN ? 180 : dir.getAxis().isHorizontal() ? 90 : 0)
                            .rotationY(dir.getAxis().isVertical() ? 0 : (((int) dir.toYRot()) + DEFAULT_ANGLE_OFFSET) % 360)
                            .build();
                });
    }
}

