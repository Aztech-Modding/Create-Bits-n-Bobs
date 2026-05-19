package com.kipti.bnb.content.decoration.truss;

import com.kipti.bnb.registry.content.BnbBlockEntities;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.api.schematic.requirement.SpecialBlockItemRequirement;
import com.simibubi.create.content.fluids.pipes.AxisPipeBlock;
import com.simibubi.create.content.fluids.pipes.StraightPipeBlockEntity;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

public class TrussFluidPipe extends AxisPipeBlock implements IBE<StraightPipeBlockEntity>, SimpleWaterloggedBlock, SpecialBlockItemRequirement {

    public TrussFluidPipe(final Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> p_55933_) {
        super.createBlockStateDefinition(p_55933_);
        p_55933_.add(BlockStateProperties.WATERLOGGED);
    }

    @Override
    public ItemRequirement getRequiredItems(final BlockState state, @Nullable final BlockEntity blockEntity) {
        return new ItemRequirement(
                ItemRequirement.ItemUseType.CONSUME,
                this.asItem().getDefaultInstance()
        ).union(new ItemRequirement(
                ItemRequirement.ItemUseType.CONSUME,
                AllBlocks.FLUID_PIPE.asStack()
        ));
    }

    @Override
    public Class<StraightPipeBlockEntity> getBlockEntityClass() {
        return StraightPipeBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends StraightPipeBlockEntity> getBlockEntityType() {
        return BnbBlockEntities.METAL_TRUSS_PIPE.get();
    }

}
