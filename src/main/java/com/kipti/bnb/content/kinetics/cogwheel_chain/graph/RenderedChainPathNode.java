package com.kipti.bnb.content.kinetics.cogwheel_chain.graph;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public record RenderedChainPathNode(BlockPos relativePos, Vec3 nodeOffset, Vec3 sourceCogwheelAxis) {

    public Vec3 getPosition() {
        return relativePos.getCenter().add(nodeOffset);
    }

}

