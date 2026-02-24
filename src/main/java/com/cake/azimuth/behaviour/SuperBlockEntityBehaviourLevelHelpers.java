package com.cake.azimuth.behaviour;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

sealed interface SuperBlockEntityBehaviourLevelHelpers permits SuperBlockEntityBehaviour {

    /**
     * Shorthand for getting a complementary behaviour of the same type on another block entity. This is designed for things
     * such as multi-blocks, where you want to have the same behaviour on multiple block entities and have them interact
     * with each other, but can also be used for other things such as linked machines.
     * <br/>
     * Example usage:
     * <pre>
     * if (controllerOffset != null && getLevel() != null) {
     *     final BlockPos controllerPos = getPos().offset(controllerOffset);
     *     CogwheelChainComponentBehaviour controller = this.getComplementaryBehaviour(controllerPos);
     *     if (controller != null) {
     *         controller.chainsToRefund = 0;
     *     }
     * }
     * </pre>
     *
     * @param otherPos the position of the other block entity to get the behaviour from
     * @param <T>      the type of the current behaviour, used to ensure the returned value is of the correct type
     * @return the complementary behaviour if it exists and is of the same type, or null if it doesn't exist, isn't loaded, or isn't of the same type.
     */
    @SuppressWarnings("unchecked")
    @Nullable
    default <T extends SuperBlockEntityBehaviour> T getComplementaryBehaviour(final BlockPos otherPos) {
        final Level level = this.getLevel();
        if (level == null || !level.isLoaded(otherPos))
            return null;
        if (!(level.getBlockEntity(otherPos) instanceof final SmartBlockEntity otherBE))
            return null;
        return otherBE.getBehaviour((BehaviourType<? extends T>) this.getType());
    }

    /**
     * Shorthand for getting an optional complementary behaviour of the same type on another block entity. This is designed for things
     * such as multi-blocks, where you want to have the same behaviour on multiple block entities and have them interact
     * with each other, but can also be used for other things such as linked machines.
     * <br/>
     * Example usage:
     * <pre>
     * if (controllerOffset != null && getLevel() != null) {
     *     final BlockPos controllerPos = getPos().offset(controllerOffset);
     *     this.<CogwheelChainComponentBehaviour>getComplementaryBehaviourOptional(controllerPos)
     *        .ifPresent(controller -> controller.chainsToRefund = 0);
     * }
     * </pre>
     *
     * @param otherPos the position of the other block entity to get the behaviour from
     * @param <T>      the type of the current behaviour, used to ensure the optional is of the correct type
     * @return an optional containing the complementary behaviour if it exists and is of the same type, or an empty optional if it doesn't exist, isn't loaded, or isn't of the same type.
     */
    default <T extends SuperBlockEntityBehaviour> Optional<T> getComplementaryBehaviourOptional(final BlockPos otherPos) {
        return Optional.ofNullable(getComplementaryBehaviour(otherPos));
    }

    /**
     * Shorthand for getting a complementary behaviour of the same type on another block entity, and expect it to exist.
     * If you do not want to throw, then use {@link #getComplementaryBehaviourOptional(BlockPos)} or {@link #getComplementaryBehaviour(BlockPos)}.
     *
     * @param otherPos the position of the other block entity to get the behaviour from
     * @param <T>      the type of the current behaviour, used to ensure the returned value is of the correct type
     * @return the complementary behaviour if it exists and is of the same type
     * @throws IllegalStateException if the complementary behaviour does not exist, is not loaded, or is of a different type
     */
    default <T extends SuperBlockEntityBehaviour> T getComplementaryBehaviourOrThrow(final BlockPos otherPos) {
        return this.<T>getComplementaryBehaviourOptional(otherPos)
                .orElseThrow(() -> new IllegalStateException(
                        "Expected to find a complementary behaviour (type " +
                                this.getType() +
                                ") at position " +
                                otherPos +
                                ", but it was not present or was not of the correct type."
                ));
    }

    /**
     * Shorthand for getting a complementary behaviour of the same type on another block entity. This is designed for things
     * such as multi-blocks, where you want to have the same behaviour on multiple block entities and have them interact
     * with each other, but can also be used for other things such as linked machines.
     * <br/>
     * Example usage:
     * <pre>
     * if (getLevel() != null) {
     *     CogwheelChainComponentBehaviour other = this.getComplementaryBehaviour(otherBlockEntity);
     *     if (other != null) {
     *         other.chainsToRefund = 0;
     *     }
     * }
     * </pre>
     *
     * @param otherBlockEntity the other block entity to get the behaviour from
     * @param <T>              the type of the current behaviour, used to ensure the returned value is of the correct type
     * @return the complementary behaviour if it exists and is of the same type, or null if it doesn't exist, isn't loaded, or isn't of the same type.
     */
    @SuppressWarnings("unchecked")
    @Nullable
    default <T extends SuperBlockEntityBehaviour> T getComplementaryBehaviour(final BlockEntity otherBlockEntity) {
        final Level level = this.getLevel();
        if (level == null || !(otherBlockEntity instanceof final SmartBlockEntity otherBE))
            return null;
        return otherBE.getBehaviour((BehaviourType<? extends T>) this.getType());
    }

    /**
     * Shorthand for getting an optional complementary behaviour of the same type on another block entity. This is designed for things
     * such as multi-blocks, where you want to have the same behaviour on multiple block entities and have them interact
     * with each other, but can also be used for other things such as linked machines.
     * <br/>
     * Example usage:
     * <pre>
     * if (controllerOffset != null && getLevel() != null) {
     *     final BlockPos controllerPos = getPos().offset(controllerOffset);
     *     this.<CogwheelChainComponentBehaviour>getComplementaryBehaviourOptional(controllerPos)
     *        .ifPresent(controller -> controller.chainsToRefund = 0);
     * }
     * </pre>
     *
     * @param otherBlockEntity the other block entity to get the behaviour from
     * @param <T>              the type of the current behaviour, used to ensure the optional is of the correct type
     * @return an optional containing the complementary behaviour if it exists and is of the same type, or an empty optional if it doesn't exist, isn't loaded, or isn't of the same type.
     */
    default <T extends SuperBlockEntityBehaviour> Optional<T> getComplementaryBehaviourOptional(final BlockEntity otherBlockEntity) {
        return Optional.ofNullable(getComplementaryBehaviour(otherBlockEntity));
    }

    /**
     * Shorthand for getting a complementary behaviour of the same type on another block entity, and expect it to exist.
     * If you do not want to throw, then use {@link #getComplementaryBehaviourOptional(BlockEntity)} or {@link #getComplementaryBehaviour(BlockEntity)}.
     *
     * @param otherBlockEntity the other block entity to get the behaviour from
     * @param <T>              the type of the current behaviour, used to ensure the returned value is of the correct type
     * @return the complementary behaviour if it exists and is of the same type
     * @throws IllegalStateException if the complementary behaviour does not exist or is of a different type
     */
    default <T extends SuperBlockEntityBehaviour> T getComplementaryBehaviourOrThrow(final BlockEntity otherBlockEntity) {
        return this.<T>getComplementaryBehaviourOptional(otherBlockEntity)
                .orElseThrow(() -> new IllegalStateException(
                        "Expected to find a complementary behaviour (type " +
                                this.getType() +
                                ") inside block entity " +
                                otherBlockEntity +
                                " at position " +
                                otherBlockEntity.getBlockPos() +
                                ", but it was not present or was not of the correct type."
                ));
    }


    //Required methods for non-static helpers
    Level getLevel();

    BehaviourType<? extends SuperBlockEntityBehaviour> getType();

}
