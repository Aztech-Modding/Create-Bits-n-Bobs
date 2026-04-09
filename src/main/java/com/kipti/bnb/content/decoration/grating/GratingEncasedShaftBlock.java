package com.kipti.bnb.content.decoration.grating;

import com.kipti.bnb.content.kinetics.encased_blocks.BnbEncasedShaftBlock;
import com.kipti.bnb.registry.content.blocks.deco.BnbDecorativeBlocks;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class GratingEncasedShaftBlock extends BnbEncasedShaftBlock implements IGratingPanel {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public GratingEncasedShaftBlock(final Properties properties) {
        super(properties, BnbDecorativeBlocks.INDUSTRIAL_GRATING_PANEL::get);
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.UP));
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.@NotNull Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    public @NonNull BlockState getStateForPlacement(final @NotNull BlockPlaceContext context) {
        return super.getStateForPlacement(context).setValue(FACING, this.getPanelFacing(context.getPlayer()));
    }

    @Override
    public InteractionResult onWrenched(final BlockState state, final UseOnContext context) {
        final BlockState rotatedState = this.getTargetedPart(state, context) == TargetedPart.PANEL
                ? this.getPanelRotatedState(state, context.getClickedFace())
                : this.getRotatedBlockState(state, context.getClickedFace());
        return this.applyWrenchedState(state, rotatedState, context);
    }

    @Override
    public InteractionResult onSneakWrenched(final BlockState state, final UseOnContext context) {
        if (this.getTargetedPart(state, context) != TargetedPart.PANEL) {
            return InteractionResult.PASS;
        }
        if (context.getLevel().isClientSide) {
            return InteractionResult.SUCCESS;
        }

        context.getLevel().levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, context.getClickedPos(), Block.getId(state));
        KineticBlockEntity.switchToBlockState(
                context.getLevel(),
                context.getClickedPos(),
                AllBlocks.SHAFT.getDefaultState().setValue(AXIS, state.getValue(AXIS))
        );
        if (context.getPlayer() != null && !context.getPlayer().isCreative()) {
            context.getPlayer().getInventory().placeItemBackInInventory(BnbDecorativeBlocks.INDUSTRIAL_GRATING_PANEL.asStack());
        }
        IWrenchable.playRemoveSound(context.getLevel(), context.getClickedPos());
        return InteractionResult.SUCCESS;
    }

    @Override
    public void handleEncasing(final BlockState state,
                               final Level level,
                               final BlockPos pos,
                               final ItemStack heldItem,
                               final Player player,
                               final InteractionHand hand,
                               final BlockHitResult ray) {
        KineticBlockEntity.switchToBlockState(
                level, pos, this.defaultBlockState()
                        .setValue(AXIS, state.getValue(AXIS))
                        .setValue(FACING, this.getPanelFacing(player))
        );
    }

    @Override
    protected @NotNull VoxelShape getShape(final BlockState state,
                                           final @NotNull BlockGetter level,
                                           final @NotNull BlockPos pos,
                                           final @NotNull CollisionContext context) {
        return Shapes.or(this.getPanelShape(state), this.getShaftShape(state));
    }

    @Override
    protected boolean isPathfindable(@NotNull final BlockState state,
                                     @NotNull final PathComputationType pathComputationType) {
        return false;
    }

    @Override
    protected boolean skipRendering(final BlockState state, final BlockState adjacentState, final Direction direction) {
        return adjacentState.getBlock() instanceof IGratingPanel && (adjacentState.getValue(FACING) == state.getValue(
                FACING)) || super.skipRendering(state, adjacentState, direction);
    }

    private InteractionResult applyWrenchedState(final BlockState state,
                                                 final BlockState rotatedState,
                                                 final UseOnContext context) {
        if (!rotatedState.canSurvive(context.getLevel(), context.getClickedPos())) {
            return InteractionResult.PASS;
        }

        KineticBlockEntity.switchToBlockState(
                context.getLevel(),
                context.getClickedPos(),
                this.updateAfterWrenched(rotatedState, context)
        );
        if (context.getLevel().getBlockState(context.getClickedPos()) != state) {
            IWrenchable.playRotateSound(context.getLevel(), context.getClickedPos());
        }
        return InteractionResult.SUCCESS;
    }

    private BlockState getPanelRotatedState(final BlockState state, final Direction targetedFace) {
        final Direction stateFacing = state.getValue(FACING);
        if (stateFacing.getAxis() == targetedFace.getAxis()) {
            return state;
        }

        BlockState rotatedState = state;
        do {
            rotatedState = rotatedState.setValue(
                    FACING,
                    rotatedState.getValue(FACING).getClockWise(targetedFace.getAxis())
            );
        } while (rotatedState.getValue(FACING).getAxis() == targetedFace.getAxis());
        return rotatedState;
    }

    private Direction getPanelFacing(final @Nullable Player player) {
        return player == null ? Direction.UP : player.getNearestViewDirection().getOpposite();
    }

    private VoxelShape getPanelShape(final BlockState state) {
        return AllShapes.CASING_3PX.get(state.getValue(FACING));
    }

    private VoxelShape getShaftShape(final BlockState state) {
        return AllShapes.FOUR_VOXEL_POLE.get(state.getValue(AXIS));
    }

    private TargetedPart getTargetedPart(final BlockState state, final UseOnContext context) {
        final Vec3 start = this.getRayStart(context);
        Vec3 end = this.getRayEnd(context);
        end = end.add(end.subtract(start));
        final BlockHitResult panelHit = this.getPanelShape(state).clip(start, end, context.getClickedPos());
        final BlockHitResult shaftHit = this.getShaftShape(state).clip(start, end, context.getClickedPos());
        if (panelHit == null) {
            return TargetedPart.SHAFT;
        }
        if (shaftHit == null) {
            return TargetedPart.PANEL;
        }

        final double panelDistance = panelHit.getLocation().distanceToSqr(start);
        final double shaftDistance = shaftHit.getLocation().distanceToSqr(start);
        return panelDistance <= shaftDistance ? TargetedPart.PANEL : TargetedPart.SHAFT;
    }

    private Vec3 getRayStart(final UseOnContext context) {
        if (context.getPlayer() != null) {
            return context.getPlayer().getEyePosition();
        }
        return context.getClickLocation().subtract(Vec3.atLowerCornerOf(context.getClickedFace().getNormal()).scale(2));
    }

    private Vec3 getRayEnd(final UseOnContext context) {
        return context.getClickLocation();
    }

    private enum TargetedPart {
        PANEL,
        SHAFT
    }

}
