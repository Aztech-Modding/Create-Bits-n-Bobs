package com.kipti.bnb.content.decoration.weathered_girder;

import com.kipti.bnb.registry.content.blocks.deco.BnbDecorativeBlocks;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.decoration.girder.GirderBlock;
import com.simibubi.create.content.decoration.girder.GirderEncasedShaftBlock;
import com.simibubi.create.content.decoration.placard.PlacardBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.logistics.chute.AbstractChuteBlock;
import com.simibubi.create.content.redstone.nixieTube.NixieTubeBlock;
import net.createmod.catnip.placement.IPlacementHelper;
import net.createmod.catnip.placement.PlacementHelpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;

public class WeatheredGirderBlock extends GirderBlock {

    private static final int placementHelperId = PlacementHelpers.register(new WeatheredGirderPlacementHelper());

    public WeatheredGirderBlock(final Properties p_49795_) {
        super(p_49795_);
    }

    /**
     * Redirecting to weathered girder handling
     */
    @Override
    protected @NotNull ItemInteractionResult useItemOn(final ItemStack stack, final BlockState state, final Level level, final BlockPos pos, final Player player, final InteractionHand hand, final BlockHitResult hitResult) {
        if (AllBlocks.SHAFT.isIn(stack)) {
            KineticBlockEntity.switchToBlockState(level, pos, BnbDecorativeBlocks.WEATHERED_METAL_GIRDER_ENCASED_SHAFT.getDefaultState()
                    .setValue(WATERLOGGED, state.getValue(WATERLOGGED))
                    .setValue(TOP, state.getValue(TOP))
                    .setValue(BOTTOM, state.getValue(BOTTOM))
                    .setValue(GirderEncasedShaftBlock.HORIZONTAL_AXIS, state.getValue(X) || hitResult.getDirection()
                            .getAxis() == Direction.Axis.Z ? Direction.Axis.Z : Direction.Axis.X));

            level.playSound(null, pos, SoundEvents.NETHERITE_BLOCK_HIT, SoundSource.BLOCKS, 0.5f, 1.25f);
            if (!level.isClientSide && !player.isCreative()) {
                stack.shrink(1);
                if (stack.isEmpty())
                    player.setItemInHand(hand, ItemStack.EMPTY);
            }

            return ItemInteractionResult.SUCCESS;
        }

        if (AllItems.WRENCH.isIn(stack) && !player.isShiftKeyDown()) {
            if (WeatheredGirderWrenchBehaviour.handleClick(level, pos, state, hitResult))
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            return ItemInteractionResult.FAIL;
        }

        final IPlacementHelper helper = PlacementHelpers.get(placementHelperId);
        if (helper.matchesItem(stack))
            return helper.getOffset(player, level, state, pos, hitResult)
                    .placeInWorld(level, (BlockItem) stack.getItem(), player, hand, hitResult);

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    public static boolean isConnected(final BlockAndTintGetter world, final BlockPos pos, final BlockState state, final Direction side) {
        final Direction.Axis axis = side.getAxis();
        if (state.getBlock() instanceof WeatheredGirderBlock && !state.getValue(axis == Direction.Axis.X ? X : Z))
            return false;
        if (state.getBlock() instanceof WeatheredGirderEncasedShaftBlock
                && state.getValue(WeatheredGirderEncasedShaftBlock.HORIZONTAL_AXIS) == axis)
            return false;
        final BlockPos relative = pos.relative(side);
        final BlockState blockState = world.getBlockState(relative);
        if (blockState.isAir())
            return false;
        if (blockState.getBlock() instanceof NixieTubeBlock && NixieTubeBlock.getFacing(blockState) == side)
            return true;
        if (isFacingBracket(world, pos, side))
            return true;
        if (blockState.getBlock() instanceof PlacardBlock && PlacardBlock.connectedDirection(blockState) == side)
            return true;
        final VoxelShape shape = blockState.getShape(world, relative);
        if (shape.isEmpty())
            return false;
        if (Block.isFaceFull(shape, side.getOpposite()) && blockState.isSolid())
            return true;
        return AbstractChuteBlock.getChuteFacing(blockState) == Direction.DOWN;
    }

}

