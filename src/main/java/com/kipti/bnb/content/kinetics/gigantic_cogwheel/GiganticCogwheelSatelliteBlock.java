package com.kipti.bnb.content.kinetics.gigantic_cogwheel;

import com.kipti.bnb.registry.content.blocks.BnbKineticBlocks;
import com.kipti.bnb.registry.client.BnbShapes;
import com.mojang.serialization.MapCodec;
import com.simibubi.create.api.equipment.goggles.IProxyHoveringInformation;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.render.MultiPosDestructionHandler;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;

import java.util.HashSet;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Structural satellite block forming the outer ring of the 3×3 gigantic cogwheel multiblock.
 * Each satellite's {@link #FACING} points toward the center {@link GiganticCogwheelBlock}.
 * Delegates wrench interactions and hover information to the center block.
 */
public class GiganticCogwheelSatelliteBlock extends DirectionalBlock implements IWrenchable, IProxyHoveringInformation {

    public static final MapCodec<GiganticCogwheelSatelliteBlock> CODEC =
            simpleCodec(GiganticCogwheelSatelliteBlock::new);

    public GiganticCogwheelSatelliteBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(FACING));
    }

    public static BlockState statePointingTo(BlockGetter level, BlockPos satellitePos, BlockPos centerPos) {
        BlockPos delta = centerPos.subtract(satellitePos);
        for (Direction dir : Direction.values()) {
            if (dir.getNormal().equals(delta)) return BnbKineticBlocks.GIGANTIC_COGWHEEL_SATELLITE
                    .getDefaultState().setValue(FACING, dir);
        }
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

    public boolean stillValid(BlockGetter level, BlockPos pos, BlockState state) {
        if (!state.is(this)) return false;
        BlockPos centerPos = getCenterPos(level, pos, state);
        BlockState centerState = level.getBlockState(centerPos);
        if (!(centerState.getBlock() instanceof GiganticCogwheelBlock gigantic)) return false;
        Direction.Axis satelliteAxis = state.getValue(FACING).getAxis();
        Direction.Axis cogAxis = gigantic.getRotationAxis(centerState);
        return satelliteAxis != cogAxis;
    }

    public static BlockPos getCenterPos(BlockGetter level, BlockPos pos, BlockState state) {
        return getCenterPos(level, pos, state, 3);
    }

    private static BlockPos getCenterPos(BlockGetter level, BlockPos pos, BlockState state, int maxDepth) {
        Direction facing = state.getValue(FACING);
        BlockPos next = pos.relative(facing);
        if (maxDepth <= 0) return next;
        BlockState nextState = level.getBlockState(next);
        if (nextState.getBlock() instanceof GiganticCogwheelSatelliteBlock)
            return getCenterPos(level, next, nextState, maxDepth - 1);
        return next;
    }

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
        return BnbKineticBlocks.GIGANTIC_COGWHEEL.asStack();
    }

    @Override
    protected MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                                       Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!this.stillValid(level, pos, state))
            return ItemInteractionResult.FAIL;
        BlockPos centerPos = getCenterPos(level, pos, state);
        if (!(level.getBlockEntity(centerPos) instanceof GiganticCogwheelBlockEntity centerBe))
            return ItemInteractionResult.FAIL;
        return centerBe.applyMaterialIfValid(stack);
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
        BlockPos clickedPos = context.getClickedPos();
        Level level = context.getLevel();
        if (this.stillValid(level, clickedPos, state)) {
            BlockPos masterPos = getCenterPos(level, clickedPos, state);
            context = new UseOnContext(level, context.getPlayer(), context.getHand(), context.getItemInHand(),
                new BlockHitResult(context.getClickLocation(), context.getClickedFace(), masterPos, context.isInside()));
            state = level.getBlockState(masterPos);
        }
        return IWrenchable.super.onSneakWrenched(state, context);
    }

    @Override
    public BlockPos getInformationSource(Level level, BlockPos pos, BlockState state) {
        return this.stillValid(level, pos, state) ? getCenterPos(level, pos, state) : pos;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        BlockPos centerPos = getCenterPos(level, pos, state);
        BlockState centerState = level.getBlockState(centerPos);
        if (centerState.getBlock() instanceof GiganticCogwheelBlock gigantic) {
            Direction.Axis axis = gigantic.getRotationAxis(centerState);
            return BnbShapes.GIGANTIC_COGWHEEL_SATELLITE.get(axis);
        }
        return BnbShapes.GIGANTIC_COGWHEEL_SATELLITE.get(Direction.Axis.Y);
    }

    @Override
    public boolean addLandingEffects(BlockState state1, ServerLevel level, BlockPos pos, BlockState state2,
                                     LivingEntity entity, int numberOfParticles) {
        return true;
    }

    @Override
    public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return false;
    }

    public static class RenderProperties implements IClientBlockExtensions, MultiPosDestructionHandler {

        @Override
        public boolean addDestroyEffects(BlockState state, Level level, BlockPos pos, ParticleEngine manager) {
            return true;
        }

        @Override
        public boolean addHitEffects(BlockState state, Level level, HitResult target, ParticleEngine manager) {
            if (target instanceof BlockHitResult bhr) {
                BlockPos targetPos = bhr.getBlockPos();
                GiganticCogwheelSatelliteBlock satellite = BnbKineticBlocks.GIGANTIC_COGWHEEL_SATELLITE.get();
                if (satellite.stillValid(level, targetPos, state))
                    manager.crack(GiganticCogwheelSatelliteBlock.getCenterPos(level, targetPos, state), bhr.getDirection());
                return true;
            }
            return IClientBlockExtensions.super.addHitEffects(state, level, target, manager);
        }

        @Override
        @Nullable
        public Set<BlockPos> getExtraPositions(ClientLevel level, BlockPos pos, BlockState blockState, int progress) {
            GiganticCogwheelSatelliteBlock satellite = BnbKineticBlocks.GIGANTIC_COGWHEEL_SATELLITE.get();
            if (!satellite.stillValid(level, pos, blockState))
                return null;
            HashSet<BlockPos> set = new HashSet<>();
            set.add(GiganticCogwheelSatelliteBlock.getCenterPos(level, pos, blockState));
            return set;
        }
    }
}