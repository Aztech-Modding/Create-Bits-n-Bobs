package com.kipti.bnb.content.kinetics.throttle_lever;

import com.kipti.bnb.registry.client.BnbShapes;
import com.kipti.bnb.registry.content.BnbBlockEntities;
import com.mojang.serialization.MapCodec;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ThrottleLeverBlock extends Block implements IBE<ThrottleLeverBlockEntity> {

    public static final MapCodec<ThrottleLeverBlock> CODEC = simpleCodec(ThrottleLeverBlock::new);

    public static final EnumProperty<AttachFace> FACE = BlockStateProperties.ATTACH_FACE;
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public ThrottleLeverBlock(final Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                                          .setValue(FACE, AttachFace.WALL)
                                          .setValue(FACING, Direction.NORTH)
                                          .setValue(BlockStateProperties.POWER, 0));
    }

    @Override
    protected MapCodec<? extends ThrottleLeverBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACE, FACING, BlockStateProperties.POWER);
    }

    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context) {
        for (final Direction direction : context.getNearestLookingDirections()) {
            final BlockState blockstate;
            if (direction.getAxis() == Direction.Axis.Y) {
                blockstate = this.defaultBlockState()
                        .setValue(FACE, direction == Direction.UP ? AttachFace.CEILING : AttachFace.FLOOR)
                        .setValue(FACING, context.getHorizontalDirection());
            } else {
                blockstate = this.defaultBlockState()
                        .setValue(FACE, AttachFace.WALL)
                        .setValue(FACING, direction.getOpposite());
            }
            if (blockstate.canSurvive(context.getLevel(), context.getClickedPos())) {
                return blockstate;
            }
        }
        return null;
    }

    @Override
    protected boolean canSurvive(final BlockState state, final LevelReader level, final BlockPos pos) {
        final Direction shaftDir = getShaftDirection(state);
        final BlockPos attachedPos = pos.relative(shaftDir);
        return level.getBlockState(attachedPos).isFaceSturdy(level, attachedPos, shaftDir.getOpposite());
    }

    public static Direction getConnectedDirection(final BlockState state) {
        final AttachFace face = state.getValue(FACE);
        return switch (face) {
            case CEILING -> Direction.DOWN;
            case FLOOR -> Direction.UP;
            case WALL -> state.getValue(FACING);
        };
    }

    public static Direction getShaftDirection(final BlockState state) {
        return getConnectedDirection(state).getOpposite();
    }

    @Override
    protected BlockState updateShape(final BlockState state,
                                     final Direction direction,
                                     final BlockState neighborState,
                                     final LevelAccessor level,
                                     final BlockPos pos,
                                     final BlockPos neighborPos) {
        final Direction shaftDir = getShaftDirection(state);

        if (direction == shaftDir && !state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }

        return state;
    }

    @Override
    protected VoxelShape getShape(final BlockState state,
                                  final BlockGetter level,
                                  final BlockPos pos,
                                  final CollisionContext context) {
        final AttachFace face = state.getValue(FACE);
        final Direction facing = state.getValue(FACING);
        return switch (face) {
            case FLOOR -> facing.getAxis() == Direction.Axis.Z
                    ? BnbShapes.THROTTLE_LEVER_FLOOR_Z : BnbShapes.THROTTLE_LEVER_FLOOR_X;
            case CEILING -> facing.getAxis() == Direction.Axis.Z
                    ? BnbShapes.THROTTLE_LEVER_CEILING_Z : BnbShapes.THROTTLE_LEVER_CEILING_X;
            case WALL -> switch (facing) {
                case NORTH -> BnbShapes.THROTTLE_LEVER_WALL_NORTH;
                case SOUTH -> BnbShapes.THROTTLE_LEVER_WALL_SOUTH;
                case EAST -> BnbShapes.THROTTLE_LEVER_WALL_EAST;
                case WEST -> BnbShapes.THROTTLE_LEVER_WALL_WEST;
                default -> BnbShapes.THROTTLE_LEVER_WALL_NORTH;
            };
        };
    }

    @Override
    protected boolean isSignalSource(final BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(final BlockState state,
                            final BlockGetter level,
                            final BlockPos pos,
                            final Direction direction) {
        return state.getValue(BlockStateProperties.POWER);
    }

    @Override
    protected int getDirectSignal(final BlockState state,
                                  final BlockGetter level,
                                  final BlockPos pos,
                                  final Direction direction) {
        return getConnectedDirection(state) == direction ? state.getValue(BlockStateProperties.POWER) : 0;
    }

    @Override
    protected InteractionResult useWithoutItem(final BlockState state,
                                               final Level level,
                                               final BlockPos pos,
                                               final Player player,
                                               final BlockHitResult hitResult) {
        return InteractionResult.SUCCESS;
    }

    @Override
    protected BlockState rotate(final BlockState state, final Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(final BlockState state, final Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
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

