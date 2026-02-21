package com.kipti.bnb.content.kinetics.encased_blocks.cogwheel_chain;

import com.kipti.bnb.content.kinetics.cogwheel_chain.block.ICogwheelChainBlock;
import com.kipti.bnb.content.kinetics.encased_blocks.BnbEncasedCogwheelBlock;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

public class BnbEncasedConnectingCogwheelChainBlock extends BnbEncasedCogwheelBlock implements ICogwheelChainBlock {

    public BnbEncasedConnectingCogwheelChainBlock(final Properties properties, final boolean large, final Supplier<Block> casing) {
        super(properties, large, casing);
    }

}