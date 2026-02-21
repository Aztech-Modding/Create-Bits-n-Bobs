package com.kipti.bnb.content.kinetics.encased_blocks;

import com.kipti.bnb.registry.content.BnbBlockEntitiesBootstrap;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.encased.EncasedShaftBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

public class BnbEncasedShaftBlock extends EncasedShaftBlock {

    public BnbEncasedShaftBlock(final Properties properties, final Supplier<Block> casing) {
        super(properties, casing);
    }

    @Override
    public BlockEntityType<? extends KineticBlockEntity> getBlockEntityType() {
        return BnbBlockEntitiesBootstrap.ENCASED_SHAFT.get();
    }
}

