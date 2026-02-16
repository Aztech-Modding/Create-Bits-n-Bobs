package com.kipti.bnb.content.kinetics.throttle_lever;

import com.kipti.bnb.registry.BnbBlockEntities;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.NotNull;

public class ThrottleLeverBlock extends DirectionalKineticBlock implements IBE<ThrottleLeverBlockEntity> {

    public static final BooleanProperty HAS_SHAFT = BooleanProperty.create("has_shaft");

    public ThrottleLeverBlock(final Properties p_i48402_1_) {
        super(p_i48402_1_);
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HAS_SHAFT, BlockStateProperties.POWER);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING).getAxis();
    }

    @Override
    public boolean hasShaftTowards(final LevelReader world, final BlockPos pos, final BlockState state, final Direction face) {
        return face == state.getValue(FACING).getOpposite();
    }

    @Override
    protected @NotNull BlockState updateShape(final BlockState state, final @NotNull Direction direction, final @NotNull BlockState neighborState, final @NotNull LevelAccessor level, final @NotNull BlockPos pos, final @NotNull BlockPos neighborPos) {
        return state.setValue(HAS_SHAFT,
                direction == state.getValue(FACING).getOpposite()
                        && neighborState.getBlock() instanceof final IRotate rotatingNeighbor
                        && rotatingNeighbor.getRotationAxis(neighborState) == state.getValue(FACING).getAxis()
        );
    }

    @Override
    public Class<ThrottleLeverBlockEntity> getBlockEntityClass() {
        return ThrottleLeverBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ThrottleLeverBlockEntity> getBlockEntityType() {
        return BnbBlockEntities.THROTTLE_LEVER.get();
    }
}
