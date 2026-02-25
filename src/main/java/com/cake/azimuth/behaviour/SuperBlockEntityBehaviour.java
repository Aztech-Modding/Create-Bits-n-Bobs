package com.cake.azimuth.behaviour;

import com.simibubi.create.content.contraptions.StructureTransform;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Extended version of {@link BlockEntityBehaviour} designed for invasive additions of extra functionality for {@link SmartBlockEntity},
 * where you want to compose (or apply) almost full block entity functionality.
 * This also includes some shorthands for accessing things such as block entity level, or getting a likewise behaviour on another block entity.
 */
public abstract class SuperBlockEntityBehaviour extends BlockEntityBehaviour {

    public SuperBlockEntityBehaviour(final SmartBlockEntity be) {
        super(be);
    }

    public Level getLevel() {
        return this.blockEntity.getLevel();
    }

    public BlockState getBlockState() {
        return this.blockEntity.getBlockState();
    }

    //TODO: provide a method where just the behaviour is syncing, not the entire block entity
    public void sendData() {
        this.blockEntity.sendData();
    }

    public boolean isClientLevel() {
        return this.getLevel().isClientSide;
    }

    public boolean isServerLevel() {
        return !this.isClientLevel();
    }

    public void transform(final BlockEntity be, final StructureTransform structureTransformMixin) {
    }

    //region Static Get Helpers

    /**
     * Shorthand for getting an optional behaviour of a specific type from a level at a given position.
     *
     * @param level the level to get the behaviour from
     * @param pos   the position of the block entity to get the behaviour from
     * @param type  the type of the behaviour to get
     * @param <T>   the type of the behaviour
     * @return an optional containing the behaviour if it exists and is of the correct type, or an empty optional if it doesn't exist, isn't loaded, or isn't a SmartBlockEntity.
     */
    public static <T extends BlockEntityBehaviour> Optional<T> getOptional(final Level level, @NotNull final BlockPos pos, final BehaviourType<T> type) {
        return Optional.ofNullable(BlockEntityBehaviour.get(level, pos, type));
    }

    /**
     * Shorthand for getting an optional behaviour of a specific type from a level at a given position, and expect it.
     * If you do not want to throw, then use {@link #getOptional(Level, BlockPos, BehaviourType)} or {@link #get(BlockGetter, BlockPos, BehaviourType)}.
     *
     * @param level the level to get the behaviour from
     * @param pos   the position of the block entity to get the behaviour from
     * @param type  the type of the behaviour to get
     * @param <T>   the type of the behaviour
     * @return an optional containing the behaviour if it exists and is of the correct type, or an empty optional if it doesn't exist, isn't loaded, or isn't a SmartBlockEntity.
     */
    public static <T extends BlockEntityBehaviour> T getOrThrow(final Level level, @NotNull final BlockPos pos, final BehaviourType<T> type) {
        return Optional.ofNullable(BlockEntityBehaviour.get(level, pos, type))
                .orElseThrow(() -> new IllegalStateException(
                        "Expected to find a behaviour (type " +
                                type +
                                ") at position " +
                                pos +
                                ", but it was not present or was not of the correct type."
                ));
    }

    /**
     * Shorthand for getting an optional behaviour of a specific type from a level at a given position.
     *
     * @param be   the block entity to get the behaviour from
     * @param type the type of the behaviour to get
     * @param <T>  the type of the behaviour
     * @return an optional containing the behaviour if it exists and is of the correct type, or an empty optional if it doesn't exist, isn't loaded, or isn't a SmartBlockEntity.
     */
    public static <T extends BlockEntityBehaviour> Optional<T> getOptional(final BlockEntity be, final BehaviourType<T> type) {
        return Optional.ofNullable(BlockEntityBehaviour.get(be, type));
    }

    /**
     * Shorthand for getting an optional behaviour of a specific type from a level at a given position.
     * If you do not want to throw, then use {@link #getOptional(BlockEntity, BehaviourType)} or {@link #get(BlockEntity, BehaviourType)}.
     *
     * @param be   the block entity to get the behaviour from
     * @param type the type of the behaviour to get
     * @param <T>  the type of the behaviour
     * @return an optional containing the behaviour if it exists and is of the correct type, or an empty optional if it doesn't exist, isn't loaded, or isn't a SmartBlockEntity.
     */
    public static <T extends BlockEntityBehaviour> T getOrThrow(final BlockEntity be, final BehaviourType<T> type) {
        return Optional.ofNullable(BlockEntityBehaviour.get(be, type))
                .orElseThrow(() -> new IllegalStateException(
                        "Expected to find a behaviour (type " +
                                type +
                                ") inside block entity " +
                                be +
                                " at position " +
                                be.getBlockPos() +
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
    public <T extends SuperBlockEntityBehaviour> T getSameBehaviour(final BlockPos otherPos) {
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
    public <T extends SuperBlockEntityBehaviour> Optional<T> getSameBehaviourOptional(final BlockPos otherPos) {
        return Optional.ofNullable(getSameBehaviour(otherPos));
    }

    /**
     * Shorthand for getting a complementary behaviour of the same type on another block entity, and expect it to exist.
     * If you do not want to throw, then use {@link #getSameBehaviourOptional(BlockPos)} or {@link #getSameBehaviour(BlockPos)}.
     *
     * @param otherPos the position of the other block entity to get the behaviour from
     * @param <T>      the type of the current behaviour, used to ensure the returned value is of the correct type
     * @return the complementary behaviour if it exists and is of the same type
     * @throws IllegalStateException if the complementary behaviour does not exist, is not loaded, or is of a different type
     */
    public <T extends SuperBlockEntityBehaviour> T getSameBehaviourOrThrow(final BlockPos otherPos) {
        return this.<T>getSameBehaviourOptional(otherPos)
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
    public <T extends SuperBlockEntityBehaviour> T getSameBehaviour(final BlockEntity otherBlockEntity) {
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
    public <T extends SuperBlockEntityBehaviour> Optional<T> getSameBehaviourOptional(final BlockEntity otherBlockEntity) {
        return Optional.ofNullable(getSameBehaviour(otherBlockEntity));
    }

    /**
     * Shorthand for getting a complementary behaviour of the same type on another block entity, and expect it to exist.
     * If you do not want to throw, then use {@link #getSameBehaviourOptional(BlockEntity)} or {@link #getSameBehaviour(BlockEntity)}.
     *
     * @param otherBlockEntity the other block entity to get the behaviour from
     * @param <T>              the type of the current behaviour, used to ensure the returned value is of the correct type
     * @return the complementary behaviour if it exists and is of the same type
     * @throws IllegalStateException if the complementary behaviour does not exist or is of a different type
     */
    public <T extends SuperBlockEntityBehaviour> T getSameBehaviourOrThrow(final BlockEntity otherBlockEntity) {
        return this.<T>getSameBehaviourOptional(otherBlockEntity)
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

    public void onBlockBroken(final BlockEvent.BreakEvent event) {
    }

    //endregion

}
