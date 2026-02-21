package com.kipti.bnb.content.kinetics.cogwheel_chain.graph;

import com.kipti.bnb.foundation.EncasedBlockList;
import com.kipti.bnb.registry.content.BnbBlocksBootstrap;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.api.registry.SimpleRegistry;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

public record CogwheelChainCandidateInfo(boolean isLarge, boolean hasSmallCogwheelOffset,
                                         Supplier<? extends Block> resultingBlock) {

    public static final SimpleRegistry<Block, CogwheelChainCandidateInfo> REGISTRY = SimpleRegistry.create();

    static {
        REGISTRY.registerProvider((b) -> {
            if (b == AllBlocks.COGWHEEL.get()) {
                return new CogwheelChainCandidateInfo(false, true, BnbBlocksBootstrap.SMALL_COGWHEEL_CHAIN);
            } else if (b == AllBlocks.LARGE_COGWHEEL.get()) {
                return new CogwheelChainCandidateInfo(true, false, BnbBlocksBootstrap.LARGE_COGWHEEL_CHAIN);
            }

            if (b == AllBlocks.ANDESITE_ENCASED_COGWHEEL.get()) {
                return new CogwheelChainCandidateInfo(false, false, BnbBlocksBootstrap.ENCASED_CHAIN_COGWHEEL.get(EncasedBlockList.CasingMaterial.ANDESITE));
            } else if (b == AllBlocks.BRASS_ENCASED_COGWHEEL.get()) {
                return new CogwheelChainCandidateInfo(false, false, BnbBlocksBootstrap.ENCASED_CHAIN_COGWHEEL.get(EncasedBlockList.CasingMaterial.BRASS));
            } else if (b == AllBlocks.ANDESITE_ENCASED_LARGE_COGWHEEL.get()) {
                return new CogwheelChainCandidateInfo(true, false, BnbBlocksBootstrap.ENCASED_LARGE_CHAIN_COGWHEEL.get(EncasedBlockList.CasingMaterial.ANDESITE));
            } else if (b == AllBlocks.BRASS_ENCASED_LARGE_COGWHEEL.get()) {
                return new CogwheelChainCandidateInfo(true, false, BnbBlocksBootstrap.ENCASED_LARGE_CHAIN_COGWHEEL.get(EncasedBlockList.CasingMaterial.BRASS));
            }
            return null;
        });
    }

    /**
     * Creates a consumer that will register a candidate info to a block. Useful for Registrate.
     */
    public static <B extends Block> NonNullConsumer<? super B> candidate(CogwheelChainCandidateInfo info) {
        return b -> REGISTRY.register(b, info);
    }
}

