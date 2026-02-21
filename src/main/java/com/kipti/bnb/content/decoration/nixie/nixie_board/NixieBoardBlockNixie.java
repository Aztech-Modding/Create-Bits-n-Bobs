package com.kipti.bnb.content.decoration.nixie.nixie_board;

import com.kipti.bnb.content.decoration.nixie.foundation.*;
import com.kipti.bnb.registry.content.BnbBlockEntitiesBootstrap;
import com.kipti.bnb.registry.content.BnbBlocksBootstrap;
import com.kipti.bnb.registry.client.BnbShapes;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
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
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public class NixieBoardBlockNixie extends GenericNixieDisplayBlock implements IBE<GenericNixieDisplayBlockEntity>, IWrenchable, DyeProviderBlock, IGenericNixieDisplayBlock {

    public static final BooleanProperty LEFT = BooleanProperty.create("left");
    public static final BooleanProperty RIGHT = BooleanProperty.create("right");
    public static final BooleanProperty BOTTOM = BooleanProperty.create("bottom");
    public static final BooleanProperty TOP = BooleanProperty.create("top");

    private final @Nullable DyeColor dyeColor;
    private static final int placementHelperId = PlacementHelpers.register(new PlacementHelper());

    public NixieBoardBlockNixie(final Properties p_52591_, @Nullable final DyeColor dyeColor) {
        super(p_52591_);
        this.dyeColor = dyeColor;
    }

    @Override
    public @NotNull ItemStack getCloneItemStack(final @NotNull BlockState state, final @NotNull HitResult target, final @NotNull LevelReader level, final @NotNull BlockPos pos, final @NotNull Player player) {
        return BnbBlocksBootstrap.NIXIE_BOARD.asItem().getDefaultInstance();
    }

    @Override
    public @Nullable BlockState getStateForPlacement(final BlockPlaceContext context) {
        final BlockState state = super.getStateForPlacement(context);
        if (state == null) {
            return null;
        }
        return getConnectedState(context.getLevel(), state, state.getValue(ORIENTATION), state.getValue(FACING), context.getClickedPos());
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.@NotNull Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LEFT, RIGHT, BOTTOM, TOP);
    }

    @Override
    protected @NotNull BlockState updateShape(final @NotNull BlockState state, final @NotNull Direction direction, final @NotNull BlockState neighborState, final @NotNull LevelAccessor level, final @NotNull BlockPos pos, final @NotNull BlockPos neighborPos) {
        final Direction left = DoubleOrientedDirections.getLeft(state);
        final Direction right = left.getOpposite();
        final Direction bottom = state.getValue(FACING).getOpposite();
        final Direction top = state.getValue(FACING);

        if (direction == left) {
            return state.setValue(LEFT, GenericNixieDisplayBlockEntity.areStatesComprableForConnection(state, neighborState));
        } else if (direction == right) {
            return state.setValue(RIGHT, GenericNixieDisplayBlockEntity.areStatesComprableForConnection(state, neighborState));
        } else if (direction == bottom) {
            return state.setValue(BOTTOM, GenericNixieDisplayBlockEntity.areStatesComprableForConnection(state, neighborState));
        } else if (direction == top) {
            return state.setValue(TOP, GenericNixieDisplayBlockEntity.areStatesComprableForConnection(state, neighborState));
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
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

    @Override
    protected @NotNull VoxelShape getShape(final BlockState state, final @NotNull BlockGetter level, final @NotNull BlockPos pos, final @NotNull CollisionContext context) {
        final Direction frontTarget = DoubleOrientedDirections.getFront(state.getValue(FACING), state.getValue(ORIENTATION));
        final boolean isFront = frontTarget.getAxis() == state.getValue(ORIENTATION).getAxis();
        return isFront ? BnbShapes.NIXIE_BOARD_SIDE.get(state.getValue(FACING))
                : BnbShapes.NIXIE_BOARD_FRONT.get(state.getValue(FACING));
    }

    @Override
    public Class<GenericNixieDisplayBlockEntity> getBlockEntityClass() {
        return GenericNixieDisplayBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends GenericNixieDisplayBlockEntity> getBlockEntityType() {
        return BnbBlockEntitiesBootstrap.GENERIC_NIXIE_DISPLAY.get();
    }

    public DyeColor getDyeColor() {
        return dyeColor != null ? dyeColor : DyeColor.ORANGE;
    }

    @Override
    public List<GenericNixieDisplayBlockEntity.ConfigurableDisplayOptions> getPossibleDisplayOptions() {
        return List.of(GenericNixieDisplayBlockEntity.ConfigurableDisplayOptions.NONE, GenericNixieDisplayBlockEntity.ConfigurableDisplayOptions.DOUBLE_CHAR, GenericNixieDisplayBlockEntity.ConfigurableDisplayOptions.DOUBLE_CHAR_DOUBLE_LINES);
    }

    @MethodsReturnNonnullByDefault
    private static class PlacementHelper implements IPlacementHelper {
        @Override
        public Predicate<ItemStack> getItemPredicate() {
            return BnbBlocksBootstrap.NIXIE_BOARD::isIn;
        }

        @Override
        public Predicate<BlockState> getStatePredicate() {
            return s -> s.getBlock() instanceof NixieBoardBlockNixie;
        }

        @Override
        public PlacementOffset getOffset(@NotNull final Player player, @NotNull final Level world, final BlockState state, @NotNull final BlockPos pos, final BlockHitResult ray) {
            final List<Direction> directions = IPlacementHelper.orderedByDistanceExceptAxis(pos, ray.getLocation(),
                    state.getValue(NixieBoardBlockNixie.ORIENTATION).getAxis(), dir -> world.getBlockState(pos.relative(dir)).canBeReplaced());

            if (directions.isEmpty()) {
                return PlacementOffset.fail();
            } else {
                final BlockPos newPos = pos.relative(directions.getFirst());

                return PlacementOffset.success(newPos, sourceState ->
                        getConnectedState(world, state, state.getValue(ORIENTATION), state.getValue(FACING), newPos));
            }
        }
    }

    private static @NotNull BlockState getConnectedState(@NotNull final Level world, BlockState state, final Direction orientation, final Direction facing, final BlockPos position) {
        state = state
                .setValue(ORIENTATION, orientation)
                .setValue(FACING, facing);
        final Direction left = DoubleOrientedDirections.getLeft(state);
        final Direction below = facing.getOpposite();
        return state
                .setValue(LEFT, GenericNixieDisplayBlockEntity.areStatesComprableForConnection(state, world.getBlockState(position.relative(left))))
                .setValue(RIGHT, GenericNixieDisplayBlockEntity.areStatesComprableForConnection(state, world.getBlockState(position.relative(left.getOpposite()))))
                .setValue(BOTTOM, GenericNixieDisplayBlockEntity.areStatesComprableForConnection(state, world.getBlockState(position.relative(below))))
                .setValue(TOP, GenericNixieDisplayBlockEntity.areStatesComprableForConnection(state, world.getBlockState(position.relative(below.getOpposite()))));
    }
}

