package com.kipti.bnb.content.kinetics.throttle_lever;

import com.kipti.bnb.foundation.behaviour.drag.DragInteractionBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.List;

public class ThrottleLeverBlockEntity extends SmartBlockEntity {

    public ThrottleLeverBlockEntity(final BlockEntityType<?> typeIn, final BlockPos pos, final BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    public void addBehaviours(final List<BlockEntityBehaviour> behaviours) {
        final DragInteractionBehaviour dragBehaviour = new DragInteractionBehaviour(this)
                .withRange(0, 15)
                .withDebounce(3, 5)
                .withCallback(this::onPowerCommitted);
        behaviours.add(dragBehaviour);
    }

    private void onPowerCommitted(final int newPower) {
        if (this.level == null || this.level.isClientSide) return;
        final BlockState state = this.getBlockState();
        if (state.getValue(BlockStateProperties.POWER) != newPower) {
            this.level.setBlock(this.worldPosition, state.setValue(BlockStateProperties.POWER, newPower), 3);
            this.updateNeighbours(state);
        }
    }

    private void updateNeighbours(final BlockState state) {
        this.level.updateNeighborsAt(this.worldPosition, state.getBlock());
        final Direction shaftDir = ThrottleLeverBlock.getShaftDirection(state);
        this.level.updateNeighborsAt(this.worldPosition.relative(shaftDir), state.getBlock());
    }

    public float getCurrentPower(final float pt) {
        final DragInteractionBehaviour behaviour = this.getBehaviour(DragInteractionBehaviour.TYPE);
        return behaviour != null ? behaviour.getAnimatedValue(pt) : 0f;
    }
}

