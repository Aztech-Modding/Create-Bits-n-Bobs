package com.kipti.bnb.content.kinetics.cogwheel_chain.block;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import com.tterrag.registrate.util.entry.BlockEntry;

import java.util.function.Supplier;

public class ConnectingCogwheelChainBlock extends CogwheelChainBlock implements ICogWheel {

    protected ConnectingCogwheelChainBlock(final boolean large, Properties properties, Supplier<BlockEntry<?>> sourceBlock) {
        super(large, properties, sourceBlock);
    }

    public static ConnectingCogwheelChainBlock small(final Properties properties) {
        return new ConnectingCogwheelChainBlock(false, properties, () -> AllBlocks.COGWHEEL);
    }

    public static ConnectingCogwheelChainBlock large(final Properties properties) {
        return new ConnectingCogwheelChainBlock(true, properties, () -> AllBlocks.LARGE_COGWHEEL);
    }

    @Override
    public boolean isLargeCog() {
        return isLargeChainCog();
    }
}
