package com.kipti.bnb.content.kinetics.encased_blocks.piston_pole;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.piston.PistonExtensionPoleBlock;
import com.simibubi.create.content.decoration.encasing.EncasedBlock;
import com.simibubi.create.foundation.utility.BlockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Supplier;

public class EncasedPistonExtensionPoleBlock extends PistonExtensionPoleBlock implements EncasedBlock {

    public static final BooleanProperty EMPTY = BooleanProperty.create("empty");

    private final Supplier<? extends Block> casing;

    public EncasedPistonExtensionPoleBlock(final Properties properties, final Supplier<? extends Block> casing) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(EMPTY, false));
        this.casing = casing;
    }

    @Override
    public VoxelShape getShape(final BlockState state, final BlockGetter worldIn, final BlockPos pos, final CollisionContext context) {
        return Shapes.block();
    }

    @Override
    protected ItemInteractionResult useItemOn(final ItemStack stack, final BlockState state, final Level level, final BlockPos pos, final Player player, final InteractionHand hand, final BlockHitResult hitResult) {
        if (state.getValue(EMPTY) && AllBlocks.PISTON_EXTENSION_POLE.isIn(stack)) {
            if (!level.isClientSide) {
                level.setBlock(pos, state.setValue(EMPTY, false), Block.UPDATE_CLIENTS);
                if (!player.hasInfiniteMaterials()) stack.shrink(1);
            }
            return ItemInteractionResult.SUCCESS;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public InteractionResult onSneakWrenched(final BlockState state, final UseOnContext context) {
        if (context.getLevel().isClientSide)
            return InteractionResult.SUCCESS;
        context.getLevel()
                .levelEvent(2001, context.getClickedPos(), Block.getId(state));
        context.getLevel().setBlock(
                context.getClickedPos(),
                BlockHelper.copyProperties(state, AllBlocks.PISTON_EXTENSION_POLE.getDefaultState()),
                Block.UPDATE_CLIENTS
        );
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(EMPTY);
    }

    @Override
    public Block getCasing() {
        return casing.get();
    }

    @Override
    public void handleEncasing(final BlockState state, final Level level, final BlockPos pos, final ItemStack heldItem, final Player player, final InteractionHand hand, final BlockHitResult ray) {
        level.setBlock(pos, BlockHelper.copyProperties(state, defaultBlockState()), Block.UPDATE_ALL | Block.UPDATE_MOVE_BY_PISTON);
    }

}
