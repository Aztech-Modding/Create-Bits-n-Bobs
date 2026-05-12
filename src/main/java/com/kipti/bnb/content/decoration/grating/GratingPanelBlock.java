package com.kipti.bnb.content.decoration.grating;

import com.simibubi.create.AllShapes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GratingPanelBlock extends GratingBlock implements IGratingPanel {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public GratingPanelBlock(final Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.UP));
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.@NotNull Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(final @NotNull BlockPlaceContext context) {
        return super.getStateForPlacement(context).setValue(FACING, this.getPanelFacing(context.getPlayer()));
    }

    @Override
    protected @NotNull VoxelShape getShape(final BlockState state,
                                           final @NotNull BlockGetter level,
                                           final @NotNull BlockPos pos,
                                           final @NotNull CollisionContext context) {
        return AllShapes.CASING_3PX.get(state.getValue(FACING));
    }

    @Override
    protected boolean isPathfindable(@NotNull final BlockState state,
                                     @NotNull final PathComputationType pathComputationType) {
        return false;
    }

    @Override
    protected boolean skipRendering(final BlockState state, final BlockState adjacentState, final Direction direction) {
        return adjacentState.getBlock() instanceof IGratingPanel && (adjacentState.getValue(FACING) == state.getValue(
                FACING)) || super.skipRendering(state, adjacentState, direction);
    }

    private Direction getPanelFacing(final @Nullable Player player) {
        return player == null ? Direction.UP : player.getNearestViewDirection().getOpposite();
    }

}

