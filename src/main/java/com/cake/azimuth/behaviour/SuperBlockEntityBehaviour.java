package com.cake.azimuth.behaviour;

import com.simibubi.create.content.contraptions.StructureTransform;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

/**
 * Extended version of {@link BlockEntityBehaviour} designed for invasive additions of extra functionality for {@link SmartBlockEntity},
 * where you want to compose (or apply) almost full block entity functionality.
 * This also includes some shorthands for accessing things such as block entity level, or getting a likewise behaviour on another block entity.
 */
public abstract class SuperBlockEntityBehaviour extends BlockEntityBehaviour {
    public SuperBlockEntityBehaviour(SmartBlockEntity be) {
        super(be);
    }

    /**
     * Shorthand for getting a complimentary behaviour of the same type on another block entity. This is designed for things
     * such as multi-blocks, where you want to have the same behaviour on multiple block entities and have them interact
     * with each other, but can also be used for other things such as linked machines.
     * <br/>
     * Example usage:
     * <pre>
     * if (controllerOffset != null && getLevel() != null) {
     * final BlockPos controllerPos = getPos().offset(controllerOffset);
     * this.<CogwheelChainComponentBehaviour>getComplimentaryBehaviour(controllerPos)
     *    .ifPresent(controller -> controller.chainsToRefund = 0);
     * }
     * </pre>
     *
     * @param otherPos the position of the other block entity to get the behaviour from
     * @param <T>      the type of the current behaviour, used to ensure the optional is of the correct type
     * @return an optional containing the complimentary behaviour if it exists and is of the same type, or an empty optional if it doesn't exist, isn't loaded, or isn't of the same type.
     */
    @SuppressWarnings("unchecked")
    public <T extends SuperBlockEntityBehaviour> Optional<T> getComplimentaryBehaviour(BlockPos otherPos) {
        final Level level = this.getLevel();
        if (level == null)
            return Optional.empty();
        if (!level.isLoaded(otherPos))
            return Optional.empty();
        if (!(level.getBlockEntity(otherPos) instanceof SmartBlockEntity otherBE))
            return Optional.empty();
        return Optional.ofNullable(otherBE.getBehaviour((BehaviourType<? extends T>) this.getType()));
    }

    /**
     * Shorthand for getting a complimentary behaviour of the same type on another block entity. This is designed for things
     * such as multi-blocks, where you want to have the same behaviour on multiple block entities and have them interact
     * with each other, but can also be used for other things such as linked machines.
     * <br/>
     * Example usage:
     * <pre>
     * if (controllerOffset != null && getLevel() != null) {
     * final BlockPos controllerPos = getPos().offset(controllerOffset);
     * this.<CogwheelChainComponentBehaviour>getComplimentaryBehaviour(controllerPos)
     *    .ifPresent(controller -> controller.chainsToRefund = 0);
     * }
     * </pre>
     *
     * @param otherBlockEntity the other block entity to get the behaviour from
     * @param <T>              the type of the current behaviour, used to ensure the optional is of the correct type
     * @return an optional containing the complimentary behaviour if it exists and is of the same type, or an empty optional if it doesn't exist, isn't loaded, or isn't of the same type.
     */
    @SuppressWarnings("unchecked")
    public <T extends SuperBlockEntityBehaviour> Optional<T> getComplimentaryBehaviour(BlockEntity otherBlockEntity) {
        final Level level = this.getLevel();
        if (level == null)
            return Optional.empty();
        if (!(otherBlockEntity instanceof SmartBlockEntity otherBE))
            return Optional.empty();
        return Optional.ofNullable(otherBE.getBehaviour((BehaviourType<? extends T>) this.getType()));
    }

    public Level getLevel() {
        return this.blockEntity.getLevel();
    }

    public BlockState getBlockState() {
        return this.blockEntity.getBlockState();
    }

    public boolean isClientLevel() {
        return this.getLevel().isClientSide;
    }

    public boolean isServerLevel() {
        return !this.isClientLevel();
    }

    public void transform(BlockEntity be, StructureTransform structureTransformMixin) {
    }
}
