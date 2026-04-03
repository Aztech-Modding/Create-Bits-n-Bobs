package com.kipti.bnb.content.kinetics.throttle_lever;

import com.kipti.bnb.foundation.behaviour.drag.DragInteractionBehaviour;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.List;

/** Block entity for the throttle lever, delegates input handling to {@link DragInteractionBehaviour}. */
public class ThrottleLeverBlockEntity extends GeneratingKineticBlockEntity {

    public ThrottleLeverBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        DragInteractionBehaviour dragBehaviour = new DragInteractionBehaviour(this)
                .withRange(0, 15)
                .withDebounce(3, 5)
                .withCallback(this::onPowerCommitted);
        behaviours.add(dragBehaviour);
    }

    private void onPowerCommitted(int newPower) {
        if (this.level == null || this.level.isClientSide) return;
        BlockState state = this.getBlockState();
        if (state.getValue(BlockStateProperties.POWER) != newPower) {
            this.level.setBlock(this.worldPosition, state.setValue(BlockStateProperties.POWER, newPower), 3);
            this.updateNeighbours(state);
        }
    }

    private void updateNeighbours(BlockState state) {
        this.level.updateNeighborsAt(this.worldPosition, state.getBlock());
        Direction shaftDir = ThrottleLeverBlock.getShaftDirection(state);
        this.level.updateNeighborsAt(this.worldPosition.relative(shaftDir), state.getBlock());
    }

    @Override
    public float getGeneratedSpeed() {
        return 16f;
    }

    @Override
    public float calculateAddedStressCapacity() {
        return 16f;
    }

    public float getCurrentPower(float pt) {
        DragInteractionBehaviour behaviour = this.getBehaviour(DragInteractionBehaviour.TYPE);
        return behaviour != null ? behaviour.getAnimatedValue(pt) : 0f;
    }
}

