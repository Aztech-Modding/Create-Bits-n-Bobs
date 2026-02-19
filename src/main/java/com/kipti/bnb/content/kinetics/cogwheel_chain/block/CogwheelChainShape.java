package com.kipti.bnb.content.kinetics.cogwheel_chain.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.trains.track.TrackBlockOutline;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.Nullable;

/**
 * Selection and outline geometry for cogwheel chains.
 *
 * Positions passed to and from these shapes are in block-local space,
 * with the {@link BlockPos} anchor representing the controller block.
 */
public abstract class CogwheelChainShape {

    @Nullable
    public abstract Vec3 intersect(Vec3 from, Vec3 to);

    public abstract float getChainPosition(Vec3 intersection);

    protected abstract void drawOutline(BlockPos anchor, PoseStack ms, VertexConsumer vb);

    public abstract Vec3 getVec(BlockPos anchor, float position);

    public static class CogwheelChainSegmentShape extends CogwheelChainShape {

        private final Vec3 start;
        private final Vec3 end;
        private final float chainStartOffset;

        private final double yaw;
        private final double pitch;
        private final AABB bounds;
        private final Vec3 pivot;
        private final VoxelShape voxelShape;

        /**
         * @param startLocal       segment start in controller-local coordinates
         * @param endLocal         segment end in controller-local coordinates
         * @param chainStartOffset cumulative chain distance at this segment's start
         * @param radius           selection radius around the segment centerline
         */
        public CogwheelChainSegmentShape(final Vec3 startLocal,
                                         final Vec3 endLocal,
                                         final float chainStartOffset,
                                         final double radius) {
            this.start = startLocal;
            this.end = endLocal;
            this.chainStartOffset = chainStartOffset;

            final Vec3 diff = endLocal.subtract(startLocal);
            final double d = diff.length();
            final double dxz = diff.multiply(1, 0, 1)
                    .length();

            this.yaw = Mth.RAD_TO_DEG * Mth.atan2(diff.x, diff.z);
            this.pitch = Mth.RAD_TO_DEG * Mth.atan2(-diff.y, dxz);
            this.bounds = new AABB(startLocal, startLocal).expandTowards(new Vec3(0, 0, d))
                    .inflate(radius, radius, 0);
            this.pivot = startLocal;
            this.voxelShape = Shapes.create(bounds);
        }

        @Override
        @Nullable
        public Vec3 intersect(final Vec3 from, final Vec3 to) {
            Vec3 localFrom = counterTransform(from);
            Vec3 localTo = counterTransform(to);

            final Vec3 result = bounds.clip(localFrom, localTo)
                    .orElse(null);
            if (result == null)
                return null;

            return transform(result);
        }

        private Vec3 counterTransform(final Vec3 vec) {
            Vec3 result = vec.subtract(pivot);
            result = VecHelper.rotate(result, -yaw, Axis.Y);
            result = VecHelper.rotate(result, -pitch, Axis.X);
            result = result.add(pivot);
            return result;
        }

        private Vec3 transform(final Vec3 vec) {
            Vec3 result = vec.subtract(pivot);
            result = VecHelper.rotate(result, pitch, Axis.X);
            result = VecHelper.rotate(result, yaw, Axis.Y);
            result = result.add(pivot);
            return result;
        }

        @Override
        public float getChainPosition(final Vec3 intersection) {
            final double segmentLength = start.distanceTo(end);
            if (segmentLength <= 0)
                return chainStartOffset;

            final double along = Mth.clamp(intersection.distanceTo(pivot), 0.0, segmentLength);
            return chainStartOffset + (float) along;
        }

        @Override
        protected void drawOutline(final BlockPos anchor, final PoseStack ms, final VertexConsumer vb) {
            TransformStack.of(ms)
                    .translate(pivot)
                    .rotateYDegrees((float) yaw)
                    .rotateXDegrees((float) pitch)
                    .translateBack(pivot);
            TrackBlockOutline.renderShape(voxelShape, ms, vb, null);
        }

        @Override
        public Vec3 getVec(final BlockPos anchor, final float position) {
            final double segmentLength = start.distanceTo(end);
            if (segmentLength <= 0) {
                return start.add(Vec3.atLowerCornerOf(anchor));
            }

            final double local = Mth.clamp(position - chainStartOffset, 0.0, segmentLength);
            final double t = local / segmentLength;
            final Vec3 point = start.lerp(end, t);
            return point.add(Vec3.atLowerCornerOf(anchor));
        }
    }
}

