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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class GiganticCogwheelBlock extends RotatedPillarKineticBlock
        implements IBE<GiganticCogwheelBlockEntity>, IRotate {

    public GiganticCogwheelBlock(Properties properties) {
        super(properties);
    }

    // -------------------------------------------------------------------------
    // 🚨 NO FACE CULLING / FULL LIGHT PASS-THROUGH (1.21 FIX)
    // -------------------------------------------------------------------------

    @Override
    public boolean skipRendering(BlockState state, BlockState adjacentBlockState, Direction direction) {
        return true;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    public boolean isOcclusionShapeFullBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return false;
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
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
                if (!context.getLevel().getBlockState(pos.offset(offset)).canBeReplaced())
                    return null;
            }
        }

        return state;
    }

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
        nudgeNearbyCogs(level, pos, state);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        Axis axis = state.getValue(AXIS);

        for (int a = -1; a <= 1; a++) {
            for (int b = -1; b <= 1; b++) {
                BlockPos offset = offsetInPlane(axis, a, b);
                if (offset.equals(BlockPos.ZERO)) continue;

                BlockPos satellitePos = pos.offset(offset);
                BlockState existing = level.getBlockState(satellitePos);

                if (existing.getBlock() instanceof GiganticCogwheelSatelliteBlock)
                    continue;

                if (!existing.canBeReplaced()) {
                    level.destroyBlock(pos, false);
                    return;
                }

                level.setBlockAndUpdate(satellitePos,
                        GiganticCogwheelSatelliteBlock.statePointingTo(level, satellitePos, pos));
            }
        }
    }

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

    @Override
    protected @NotNull ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                                       Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.isEmpty())
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if (level.isClientSide)
            return (stack.is(ItemTags.PLANKS) || stack.is(ItemTags.LOGS))
                    ? ItemInteractionResult.SUCCESS
                    : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        GiganticCogwheelBlockEntity be = getBlockEntity(level, pos);
        if (be == null)
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        boolean applied = be.tryApplyTextureFromItem(stack);

        if (applied)
            be.notifyUpdate();

        return applied
                ? ItemInteractionResult.SUCCESS
                : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public Class<GiganticCogwheelBlockEntity> getBlockEntityClass() {
        return GiganticCogwheelBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends GiganticCogwheelBlockEntity> getBlockEntityType() {
        return BnbBlockEntities.GIGANTIC_COGWHEEL.get();
    }

    public static BlockPos offsetInPlane(Axis axis, int a, int b) {
        return switch (axis) {
            case X -> new BlockPos(0, a, b);
            case Y -> new BlockPos(a, 0, b);
            case Z -> new BlockPos(a, b, 0);
        };
    }
}