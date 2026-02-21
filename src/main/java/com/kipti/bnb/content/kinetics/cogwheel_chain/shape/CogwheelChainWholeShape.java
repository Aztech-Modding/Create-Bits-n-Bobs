package com.kipti.bnb.content.kinetics.cogwheel_chain.shape;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("null")
public class CogwheelChainWholeShape extends CogwheelChainShape {

    private static final double INFLATE_PIXELS = 1.0 / 16.0;
    private static final double MIN_LENGTH_SQR = 1e-12;

    private final List<Vec3> points;
    private final double radius;
    private final float[] cumulativeLengths;
    private final float totalLength;
    private final AABB bounds;
    private final Vec3[] tangentAtVertex;
    private final Vec3[] uAtVertex;
    private final Vec3[] vAtVertex;
    private final boolean closedLoop;
    private final int segmentCount;

    public CogwheelChainWholeShape(final List<Vec3> pointsLocal, final double baseRadius) {
        this.points = pointsLocal;
        this.radius = baseRadius + INFLATE_PIXELS;
        this.closedLoop = pointsLocal.size() > 2
                && pointsLocal.getFirst().distanceToSqr(pointsLocal.getLast()) > MIN_LENGTH_SQR;
        this.segmentCount = closedLoop ? pointsLocal.size() : pointsLocal.size() - 1;

        final int n = pointsLocal.size();
        this.cumulativeLengths = new float[segmentCount + 1];
        float lengthSum = 0f;
        for (int segmentIndex = 0; segmentIndex < segmentCount; segmentIndex++) {
            final Vec3 a = pointsLocal.get(segmentStartIndex(segmentIndex));
            final Vec3 b = pointsLocal.get(segmentEndIndex(segmentIndex));
            lengthSum += (float) a.distanceTo(b);
            cumulativeLengths[segmentIndex + 1] = lengthSum;
        }
        this.totalLength = lengthSum;

        AABB box = new AABB(pointsLocal.get(0), pointsLocal.get(0)).inflate(radius);
        for (int i = 1; i < n; i++) {
            box = box.minmax(new AABB(pointsLocal.get(i), pointsLocal.get(i)).inflate(radius));
        }
        this.bounds = box;

        this.tangentAtVertex = new Vec3[n];
        this.uAtVertex = new Vec3[n];
        this.vAtVertex = new Vec3[n];
        buildFrames();
    }

    @Override
    @Nullable
    public Vec3 intersect(final Vec3 from, final Vec3 to) {
        if (points.size() < 2) return null;
        if (bounds.inflate(radius).clip(from, to).isEmpty()) return null;

        final Vec3 rayDir = to.subtract(from);
        if (rayDir.lengthSqr() < MIN_LENGTH_SQR) return null;

        double bestDistanceSq = Double.POSITIVE_INFINITY;
        Vec3 bestHit = null;

        for (int i = 0; i < segmentCount; i++) {
            final int startIndex = segmentStartIndex(i);
            final int endIndex = segmentEndIndex(i);
            final Vec3 a = points.get(startIndex);
            final Vec3 b = points.get(endIndex);
            final Vec3 segment = b.subtract(a);
            final double segmentLenSq = segment.lengthSqr();
            if (segmentLenSq < MIN_LENGTH_SQR) continue;

            final Vec3 segmentTangent = segment.normalize();
            double tRay = getClosestPointT(from, to, a, b);
            tRay = Mth.clamp(tRay, 0, 1);
            final Vec3 pointOnRay = from.add(rayDir.scale(tRay));

            final double segT = Mth.clamp(pointOnRay.subtract(a).dot(segment) / segmentLenSq, 0, 1);
            final Vec3 pointOnSegment = a.add(segment.scale(segT));
            final Frame frame = frameForSegment(startIndex, endIndex, segT, segmentTangent);

            final Vec3 delta = pointOnRay.subtract(pointOnSegment);
            final double du = Math.abs(delta.dot(frame.u));
            final double dv = Math.abs(delta.dot(frame.v));

            if (du <= radius && dv <= radius) {
                final double distSq = from.distanceToSqr(pointOnRay);
                if (distSq < bestDistanceSq) {
                    bestDistanceSq = distSq;
                    bestHit = pointOnRay;
                }
            }
        }

        return bestHit;
    }

    private static double getClosestPointT(final Vec3 p1, final Vec3 p2, final Vec3 p3, final Vec3 p4) {
        final Vec3 d1 = p2.subtract(p1);
        final Vec3 d2 = p4.subtract(p3);
        final Vec3 r = p1.subtract(p3);

        final double a = d1.lengthSqr();
        final double e = d2.lengthSqr();
        final double f = d2.dot(r);
        final double b = d1.dot(d2);
        final double c = d1.dot(r);
        final double denom = a * e - b * b;

        if (Math.abs(denom) < 1e-6) return 0.0;
        return (b * f - c * e) / denom;
    }

    @Override
    public float getChainPosition(final Vec3 intersection) {
        if (segmentCount <= 0) return 0f;
        int bestSeg = 0;
        double bestDistSq = Double.POSITIVE_INFINITY;
        double bestT = 0;

        for (int i = 0; i < segmentCount; i++) {
            final Vec3 a = points.get(segmentStartIndex(i));
            final Vec3 b = points.get(segmentEndIndex(i));
            final Vec3 ab = b.subtract(a);
            final Vec3 ap = intersection.subtract(a);
            final double lenSq = ab.lengthSqr();
            final double t = lenSq > 0 ? Mth.clamp(ap.dot(ab) / lenSq, 0, 1) : 0;
            final Vec3 closest = a.add(ab.scale(t));
            final double dSq = intersection.distanceToSqr(closest);
            if (dSq < bestDistSq) {
                bestDistSq = dSq;
                bestSeg = i;
                bestT = t;
            }
        }
        final double segLen = points.get(segmentStartIndex(bestSeg)).distanceTo(points.get(segmentEndIndex(bestSeg)));
        return cumulativeLengths[bestSeg] + (float) (bestT * segLen);
    }

    @Override
    protected void drawOutline(final BlockPos anchor, final PoseStack ms, final VertexConsumer vb) {
        if (segmentCount <= 0) return;

        final float r = (float) radius;
        final int color = 0x66000000;

        for (int i = 0; i < segmentCount; i++) {
            final int startIndex = segmentStartIndex(i);
            final int endIndex = segmentEndIndex(i);
            final Vec3 p0 = points.get(startIndex);
            final Vec3 p1 = points.get(endIndex);

            final Vec3 u0 = uAtVertex[startIndex];
            final Vec3 v0 = vAtVertex[startIndex];
            final Vec3 u1 = uAtVertex[endIndex];
            final Vec3 v1 = vAtVertex[endIndex];

            final List<Vec3> startCorners = getCorners(p0, u0, v0, r);
            final List<Vec3> endCorners = getPointsInClosestOrder(getCorners(p1, u1, v1, r), startCorners);

            for (int cornerIndex = 0; cornerIndex < 4; cornerIndex++) {
                line(vb, ms, startCorners.get(cornerIndex), endCorners.get(cornerIndex), color);
            }

            drawRing(vb, ms,
                    startCorners.get(0),
                    startCorners.get(1),
                    startCorners.get(2),
                    startCorners.get(3),
                    color);
        }

        if (!closedLoop) {
            final int last = points.size() - 1;
            final Vec3 pn = points.get(last);
            final Vec3 un = uAtVertex[last];
            final Vec3 vn = vAtVertex[last];
            final List<Vec3> endCorners = getCorners(pn, un, vn, r);
            drawRing(vb, ms,
                    endCorners.get(0),
                    endCorners.get(1),
                    endCorners.get(2),
                    endCorners.get(3),
                    color);
        }
    }

    private static List<Vec3> getCorners(final Vec3 center, final Vec3 u, final Vec3 v, final float radius) {
        final Vec3 uScaled = u.scale(radius);
        final Vec3 vScaled = v.scale(radius);
        final ArrayList<Vec3> corners = new ArrayList<>(4);
        corners.add(center.add(uScaled).add(vScaled));
        corners.add(center.add(uScaled).subtract(vScaled));
        corners.add(center.subtract(uScaled).subtract(vScaled));
        corners.add(center.subtract(uScaled).add(vScaled));
        return corners;
    }

    private static List<Vec3> getPointsInClosestOrder(final List<Vec3> destinationPoints, final List<Vec3> sourcePoints) {
        if (destinationPoints.size() != 4 || sourcePoints.size() != 4) {
            return new ArrayList<>(destinationPoints);
        }

        double bestScore = Double.POSITIVE_INFINITY;
        List<Vec3> best = new ArrayList<>(destinationPoints);

        for (int reversed = 0; reversed <= 1; reversed++) {
            for (int shift = 0; shift < 4; shift++) {
                final ArrayList<Vec3> candidate = new ArrayList<>(4);
                double pointScore = 0.0;

                for (int i = 0; i < 4; i++) {
                    final int j = (reversed == 0)
                            ? ((i + shift) & 3)
                            : ((shift - i + 4) & 3);

                    final Vec3 point = destinationPoints.get(j);
                    candidate.add(point);
                    pointScore += sourcePoints.get(i).distanceToSqr(point);
                }

                double edgeScore = 0.0;
                for (int i = 0; i < 4; i++) {
                    final Vec3 sourceEdge = sourcePoints.get((i + 1) & 3).subtract(sourcePoints.get(i)).normalize();
                    final Vec3 destinationEdge = candidate.get((i + 1) & 3).subtract(candidate.get(i)).normalize();
                    edgeScore += 1.0 - sourceEdge.dot(destinationEdge);
                }

                final double windingPenalty = reversed == 1 ? 1e-4 : 0.0;
                final double score = pointScore + edgeScore * 0.25 + windingPenalty;

                if (score < bestScore) {
                    bestScore = score;
                    best = candidate;
                }
            }
        }

        return best;
    }

    private static void drawRing(final VertexConsumer vb, final PoseStack ms,
                                 final Vec3 a, final Vec3 b, final Vec3 c, final Vec3 d,
                                 final int color) {
        line(vb, ms, a, b, color);
        line(vb, ms, b, c, color);
        line(vb, ms, c, d, color);
        line(vb, ms, d, a, color);
    }

    private void buildFrames() {
        final int n = points.size();
        if (n == 0) return;

        for (int i = 0; i < n; i++) {
            final Vec3 tangent;
            if (closedLoop) {
                final int prev = (i - 1 + n) % n;
                final int next = (i + 1) % n;
                final Vec3 in = safeDirection(points.get(i).subtract(points.get(prev)));
                final Vec3 out = safeDirection(points.get(next).subtract(points.get(i)));
                tangent = safeDirection(in.add(out));
            } else if (i == 0) {
                tangent = safeDirection(points.get(1).subtract(points.get(0)));
            } else if (i == n - 1) {
                tangent = safeDirection(points.get(n - 1).subtract(points.get(n - 2)));
            } else {
                final Vec3 in = safeDirection(points.get(i).subtract(points.get(i - 1)));
                final Vec3 out = safeDirection(points.get(i + 1).subtract(points.get(i)));
                tangent = safeDirection(in.add(out));
            }
            tangentAtVertex[i] = tangent;
        }

        for (int i = 0; i < n; i++) {
            final Vec3 tangent = tangentAtVertex[i];
            final Vec3 u;
            if (i == 0) {
                u = perpendicularUnit(tangent);
            } else {
                Vec3 projected = uAtVertex[i - 1].subtract(tangent.scale(uAtVertex[i - 1].dot(tangent)));
                if (projected.lengthSqr() < MIN_LENGTH_SQR) {
                    projected = perpendicularUnit(tangent);
                }
                u = projected.normalize();
            }
            final Vec3 v = safeDirection(tangent.cross(u));
            uAtVertex[i] = u;
            vAtVertex[i] = v;
        }
    }

    private static Vec3 safeDirection(final Vec3 vec) {
        if (vec.lengthSqr() < MIN_LENGTH_SQR) return new Vec3(1, 0, 0);
        return vec.normalize();
    }

    private static Vec3 perpendicularUnit(final Vec3 tangent) {
        Vec3 candidate = new Vec3(0, 1, 0).cross(tangent);
        if (candidate.lengthSqr() < MIN_LENGTH_SQR) {
            candidate = new Vec3(1, 0, 0).cross(tangent);
        }
        return safeDirection(candidate);
    }

    private Frame frameForSegment(final int startIndex, final int endIndex, final double segT, final Vec3 segmentTangent) {
        Vec3 u = uAtVertex[startIndex].lerp(uAtVertex[endIndex], segT);
        u = u.subtract(segmentTangent.scale(u.dot(segmentTangent)));
        if (u.lengthSqr() < MIN_LENGTH_SQR) {
            u = perpendicularUnit(segmentTangent);
        } else {
            u = u.normalize();
        }
        final Vec3 v = safeDirection(segmentTangent.cross(u));
        return new Frame(u, v);
    }

    @Override
    public Vec3 getVec(final BlockPos anchor, final float position) {
        if (segmentCount <= 0 || totalLength <= 0f) return points.get(0).add(Vec3.atLowerCornerOf(anchor));

        final float pos;
        if (closedLoop) {
            final float wrapped = position % totalLength;
            pos = wrapped < 0 ? wrapped + totalLength : wrapped;
        } else {
            pos = Mth.clamp(position, 0f, totalLength);
        }

        int seg = 0;
        while (seg < segmentCount - 1 && cumulativeLengths[seg + 1] <= pos) seg++;

        final Vec3 a = points.get(segmentStartIndex(seg));
        final Vec3 b = points.get(segmentEndIndex(seg));
        final float segStart = cumulativeLengths[seg];
        final float segLen = (float) a.distanceTo(b);
        final float t = segLen > 0 ? (pos - segStart) / segLen : 0f;
        return a.lerp(b, Mth.clamp(t, 0, 1)).add(Vec3.atLowerCornerOf(anchor));
    }

    private int segmentStartIndex(final int segmentIndex) {
        return segmentIndex;
    }

    private int segmentEndIndex(final int segmentIndex) {
        if (closedLoop) {
            return (segmentIndex + 1) % points.size();
        }
        return segmentIndex + 1;
    }

    private static void line(final VertexConsumer vb, final PoseStack ms,
                             final Vec3 a, final Vec3 b, final int color) {
        final PoseStack.Pose transform = ms.last();
        final Matrix4f pose = transform.pose();
        final float dx = (float) (b.x - a.x);
        final float dy = (float) (b.y - a.y);
        final float dz = (float) (b.z - a.z);
        final float len = Mth.sqrt(dx * dx + dy * dy + dz * dz);
        final float nx = len > 0 ? dx / len : 0f;
        final float ny = len > 0 ? dy / len : 1f;
        final float nz = len > 0 ? dz / len : 0f;
        final float r = ((color >> 16) & 0xFF) / 255f;
        final float g = ((color >> 8) & 0xFF) / 255f;
        final float b_ = ((color) & 0xFF) / 255f;
        final float alpha = ((color >> 24) & 0xFF) / 255f;
        vb.addVertex(pose, (float) a.x, (float) a.y, (float) a.z)
                .setColor(r, g, b_, alpha)
                .setNormal(transform.copy(), nx, ny, nz);
        vb.addVertex(pose, (float) b.x, (float) b.y, (float) b.z)
                .setColor(r, g, b_, alpha)
                .setNormal(transform.copy(), nx, ny, nz);
    }

    private static class Frame {
        final Vec3 u;
        final Vec3 v;

        private Frame(final Vec3 u, final Vec3 v) {
            this.u = u;
            this.v = v;
        }
    }
}
