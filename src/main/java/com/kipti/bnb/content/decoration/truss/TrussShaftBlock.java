package com.kipti.bnb.content.decoration.truss;

import com.kipti.bnb.content.kinetics.encased_blocks.BnbEncasedShaftBlock;
import com.kipti.bnb.registry.client.BnbShapes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Supplier;

public class TrussShaftBlock extends BnbEncasedShaftBlock {

    public TrussShaftBlock(final Properties properties,
                           final Supplier<Block> casing) {
        super(properties, casing);
    }

    @Override
    protected VoxelShape getShape(final BlockState p_60555_,
                                  final BlockGetter p_60556_,
                                  final BlockPos p_60557_,
                                  final CollisionContext p_60558_) {
        return BnbShapes.TRUSS.get(p_60555_.getValue(AXIS));
    }

}
