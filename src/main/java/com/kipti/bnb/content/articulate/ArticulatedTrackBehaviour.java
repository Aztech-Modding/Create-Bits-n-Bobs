package com.kipti.bnb.content.articulate;

import com.cake.azimuth.behaviour.SuperBlockEntityBehaviour;
import com.kipti.bnb.registry.core.BnbDataComponents;
import com.simibubi.create.content.trains.track.ITrackBlock;
import com.simibubi.create.content.trains.track.TrackBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.jetbrains.annotations.Nullable;

public class ArticulatedTrackBehaviour extends SuperBlockEntityBehaviour {

    public static final BehaviourType<ArticulatedTrackBehaviour> TYPE = new BehaviourType<>();

    private static final String TILT_TAG = "TrackTilt";

    private float tiltDegrees;

    public ArticulatedTrackBehaviour(final SmartBlockEntity be) {
        super(be);
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

    public float getTiltDegrees() {
        return tiltDegrees;
    }

    public boolean hasTilt() {
        return Float.compare(tiltDegrees, 0f) != 0;
    }

    public void setTiltDegrees(final float tiltDegrees) {
        final float validatedTilt = ArticulatedTrackUtils.isValidTilt(tiltDegrees)
                ? tiltDegrees
                : ArticulatedTrackUtils.snapToNearest(tiltDegrees);
        if (Float.compare(validatedTilt, 0f) != 0
                && this.blockEntity instanceof final TrackBlockEntity trackBlockEntity
                && trackBlockEntity.getConnections().size() > 1) {
            return;
        }
        if (Float.compare(this.tiltDegrees, validatedTilt) == 0) {
            return;
        }

        this.tiltDegrees = validatedTilt;
        this.blockEntity.setChanged();

        if (!hasLevel()) {
            return;
        }

        if (isClientLevel()) {
            refreshRenderedModel();
            return;
        }

        sendData();
    }

    @Override
    public void write(final CompoundTag compound, final HolderLookup.Provider registries, final boolean clientPacket) {
        super.write(compound, registries, clientPacket);
        if (Float.compare(tiltDegrees, 0f) != 0) {
            compound.putFloat(TILT_TAG, tiltDegrees);
        }
    }

    @Override
    public void read(final CompoundTag compound, final HolderLookup.Provider registries, final boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        final float previousTilt = tiltDegrees;
        tiltDegrees = compound.contains(TILT_TAG) ? ArticulatedTrackUtils.snapToNearest(compound.getFloat(TILT_TAG)) : 0f;

        if (clientPacket && Float.compare(previousTilt, tiltDegrees) != 0) {
            refreshRenderedModel();
        }
    }

    @Override
    public void onBlockPlaced(final BlockEvent.EntityPlaceEvent event) {
        if (hasTilt()) {
            return;
        }

        if (!(event.getEntity() instanceof final Player player)) {
            return;
        }

        final ItemStack placementStack = resolvePlacementStack(player, event.getPlacedBlock());
        if (placementStack == null) {
            return;
        }

        final float tiltDegrees = placementStack.getOrDefault(BnbDataComponents.TRACK_TILT, 0f);
        if (Float.compare(tiltDegrees, 0f) != 0) {
            setTiltDegrees(tiltDegrees);
        }
    }

    public void refreshRenderedModel() {
        this.blockEntity.requestModelDataUpdate();
        if (!hasLevel()) {
            return;
        }

        final BlockState blockState = getBlockState();
        getLevel().sendBlockUpdated(this.blockEntity.getBlockPos(), blockState, blockState, 16);
    }

    @Nullable
    private static ItemStack resolvePlacementStack(final Player player, final BlockState placedState) {
        final InteractionHand swingingHand = player.swingingArm;
        if (swingingHand != null) {
            final ItemStack swungStack = player.getItemInHand(swingingHand);
            if (isMatchingPlacementStack(swungStack, placedState) || isTrackItem(swungStack)) {
                return swungStack;
            }
        }

        final ItemStack mainHand = player.getMainHandItem();
        if (isMatchingPlacementStack(mainHand, placedState)) {
            return mainHand;
        }

        final ItemStack offHand = player.getOffhandItem();
        if (isMatchingPlacementStack(offHand, placedState)) {
            return offHand;
        }

        if (isTrackItem(mainHand)) {
            return mainHand;
        }

        if (isTrackItem(offHand)) {
            return offHand;
        }

        return null;
    }

    private static boolean isMatchingPlacementStack(final ItemStack stack, final BlockState placedState) {
        return isTrackItem(stack) && Block.byItem(stack.getItem()) == placedState.getBlock();
    }

    private static boolean isTrackItem(final ItemStack stack) {
        return !stack.isEmpty() && Block.byItem(stack.getItem()) instanceof ITrackBlock;
    }

    public static @Nullable ArticulatedTrackBehaviour get(final @Nullable BlockEntity blockEntity) {
        if (!(blockEntity instanceof final SmartBlockEntity smartBlockEntity) || smartBlockEntity.getLevel() == null) {
            return null;
        }

        return SuperBlockEntityBehaviour.getOptional(smartBlockEntity.getLevel(), smartBlockEntity.getBlockPos(), TYPE)
                .orElse(null);
    }

    public static float getTiltDegrees(final @Nullable BlockEntity blockEntity) {
        final ArticulatedTrackBehaviour behaviour = get(blockEntity);
        return behaviour == null ? 0f : behaviour.getTiltDegrees();
    }

}
