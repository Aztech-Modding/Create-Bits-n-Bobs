package com.kipti.bnb.content.decoration.truss;

import com.kipti.bnb.CreateBitsnBobs;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;

import java.util.LinkedHashMap;
import java.util.Map;

public class TrussBlockStateGen {

    public static <T extends TrussEncasedShaftBlock> void trussEncasedShaftModel(final DataGenContext<Block, T> ctx,
                                                                                 final RegistrateBlockstateProvider prov) {
        prov.getVariantBuilder(ctx.get())
                .forAllStates(state -> {
                    final Direction.Axis trussAxis = state.getValue(TrussEncasedShaftBlock.TRUSS_AXIS);
                    final Direction.Axis shaftAxis = state.getValue(RotatedPillarBlock.AXIS);
                    final boolean alternating = state.getValue(TrussEncasedShaftBlock.ALTERNATING);
                    final Direction trussDir = Direction.fromAxisAndDirection(
                            trussAxis,
                            Direction.AxisDirection.POSITIVE
                    );
                    final int rotX = trussDir.getAxis().isHorizontal() ? 90 : 0;
                    final int rotY = trussDir.getAxis().isVertical() ? 0 : (((int) trussDir.toYRot()) + 180) % 360;
                    final String alternatingPart = alternating ? "_alternating" : "";
                    final String cutoutSuffix = getCutoutSuffix(trussAxis, shaftAxis);
                    final String modelPath = "block/industrial_truss/industrial_truss" + alternatingPart + cutoutSuffix;
                    return ConfiguredModel.builder()
                            .modelFile(prov.models().getExistingFile(CreateBitsnBobs.asResource(modelPath)))
                            .rotationX(rotX)
                            .rotationY(rotY)
                            .build();
                });
    }

    private static String getCutoutSuffix(final Direction.Axis trussAxis, final Direction.Axis shaftAxis) {
        if (trussAxis == shaftAxis)
            return "";
        if (shaftAxis == Direction.Axis.Y)
            return "_cutout_z";
        if (shaftAxis == Direction.Axis.X)
            return "_cutout_x";
        return trussAxis == Direction.Axis.Y ? "_cutout_z" : "_cutout_x";
    }

    public static <T extends TrussEncasedPipeBlock> void trussEncasedPipeModel(final DataGenContext<Block, T> ctx,
                                                                               final RegistrateBlockstateProvider prov) {
        for (final Direction.Axis trussAxis : Direction.Axis.values()) {
            for (final boolean alternating : new boolean[]{false, true}) {
                final Direction trussDir = Direction.fromAxisAndDirection(trussAxis, Direction.AxisDirection.POSITIVE);
                final int rotX = trussDir.getAxis().isHorizontal() ? 90 : 0;
                final int rotY = trussDir.getAxis().isVertical() ? 0 : (((int) trussDir.toYRot()) + 180) % 360;

                final String altPart = alternating ? "_alternating" : "";

                prov.getMultipartBuilder(ctx.get())
                        .part()
                        .modelFile(prov.models().getExistingFile(
                                CreateBitsnBobs.asResource("block/industrial_truss/industrial_truss_beams")))
                        .rotationX(rotX).rotationY(rotY)
                        .addModel()
                        .condition(TrussEncasedPipeBlock.TRUSS_AXIS, trussAxis)
                        .condition(TrussEncasedPipeBlock.ALTERNATING, alternating)
                        .end();

                final Map<String, Direction> cutoutToWorld = getDirectionMapping(trussAxis);
                for (final Map.Entry<String, Direction> entry : cutoutToWorld.entrySet()) {
                    final String cutoutSuffix = entry.getKey();
                    final Direction worldDir = entry.getValue();
                    final BooleanProperty connectionProp = getConnectionProperty(worldDir);
                    final String modelPath = "block/industrial_truss/industrial_truss" + altPart + "_pipe_cutout_" + cutoutSuffix;

                    prov.getMultipartBuilder(ctx.get())
                            .part()
                            .modelFile(prov.models().getExistingFile(
                                    CreateBitsnBobs.asResource(modelPath)))
                            .rotationX(rotX).rotationY(rotY)
                            .addModel()
                            .condition(TrussEncasedPipeBlock.TRUSS_AXIS, trussAxis)
                            .condition(TrussEncasedPipeBlock.ALTERNATING, alternating)
                            .condition(connectionProp, true)
                            .end();
                }
            }
        }
    }

    private static Map<String, Direction> getDirectionMapping(final Direction.Axis trussAxis) {
        final Map<String, Direction> map = new LinkedHashMap<>();
        switch (trussAxis) {
            case Y:
                map.put("z_pos", Direction.SOUTH);
                map.put("z_neg", Direction.NORTH);
                map.put("x_pos", Direction.EAST);
                map.put("x_neg", Direction.WEST);
                break;
            case X:
                map.put("z_pos", Direction.DOWN);
                map.put("z_neg", Direction.UP);
                map.put("x_pos", Direction.SOUTH);
                map.put("x_neg", Direction.NORTH);
                break;
            case Z:
                map.put("z_pos", Direction.DOWN);
                map.put("z_neg", Direction.UP);
                map.put("x_pos", Direction.EAST);
                map.put("x_neg", Direction.WEST);
                break;
        }
        return map;
    }

    private static BooleanProperty getConnectionProperty(final Direction dir) {
        return PipeBlock.PROPERTY_BY_DIRECTION.get(dir);
    }
}
