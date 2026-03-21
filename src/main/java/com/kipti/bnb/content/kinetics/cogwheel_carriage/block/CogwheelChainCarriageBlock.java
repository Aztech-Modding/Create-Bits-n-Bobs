package com.kipti.bnb.content.kinetics.cogwheel_carriage.block;

import com.kipti.bnb.registry.content.BnbBlockEntities;
import com.mojang.serialization.MapCodec;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

/**
 * A carriage block that attaches to a nearby cogwheel chain and assembles
 * blocks below it into a contraption that rides along the chain.
 *
 * <p>Right-click with an empty hand to attach to the nearest chain and begin
 * assembly, or to disassemble an already-running contraption.</p>
 */
public class CogwheelChainCarriageBlock extends HorizontalDirectionalBlock implements IBE<CogwheelChainCarriageBlockEntity> {

    public static final MapCodec<CogwheelChainCarriageBlock> CODEC = simpleCodec(CogwheelChainCarriageBlock::new);

    public CogwheelChainCarriageBlock(final Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> p_49915_) {
        super.createBlockStateDefinition(p_49915_);
        p_49915_.add(FACING);
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(final @NotNull ItemStack stack,
                                                       final @NotNull BlockState state,
                                                       final @NotNull Level level,
                                                       final @NotNull BlockPos pos,
                                                       final Player player,
                                                       final @NotNull InteractionHand hand,
                                                       final @NotNull BlockHitResult hitResult) {
        if (!player.mayBuild())
            return ItemInteractionResult.FAIL;
        if (player.isShiftKeyDown())
            return ItemInteractionResult.FAIL;
        if (!stack.isEmpty())
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (level.isClientSide)
            return ItemInteractionResult.SUCCESS;

        this.withBlockEntityDo(level, pos, be -> {
            be.assembleNextTick = true;
        });
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected void onRemove(final @NotNull BlockState state,
                            final @NotNull Level level,
                            final @NotNull BlockPos pos,
                            final @NotNull BlockState newState,
                            final boolean isMoving) {
        IBE.onRemove(state, level, pos, newState);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(final BlockPlaceContext p_49820_) {
        return super.getStateForPlacement(p_49820_)
                .setValue(FACING, p_49820_.getHorizontalDirection());
    }

    @Override
    public Class<CogwheelChainCarriageBlockEntity> getBlockEntityClass() {
        return CogwheelChainCarriageBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends CogwheelChainCarriageBlockEntity> getBlockEntityType() {
        return BnbBlockEntities.COGWHEEL_CHAIN_CARRIAGE.get();
    }
}
