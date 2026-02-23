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
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Extended version of {@link BlockEntityBehaviour} designed for invasive additions of extra functionality for {@link SmartBlockEntity},
 * where you want to compose (or apply) almost full block entity functionality.
 * This also includes some shorthands for accessing things such as block entity level, or getting a likewise behaviour on another block entity.
 */
public abstract non-sealed class SuperBlockEntityBehaviour extends BlockEntityBehaviour implements SuperBlockEntityBehaviourLevelHelpers {
    public SuperBlockEntityBehaviour(SmartBlockEntity be) {
        super(be);
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

    /**
     * Shorthand for getting an optional behaviour of a specific type from a level at a given position.
     *
     * @param level the level to get the behaviour from
     * @param pos   the position of the block entity to get the behaviour from
     * @param type  the type of the behaviour to get
     * @param <T>   the type of the behaviour
     * @return an optional containing the behaviour if it exists and is of the correct type, or an empty optional if it doesn't exist, isn't loaded, or isn't a SmartBlockEntity.
     */
    public static <T extends BlockEntityBehaviour> Optional<T> getOptional(Level level, @NotNull BlockPos pos, BehaviourType<T> type) {
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
    public static <T extends BlockEntityBehaviour> T getOrThrow(Level level, @NotNull BlockPos pos, BehaviourType<T> type) {
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
    public static <T extends BlockEntityBehaviour> Optional<T> getOptional(BlockEntity be, BehaviourType<T> type) {
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
    public static <T extends BlockEntityBehaviour> T getOrThrow(BlockEntity be, BehaviourType<T> type) {
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

}
