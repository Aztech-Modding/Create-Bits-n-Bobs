package com.kipti.bnb.content.kinetics.cogwheel_chain.graph;

import com.kipti.bnb.content.kinetics.cogwheel_chain.block.IExclusiveCogwheelChainBlock;
import com.kipti.bnb.registry.core.BnbTags;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public record CogwheelChainCandidate(Direction.Axis axis, boolean isLarge, boolean hasSmallCogwheelOffset) {

    public static Direction.Axis getAxis(final BlockState state) {
        if (state.getBlock() instanceof final ICogWheel cogwheelBlock)
            return cogwheelBlock.getRotationAxis(state);
        if (state.getBlock() instanceof final IExclusiveCogwheelChainBlock exclusiveBlock)
            return exclusiveBlock.getRotationAxis(state);
        return Direction.Axis.Y;
    }

    public static boolean isValidCandidate(final BlockState state) {
        final Block block = state.getBlock();
        return block instanceof ICogWheel || block instanceof IExclusiveCogwheelChainBlock;
    }

    public static boolean isLargeCogwheel(final BlockState state) {
        if (state.getBlock() instanceof final ICogWheel cogwheelBlock)
            return cogwheelBlock.isLargeCog();
        if (state.getBlock() instanceof final IExclusiveCogwheelChainBlock exclusiveBlock)
            return exclusiveBlock.isLargeCog();
        return false;
    }

    private static boolean hasSmallCogwheelOffset(final BlockState state) {
        if (BnbTags.BnbBlockTags.COGWHEEL_CHAIN_NO_SMALL_OFFSET.matches(state))
            return false;
        if (state.getBlock() instanceof final IExclusiveCogwheelChainBlock exclusiveBlock)
            return !exclusiveBlock.isLargeCog();
        return true;
    }

    public static @Nullable CogwheelChainCandidate getForBlock(final BlockState state) {
        if (!isValidCandidate(state))
            return null;
        return new CogwheelChainCandidate(getAxis(state), isLargeCogwheel(state), hasSmallCogwheelOffset(state));
    }

    public boolean isConsistentWithNode(final ICogwheelNode node) {
        if (node.isLarge() != isLarge)
            return false;
        if (node.rotationAxis() != axis)
            return false;
        return node.hasSmallCogwheelOffset() == hasSmallCogwheelOffset;
    }
}

