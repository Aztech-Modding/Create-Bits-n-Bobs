package com.kipti.bnb.content.decoration.grating;

import com.kipti.bnb.content.decoration.dyeable.DyeableTransitionHelper;
import com.kipti.bnb.registry.content.BnbBlockEntities;
import com.kipti.bnb.registry.content.blocks.deco.BnbDecorativeBlocks;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.api.schematic.requirement.SpecialBlockItemRequirement;
import com.simibubi.create.content.contraptions.StructureTransform;
import com.simibubi.create.content.decoration.encasing.EncasedBlock;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.fluids.pipes.EncasedPipeBlock;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlock;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlockEntity;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import net.createmod.catnip.data.Iterate;
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
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class GratingPipePanelBlock extends FluidPipeBlock
        implements IGratingPanel, EncasedBlock, SpecialBlockItemRequirement {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    private static final VoxelShape PIPE_CENTER_BOX = Block.box(4, 4, 4, 12, 12, 12);

    public GratingPipePanelBlock(final Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.UP));
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.@NotNull Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    public @NotNull BlockState getStateForPlacement(final @NotNull BlockPlaceContext context) {
        return super.getStateForPlacement(context).setValue(FACING, this.getPanelFacing(context.getPlayer()));
    }

    @Override
    public BlockEntityType<? extends FluidPipeBlockEntity> getBlockEntityType() {
        return BnbBlockEntities.ENCASED_PIPE.get();
    }

    @Override
    public Block getCasing() {
        return BnbDecorativeBlocks.INDUSTRIAL_GRATING_PANEL.get();
    }

    @Override
    public void handleEncasing(final BlockState state,
                               final Level level,
                               final BlockPos pos,
                               final ItemStack heldItem,
                               final Player player,
                               final InteractionHand hand,
                               final BlockHitResult ray) {
        DyeableTransitionHelper.saveCurrentDye(level, pos);
        FluidTransportBehaviour.cacheFlows(level, pos);
        level.setBlockAndUpdate(pos,
                EncasedPipeBlock.transferSixWayProperties(state, this.defaultBlockState())
                        .setValue(FACING, this.getPanelFacing(player))
                        .setValue(BlockStateProperties.WATERLOGGED, state.getValue(BlockStateProperties.WATERLOGGED)));
        FluidTransportBehaviour.loadFlows(level, pos);
        DyeableTransitionHelper.applyPreviousDye(level, pos);
    }

    @Override
    public @NotNull InteractionResult onWrenched(final BlockState state, final UseOnContext context) {
        if (this.getTargetedPart(state, context) == TargetedPart.PANEL) {
            final BlockState rotatedState = this.getPanelRotatedState(state, context.getClickedFace());
            if (rotatedState == state || !rotatedState.canSurvive(context.getLevel(), context.getClickedPos())) {
                return InteractionResult.PASS;
            }
            context.getLevel().setBlockAndUpdate(context.getClickedPos(), rotatedState);
            IWrenchable.playRotateSound(context.getLevel(), context.getClickedPos());
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult onSneakWrenched(final BlockState state, final UseOnContext context) {
        if (context.getLevel().isClientSide) {
            return InteractionResult.SUCCESS;
        }

        final Level level = context.getLevel();
        final BlockPos pos = context.getClickedPos();
        final TargetedPart targeted = this.getTargetedPart(state, context);

        level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(state));

        if (targeted == TargetedPart.PANEL) {
            DyeableTransitionHelper.saveCurrentDye(level, pos);
            FluidTransportBehaviour.cacheFlows(level, pos);
            final BlockState pipeState = EncasedPipeBlock.transferSixWayProperties(
                            state, AllBlocks.FLUID_PIPE.getDefaultState())
                    .setValue(BlockStateProperties.WATERLOGGED, state.getValue(BlockStateProperties.WATERLOGGED));
            Direction firstFound = Direction.UP;
            for (final Direction d : Iterate.directions) {
                if (state.getValue(PROPERTY_BY_DIRECTION.get(d))) {
                    firstFound = d;
                    break;
                }
            }
            level.setBlockAndUpdate(pos, AllBlocks.FLUID_PIPE.get()
                    .updateBlockState(pipeState, firstFound, null, level, pos));
            FluidTransportBehaviour.loadFlows(level, pos);
            DyeableTransitionHelper.applyPreviousDye(level, pos);
            if (context.getPlayer() != null && !context.getPlayer().isCreative()) {
                context.getPlayer().getInventory().placeItemBackInInventory(
                        BnbDecorativeBlocks.INDUSTRIAL_GRATING_PANEL.asStack());
            }
        } else {
            level.setBlockAndUpdate(pos,
                    BnbDecorativeBlocks.INDUSTRIAL_GRATING_PANEL.getDefaultState()
                            .setValue(FACING, state.getValue(FACING)));
            if (context.getPlayer() != null && !context.getPlayer().isCreative()) {
                context.getPlayer().getInventory().placeItemBackInInventory(AllBlocks.FLUID_PIPE.asStack());
            }
        }

        IWrenchable.playRemoveSound(level, pos);
        return InteractionResult.SUCCESS;
    }

    @Override
    public @NotNull ItemStack getCloneItemStack(final @NotNull BlockState state,
                                                final @NotNull HitResult target,
                                                final @NotNull LevelReader level,
                                                final @NotNull BlockPos pos,
                                                final @NotNull Player player) {
        return AllBlocks.FLUID_PIPE.asStack();
    }

    @Override
    public ItemRequirement getRequiredItems(final BlockState state, final @Nullable BlockEntity blockEntity) {
        return ItemRequirement.of(AllBlocks.FLUID_PIPE.getDefaultState(), blockEntity)
                .union(ItemRequirement.of(BnbDecorativeBlocks.INDUSTRIAL_GRATING_PANEL.getDefaultState(), blockEntity));
    }

    @Override
    protected @NotNull VoxelShape getShape(final @NotNull BlockState state,
                                           final @NotNull BlockGetter level,
                                           final @NotNull BlockPos pos,
                                           final @NotNull CollisionContext context) {
        return Shapes.or(this.getPanelShape(state), super.getShape(state, level, pos, context));
    }

    @Override
    public Optional<ItemStack> removeBracket(final BlockGetter world, final BlockPos pos, final boolean inOnReplacedContext) {
        return Optional.empty();
    }

    @Override
    protected boolean skipRendering(final @NotNull BlockState state,
                                    final @NotNull BlockState adjacentState,
                                    final @NotNull Direction direction) {
        return adjacentState.getBlock() instanceof IGratingPanel
                && adjacentState.getValue(FACING) == state.getValue(FACING)
                || super.skipRendering(state, adjacentState, direction);
    }

    @Override
    public @NotNull BlockState rotate(final @NotNull BlockState state, final @NotNull Rotation rotation) {
        return super.rotate(state, rotation)
                .setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public @NotNull BlockState mirror(final @NotNull BlockState state, final @NotNull Mirror mirror) {
        return super.mirror(state, mirror)
                .setValue(FACING, mirror.mirror(state.getValue(FACING)));
    }

    @Override
    public @NotNull BlockState transform(final BlockState state, final StructureTransform transform) {
        Direction facing = state.getValue(FACING);
        if (transform.mirror != null) {
            facing = transform.mirror.mirror(facing);
        }
        if (transform.rotationAxis == Direction.Axis.Y) {
            facing = transform.rotation.rotate(facing);
        } else {
            facing = transform.rotateFacing(facing);
        }
        return super.transform(state, transform).setValue(FACING, facing);
    }

    private Direction getPanelFacing(final @Nullable Player player) {
        return player == null ? Direction.UP : player.getNearestViewDirection().getOpposite();
    }

    private VoxelShape getPanelShape(final BlockState state) {
        return AllShapes.CASING_3PX.get(state.getValue(FACING));
    }

    private BlockState getPanelRotatedState(final BlockState state, final Direction targetedFace) {
        final Direction stateFacing = state.getValue(FACING);
        if (stateFacing.getAxis() == targetedFace.getAxis()) {
            return state;
        }
        BlockState rotatedState = state;
        do {
            rotatedState = rotatedState.setValue(FACING,
                    rotatedState.getValue(FACING).getClockWise(targetedFace.getAxis()));
        } while (rotatedState.getValue(FACING).getAxis() == targetedFace.getAxis());
        return rotatedState;
    }

    private TargetedPart getTargetedPart(final BlockState state, final UseOnContext context) {
        final Vec3 start = this.getRayStart(context);
        Vec3 end = this.getRayEnd(context);
        end = end.add(end.subtract(start));
        final BlockHitResult panelHit = this.getPanelShape(state).clip(start, end, context.getClickedPos());
        final BlockHitResult pipeHit = PIPE_CENTER_BOX.clip(start, end, context.getClickedPos());
        if (panelHit == null) {
            return TargetedPart.PIPE;
        }
        if (pipeHit == null) {
            return TargetedPart.PANEL;
        }
        final double panelDistance = panelHit.getLocation().distanceToSqr(start);
        final double pipeDistance = pipeHit.getLocation().distanceToSqr(start);
        return panelDistance <= pipeDistance ? TargetedPart.PANEL : TargetedPart.PIPE;
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
        PIPE
    }

}
