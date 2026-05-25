package com.kipti.bnb.content.decoration.truss;

import com.kipti.bnb.registry.content.blocks.deco.BnbDecorativeBlocks;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jspecify.annotations.NonNull;

public class TrussBlockItem extends BlockItem {

    public TrussBlockItem(final TrussBlock block, final Properties properties) {
        super(block, properties);
    }

    @Override
    public @NonNull InteractionResult useOn(final UseOnContext context) {
        final BlockPos clickedPos = context.getClickedPos();
        final Level level = context.getLevel();

        final BlockState clickedState = level.getBlockState(clickedPos);

        if (AllBlocks.FLUID_PIPE.is(clickedState.getBlock())) {
            FluidTransportBehaviour.cacheFlows(level, clickedPos);
            level.setBlockAndUpdate(
                    clickedPos, BnbDecorativeBlocks.METAL_TRUSS_PIPE.getDefaultState()
                            .setValue(TrussFluidPipe.AXIS, this.getAxisOfPipe(clickedState, context.getClickedFace()))
                            .setValue(
                                    BlockStateProperties.WATERLOGGED,
                                    clickedState.getValue(BlockStateProperties.WATERLOGGED)
                            )
            );
            FluidTransportBehaviour.loadFlows(level, clickedPos);
            level.playSound(
                    context.getPlayer(),
                    clickedPos,
                    BnbDecorativeBlocks.METAL_TRUSS_PIPE.get().getSoundType(
                            clickedState,
                            level,
                            clickedPos,
                            context.getPlayer()
                    ).getPlaceSound(),
                    SoundSource.BLOCKS,
                    0.5f,
                    1.25f
            );
            return InteractionResult.SUCCESS;
        }

        return super.useOn(context);
    }

    private Direction.Axis getAxisOfPipe(final BlockState clickedState, final Direction clickedFace) {
        Direction.Axis singlePipeAxis = null;
        for (final Direction direction : Direction.values()) {
            if (clickedState.getValue(PipeBlock.PROPERTY_BY_DIRECTION.get(direction))) {
                if (singlePipeAxis == null) {
                    singlePipeAxis = direction.getAxis();
                } else if (singlePipeAxis != direction.getAxis()) {
                    return clickedFace.getAxis();
                }
            }
        }
        return singlePipeAxis;
    }
}
