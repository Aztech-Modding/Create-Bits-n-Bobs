package com.kipti.bnb.content.dyeable_pipes;

import com.cake.azimuth.behaviour.SuperBlockEntityBehaviour;
import com.simibubi.create.content.fluids.FluidPropagator;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlock;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.utility.BlockHelper;
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
        return color;
    }

    public void setColor(@Nullable final DyeColor color) {
        if (this.color == color) {
            return;
        }

        this.color = color;
        refreshPipeState();
        blockEntity.notifyUpdate();
    }

    @Override
    public void onItemUse(final PlayerInteractEvent.RightClickBlock event) {
        final ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof final DyeItem dyeItem)) {
            return;
        }

        if (!event.getLevel().isClientSide) {
            setColor(dyeItem.getDyeColor());
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
        DyedPipeTransitionHelper.consumePendingPlacementColor(level, pos);

        final ItemStack offhand = player.getOffhandItem();
        if (offhand.getItem() instanceof final DyeItem dyeItem) {
            setColor(dyeItem.getDyeColor());
        }
    }

    @Override
    public void write(final CompoundTag nbt, final HolderLookup.Provider registries, final boolean clientPacket) {
        super.write(nbt, registries, clientPacket);
        if (color != null) {
            nbt.putInt("DyeColor", color.getId());
        }
    }

    @Override
    public void read(final CompoundTag nbt, final HolderLookup.Provider registries, final boolean clientPacket) {
        super.read(nbt, registries, clientPacket);
        final DyeColor previousColor = color;
        if (nbt.contains("DyeColor")) {
            color = DyeColor.byId(nbt.getInt("DyeColor"));
        } else {
            color = null;
        }

        if (clientPacket && previousColor != color) {
            redraw();
        }
    }

    private void redraw() {
        blockEntity.requestModelDataUpdate();
        if (hasLevel()) {
            final Level level = getLevel();
            final BlockPos pos = getPos();
            level.sendBlockUpdated(pos, getBlockState(), getBlockState(), 16);
        }
    }

    private void refreshPipeState() {
        if (!hasLevel() || !isServerLevel()) {
            return;
        }

        final Level level = getLevel();
        final BlockPos pos = getPos();
        BlockState state = getBlockState();

        if (state.getBlock() instanceof final FluidPipeBlock pipeBlock) {
            final BlockState refreshedState = pipeBlock.updateBlockState(
                    BlockHelper.copyProperties(state, pipeBlock.defaultBlockState()),
                    getPreferredDirection(state),
                    null,
                    level,
                    pos
            );
            if (refreshedState != state) {
                level.setBlock(pos, refreshedState, 3);
                state = refreshedState;
            }
        }

        state.updateNeighbourShapes(level, pos, 3);
        FluidPropagator.propagateChangedPipe(level, pos, state);
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
