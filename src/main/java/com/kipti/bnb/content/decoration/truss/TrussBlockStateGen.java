package com.kipti.bnb.content.decoration.truss;

import com.kipti.bnb.foundation.client.block_state_gen.BnbBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;

public class TrussBlockStateGen {

    public static <T extends TrussBlock> void alternatingTrussModel(final DataGenContext<Block, T> ctx,
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
                                            TrussBlock.ALTERNATING) ? "_alternating" : ""))
                            ))
                            .rotationX(dir == Direction.DOWN ? 180 : dir.getAxis().isHorizontal() ? 90 : 0)
                            .rotationY(dir.getAxis().isVertical() ? 0 : (((int) dir.toYRot()) + BnbBlockStateGen.DEFAULT_ANGLE_OFFSET) % 360)
                            .build();
                });
    }
    
}
