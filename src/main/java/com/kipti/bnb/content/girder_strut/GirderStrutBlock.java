package com.kipti.bnb.content.girder_strut;

import com.kipti.bnb.registry.BnbBlockEntities;
import com.kipti.bnb.registry.BnbShapes;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;

public class GirderStrutBlock extends Block implements IBE<GirderStrutBlockEntity>, SimpleWaterloggedBlock, IWrenchable {

    public static final DirectionProperty FACING = DirectionalBlock.FACING;
    public static final int MAX_SPAN = 8;

    private StrutModelType modelType;

    public GirderStrutBlock(final Properties properties, final StrutModelType modelType) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.UP).setValue(WATERLOGGED, false));
        this.modelType = modelType;
    }

    @Override
    protected BlockState rotate(final BlockState state, final Rotation rotation) {
        return super.rotate(state, rotation)
                .setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(final BlockState state, final Mirror mirror) {
        return super.mirror(state, mirror)
                .setValue(FACING, mirror.mirror(state.getValue(FACING)));
    }

    @Override
    public InteractionResult onWrenched(final BlockState state, final UseOnContext context) {
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult onSneakWrenched(final BlockState state, final UseOnContext context) {
        final Player player = context.getPlayer();
        final Level level = context.getLevel();
        final BlockPos pos = context.getClickedPos();
        if (!level.isClientSide) {
            final BlockState currentState = level.getBlockState(pos);
            final ItemStack itemToReturn = new ItemStack(currentState.getBlock());
            destroyConnectedStrut(level, pos, false);
            if (player != null && !player.hasInfiniteMaterials())
                player.getInventory().placeItemBackInInventory(itemToReturn);
        }
        return IWrenchable.super.onSneakWrenched(state, context);
    }

    public static NonNullFunction<Properties, GirderStrutBlock> normal() {
        return properties -> new GirderStrutBlock(properties, StrutModelType.NORMAL);
    }

    public static NonNullFunction<Properties, GirderStrutBlock> weathered() {
        return properties -> new GirderStrutBlock(properties, StrutModelType.WEATHERED);
    }

    public static NonNullFunction<Properties, GirderStrutBlock> wooden() {
        return properties -> new GirderStrutBlock(properties, StrutModelType.WOODEN);
    }

    @Override
    public @NotNull BlockState updateShape(final BlockState state, final @NotNull Direction direction, final @NotNull BlockState neighbourState, final @NotNull LevelAccessor world,
                                           final @NotNull BlockPos pos, final @NotNull BlockPos neighbourPos) {
        if (state.getValue(WATERLOGGED))
            world.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        return state;
    }

    @Override
    public @NotNull FluidState getFluidState(final BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : Fluids.EMPTY.defaultFluidState();
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context) {
        final Level level = context.getLevel();
        final BlockPos pos = context.getClickedPos();
        final FluidState ifluidstate = level.getFluidState(pos);
        final BlockState state = super.getStateForPlacement(context);
        if (state == null)
            return null;
        return state.setValue(FACING, context.getClickedFace()).setValue(WATERLOGGED, ifluidstate.getType() == Fluids.WATER);
    }

    @Override
    public @NotNull VoxelShape getShape(final BlockState state, final @NotNull BlockGetter level, final @NotNull BlockPos pos, final @NotNull CollisionContext context) {
        return BnbShapes.GIRDER_STRUT.get(state.getValue(FACING));
    }

    @Override
    public PushReaction getPistonPushReaction(final @NotNull BlockState state) {
        return PushReaction.NORMAL;
    }

    @Override
    public @NotNull BlockState playerWillDestroy(final @NotNull Level level, final @NotNull BlockPos pos, final @NotNull BlockState state, final Player player) {
        final boolean shouldPreventDrops = player.hasInfiniteMaterials();

        if (shouldPreventDrops && !level.isClientSide) {
            destroyConnectedStrut(level, pos, false);
        }

        return super.playerWillDestroy(level, pos, state, player);
    }

    private void destroyConnectedStrut(final Level level, final BlockPos pos, final boolean dropBlock) {
        withBlockEntityDo(level, pos, (gbe) -> {
            for (BlockPos otherPos : gbe.getConnectionsCopy()) {
                otherPos = otherPos.offset(pos);
                final BlockEntity otherBe = level.getBlockEntity(otherPos);
                if (otherBe instanceof final GirderStrutBlockEntity other) {
                    other.removeConnection(pos);
                    if (other.connectionCount() == 0) {
                        level.destroyBlock(otherPos, dropBlock);
                    }
                }
            }
        });
    }

    @Override
    public void onRemove(final BlockState state, final @NotNull Level level, final @NotNull BlockPos pos, final BlockState newState, final boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (!level.isClientSide) {
                destroyConnectedStrut(level, pos, true);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public Class<GirderStrutBlockEntity> getBlockEntityClass() {
        return GirderStrutBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends GirderStrutBlockEntity> getBlockEntityType() {
        return BnbBlockEntities.GIRDER_STRUT.get();
    }

    public StrutModelType getModelType() {
        return modelType;
    }

    public void setModelType(final StrutModelType modelType) {
        this.modelType = modelType;
    }
}
