package com.kipti.bnb.content.kinetics.cogwheel_chain.attachment;

import com.cake.azimuth.behaviour.SuperBlockEntityBehaviour;
import com.kipti.bnb.content.kinetics.cogwheel_chain.behaviour.CogwheelChainBehaviour;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.CogwheelChain;
import com.kipti.bnb.content.kinetics.cogwheel_chain.segment.CogwheelChainSegment;
import com.kipti.bnb.content.kinetics.cogwheel_chain.world.CogwheelChainWorld;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Represents an attachment point on a cogwheel chain, parametrized by cumulative distance
 * from the chain origin. The attachment follows the chain's linear movement as the
 * driving cogwheels rotate.
 */
public class CogwheelChainAttachment {

    private static final float DIRECTION_SAMPLING_RANGE = 0.1f; //Range ahead of the current pos to sample for the direction

    private final BlockPos controllerPos;
    private float dist;

    public CogwheelChainAttachment(final BlockPos controllerPos, final float dist) {
        this.controllerPos = controllerPos.immutable();
        this.dist = Math.max(0, dist);
    }

    public boolean isWrappedDistFurther(final Level level,
                                        final float clientChasingChainAttachmentDist,
                                        final float chainAttachmentDist,
                                        final float signum) {
        final float length = this.getTotalLength(level);
        //Find the closest distance (i.e. straight or wrapped dist) then whichever is first in the signum direction
        final float straightDist = Math.abs(clientChasingChainAttachmentDist - chainAttachmentDist);
        final float wrappedDist = length - straightDist;
        if (signum > 0) {
            return straightDist < wrappedDist ? clientChasingChainAttachmentDist > chainAttachmentDist : clientChasingChainAttachmentDist < chainAttachmentDist;
        } else {
            return straightDist < wrappedDist ? clientChasingChainAttachmentDist < chainAttachmentDist : clientChasingChainAttachmentDist > chainAttachmentDist;
        }
    }

    /**
     * Advances {@link #dist} by the chain's linear speed for one tick.
     * Speed is derived from the controller cogwheel's angular velocity and radius,
     * matching the renderer formula: {@code 2π × chainRotationFactor × RPM / 1200}.
     */
    public void tick(final Level level) {
        final CogwheelChainBehaviour behaviour = SuperBlockEntityBehaviour.get(
                level, this.controllerPos, CogwheelChainBehaviour.TYPE);
        if (behaviour == null) return;

        if (!(level.getBlockEntity(this.controllerPos) instanceof final KineticBlockEntity kbe)) return;

        final float totalLength = this.getTotalLength(level);
        if (totalLength <= 0) return;

        final float distPerTick = getDistPerTick(kbe, behaviour);

        this.dist = wrapDist(this.dist + distPerTick, totalLength);
    }

    private static float getDistPerTick(final KineticBlockEntity kbe, final CogwheelChainBehaviour behaviour) {
        final float chainRotationFactor = behaviour.getChainRotationFactor();
        final float rpm = kbe.getSpeed();
        return (float) -(Math.PI * 2.0 * chainRotationFactor * rpm / (60.0 * 20.0));
    }

    public float getDistPerTick(final Level level) {
        final CogwheelChainBehaviour behaviour = SuperBlockEntityBehaviour.get(
                level, this.controllerPos, CogwheelChainBehaviour.TYPE);
        if (behaviour == null) return 0;

        if (!(level.getBlockEntity(this.controllerPos) instanceof final KineticBlockEntity kbe)) return 0;

        return getDistPerTick(kbe, behaviour);
    }

    /**
     * Resolves the current world position of this attachment on the chain.
     */
    public Vec3 getCurrentPosition(final Level level) {
        return this.getCurrentPosition(level, 0);
    }

    /**
     * Resolves the world position at {@code dist + offset} along the chain.
     */
    public Vec3 getCurrentPosition(final Level level, final float offset) {
        final CogwheelChain chain = CogwheelChainWorld.get(level).getChain(this.controllerPos);
        if (chain == null) return Vec3.ZERO;

        final List<CogwheelChainSegment> segments = chain.getSegments();
        if (segments.isEmpty()) return Vec3.ZERO;

        final float totalLength = segments.getLast().endDist();
        final float dist = wrapDist(this.dist + offset, totalLength);

        return this.resolvePositionOnSegments(segments, dist);
    }

    /**
     * Binary-searches the chain's segment list to find which segment this attachment
     * currently occupies based on {@link #dist}.
     */
    @Nullable
    public CogwheelChainSegment getCurrentCogwheelChainSegment(final Level level) {
        final List<CogwheelChainSegment> segments = this.getCurrentCogwheelChainSegments(level);
        if (segments == null) return null;
        if (segments.isEmpty()) return null;

        final float totalLength = segments.getLast().endDist();
        final float dist = wrapDist(this.dist, totalLength);

        return findSegmentAtDist(segments, dist);
    }

    private @Nullable List<CogwheelChainSegment> getCurrentCogwheelChainSegments(final Level level) {
        final CogwheelChain chain = CogwheelChainWorld.get(level).getChain(this.controllerPos);
        if (chain == null) return null;
        return chain.getSegments();
    }

    /**
     * Returns {@code true} if the chain still exists at the controller position.
     */
    public boolean isValid(final Level level) {
        if (CogwheelChainWorld.get(level).containsChain(this.controllerPos)) {
            return true;
        }
        if (!level.isLoaded(this.controllerPos)) {
            return true;
        }
        final CogwheelChainBehaviour behaviour = SuperBlockEntityBehaviour.get(
                level, this.controllerPos, CogwheelChainBehaviour.TYPE);
        return behaviour != null && behaviour.isController();
    }

    public void write(final CompoundTag tag) {
        tag.putInt("ControllerX", this.controllerPos.getX());
        tag.putInt("ControllerY", this.controllerPos.getY());
        tag.putInt("ControllerZ", this.controllerPos.getZ());
        tag.putFloat("Dist", this.dist);
    }

    public static CogwheelChainAttachment read(final CompoundTag tag) {
        final BlockPos pos = new BlockPos(
                tag.getInt("ControllerX"),
                tag.getInt("ControllerY"),
                tag.getInt("ControllerZ")
        );
        final float dist = tag.getFloat("Dist");
        return new CogwheelChainAttachment(pos, dist);
    }

    public BlockPos getControllerPos() {
        return this.controllerPos;
    }

    public float getDist() {
        return this.dist;
    }

    public void setDist(final float dist) {
        this.dist = Math.max(0, dist);
    }

    /**
     * Returns the {@link CogwheelChainSegment} at {@code dist + offset} along the chain,
     * or {@code null} if the chain is missing or has no segments.
     */
    @Nullable
    public CogwheelChainSegment getSegmentAtOffset(final Level level, final float offset) {
        final List<CogwheelChainSegment> segments = this.getCurrentCogwheelChainSegments(level);
        if (segments == null) return null;
        if (segments.isEmpty()) return null;

        final float totalLength = segments.getLast().endDist();
        final float dist = wrapDist(this.dist + offset, totalLength);

        return findSegmentAtDist(segments, dist);
    }

    private float getTotalLength(final Level level) {
        final CogwheelChain chain = CogwheelChainWorld.get(level).getChain(this.controllerPos);
        if (chain == null) return 0;

        final List<CogwheelChainSegment> segments = chain.getSegments();
        if (segments.isEmpty()) return 0;

        return segments.getLast().endDist();
    }

    private Vec3 resolvePositionOnSegments(final List<CogwheelChainSegment> segments, final float pos) {
        final CogwheelChainSegment segment = findSegmentAtDist(segments, pos);
        if (segment == null) return Vec3.ZERO;

        final float t = segment.length() > 0 ? (pos - segment.startDist()) / segment.length() : 0;
        final Vec3 localPos = segment.fromPosition().lerp(segment.toPosition(), t);
        return localPos.add(Vec3.atLowerCornerOf(this.controllerPos));
    }

    @Nullable
    private static CogwheelChainSegment findSegmentAtDist(final List<CogwheelChainSegment> segments, final float dist) {
        if (segments.isEmpty()) return null;

        int lo = 0;
        int hi = segments.size() - 1;
        while (lo < hi) {
            final int mid = (lo + hi) >>> 1;
            if (segments.get(mid).endDist() <= dist) {
                lo = mid + 1;
            } else {
                hi = mid;
            }
        }
        return segments.get(lo);
    }

    public static float wrapDist(final float dist, final float totalLength) {
        if (totalLength <= 0) return 0;
        final float wrapped = dist % totalLength;
        return wrapped < 0 ? wrapped + totalLength : wrapped;
    }

    private static Vec3 getSmoothedCurrentDirection(final List<CogwheelChainSegment> segments, final float dist) {
        final float backDist = dist - 0.2f;
        final float frontDist = dist + 0.2f;

        final CogwheelChainSegment backSegment = findSegmentAtDist(segments, backDist);
        final CogwheelChainSegment frontSegment = findSegmentAtDist(segments, frontDist);
        if (backSegment == null || frontSegment == null) return Vec3.ZERO;

        final Vec3 backDirection = backSegment.toPosition().subtract(backSegment.fromPosition()).normalize();
        final Vec3 frontDirection = frontSegment.toPosition().subtract(frontSegment.fromPosition()).normalize();

        final float pointOfChange = (backSegment.endDist() + frontSegment.startDist()) / 2f;
        final float lerpFrom = (pointOfChange - backDist) / (frontDist - backDist);

        return frontDirection.lerp(backDirection, lerpFrom);
    }

    public Vec3 getCurrentDirection(final Level level, final float offset) {
        final @Nullable List<CogwheelChainSegment> segments = this.getCurrentCogwheelChainSegments(level);
        if (segments == null) return Vec3.ZERO;
        return getSmoothedCurrentDirection(segments, this.dist + offset);
    }

    public Vec3 getCurrentDirection(final Level level) {
        return this.getCurrentDirection(level, this.dist);
    }

    public float wrapDist(final Level level, final float dist) {
        return wrapDist(dist, this.getTotalLength(level));
    }

    public float getMinWrappedDist(final Level level,
                                   final float clientChasingChainAttachmentDist,
                                   final float chainAttachmentDist) {
        final float length = this.getTotalLength(level);
        return Math.min(
                Math.abs(clientChasingChainAttachmentDist - chainAttachmentDist),
                length - Math.abs(clientChasingChainAttachmentDist - chainAttachmentDist)
        );
    }
}
