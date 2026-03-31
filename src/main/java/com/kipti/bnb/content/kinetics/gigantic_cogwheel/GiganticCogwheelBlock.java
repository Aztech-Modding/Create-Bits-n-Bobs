package com.kipti.bnb.content.kinetics.gigantic_cogwheel;

import com.kipti.bnb.registry.client.BnbShapes;
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
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

/**
 * Center block of the 3×3 gigantic cogwheel multiblock.
 * Places eight {@link GiganticCogwheelSatelliteBlock} satellites around itself
 * and propagates rotation to small cogwheels three blocks away at a 5:1 ratio.
 */
public class GiganticCogwheelBlock extends RotatedPillarKineticBlock
        implements IBE<GiganticCogwheelBlockEntity>, IRotate {

    public GiganticCogwheelBlock(final Properties properties) {
        super(properties);
    }

    @Override
    public boolean skipRendering(final BlockState state,
                                 final BlockState adjacentBlockState,
                                 final Direction direction) {
        return true;
    }

    @Override
    public boolean propagatesSkylightDown(final BlockState state, final BlockGetter level, final BlockPos pos) {
        return true;
    }

    @Override
    public boolean isOcclusionShapeFullBlock(final BlockState state, final BlockGetter level, final BlockPos pos) {
        return false;
    }

    @Override
    public VoxelShape getOcclusionShape(final BlockState state, final BlockGetter level, final BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getShape(final BlockState state,
                               final BlockGetter worldIn,
                               final BlockPos pos,
                               final CollisionContext context) {
        return BnbShapes.GIGANTIC_COGWHEEL.get(state.getValue(AXIS));
    }

    @Override
    public boolean isFlammable(final BlockState state,
                               final BlockGetter level,
                               final BlockPos pos,
                               final Direction direction) {
        return false;
    }

    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context) {
        final BlockState state = super.getStateForPlacement(context);
        if (state == null) return null;

        final BlockPos pos = context.getClickedPos();
        final Axis axis = state.getValue(AXIS);

        for (int a = -1; a <= 1; a++) {
            for (int b = -1; b <= 1; b++) {
                final BlockPos offset = offsetInPlane(axis, a, b);
                if (offset.equals(BlockPos.ZERO)) continue;
                if (!context.getLevel().getBlockState(pos.offset(offset)).canBeReplaced())
                    return null;
            }
        }

        return state;
    }

    @Override
    public void onPlace(final BlockState state,
                        final Level level,
                        final BlockPos pos,
                        final BlockState oldState,
                        final boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.getBlockTicks().hasScheduledTick(pos, this))
            level.scheduleTick(pos, this, 1);
    }

    @Override
    public void setPlacedBy(final Level level,
                            final BlockPos pos,
                            final BlockState state,
                            final LivingEntity placer,
                            final ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide) return;
        this.nudgeNearbyCogs(level, pos, state);
    }

    @Override
    public void neighborChanged(final BlockState state, final Level level, final BlockPos pos, final Block block,
                                final BlockPos fromPos, final boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (level.isClientSide) return;
        this.nudgeNearbyCogs(level, pos, state);
    }

    @Override
    public void tick(final BlockState state, final ServerLevel level, final BlockPos pos, final RandomSource random) {
        final Axis axis = state.getValue(AXIS);

        for (int a = -1; a <= 1; a++) {
            for (int b = -1; b <= 1; b++) {
                final BlockPos offset = offsetInPlane(axis, a, b);
                if (offset.equals(BlockPos.ZERO)) continue;

                final BlockPos satellitePos = pos.offset(offset);
                final BlockState existing = level.getBlockState(satellitePos);

                if (existing.getBlock() instanceof GiganticCogwheelSatelliteBlock)
                    continue;

                if (!existing.canBeReplaced()) {
                    level.destroyBlock(pos, false);
                    return;
                }

                level.setBlockAndUpdate(
                        satellitePos,
                        GiganticCogwheelSatelliteBlock.statePointingTo(level, satellitePos, pos)
                );
            }
        }
    }

    private void nudgeNearbyCogs(final Level level, final BlockPos pos, final BlockState state) {
        final Axis axis = this.getRotationAxis(state);

        for (final Direction dir : Direction.values()) {
            if (dir.getAxis() == axis) continue;

            final BlockPos targetPos = pos.relative(dir, 3);
            final BlockState targetState = level.getBlockState(targetPos);

            if (!(targetState.getBlock() instanceof final ICogWheel cog)) continue;
            if (cog.isLargeCog()) continue;
            if (cog.getRotationAxis(targetState) != axis) continue;

            final BlockEntity be = level.getBlockEntity(targetPos);
            if (be instanceof final KineticBlockEntity kbe) {
                kbe.detachKinetics();
                kbe.updateSpeed = true;
            }
        }
    }

    @Override
    public Axis getRotationAxis(final BlockState state) {
        return state.getValue(AXIS);
    }

    @Override
    public boolean hasShaftTowards(final LevelReader world,
                                   final BlockPos pos,
                                   final BlockState state,
                                   final Direction face) {
        return face.getAxis() == this.getRotationAxis(state);
    }

    @Override
    public RenderShape getRenderShape(final BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(final ItemStack stack,
                                                       final BlockState state,
                                                       final Level level,
                                                       final BlockPos pos,
                                                       final Player player,
                                                       final InteractionHand hand,
                                                       final BlockHitResult hitResult) {
        return this.onBlockEntityUseItemOn(level, pos, be -> be.applyMaterialIfValid(stack));
    }

    @Override
    public Class<GiganticCogwheelBlockEntity> getBlockEntityClass() {
        return GiganticCogwheelBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends GiganticCogwheelBlockEntity> getBlockEntityType() {
        return BnbBlockEntities.GIGANTIC_COGWHEEL.get();
    }

    public static BlockPos offsetInPlane(final Axis axis, final int a, final int b) {
        return switch (axis) {
            case X -> new BlockPos(0, a, b);
            case Y -> new BlockPos(a, 0, b);
            case Z -> new BlockPos(a, b, 0);
        };
    }
}