package com.kipti.bnb.content.kinetics.encased_blocks.cogwheel_chain;

import com.kipti.bnb.content.kinetics.encased_blocks.BnbEncasedCogwheelBlock;
import com.kipti.bnb.registry.BnbBlockEntities;
import com.simibubi.create.content.kinetics.simpleRelays.SimpleKineticBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

public class BnbEncasedEmptyFlangedGearBlock extends BnbEncasedCogwheelBlock {
    public BnbEncasedEmptyFlangedGearBlock(Properties properties, boolean large, Supplier<Block> casing) {
        super(properties, large, casing);
    }

    @Override
    public BlockEntityType<? extends SimpleKineticBlockEntity> getBlockEntityType() {
        return BnbBlockEntities.EMPTY_FLANGED_COGWHEEL.get();
    }
}
