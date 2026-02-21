package com.kipti.bnb.content.kinetics.encased_blocks;

import com.kipti.bnb.registry.content.BnbBlockEntities;
import com.simibubi.create.content.kinetics.simpleRelays.SimpleKineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.encased.EncasedCogwheelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

public class BnbEncasedCogwheelBlock extends EncasedCogwheelBlock {

    public BnbEncasedCogwheelBlock(final Properties properties, final boolean large, final Supplier<Block> casing) {
        super(properties, large, casing);
    }

    @Override
    public BlockEntityType<? extends SimpleKineticBlockEntity> getBlockEntityType() {
        return isLarge ? BnbBlockEntities.ENCASED_LARGE_COGWHEEL.get() : BnbBlockEntities.ENCASED_COGWHEEL.get();
    }

}

