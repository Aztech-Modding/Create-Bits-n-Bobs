package com.kipti.bnb.content.decoration.grating;

import com.kipti.bnb.CreateBitsnBobs;
import com.kipti.bnb.foundation.client.BnbBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import java.util.Map;

public class GratingBlockStateGen {
    
    public static <T extends Block> void gratingEncasedShaftBlock(final DataGenContext<Block, T> ctx,
                                                                   final RegistrateBlockstateProvider prov) {
        for (final Direction dir : Direction.values()) {//Specifically not using forAllStates cause uh
            final Direction.Axis otherAxisA = dir.getAxis() == Direction.Axis.X ? Direction.Axis.Y : Direction.Axis.X;
            final Direction.Axis otherAxisB = dir.getAxis() == Direction.Axis.Z ? Direction.Axis.Y : Direction.Axis.Z;

            prov.getMultipartBuilder(ctx.get())
                    .part()
                    .modelFile(prov.models().getExistingFile(CreateBitsnBobs.asResource(
                            "block/industrial_grating/panel_side")))
                    .rotationX(dir == Direction.DOWN ? 180 : dir.getAxis().isHorizontal() ? 90 : 0)
                    .rotationY(dir.getAxis().isVertical() ? 0 : (((int) dir.toYRot()) + BnbBlockStateGen.DEFAULT_ANGLE_OFFSET) % 360)
                    .uvLock(false)
                    .addModel()
                    .condition(BlockStateProperties.FACING, dir)
                    .end()
                    .part()
                    .modelFile(prov.models().getExistingFile(CreateBitsnBobs.asResource(
                            "block/industrial_grating/panel_side_cutout")))
                    .rotationX(dir == Direction.DOWN ? 180 : dir.getAxis().isHorizontal() ? 90 : 0)
                    .rotationY(dir.getAxis().isVertical() ? 0 : (((int) dir.toYRot()) + BnbBlockStateGen.DEFAULT_ANGLE_OFFSET) % 360)
                    .uvLock(false)
                    .addModel()
                    .condition(BlockStateProperties.FACING, dir)
                    .condition(GratingEncasedShaftBlock.AXIS, dir.getAxis())
                    .end();

            prov.getMultipartBuilder(ctx.get()).part()
                    .modelFile(prov.models().getExistingFile(CreateBitsnBobs.asResource("block/industrial_grating/panel")))
                    .rotationX(dir == Direction.DOWN ? 180 : dir.getAxis().isHorizontal() ? 90 : 0)
                    .rotationY(dir.getAxis().isVertical() ? 0 : (((int) dir.toYRot()) + BnbBlockStateGen.DEFAULT_ANGLE_OFFSET) % 360)
                    .uvLock(true)
                    .addModel()
                    .condition(BlockStateProperties.FACING, dir)
                    .condition(GratingEncasedShaftBlock.AXIS, otherAxisA, otherAxisB)
                    .end()
                    .part()
                    .modelFile(prov.models().getExistingFile(CreateBitsnBobs.asResource(
                            "block/industrial_grating/panel_cutout")))
                    .rotationX(dir == Direction.DOWN ? 180 : dir.getAxis().isHorizontal() ? 90 : 0)
                    .rotationY(dir.getAxis().isVertical() ? 0 : (((int) dir.toYRot()) + BnbBlockStateGen.DEFAULT_ANGLE_OFFSET) % 360)
                    .uvLock(true)
                    .addModel()
                    .condition(BlockStateProperties.FACING, dir)
                    .condition(GratingEncasedShaftBlock.AXIS, dir.getAxis())
                    .end();
        }
    }

    public static <T extends Block> void gratingEncasedPipeBlock(final DataGenContext<Block, T> ctx,
                                                                   final RegistrateBlockstateProvider prov) {
        final Map<Direction, BooleanProperty> connectionProperties = Map.of(
                Direction.UP, BlockStateProperties.UP,
                Direction.DOWN, BlockStateProperties.DOWN,
                Direction.NORTH, BlockStateProperties.NORTH,
                Direction.SOUTH, BlockStateProperties.SOUTH,
                Direction.EAST, BlockStateProperties.EAST,
                Direction.WEST, BlockStateProperties.WEST
        );

        for (final Direction dir : Direction.values()) {
            final BooleanProperty connectionProp = connectionProperties.get(dir);
            final int rotX = dir == Direction.DOWN ? 180 : dir.getAxis().isHorizontal() ? 90 : 0;
            final int rotY = dir.getAxis().isVertical() ? 0 : (((int) dir.toYRot()) + BnbBlockStateGen.DEFAULT_ANGLE_OFFSET) % 360;

            prov.getMultipartBuilder(ctx.get())
                    .part()
                    .modelFile(prov.models().getExistingFile(CreateBitsnBobs.asResource(
                            "block/industrial_grating/panel_side")))
                    .rotationX(rotX)
                    .rotationY(rotY)
                    .uvLock(false)
                    .addModel()
                    .condition(BlockStateProperties.FACING, dir)
                    .end()
                    .part()
                    .modelFile(prov.models().getExistingFile(CreateBitsnBobs.asResource(
                            "block/industrial_grating/panel_side_pipe_cutout")))
                    .rotationX(rotX)
                    .rotationY(rotY)
                    .uvLock(false)
                    .addModel()
                    .condition(BlockStateProperties.FACING, dir)
                    .condition(connectionProp, true)
                    .end();

            prov.getMultipartBuilder(ctx.get())
                    .part()
                    .modelFile(prov.models().getExistingFile(CreateBitsnBobs.asResource(
                            "block/industrial_grating/panel_pipe_cutout")))
                    .rotationX(rotX)
                    .rotationY(rotY)
                    .uvLock(true)
                    .addModel()
                    .condition(BlockStateProperties.FACING, dir)
                    .condition(connectionProp, true)
                    .end()
                    .part()
                    .modelFile(prov.models().getExistingFile(CreateBitsnBobs.asResource(
                            "block/industrial_grating/panel")))
                    .rotationX(rotX)
                    .rotationY(rotY)
                    .uvLock(true)
                    .addModel()
                    .condition(BlockStateProperties.FACING, dir)
                    .condition(connectionProp, false)
                    .end();
        }
    }

}
