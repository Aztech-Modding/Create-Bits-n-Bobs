package com.kipti.bnb.content.decoration.grating;

import com.kipti.bnb.CreateBitsnBobs;
import com.kipti.bnb.foundation.client.BnbBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

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

}
