package com.kipti.bnb.content.chair;

import com.kipti.bnb.registry.BnbBlocks;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import com.simibubi.create.content.contraptions.actors.seat.SeatEntity;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.utility.BlockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.util.FakePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ChairBlock extends SeatBlock implements IWrenchable {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public static final BooleanProperty LEFT_ARM = BooleanProperty.create("left_arm");
    public static final BooleanProperty RIGHT_ARM = BooleanProperty.create("right_arm");
    public static final BooleanProperty BACK_FLAT = BooleanProperty.create("back_flat");
    public static final BooleanProperty FORCED_BACK_FLAT = BooleanProperty.create("forced_back_flat");
    public static final BooleanProperty INVERTED_CORNER = BooleanProperty.create("inverted_corner");
    public static final BooleanProperty CORNER = BooleanProperty.create("corner");

    public ChairBlock(final Properties properties, final DyeColor color) {
        super(properties, color);
        this.registerDefaultState(
                defaultBlockState()
                        .setValue(LEFT_ARM, true)
                        .setValue(RIGHT_ARM, true)
                        .setValue(CORNER, false)
                        .setValue(BACK_FLAT, false)
                        .setValue(FORCED_BACK_FLAT, false)
                        .setValue(INVERTED_CORNER, false)
        );
    }

    @Override
    public InteractionResult onWrenched(final BlockState state, final UseOnContext context) {
        if (context.getLevel().isClientSide)
            return InteractionResult.SUCCESS;
        final BlockState newState = state.cycle(FORCED_BACK_FLAT);
        final boolean isForcedFlat = newState.getValue(FORCED_BACK_FLAT);
        context.getLevel().setBlockAndUpdate(context.getClickedPos(), newState);
        context.getLevel().playSound(null, context.getClickedPos(), isForcedFlat ? SoundEvents.WOODEN_TRAPDOOR_CLOSE : SoundEvents.WOODEN_TRAPDOOR_OPEN, SoundSource.BLOCKS, 1.0f, 1.0f);
        return InteractionResult.SUCCESS;
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, Player player, @NotNull InteractionHand hand,
                                          @NotNull BlockHitResult p_225533_6_) {
        if (player.isShiftKeyDown() || player instanceof FakePlayer)
            return InteractionResult.PASS;

        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(AllItems.WRENCH.get())) {
            return InteractionResult.PASS;
        }

        DyeColor color = DyeColor.getColor(stack);

        if (color != null && color != this.color) {
            if (level.isClientSide)
                return InteractionResult.SUCCESS;
            BlockState newState = BlockHelper.copyProperties(state, BnbBlocks.CHAIRS.get(color)
                    .getDefaultState());
            level.setBlockAndUpdate(pos, newState);
            return InteractionResult.SUCCESS;
        }

        final List<SeatEntity> seats = level.getEntitiesOfClass(SeatEntity.class, new AABB(pos));
        if (!seats.isEmpty()) {
            final SeatEntity seatEntity = seats.get(0);
            final List<Entity> passengers = seatEntity.getPassengers();
            if (!passengers.isEmpty() && passengers.get(0) instanceof Player)
                return InteractionResult.PASS;
            if (!level.isClientSide) {
                seatEntity.ejectPassengers();
                player.startRiding(seatEntity);
            }
            return InteractionResult.SUCCESS;
        }

        if (level.isClientSide)
            return InteractionResult.SUCCESS;
        sitDown(level, pos, getLeashed(level, player).or(player));
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.@NotNull Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(FACING, LEFT_ARM, RIGHT_ARM, CORNER, BACK_FLAT, FORCED_BACK_FLAT, INVERTED_CORNER);
    }

    @Override
    public @NotNull BlockState getStateForPlacement(final @NotNull BlockPlaceContext pContext) {
        return calculateShape(super.getStateForPlacement(pContext).setValue(FACING, pContext.getHorizontalDirection().getOpposite()), pContext.getLevel(), pContext.getClickedPos());
    }

    @Override
    public @NotNull BlockState updateShape(final @NotNull BlockState pState, final @NotNull Direction pDirection, final @NotNull BlockState pNeighborState, final @NotNull LevelAccessor pLevel, final @NotNull BlockPos pCurrentPos, final @NotNull BlockPos pNeighborPos) {
        final BlockState blockState = super.updateShape(pState, pDirection, pNeighborState, pLevel, pCurrentPos, pNeighborPos);
        return calculateShape(blockState, pLevel, pCurrentPos);
    }

    private @NotNull BlockState calculateShape(final BlockState blockState, final LevelAccessor pLevel, final BlockPos pCurrentPos) {
        final Direction facing = blockState.getValue(FACING);

        final BlockState stateLeft = pLevel.getBlockState(pCurrentPos.relative(facing.getClockWise()));
        final boolean hasConnectableLeft = AllBlocks.SEATS.contains(stateLeft.getBlock()) || stateLeft.getBlock() instanceof ChairBlock &&
                (stateLeft.getValue(FACING) == facing || stateLeft.getValue(FACING) == facing.getCounterClockWise() || stateLeft.getValue(INVERTED_CORNER));

        final BlockState stateRight = pLevel.getBlockState(pCurrentPos.relative(facing.getCounterClockWise()));
        final boolean hasConnectableRight = AllBlocks.SEATS.contains(stateRight.getBlock()) || stateRight.getBlock() instanceof ChairBlock &&
                (stateRight.getValue(FACING) == facing || stateRight.getValue(FACING) == facing.getClockWise() || stateRight.getValue(INVERTED_CORNER));

        final BlockState stateFront = pLevel.getBlockState(pCurrentPos.relative(facing));
        final boolean hasConnectableFront = stateFront.getBlock() instanceof ChairBlock &&
                (stateFront.getValue(FACING) == facing.getCounterClockWise() || stateFront.getValue(FACING) == facing.getClockWise());

        final BlockState stateBack = pLevel.getBlockState(pCurrentPos.relative(facing.getOpposite()));
//        boolean isFlatBack = stateBack.getBlock() instanceof ChairBlock &&
//            (stateBack.getValue(FACING) == facing.getOpposite() || stateBack.getValue(CORNER) && stateBack.getValue(FACING) != facing);

        boolean isFlatBack = BnbBlocks.CHAIRS.contains(stateBack.getBlock()) || stateBack.isCollisionShapeFullBlock(pLevel, pCurrentPos.relative(facing.getOpposite()));
        final boolean isInvertedCorner = stateBack.getBlock() instanceof ChairBlock &&
                (stateBack.getValue(FACING).getAxis() != facing.getAxis()) && (hasConnectableRight || hasConnectableLeft);

        final boolean isCorner = hasConnectableFront && (hasConnectableLeft != hasConnectableRight);
        if (hasConnectableFront) {
            final Direction cornerFacing = hasConnectableLeft ? facing.getCounterClockWise() : facing.getClockWise();
            final BlockPos cornerPos = pCurrentPos.relative(cornerFacing);
            final BlockState stateBackCorner = pLevel.getBlockState(cornerPos);
            isFlatBack = isFlatBack || (isCorner && (BnbBlocks.CHAIRS.contains(stateBackCorner.getBlock()) || stateBackCorner.isCollisionShapeFullBlock(pLevel, cornerPos)));
        }

        return blockState
                .setValue(LEFT_ARM, !hasConnectableLeft)
                .setValue(RIGHT_ARM, !hasConnectableRight)
                .setValue(CORNER, isCorner)
                .setValue(BACK_FLAT, isFlatBack)
                .setValue(INVERTED_CORNER, isInvertedCorner);
    }

    private static boolean getFlatObstructing(final LevelAccessor pLevel, final BlockState thisState, final BlockPos pCurrentPos, final Direction facing, final BlockState blockState, final BooleanProperty property) {
        final BlockState sideState = pLevel.getBlockState(pCurrentPos.relative(facing));
        return (sideState.getBlock() instanceof ChairBlock &&
                (sideState.getValue(FACING) == facing));
    }

    @Override
    public @NotNull BlockState rotate(final @NotNull BlockState state, final @NotNull Rotation rotation) {
        return super.rotate(state, rotation)
                .setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    //    private static boolean getFlatObstructing(LevelAccessor pLevel, BlockState thisState, BlockPos pCurrentPos, Direction facing, BlockState blockState, BooleanProperty property) {
//        BlockState sideState = pLevel.getBlockState(pCurrentPos.relative(facing));
//        return (sideState.getBlock() instanceof ChairBlock &&
//            (sideState.getValue(FACING) == facing));
//    }
//
//    private static @NotNull BlockState checkForFlatObstructing(LevelAccessor pLevel, BlockState thisState, BlockPos pCurrentPos, Direction facing, BlockState blockState, BooleanProperty property) {
//        BlockState sideState = pLevel.getBlockState(pCurrentPos.relative(facing));
//        blockState = blockState.setValue(property, (sideState.getBlock() instanceof ChairBlock && (sideState.getValue(FACING) == facing)));
//        return blockState;
//    }
//
//    private static @NotNull BlockState checkForObstructing(LevelAccessor pLevel, BlockState thisState, BlockPos pCurrentPos, Direction facing, BlockState blockState, BooleanProperty property) {
//        BlockState sideState = pLevel.getBlockState(pCurrentPos.relative(facing));
//        blockState = blockState.setValue(property,
//            !(sideState.getBlock() instanceof ChairBlock && (sideState.getValue(FACING) == thisState.getValue(FACING) || sideState.getValue(FACING) == facing.getOpposite())));
//        return blockState;
//    }

}
