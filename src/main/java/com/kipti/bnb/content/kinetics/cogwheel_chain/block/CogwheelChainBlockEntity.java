package com.kipti.bnb.content.kinetics.cogwheel_chain.block;

import com.kipti.bnb.content.decoration.girder_strut.IBlockEntityRelighter;
import com.simibubi.create.content.kinetics.simpleRelays.SimpleKineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class CogwheelChainBlockEntity extends SimpleKineticBlockEntity implements IBlockEntityRelighter {

    public CogwheelChainBlockEntity(final BlockEntityType<?> type, final BlockPos pos, final BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(final List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
    }
}

