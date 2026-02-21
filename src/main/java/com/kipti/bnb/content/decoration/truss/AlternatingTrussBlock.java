package com.kipti.bnb.content.decoration.truss;

import com.kipti.bnb.content.decoration.nixie.nixie_board.NixieBoardBlockNixie;
import com.kipti.bnb.registry.BnbBlocks;
import com.kipti.bnb.registry.BnbDecoBlocks;
import com.simibubi.create.content.kinetics.simpleRelays.AbstractSimpleShaftBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;
import com.simibubi.create.content.kinetics.steamEngine.PoweredShaftBlock;
import com.simibubi.create.foundation.placement.PoleHelper;
import net.createmod.catnip.placement.IPlacementHelper;
import net.createmod.catnip.placement.PlacementHelpers;
import net.createmod.catnip.placement.PlacementOffset;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class AlternatingTrussBlock extends RotatedPillarBlock {

    public static final BooleanProperty ALTERNATING = BooleanProperty.create("alternating");
    private static final int placementHelperId = PlacementHelpers.register(new PlacementHelper());

    public AlternatingTrussBlock(final Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(ALTERNATING, false));
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.@NotNull Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ALTERNATING);
    }

    @Override
    protected @NotNull BlockState updateShape(final BlockState state, final @NotNull Direction direction, final @NotNull BlockState neighborState, final @NotNull LevelAccessor level, final @NotNull BlockPos pos, final @NotNull BlockPos neighborPos) {
        final Direction positiveAxis = Direction.get(Direction.AxisDirection.POSITIVE, state.getValue(AXIS));
        if (direction == positiveAxis) {
            final boolean isAlternating = neighborState.getBlock() instanceof AlternatingTrussBlock && neighborState.getValue(ALTERNATING);
            return state.setValue(ALTERNATING, !isAlternating);
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public @NotNull BlockState getStateForPlacement(final @NotNull BlockPlaceContext context) {
        final BlockState state = super.getStateForPlacement(context);
        final Direction positiveAxis = Direction.get(Direction.AxisDirection.POSITIVE, state.getValue(AXIS));
        final BlockState neighborState = context.getLevel().getBlockState(context.getClickedPos().relative(positiveAxis));
        final boolean isAlternating = neighborState.getBlock() instanceof AlternatingTrussBlock && neighborState.getValue(ALTERNATING);
        return state.setValue(ALTERNATING, !isAlternating);
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(final ItemStack stack, final @NotNull BlockState state, final @NotNull Level level, final @NotNull BlockPos pos, final @NotNull Player player, final @NotNull InteractionHand hand, final @NotNull BlockHitResult hitResult) {
        final ItemStack heldItem = player.getItemInHand(hand);

        final IPlacementHelper placementHelper = PlacementHelpers.get(placementHelperId);
        if (!player.isShiftKeyDown() && player.mayBuild()) {
            if (placementHelper.matchesItem(stack)) {
                placementHelper.getOffset(player, level, state, pos, hitResult)
                        .placeInWorld(level, (BlockItem) stack.getItem(), player, hand, hitResult);
                return ItemInteractionResult.SUCCESS;
            }
        }

        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }


    @MethodsReturnNonnullByDefault
    private static class PlacementHelper extends PoleHelper<Direction.Axis> implements IPlacementHelper {

        private PlacementHelper() {
            super(state -> state.getBlock() instanceof AlternatingTrussBlock || state.getBlock() instanceof AlternatingTrussBlock, state -> state.getValue(AXIS), AXIS);
        }

        @Override
        public Predicate<ItemStack> getItemPredicate() {
            return BnbDecoBlocks.INDUSTRIAL_TRUSS::isIn;
        }

        @Override
        public Predicate<BlockState> getStatePredicate() {
            return s -> s.getBlock() instanceof AlternatingTrussBlock;
        }

        @Override
        public PlacementOffset getOffset(Player player, Level world, BlockState state, BlockPos pos,
                                         BlockHitResult ray) {
            PlacementOffset offset = super.getOffset(player, world, state, pos, ray);
            if (offset.isSuccessful())
                offset.withTransform(offset.getTransform()
                        .andThen(s -> world.isClientSide() ? s
                                : ShaftBlock.pickCorrectShaftType(s, world, offset.getBlockPos())));
            return offset;
        }
    }
}
