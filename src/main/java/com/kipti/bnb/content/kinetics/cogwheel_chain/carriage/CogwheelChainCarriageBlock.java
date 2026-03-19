package com.kipti.bnb.content.kinetics.cogwheel_chain.carriage;

import com.kipti.bnb.content.kinetics.cogwheel_chain.attachment.CogwheelChainAttachmentHelper;
import com.kipti.bnb.content.kinetics.cogwheel_chain.attachment.CogwheelChainAttachments;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * A carriage block that attaches to a nearby cogwheel chain and assembles
 * blocks below it into a contraption that rides along the chain.
 *
 * <p>Right-click with an empty hand to attach to the nearest chain and begin
 * assembly, or to disassemble an already-running contraption.</p>
 *
 * <p>TODO: Register this block and its block entity in the BnB registrate system.</p>
 */
public class CogwheelChainCarriageBlock extends Block implements IBE<CogwheelChainCarriageBlockEntity> {

    public CogwheelChainCarriageBlock(final Properties properties) {
        super(properties);
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
            if (be.isAssembled()) {
                be.disassemble();
            } else {
                this.tryAttachToChain(level, pos, player, be);
            }
        });
        return ItemInteractionResult.SUCCESS;
    }

    private void tryAttachToChain(final Level level,
                                   final BlockPos pos,
                                   final Player player,
                                   final CogwheelChainCarriageBlockEntity blockEntity) {
        final Vec3 blockCenter = Vec3.atCenterOf(pos);
        final CogwheelChainAttachments attachment =
                CogwheelChainAttachmentHelper.findNearestAttachment(level, blockCenter);

        if (attachment == null || !attachment.isValid(level)) {
            player.displayClientMessage(
                    Component.translatable("bits_n_bobs.cogwheel_chain_carriage.no_chain_nearby"), true);
            return;
        }

        blockEntity.setAttachment(attachment);
        blockEntity.assembleNextTick = true;
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
    public Class<CogwheelChainCarriageBlockEntity> getBlockEntityClass() {
        return CogwheelChainCarriageBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends CogwheelChainCarriageBlockEntity> getBlockEntityType() {
        // TODO: Return BnbBlockEntities.COGWHEEL_CHAIN_CARRIAGE.get() after registration
        throw new UnsupportedOperationException("Block entity type not yet registered in BnbBlockEntities");
    }
}
