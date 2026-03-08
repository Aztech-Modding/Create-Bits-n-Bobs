package com.kipti.bnb.content.articulate;

import com.cake.azimuth.behaviour.SuperBlockEntityBehaviour;
import com.kipti.bnb.CreateBitsnBobs;
import com.simibubi.create.content.trains.track.ITrackBlock;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
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
        if (!ArticulatedTrackUtils.isValidTilt(tiltDegrees))
            CreateBitsnBobs.LOGGER.warn("Recieved invalid tilt value: {}. This may indicate a bug in handling.", tiltDegrees);
        final float validatedTilt = ArticulatedTrackUtils.isValidTilt(tiltDegrees)
                ? tiltDegrees
                : ArticulatedTrackUtils.snapToNearest(tiltDegrees);
        if (Float.compare(this.tiltDegrees, validatedTilt) == 0) {
            return;
        }

        this.tiltDegrees = validatedTilt;
        this.blockEntity.setChanged();

        if (!hasLevel()) {
            return;
        }

        if (isServerLevel())
            sendData();
        else
            refreshRenderedModel();
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
        tiltDegrees = compound.contains(TILT_TAG) ? ArticulatedTrackUtils.snapToNearest(compound.getFloat(TILT_TAG)) : 0f;

        if (clientPacket) {
            refreshRenderedModel();
        }
    }

    public void refreshRenderedModel() {
        this.blockEntity.requestModelDataUpdate();
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
