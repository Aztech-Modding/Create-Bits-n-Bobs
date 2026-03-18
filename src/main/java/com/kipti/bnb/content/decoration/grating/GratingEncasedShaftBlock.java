package com.kipti.bnb.content.decoration.grating;

import com.kipti.bnb.content.kinetics.encased_blocks.BnbEncasedShaftBlock;
import com.kipti.bnb.registry.content.blocks.deco.BnbDecorativeBlocks;
import com.simibubi.create.AllShapes;
import net.createmod.catnip.placement.IPlacementHelper;
import net.createmod.catnip.placement.PlacementHelpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

public class GratingEncasedShaftBlock extends BnbEncasedShaftBlock implements IGratingPanel {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    private final int placementHelperId = PlacementHelpers.register(new GratingPanelBlock.PlacementHelper());

    public GratingEncasedShaftBlock(final Properties properties) {
        super(properties, BnbDecorativeBlocks.INDUSTRIAL_GRATING_PANEL::get);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.UP));
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.@NotNull Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    public @NonNull BlockState getStateForPlacement(final @NotNull BlockPlaceContext context) {
        return super.getStateForPlacement(context).setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(final @NotNull ItemStack stack, final @NotNull BlockState state, final @NotNull Level level, final @NotNull BlockPos pos, final Player player, final @NotNull InteractionHand hand, final @NotNull BlockHitResult hitResult) {
        if (!player.isShiftKeyDown() && player.mayBuild()) {
            final IPlacementHelper placementHelper = PlacementHelpers.get(placementHelperId);
            if (placementHelper.matchesItem(stack)) {
                placementHelper.getOffset(player, level, state, pos, hitResult)
                        .placeInWorld(level, (BlockItem) stack.getItem(), player, hand, hitResult);
                return ItemInteractionResult.SUCCESS;
            }
        }

        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected @NotNull VoxelShape getShape(final BlockState state, final @NotNull BlockGetter level, final @NotNull BlockPos pos, final @NotNull CollisionContext context) {
        return Shapes.or(AllShapes.CASING_3PX.get(state.getValue(FACING)), AllShapes.FOUR_VOXEL_POLE.get(state.getValue(AXIS)));
    }

    @Override
    protected boolean isPathfindable(@NotNull final BlockState state, @NotNull final PathComputationType pathComputationType) {
        return false;
    }

    @Override
    protected boolean skipRendering(final BlockState state, final BlockState adjacentState, final Direction direction) {
        return adjacentState.getBlock() instanceof IGratingPanel && (adjacentState.getValue(FACING) == state.getValue(FACING)) || super.skipRendering(state, adjacentState, direction);
    }

}
