package com.kipti.bnb.content.decoration.light.lightbulb;

import com.kipti.bnb.content.decoration.light.founation.LightBlock;
import com.kipti.bnb.registry.client.BnbShapes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class LightbulbBlock extends LightBlock {

    public static final BooleanProperty CAGE = BooleanProperty.create("cage");

    public LightbulbBlock(final Properties properties) {
        super(properties, BnbShapes.LIGHTBULB_SHAPE);
        this.registerDefaultState(this.defaultBlockState().setValue(CAGE, false));
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        context.getLevel().setBlock(context.getClickedPos(), state.cycle(CAGE), 3);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected @NotNull VoxelShape getShape(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return (state.getValue(CAGE) ? BnbShapes.LIGHTBULB_CAGED_SHAPE : BnbShapes.LIGHTBULB_SHAPE).get(state.getValue(FACING));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(CAGE);
    }
}

