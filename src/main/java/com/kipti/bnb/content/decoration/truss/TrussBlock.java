package com.kipti.bnb.content.decoration.truss;

import com.kipti.bnb.registry.client.BnbShapes;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.NonNull;

public class TrussBlock extends RotatedPillarBlock implements IWrenchable {

    public TrussBlock(final Properties properties) {
        super(properties);
    }

    @Override
    protected @NonNull VoxelShape getShape(final BlockState p_60555_,
                                           final @NonNull BlockGetter p_60556_,
                                           final @NonNull BlockPos p_60557_,
                                           final @NonNull CollisionContext p_60558_) {
        return BnbShapes.TRUSS.get(p_60555_.getValue(AXIS));
    }

}

