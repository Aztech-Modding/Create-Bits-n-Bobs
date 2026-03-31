package com.kipti.bnb.content.kinetics.gigantic_cogwheel;

import com.kipti.bnb.registry.content.BnbBlockEntities;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class GiganticCogwheelBlock extends RotatedPillarKineticBlock
        implements IBE<GiganticCogwheelBlockEntity>, IRotate {

    public GiganticCogwheelBlock(Properties properties) {
        super(properties);
    }

    // -------------------------------------------------------------------------
    // Placement — cancel if the 3x3 ring is not clear
    // -------------------------------------------------------------------------

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null) return null;
        BlockPos pos = context.getClickedPos();
        Axis axis = state.getValue(AXIS);
        for (int a = -1; a <= 1; a++) {
            for (int b = -1; b <= 1; b++) {
                BlockPos offset = offsetInPlane(axis, a, b);
                if (offset.equals(BlockPos.ZERO)) continue;
                if (!context.getLevel().getBlockState(pos.offset(offset)).canBeReplaced()) return null;
            }
        }
        return state;
    }

    // -------------------------------------------------------------------------
    // On place — fill satellites, then nudge any small cogs 3 blocks away
    // so they re-scan and find us
    // -------------------------------------------------------------------------

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.getBlockTicks().hasScheduledTick(pos, this))
            level.scheduleTick(pos, this, 1);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide) return;
        nudgeNearbyCogs(level, pos, state);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
                                BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (level.isClientSide) return;
        // Re-nudge when anything changes nearby — catches small cog being replaced
        nudgeNearbyCogs(level, pos, state);
    }

    // -------------------------------------------------------------------------
    // Tick — fill or repair the 3x3 satellite ring
    // -------------------------------------------------------------------------

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        Axis axis = state.getValue(AXIS);
        for (int a = -1; a <= 1; a++) {
            for (int b = -1; b <= 1; b++) {
                BlockPos offset = offsetInPlane(axis, a, b);
                if (offset.equals(BlockPos.ZERO)) continue;
                BlockPos satellitePos = pos.offset(offset);
                BlockState existing = level.getBlockState(satellitePos);
                if (existing.getBlock() instanceof GiganticCogwheelSatelliteBlock) continue;
                if (!existing.canBeReplaced()) {
                    level.destroyBlock(pos, false);
                    return;
                }
                level.setBlockAndUpdate(satellitePos,
                        GiganticCogwheelSatelliteBlock.statePointingTo(level, satellitePos, pos));
            }
        }
    }

    // -------------------------------------------------------------------------
    // Nudge small cogs 3 blocks away along our axis so they re-attach kinetics.
    // This is needed because the small cog's own propagation scan won't reach
    // us at distance 3 — we have to make ourselves known to it.
    // -------------------------------------------------------------------------

    private void nudgeNearbyCogs(Level level, BlockPos pos, BlockState state) {
        Axis axis = getRotationAxis(state);
        for (Direction dir : Direction.values()) {
            if (dir.getAxis() == axis) continue;
            BlockPos targetPos = pos.relative(dir, 3);
            BlockState targetState = level.getBlockState(targetPos);
            if (!(targetState.getBlock() instanceof ICogWheel cog)) continue;
            if (cog.isLargeCog()) continue;
            if (cog.getRotationAxis(targetState) != axis) continue;
            BlockEntity be = level.getBlockEntity(targetPos);
            if (be instanceof KineticBlockEntity kbe) {
                kbe.detachKinetics();
                kbe.updateSpeed = true;
            }
        }
    }

    // -------------------------------------------------------------------------
    // Kinetic / shaft
    // -------------------------------------------------------------------------

    @Override
    public Axis getRotationAxis(BlockState state) {
        return state.getValue(AXIS);
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == getRotationAxis(state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    // -------------------------------------------------------------------------
    // IBE
    // -------------------------------------------------------------------------

    @Override
    public Class<GiganticCogwheelBlockEntity> getBlockEntityClass() {
        return GiganticCogwheelBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends GiganticCogwheelBlockEntity> getBlockEntityType() {
        return BnbBlockEntities.GIGANTIC_COGWHEEL.get();
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    public static BlockPos offsetInPlane(Axis axis, int a, int b) {
        return switch (axis) {
            case X -> new BlockPos(0, a, b);
            case Y -> new BlockPos(a, 0, b);
            case Z -> new BlockPos(a, b, 0);
        };
    }
}