package com.kipti.bnb.content.kinetics.chain_pulley;

import com.kipti.bnb.registry.content.blockentities.BnbBlockEntitiesBootstrap;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.pulley.PulleyBlock;
import com.simibubi.create.content.contraptions.pulley.PulleyBlockEntity;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class ChainPulleyBlock extends PulleyBlock implements IWrenchable {
    public ChainPulleyBlock(final Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntityType<? extends PulleyBlockEntity> getBlockEntityType() {
        return BnbBlockEntitiesBootstrap.CHAIN_ROPE_PULLEY.get();
    }

    @Override
    public InteractionResult onSneakWrenched(final BlockState state, final UseOnContext context) {
        if (context.getLevel().isClientSide)
            return InteractionResult.SUCCESS;
        context.getLevel()
                .levelEvent(2001, context.getClickedPos(), Block.getId(state));
        context.getLevel()
                .setBlockAndUpdate(context.getClickedPos(), AllBlocks.ROPE_PULLEY.get().defaultBlockState()
                        .setValue(PulleyBlock.HORIZONTAL_AXIS, state.getValue(PulleyBlock.HORIZONTAL_AXIS)));
        return InteractionResult.SUCCESS;
    }
}

