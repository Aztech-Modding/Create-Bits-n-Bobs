package com.kipti.bnb.content.girder_strut;

import com.kipti.bnb.registry.BnbDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class GirderStrutBlockItem extends BlockItem {

    private static final double MAX_ANGLE_DEGREES = 90;
    private static final double MIN_DOT_THRESHOLD = Math.cos(Math.toRadians(MAX_ANGLE_DEGREES));

    public GirderStrutBlockItem(final Block block, final Properties properties) {
        super(block, properties);
    }

    @Override
    public @NotNull InteractionResult useOn(final UseOnContext context) {
        final ItemStack stack = context.getItemInHand();
        final Level level = context.getLevel();
        final BlockPos clickedPos = context.getClickedPos();
        final Direction face = context.getClickedFace();

        if (context.isSecondaryUseActive()) {
            if (stack.has(BnbDataComponents.GIRDER_STRUT_FROM) || stack.has(BnbDataComponents.GIRDER_STRUT_FROM_FACE)) {
                stack.remove(BnbDataComponents.GIRDER_STRUT_FROM);
                stack.remove(BnbDataComponents.GIRDER_STRUT_FROM_FACE);
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            return InteractionResult.PASS;
        }

        final BlockPos placementPos = resolvePlacementPos(level, clickedPos, face);
        Direction targetFace = face;
        if (placementPos != null && level.getBlockState(placementPos).getBlock().equals(getBlock())) {
            targetFace = level.getBlockState(placementPos).getValue(GirderStrutBlock.FACING);
        }

        if (!stack.has(BnbDataComponents.GIRDER_STRUT_FROM)) {
            if (placementPos == null) {
                return InteractionResult.FAIL;
            }

            stack.set(BnbDataComponents.GIRDER_STRUT_FROM, placementPos);
            stack.set(BnbDataComponents.GIRDER_STRUT_FROM_FACE, targetFace);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        final BlockPos fromPos = stack.get(BnbDataComponents.GIRDER_STRUT_FROM);
        Direction fromFace = stack.get(BnbDataComponents.GIRDER_STRUT_FROM_FACE);
        if (fromPos == null) {
            stack.remove(BnbDataComponents.GIRDER_STRUT_FROM);
            stack.remove(BnbDataComponents.GIRDER_STRUT_FROM_FACE);
            return InteractionResult.FAIL;
        }

        if (placementPos == null) {
            return InteractionResult.FAIL;
        }

        if (fromFace == null) {
            final BlockState fromState = level.getBlockState(fromPos);
            if (fromState.getBlock().equals(getBlock())) {
                fromFace = fromState.getValue(GirderStrutBlock.FACING);
            } else {
                fromFace = targetFace.getOpposite();
            }
        }

        if (!level.isClientSide) {
            final ConnectionResult result = tryConnect(context, fromPos, fromFace, placementPos, targetFace);
            if (result != ConnectionResult.SUCCESS) {
                if (result == ConnectionResult.INVALID) {
                    stack.remove(BnbDataComponents.GIRDER_STRUT_FROM);
                    stack.remove(BnbDataComponents.GIRDER_STRUT_FROM_FACE);
                }
                return InteractionResult.FAIL;
            }
        }

        stack.remove(BnbDataComponents.GIRDER_STRUT_FROM);
        stack.remove(BnbDataComponents.GIRDER_STRUT_FROM_FACE);

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public boolean isFoil(final ItemStack stack) {
        return stack.has(BnbDataComponents.GIRDER_STRUT_FROM) || super.isFoil(stack);
    }

    public static boolean isValidConnection(final Level level, final BlockPos fromPos, final Direction fromFace, final BlockPos toPos, final Direction toFace) {
        if (fromPos == null || toPos == null || fromFace == null || toFace == null) {
            return false;
        }

        if (fromPos.equals(toPos)) {
            return false;
        }

        final int diffX = toPos.getX() - fromPos.getX();
        final int diffY = toPos.getY() - fromPos.getY();
        final int diffZ = toPos.getZ() - fromPos.getZ();

        final int nonZero = (diffX != 0 ? 1 : 0) + (diffY != 0 ? 1 : 0) + (diffZ != 0 ? 1 : 0);
        if (nonZero >= 3) {
            return false;
        }

        final double lengthSq = diffX * diffX + diffY * diffY + diffZ * diffZ;
        if (lengthSq > GirderStrutBlock.MAX_SPAN * GirderStrutBlock.MAX_SPAN) {
            return false;
        }

        final Vec3 fromCenter = Vec3.atCenterOf(fromPos);
        final Vec3 toCenter = Vec3.atCenterOf(toPos);
        final Vec3 connection = toCenter.subtract(fromCenter);
        final Vec3 reverseConnection = fromCenter.subtract(toCenter);

        if (!isWithinAngle(connection, fromFace)) {
            return false;
        }

        return isWithinAngle(reverseConnection, toFace);
    }

    private static boolean isWithinAngle(final Vec3 vector, final Direction face) {
        if (vector.lengthSqr() < 1.0E-6) {
            return false;
        }
        final Vec3 unitVec = vector.normalize();
        final Vec3 faceNormal = Vec3.atLowerCornerOf(face.getNormal()).normalize();
        final double dot = unitVec.dot(faceNormal);
        return dot >= MIN_DOT_THRESHOLD;
    }

    private ConnectionResult tryConnect(final UseOnContext context, final BlockPos fromPos, final Direction fromFace, final BlockPos targetPos, final Direction targetFace) {
        final Level level = context.getLevel();
        final Player player = context.getPlayer();
        final ItemStack stack = context.getItemInHand();

        if (!isValidConnection(level, fromPos, fromFace, targetPos, targetFace)) {
            return ConnectionResult.INVALID;
        }

        final BlockState fromState = level.getBlockState(fromPos);
        final BlockState targetState = level.getBlockState(targetPos);

        final boolean fromNeedsPlacement = !(fromState.getBlock().equals(getBlock()));
        final boolean targetNeedsPlacement = !(targetState.getBlock().equals(getBlock()));

        final int requiredAnchors = (fromNeedsPlacement ? 1 : 0) + (targetNeedsPlacement ? 1 : 0);

        if (fromNeedsPlacement && !canOccupy(level, fromPos)) {
            return ConnectionResult.INVALID;
        }
        if (targetNeedsPlacement && !canOccupy(level, targetPos)) {
            return ConnectionResult.INVALID;
        }

        if (player != null && !player.getAbilities().instabuild) {
            if (!hasRequiredAnchors(player, stack, requiredAnchors)) {
                return ConnectionResult.MISSING_ITEMS;
            }
        }

        int placedCount = 0;

        if (fromNeedsPlacement) {
            if (!placeAnchor(level, fromPos, fromFace, player, stack.copy())) {
                return ConnectionResult.INVALID;
            }
            placedCount++;
        } else if (fromState.getValue(GirderStrutBlock.FACING) != fromFace) {
            level.setBlock(fromPos, fromState.setValue(GirderStrutBlock.FACING, fromFace), Block.UPDATE_ALL);
        }

        if (targetNeedsPlacement) {
            if (!placeAnchor(level, targetPos, targetFace, player, stack.copy())) {
                // rollback other placement if we placed it
                if (fromNeedsPlacement) {
                    level.removeBlock(fromPos, false);
                }
                return ConnectionResult.INVALID;
            }
            placedCount++;
        } else if (targetState.getValue(GirderStrutBlock.FACING) != targetFace) {
            level.setBlock(targetPos, targetState.setValue(GirderStrutBlock.FACING, targetFace), Block.UPDATE_ALL);
        }

        final BlockState newFromState = level.getBlockState(fromPos);
        final BlockState newTargetState = level.getBlockState(targetPos);

        if (!(newFromState.getBlock().equals(getBlock())) || !(newTargetState.getBlock().equals(getBlock()))) {
            return ConnectionResult.INVALID;
        }

        if (placedCount > 0) {
            consumeAnchors(player, stack, placedCount);
        }

        final SoundType soundType = getBlock().defaultBlockState().getSoundType(level, targetPos, context.getPlayer());
        level.playSound(null, targetPos, soundType.getPlaceSound(), SoundSource.BLOCKS, (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);

        connect(level, fromPos, targetPos);
        return ConnectionResult.SUCCESS;
    }

    private void connect(final Level level, final BlockPos fromPos, final BlockPos targetPos) {
        if (!(level.getBlockEntity(fromPos) instanceof final GirderStrutBlockEntity from)) {
            return;
        }
        if (!(level.getBlockEntity(targetPos) instanceof final GirderStrutBlockEntity target)) {
            return;
        }
        from.addConnection(targetPos);
        target.addConnection(fromPos);

        final BlockState updatedFromState = level.getBlockState(fromPos);
        final BlockState updatedTargetState = level.getBlockState(targetPos);
        level.sendBlockUpdated(fromPos, updatedFromState, updatedFromState, Block.UPDATE_ALL);
        level.sendBlockUpdated(targetPos, updatedTargetState, updatedTargetState, Block.UPDATE_ALL);
    }

    private boolean hasRequiredAnchors(final Player player, final ItemStack heldStack, final int required) {
        if (required <= 0) {
            return true;
        }
        if (player == null) {
            return heldStack.getCount() >= required;
        }

        final int available = countAnchors(player, heldStack);
        if (available < required) {
            notifyMissingAnchors(player, required - available);
            return false;
        }
        return true;
    }

    private void consumeAnchors(final Player player, final ItemStack heldStack, final int amount) {
        if (amount <= 0 || player == null || player.getAbilities().instabuild) {
            return;
        }

        int remaining = amount;
        remaining -= drainStack(heldStack, remaining);

        final Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize() && remaining > 0; i++) {
            final ItemStack slotStack = inventory.getItem(i);
            if (slotStack == heldStack) {
                continue;
            }
            if (!isMatchingStrut(slotStack, heldStack)) {
                continue;
            }
            remaining -= drainStack(slotStack, remaining);
        }
    }

    private int drainStack(final ItemStack stack, final int amount) {
        if (amount <= 0) {
            return 0;
        }
        final int toRemove = Math.min(stack.getCount(), amount);
        if (toRemove > 0) {
            stack.shrink(toRemove);
        }
        return toRemove;
    }

    private int countAnchors(final Player player, final ItemStack reference) {
        final Inventory inventory = player.getInventory();
        int total = 0;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            final ItemStack slotStack = inventory.getItem(i);
            if (!isMatchingStrut(slotStack, reference)) {
                continue;
            }
            total += slotStack.getCount();
        }
        return total;
    }

    private boolean isMatchingStrut(final ItemStack candidate, final ItemStack reference) {
        return !candidate.isEmpty() && candidate.getItem() == reference.getItem();
    }

    private void notifyMissingAnchors(final Player player, final int missing) {
        if (missing <= 0) {
            return;
        }
        final Component message = Component.translatable("message.bits_n_bobs.girder_strut.missing_anchors", missing)
                .withStyle(ChatFormatting.RED); //TODO: proper red color this is ew, and for the chains
        if (player instanceof final ServerPlayer serverPlayer) {
            serverPlayer.displayClientMessage(message, true);
        } else {
            player.displayClientMessage(message, true);
        }
    }

    private boolean placeAnchor(final Level level, final BlockPos pos, final Direction face, final Player player, final ItemStack stackSnapshot) {
        final BlockState newState = getBlock().defaultBlockState()
                .setValue(GirderStrutBlock.FACING, face)
                .setValue(BlockStateProperties.WATERLOGGED, level.getFluidState(pos).getType() == Fluids.WATER);
        if (!level.setBlock(pos, newState, Block.UPDATE_ALL)) {
            return false;
        }

        final Block block = newState.getBlock();
        block.setPlacedBy(level, pos, newState, player, stackSnapshot);

        final SoundType soundType = newState.getSoundType();
        level.playSound(player, pos, soundType.getPlaceSound(), SoundSource.BLOCKS,
                (soundType.getVolume() + 1.0F) / 2.0F,
                soundType.getPitch() * 0.8F);
        level.gameEvent(player, net.minecraft.world.level.gameevent.GameEvent.BLOCK_PLACE, pos);
        return true;
    }

    private boolean canOccupy(final Level level, final BlockPos pos) {
        final BlockState state = level.getBlockState(pos);
        return state.canBeReplaced() || state.getBlock().equals(getBlock());
    }

    private BlockPos resolvePlacementPos(final Level level, final BlockPos clickedPos, final Direction face) {
        final BlockState clickedState = level.getBlockState(clickedPos);
        if (clickedState.getBlock().equals(getBlock())) {
            return clickedPos;
        }
        final BlockPos pos = clickedPos.relative(face);
        final BlockState state = level.getBlockState(pos);
        if (!state.canBeReplaced() && !(state.getBlock().equals(getBlock()))) {
            return null;
        }
        return pos;
    }

    private enum ConnectionResult {
        SUCCESS,
        INVALID,
        MISSING_ITEMS
    }

}
