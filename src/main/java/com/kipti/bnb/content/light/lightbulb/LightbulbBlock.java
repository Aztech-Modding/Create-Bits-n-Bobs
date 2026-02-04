package com.kipti.bnb.content.light.lightbulb;

import com.kipti.bnb.content.light.founation.LightBlock;
import com.kipti.bnb.registry.BnbBlocks;
import com.kipti.bnb.registry.BnbShapes;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class LightbulbBlock extends LightBlock {

    public static final BooleanProperty CAGE = BooleanProperty.create("cage");

    protected final DyeColor color;

    public LightbulbBlock(final Properties properties, DyeColor color) {
        super(properties, BnbShapes.LIGHTBULB_SHAPE);
        this.color = color;
        this.registerDefaultState(this.defaultBlockState().setValue(CAGE, false));
    }

    public DyeColor getColor() {
        return color;
    }

    public static BlockState withColor(BlockState state, DyeColor color) {
        return (color == DyeColor.WHITE ? BnbBlocks.WHITE_LIGHTBULB : BnbBlocks.LIGHTBULBS.get(color))
                .getDefaultState()
                .setValue(CAGE, state.getValue(CAGE))
                .setValue(LightBlock.FORCED_ON, state.getValue(LightBlock.FORCED_ON))
                .setValue(DirectionalBlock.FACING, state.getValue(DirectionalBlock.FACING))
        ;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        DyeColor dye = DyeColor.getColor(stack);

        if (!stack.isEmpty())
            if (dye != null) {
                level.setBlockAndUpdate(pos, withColor(state, dye));
                return ItemInteractionResult.SUCCESS;
            } else {
                return ItemInteractionResult.FAIL;
            }
        if (level.isClientSide)
            return ItemInteractionResult.SUCCESS;
        level.setBlock(pos, state.cycle(FORCED_ON), 3);
        level.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.3F, state.getValue(FORCED_ON) ? 0.6F : 0.5F);
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        ItemStack pickBlock = super.getCloneItemStack(state, target, level, pos, player);
        if (pickBlock.isEmpty())
            return BnbBlocks.WHITE_LIGHTBULB.get().getCloneItemStack(state, target, level, pos, player);
        return pickBlock;
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
