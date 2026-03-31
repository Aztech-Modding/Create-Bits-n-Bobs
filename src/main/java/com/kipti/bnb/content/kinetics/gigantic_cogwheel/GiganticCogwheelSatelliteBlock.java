package com.kipti.bnb.content.kinetics.gigantic_cogwheel;

import com.kipti.bnb.registry.content.blocks.BnbKineticBlocks;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;

/**
 * Satellite block for the Gigantic Cogwheel multiblock.
 *
 * Each satellite stores a FACING pointing back toward the center block.
 * If the center is removed, the satellite removes itself (via scheduled tick).
 * Renders as invisible — the center's BE renderer draws the whole 3x3 structure.
 */
public class GiganticCogwheelSatelliteBlock extends DirectionalBlock {

    public static final MapCodec<GiganticCogwheelSatelliteBlock> CODEC =
            simpleCodec(GiganticCogwheelSatelliteBlock::new);

    public GiganticCogwheelSatelliteBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(FACING));
    }

    // -------------------------------------------------------------------------
    // Factory helper — build a satellite state whose FACING points at centerPos
    // -------------------------------------------------------------------------

    public static BlockState statePointingTo(BlockGetter level, BlockPos satellitePos, BlockPos centerPos) {
        BlockPos delta = centerPos.subtract(satellitePos);
        // Find the dominant axis direction
        for (Direction dir : Direction.values()) {
            if (dir.getNormal().equals(delta)) return BnbKineticBlocks.GIGANTIC_COGWHEEL_SATELLITE
                    .getDefaultState().setValue(FACING, dir);
        }
        // Diagonal case (corner satellites) — pick the direction closest to delta
        Direction best = Direction.NORTH;
        double bestDot = Double.NEGATIVE_INFINITY;
        for (Direction dir : Direction.values()) {
            double dot = dir.getStepX() * delta.getX()
                    + dir.getStepY() * delta.getY()
                    + dir.getStepZ() * delta.getZ();
            if (dot > bestDot) { bestDot = dot; best = dir; }
        }
        return BnbKineticBlocks.GIGANTIC_COGWHEEL_SATELLITE.getDefaultState().setValue(FACING, best);
    }

    // -------------------------------------------------------------------------
    // Validity — is the block this satellite points at still a valid center?
    // -------------------------------------------------------------------------

    public boolean stillValid(BlockGetter level, BlockPos pos, BlockState state) {
        if (!state.is(this)) return false;
        BlockPos centerPos = getCenterPos(level, pos, state);
        BlockState centerState = level.getBlockState(centerPos);
        return centerState.getBlock() instanceof GiganticCogwheelBlock;
    }

    /** Walk FACING chain until we reach the actual center block. */
    public static BlockPos getCenterPos(BlockGetter level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(FACING);
        BlockPos next = pos.relative(facing);
        BlockState nextState = level.getBlockState(next);
        // If the neighbor is also a satellite, recurse (handles corner -> edge -> center)
        if (nextState.getBlock() instanceof GiganticCogwheelSatelliteBlock)
            return getCenterPos(level, next, nextState);
        return next;
    }

    // -------------------------------------------------------------------------
    // Destruction — when a satellite is removed, destroy the center
    // -------------------------------------------------------------------------

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (stillValid(level, pos, state))
            level.destroyBlock(getCenterPos(level, pos, state), true);
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (stillValid(level, pos, state)) {
            BlockPos centerPos = getCenterPos(level, pos, state);
            if (!level.isClientSide() && player.isCreative())
                level.destroyBlock(centerPos, false);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    // -------------------------------------------------------------------------
    // Shape updates — if the center disappears, schedule self-removal
    // -------------------------------------------------------------------------

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState,
                                  LevelAccessor level, BlockPos pos, BlockPos facingPos) {
        if (!stillValid(level, pos, state)) {
            if (!(level instanceof Level lvl) || !lvl.isClientSide())
                if (!level.getBlockTicks().hasScheduledTick(pos, this))
                    level.scheduleTick(pos, this, 1);
        }
        return state;
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!stillValid(level, pos, state))
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
    }

    // -------------------------------------------------------------------------
    // Misc
    // -------------------------------------------------------------------------

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target,
                                       net.minecraft.world.level.LevelReader level,
                                       BlockPos pos, Player player) {
        // Picking a satellite gives you the center item
        return BnbKineticBlocks.GIGANTIC_COGWHEEL.asStack();
    }

    @Override
    protected MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                                       Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.isEmpty())
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (level.isClientSide)
            return (stack.is(ItemTags.PLANKS) || stack.is(ItemTags.LOGS))
                    ? ItemInteractionResult.SUCCESS
                    : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        BlockPos centerPos = getCenterPos(level, pos, state);
        if (!(level.getBlockEntity(centerPos) instanceof GiganticCogwheelBlockEntity centerBe))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        boolean applied = centerBe.tryApplyTextureFromItem(stack);
        if (applied && !level.isClientSide)
            centerBe.notifyUpdate();

        return applied ? ItemInteractionResult.SUCCESS : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
}