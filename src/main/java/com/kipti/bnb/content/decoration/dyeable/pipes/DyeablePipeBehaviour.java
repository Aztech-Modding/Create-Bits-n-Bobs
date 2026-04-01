package com.kipti.bnb.content.decoration.dyeable.pipes;

import com.cake.azimuth.behaviour.SuperBlockEntityBehaviour;
import com.kipti.bnb.content.decoration.dyeable.DyeableTransitionHelper;
import com.kipti.bnb.registry.content.BnbAdvancements;
import com.simibubi.create.content.fluids.FluidPropagator;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlock;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import net.createmod.catnip.data.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
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

public class DyeablePipeBehaviour extends SuperBlockEntityBehaviour {

    public static final BehaviourType<DyeablePipeBehaviour> TYPE = new BehaviourType<>("dyeable_pipe");

    @Nullable
    private DyeColor color;

    public DyeablePipeBehaviour(final SmartBlockEntity be) {
        super(be);
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

    @Nullable
    public DyeColor getColor() {
        return this.color;
    }

    /**
     * Sets the color on the client side only, triggering an immediate visual refresh.
     * Used by {@link DyeablePipeBlockItem} during placement for instant feedback.
     */
    public void applyColorClientOnly(@Nullable final DyeColor color) {
        if (this.color == color) {
            return;
        }
        this.color = color;
        this.refreshRenderedModel();
    }

    public void setColor(@Nullable final DyeColor color) {
        if (this.color == color) {
            return;
        }

        this.color = color;
        this.refreshPipeState();
        if (this.hasLevel() && this.getLevel().isClientSide) {
            this.refreshRenderedModel();
        } else {
            this.blockEntity.notifyUpdate();
        }
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
        DyeableTransitionHelper.consumePendingPlacementColor(level, pos);

        final ItemStack offhand = player.getOffhandItem();
        if (offhand.getItem() instanceof final DyeItem dyeItem) {
            this.setColor(dyeItem.getDyeColor());
        }
    }

    @Override
    public void write(final CompoundTag nbt, final HolderLookup.Provider registries, final boolean clientPacket) {
        super.write(nbt, registries, clientPacket);
        if (this.color != null) {
            nbt.putInt("DyeColor", this.color.getId());
        }
    }

    @Override
    public void read(final CompoundTag nbt, final HolderLookup.Provider registries, final boolean clientPacket) {
        super.read(nbt, registries, clientPacket);
        final DyeColor previousColor = this.color;
        if (nbt.contains("DyeColor")) {
            this.color = DyeColor.byId(nbt.getInt("DyeColor"));
        } else {
            this.color = null;
        }

        if (clientPacket && previousColor != this.color) {
            this.refreshRenderedModel();
        }
    }

    public void refreshRenderedModel() {
        this.blockEntity.requestModelDataUpdate();
        if (this.hasLevel()) {
            final Level level = this.getLevel();
            final BlockPos pos = this.getPos();
            level.sendBlockUpdated(pos, this.getBlockState(), this.getBlockState(), 16);
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

    /**
     * Recalculates a pipe's connection shape based on its current dye color and neighbours.
     * When {@code propagateToNeighbors} is false the refresh is limited to this block only,
     * which avoids cascading neighbour updates (useful in ponder scenes).
     */
    public static void refreshPipeState(final Level level,
                                        final BlockPos pos,
                                        BlockState state,
                                        final boolean propagateToNeighbors) {
        if (state.getBlock() instanceof final FluidPipeBlock pipeBlock) {
            // Start from defaultBlockState (prevStateSides=0) instead of copying the current
            // connections. If we copied them, Create's fallback at:
            //   if (prevStateSides == 2) return prevState;
            // would fire whenever dye filtering produces 0 valid connections, silently
            // keeping the old (wrong) corner/straight shape. Starting fresh ensures
            // the block always gets a shape consistent with what color filtering allows.
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
