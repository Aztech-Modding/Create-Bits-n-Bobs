package com.kipti.bnb.content.decoration.truss;

import com.kipti.bnb.CreateBitsnBobs;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;

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
            return "_cutout_x";
        if (shaftAxis == Direction.Axis.X)
            return "_cutout_z";
        return trussAxis == Direction.Axis.Y ? "_cutout_x" : "_cutout_z";
    }
}
