package com.kipti.bnb.content.decoration.dyeable.pipes;

import com.kipti.bnb.content.decoration.dyeable.BaseDyeableBehaviour;
import com.kipti.bnb.content.decoration.dyeable.DyeableBlockItemHelper;
import com.kipti.bnb.registry.content.BnbAdvancements;
import com.simibubi.create.content.fluids.FluidPropagator;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlock;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import net.createmod.catnip.data.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.jetbrains.annotations.Nullable;

public class DyeablePipeBehaviour extends BaseDyeableBehaviour {

    public static final BehaviourType<DyeablePipeBehaviour> TYPE = new BehaviourType<>("dyeable_pipe");

    public DyeablePipeBehaviour(final SmartBlockEntity be) {
        super(be);
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

    @Override
    protected void onColorChanged(@Nullable final DyeColor color) {
        this.refreshPipeState();
    }

    @Override
    public void onItemUse(final PlayerInteractEvent.RightClickBlock event) {
        final ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof final DyeItem dyeItem)) {
            return;
        }

        if (!event.getLevel().isClientSide) {
            if (event.getEntity() instanceof final Player player) {
                BnbAdvancements.DYE_FLUID_COMPONENT.awardTo(player);
            }
            this.setColor(dyeItem.getDyeColor());
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }

    @Override
    public void onBlockPlaced(final BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        if (!(event.getEntity() instanceof final Player player)) {
            return;
        }

        final Level level = (Level) event.getLevel();
        final BlockPos pos = event.getPos();
        DyeableBlockItemHelper.consumePendingPlacementColor(level, pos);

        final DyeColor color = DyeableBlockItemHelper.getOffhandDyeColor(player);
        if (color != null) {
            this.setColor(color);
        }
    }

    private void refreshPipeState() {
        if (!this.hasLevel()) {
            return;
        }

        refreshPipeState(this.getLevel(), this.getPos(), this.getBlockState());
    }

    public static void refreshPipeState(final Level level, final BlockPos pos, final BlockState state) {
        refreshPipeState(level, pos, state, true);
    }

    public static void refreshPipeState(final Level level,
                                        final BlockPos pos,
                                        BlockState state,
                                        final boolean propagateToNeighbors) {
        if (state.getBlock() instanceof final FluidPipeBlock pipeBlock) {
            BlockState baseState = pipeBlock.defaultBlockState();
            if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
                baseState = baseState.setValue(
                        BlockStateProperties.WATERLOGGED,
                        state.getValue(BlockStateProperties.WATERLOGGED)
                );
            }
            final BlockState refreshedState = pipeBlock.updateBlockState(
                    baseState,
                    getPreferredDirection(state),
                    null,
                    level,
                    pos
            );
            if (refreshedState != state) {
                level.setBlock(pos, refreshedState, propagateToNeighbors ? 3 : 2);
                state = refreshedState;
            }
        }

        if (propagateToNeighbors) {
            state.updateNeighbourShapes(level, pos, 3);
            if (!level.isClientSide) {
                FluidPropagator.propagateChangedPipe(level, pos, state);
            }
        }
    }

    private static Direction getPreferredDirection(final BlockState state) {
        for (final Direction direction : Iterate.directions) {
            if (FluidPipeBlock.isOpenAt(state, direction)) {
                return direction;
            }
        }
        return Direction.UP;
    }

}
