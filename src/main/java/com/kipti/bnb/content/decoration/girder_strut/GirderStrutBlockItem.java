package com.kipti.bnb.content.decoration.girder_strut;

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
import net.minecraft.world.phys.Vec3;

public class GirderStrutBlockItem extends BlockItem {

    private static final double MAX_ANGLE_DEGREES = 75.0;
    private static final double MIN_DOT_THRESHOLD = Math.cos(Math.toRadians(MAX_ANGLE_DEGREES));

    public GirderStrutBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        Direction face = context.getClickedFace();

        if (context.isSecondaryUseActive()) {
            if (stack.has(BnbDataComponents.GIRDER_STRUT_FROM) || stack.has(BnbDataComponents.GIRDER_STRUT_FROM_FACE)) {
                stack.remove(BnbDataComponents.GIRDER_STRUT_FROM);
                stack.remove(BnbDataComponents.GIRDER_STRUT_FROM_FACE);
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            return InteractionResult.PASS;
        }

        BlockPos placementPos = resolvePlacementPos(level, clickedPos, face);
        Direction placementFace = face;
        if (placementPos != null && level.getBlockState(placementPos).getBlock().equals(getBlock())) {
            placementFace = level.getBlockState(placementPos).getValue(GirderStrutBlock.FACING);
        }

        if (!stack.has(BnbDataComponents.GIRDER_STRUT_FROM)) {
            if (placementPos == null) {
                return InteractionResult.FAIL;
            }

            stack.set(BnbDataComponents.GIRDER_STRUT_FROM, placementPos);
            stack.set(BnbDataComponents.GIRDER_STRUT_FROM_FACE, placementFace);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        BlockPos fromPos = stack.get(BnbDataComponents.GIRDER_STRUT_FROM);
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
            BlockState fromState = level.getBlockState(fromPos);
            if (fromState.getBlock().equals(getBlock())) {
                fromFace = fromState.getValue(GirderStrutBlock.FACING);
            } else {
                fromFace = placementFace.getOpposite();
            }
        }

        Direction targetFace = placementFace;
        if (targetFace == null) {
            targetFace = Direction.UP;
        }

        if (!level.isClientSide) {
            ConnectionResult result = tryConnect(context, fromPos, fromFace, placementPos, targetFace);
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
    public boolean isFoil(ItemStack stack) {
        return stack.has(BnbDataComponents.GIRDER_STRUT_FROM) || super.isFoil(stack);
    }

    public static boolean isValidConnection(Level level, BlockPos fromPos, Direction fromFace, BlockPos toPos, Direction toFace) {
        if (fromPos == null || toPos == null || fromFace == null || toFace == null) {
            return false;
        }

        if (fromPos.equals(toPos)) {
            return false;
        }

        int diffX = toPos.getX() - fromPos.getX();
        int diffY = toPos.getY() - fromPos.getY();
        int diffZ = toPos.getZ() - fromPos.getZ();

        int nonZero = (diffX != 0 ? 1 : 0) + (diffY != 0 ? 1 : 0) + (diffZ != 0 ? 1 : 0);
        if (nonZero >= 3) {
            return false;
        }

        double lengthSq = diffX * diffX + diffY * diffY + diffZ * diffZ;
        if (lengthSq > GirderStrutBlock.MAX_SPAN * GirderStrutBlock.MAX_SPAN) {
            return false;
        }

        Vec3 fromCenter = Vec3.atCenterOf(fromPos);
        Vec3 toCenter = Vec3.atCenterOf(toPos);
        Vec3 connection = toCenter.subtract(fromCenter);
        Vec3 reverseConnection = fromCenter.subtract(toCenter);

        if (!isWithinAngle(connection, fromFace)) {
            return false;
        }

        return isWithinAngle(reverseConnection, toFace);
    }

    private static boolean isWithinAngle(Vec3 vector, Direction face) {
        if (vector.lengthSqr() < 1.0E-6) {
            return false;
        }
        Vec3 unitVec = vector.normalize();
        Vec3 faceNormal = Vec3.atLowerCornerOf(face.getNormal()).normalize();
        double dot = unitVec.dot(faceNormal);
        return dot >= MIN_DOT_THRESHOLD;
    }

    private ConnectionResult tryConnect(UseOnContext context, BlockPos fromPos, Direction fromFace, BlockPos targetPos, Direction targetFace) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        if (!isValidConnection(level, fromPos, fromFace, targetPos, targetFace)) {
            return ConnectionResult.INVALID;
        }

        BlockState fromState = level.getBlockState(fromPos);
        BlockState targetState = level.getBlockState(targetPos);

        boolean fromNeedsPlacement = !(fromState.getBlock().equals(getBlock()));
        boolean targetNeedsPlacement = !(targetState.getBlock().equals(getBlock()));

        int requiredAnchors = (fromNeedsPlacement ? 1 : 0) + (targetNeedsPlacement ? 1 : 0);

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

        BlockState newFromState = level.getBlockState(fromPos);
        BlockState newTargetState = level.getBlockState(targetPos);

        if (!(newFromState.getBlock().equals(getBlock())) || !(newTargetState.getBlock().equals(getBlock()))) {
            return ConnectionResult.INVALID;
        }

        if (placedCount > 0) {
            consumeAnchors(player, stack, placedCount);
        }

        connect(level, fromPos, targetPos);
        return ConnectionResult.SUCCESS;
    }

    private void connect(Level level, BlockPos fromPos, BlockPos targetPos) {
        if (!(level.getBlockEntity(fromPos) instanceof GirderStrutBlockEntity from)) {
            return;
        }
        if (!(level.getBlockEntity(targetPos) instanceof GirderStrutBlockEntity target)) {
            return;
        }
        from.addConnection(targetPos);
        target.addConnection(fromPos);

        BlockState updatedFromState = level.getBlockState(fromPos);
        BlockState updatedTargetState = level.getBlockState(targetPos);
        level.sendBlockUpdated(fromPos, updatedFromState, updatedFromState, Block.UPDATE_ALL);
        level.sendBlockUpdated(targetPos, updatedTargetState, updatedTargetState, Block.UPDATE_ALL);
    }

    private boolean hasRequiredAnchors(Player player, ItemStack heldStack, int required) {
        if (required <= 0) {
            return true;
        }
        if (player == null) {
            return heldStack.getCount() >= required;
        }

        int available = countAnchors(player, heldStack);
        if (available < required) {
            notifyMissingAnchors(player, required - available);
            return false;
        }
        return true;
    }

    private void consumeAnchors(Player player, ItemStack heldStack, int amount) {
        if (amount <= 0 || player == null || player.getAbilities().instabuild) {
            return;
        }

        int remaining = amount;
        remaining -= drainStack(heldStack, remaining);

        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize() && remaining > 0; i++) {
            ItemStack slotStack = inventory.getItem(i);
            if (slotStack == heldStack) {
                continue;
            }
            if (!isMatchingStrut(slotStack, heldStack)) {
                continue;
            }
            remaining -= drainStack(slotStack, remaining);
        }
    }

    private int drainStack(ItemStack stack, int amount) {
        if (amount <= 0) {
            return 0;
        }
        int toRemove = Math.min(stack.getCount(), amount);
        if (toRemove > 0) {
            stack.shrink(toRemove);
        }
        return toRemove;
    }

    private int countAnchors(Player player, ItemStack reference) {
        Inventory inventory = player.getInventory();
        int total = 0;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack slotStack = inventory.getItem(i);
            if (!isMatchingStrut(slotStack, reference)) {
                continue;
            }
            total += slotStack.getCount();
        }
        return total;
    }

    private boolean isMatchingStrut(ItemStack candidate, ItemStack reference) {
        return !candidate.isEmpty() && candidate.getItem() == reference.getItem();
    }

    private void notifyMissingAnchors(Player player, int missing) {
        if (missing <= 0) {
            return;
        }
        Component message = Component.translatable("message.bits_n_bobs.girder_strut.missing_anchors", missing)
                .withStyle(ChatFormatting.RED); //TODO: proper red color this is ew, and for the chains
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.displayClientMessage(message, true);
        } else {
            player.displayClientMessage(message, true);
        }
    }

    private boolean placeAnchor(Level level, BlockPos pos, Direction face, Player player, ItemStack stackSnapshot) {
        BlockState newState = getBlock().defaultBlockState().setValue(GirderStrutBlock.FACING, face);
        if (!level.setBlock(pos, newState, Block.UPDATE_ALL)) {
            return false;
        }

        Block block = newState.getBlock();
        block.setPlacedBy(level, pos, newState, player, stackSnapshot);

        SoundType soundType = newState.getSoundType();
        level.playSound(player, pos, soundType.getPlaceSound(), SoundSource.BLOCKS,
                (soundType.getVolume() + 1.0F) / 2.0F,
                soundType.getPitch() * 0.8F);
        level.gameEvent(player, net.minecraft.world.level.gameevent.GameEvent.BLOCK_PLACE, pos);
        return true;
    }

    private boolean canOccupy(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir() || state.getBlock().equals(getBlock());
    }

    private BlockPos resolvePlacementPos(Level level, BlockPos clickedPos, Direction face) {
        BlockState clickedState = level.getBlockState(clickedPos);
        if (clickedState.getBlock().equals(getBlock())) {
            return clickedPos;
        }
        BlockPos pos = clickedPos.relative(face);
        BlockState state = level.getBlockState(pos);
        if (!state.isAir() && !(state.getBlock().equals(getBlock()))) {
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
