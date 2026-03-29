package com.kipti.bnb.content.decoration.truss;

import com.kipti.bnb.content.kinetics.encased_blocks.BnbEncasedShaftBlock;
import com.kipti.bnb.registry.client.BnbShapes;
import com.kipti.bnb.registry.content.BnbBlockEntities;
import com.kipti.bnb.registry.content.blocks.deco.BnbDecorativeBlocks;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class TrussEncasedShaftBlock extends BnbEncasedShaftBlock {

    public static final EnumProperty<Direction.Axis> TRUSS_AXIS = EnumProperty.create(
            "truss_axis",
            Direction.Axis.class
    );
    public static final BooleanProperty ALTERNATING = TrussBlock.ALTERNATING;

    public TrussEncasedShaftBlock(final Properties properties) {
        super(properties, BnbDecorativeBlocks.INDUSTRIAL_TRUSS::get);
        this.registerDefaultState(this.defaultBlockState()
                                          .setValue(TRUSS_AXIS, Direction.Axis.Y)
                                          .setValue(ALTERNATING, false));
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(TRUSS_AXIS, ALTERNATING);
    }

    @Override
    protected @NotNull BlockState updateShape(final BlockState state,
                                              final @NotNull Direction direction,
                                              final @NotNull BlockState neighborState,
                                              final @NotNull LevelAccessor level,
                                              final @NotNull BlockPos pos,
                                              final @NotNull BlockPos neighborPos) {
        final Direction positiveAxis = Direction.get(Direction.AxisDirection.POSITIVE, state.getValue(AXIS));
        if (direction == positiveAxis) {
            final boolean isAlternating = neighborState.hasProperty(ALTERNATING) && neighborState.getValue(ALTERNATING);
            return state.setValue(ALTERNATING, !isAlternating);
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public @NotNull BlockState getStateForPlacement(final @NotNull BlockPlaceContext context) {
        final BlockState state = super.getStateForPlacement(context);
        final Direction positiveAxis = Direction.get(Direction.AxisDirection.POSITIVE, state.getValue(AXIS));
        final BlockState neighborState = context.getLevel().getBlockState(context.getClickedPos().relative(positiveAxis));
        final boolean isAlternating = neighborState.hasProperty(ALTERNATING) && neighborState.getValue(ALTERNATING);
        return state.setValue(ALTERNATING, !isAlternating);
    }

    @Override
    public BlockEntityType<? extends KineticBlockEntity> getBlockEntityType() {
        return BnbBlockEntities.ENCASED_SHAFT.get();
    }

    @Override
    protected VoxelShape getShape(final BlockState state,
                                  final BlockGetter level,
                                  final BlockPos pos,
                                  final CollisionContext context) {
        return BnbShapes.ALTERNATING_TRUSS.get(state.getValue(TRUSS_AXIS));
    }

    @Override
    public InteractionResult onSneakWrenched(final BlockState state, final UseOnContext context) {
        if (context.getLevel().isClientSide)
            return InteractionResult.SUCCESS;
        context.getLevel().levelEvent(2001, context.getClickedPos(), Block.getId(state));
        KineticBlockEntity.switchToBlockState(
                context.getLevel(), context.getClickedPos(),
                BnbDecorativeBlocks.INDUSTRIAL_TRUSS.getDefaultState()
                        .setValue(RotatedPillarBlock.AXIS, state.getValue(TRUSS_AXIS))
                        .setValue(ALTERNATING, state.getValue(ALTERNATING))
        );
        if (context.getPlayer() != null && !context.getPlayer().isCreative())
            context.getPlayer().getInventory().placeItemBackInInventory(AllBlocks.SHAFT.asStack());
        return InteractionResult.SUCCESS;
    }

    @Override
    public ItemRequirement getRequiredItems(final BlockState state, final BlockEntity be) {
        return ItemRequirement.of(AllBlocks.SHAFT.getDefaultState(), be)
                .union(ItemRequirement.of(BnbDecorativeBlocks.INDUSTRIAL_TRUSS.getDefaultState(), be));
    }
}
