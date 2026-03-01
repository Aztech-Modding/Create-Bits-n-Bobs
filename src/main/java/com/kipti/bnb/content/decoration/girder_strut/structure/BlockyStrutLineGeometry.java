package com.kipti.bnb.content.decoration.girder_strut.structure;

import com.kipti.bnb.CreateBitsnBobs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Used for getting multiple collision shapes based on two GirderStrutBlockEntities.
 * Note that blocks that are behind the attachment point of the girder strut are not considered part of the shape.
 * (Since the rendered geometry gets clipped at the attachment point, so the shape should be as well).
 * Shape is a continuous line between the two attachment points, with a rectangular cross section. The size of the cross section
 * is determined by the SHAPE_SIZE_X/Y_PIXELS constants, which are in pixels and converted to blocks using the texture size of the girder strut model (16 pixels).
 * To save on an abhorrent performace cost, these connections are required to be "flat", where it can vary on only 2 axes at a time.
 * The shape is then generated in slices along the dominant axis (although which axis doesn't really matter)
 */
public class BlockyStrutLineGeometry {

    private static final float EPSILON = 1e-6f;

    public static final int SHAPE_SIZE_X_PIXELS = 8;
    public static final int SHAPE_SIZE_Y_PIXELS = 12;
    private static final double HALF_X = (SHAPE_SIZE_X_PIXELS / 16.0) / 2.0;
    private static final double HALF_Y = (SHAPE_SIZE_Y_PIXELS / 16.0) / 2.0;

    private final BlockPos[] positions;

    private final Vec3 fromAttachment;
    private final Vec3 toAttachment;

    //Effectively final
    private Vec3 localXDirection;
    private Vec3 localYDirection;
    private double totalLength;

    public BlockyStrutLineGeometry(final BlockPos from, final Direction fromFacing, final BlockPos to, final Direction toFacing) {
        this.fromAttachment = Vec3.atCenterOf(from).relative(fromFacing, -0.4);
        this.toAttachment = Vec3.atCenterOf(to).relative(toFacing, -0.4);
        this.positions = calculatePositions();
    }

    public BlockPos[] getPositions() {
        return positions;
    }

    /**
     * For each pixel along this block:
     * Get the size of the axis aligned slice (can be either of the axes this is travelling on, i.e. if going along XY, then get the y height above and below the line comparing it to this).
     * If -ve, then do nothing, otherwise, add a box with shape size (x or y depending on if this girder is horizontal), the height of the slice found.
     */
    public VoxelShape getShapeForPosition(final BlockPos pos) {
        if (totalLength < EPSILON) return Shapes.empty();

        final Vec3 difference = toAttachment.subtract(fromAttachment).normalize();
        final boolean straightX = isEpsilon(difference.y) && isEpsilon(difference.z);
        final boolean straightY = isEpsilon(difference.x) && isEpsilon(difference.z);
        final boolean straightZ = isEpsilon(difference.x) && isEpsilon(difference.y);

        if (straightX || straightY || straightZ) {
            final double minX = straightX ? 0 : 0.5 - HALF_X;
            final double maxX = straightX ? 1 : 0.5 + HALF_X;
            final double minY = straightY ? 0 : 0.5 - HALF_Y;
            final double maxY = straightY ? 1 : 0.5 + HALF_Y;
            final double minZ = straightZ ? 0 : 0.5 - HALF_X;
            final double maxZ = straightZ ? 1 : 0.5 + HALF_X;
            return Shapes.create(minX, minY, minZ, maxX, maxY, maxZ);
        }

        final double extentX = Math.abs(HALF_X * localXDirection.x()) + Math.abs(HALF_Y * localYDirection.x());
        final double extentY = Math.abs(HALF_X * localXDirection.y()) + Math.abs(HALF_Y * localYDirection.y());
        final double extentZ = Math.abs(HALF_X * localXDirection.z()) + Math.abs(HALF_Y * localYDirection.z());

        final AABB expanded = new AABB(pos).inflate(extentX, extentY, extentZ);
        final Vec3 scaledFrom = fromAttachment.subtract(difference); //Scale this to ensure shape extends fully to the attachment point;
        final Vec3 scaledDifference = difference.scale(3);
        final double[] tBounds = intersectAabbs(scaledFrom, scaledDifference, expanded);
        if (tBounds == null) return Shapes.empty();

        final double tMin = Math.max(0, tBounds[0]);
        final double tMax = Math.min(totalLength, tBounds[1]);
        if (tMin > tMax) return Shapes.empty();

        final double dom = Math.max(Math.abs(scaledDifference.x), Math.max(Math.abs(scaledDifference.y), Math.abs(scaledDifference.z)));
        final double dt = (1.0 / 16.0) / dom;

        VoxelShape totalShape = Shapes.empty();
        double t = tMin;
        while (t <= tMax) {
            final double nextT = Math.min(t + dt, tMax);
            final double midT = (t + nextT) / 2.0;

            final Vec3 center = scaledFrom.add(scaledDifference.scale(midT));
            final double centerX = center.x - pos.getX();
            final double centerY = center.y - pos.getY();
            final double centerZ = center.z - pos.getZ();

            final double minX = Math.max(0, pixelFloor(centerX - extentX));
            final double maxX = Math.min(1, pixelCeil(centerX + extentX));
            final double minY = Math.max(0, pixelFloor(centerY - extentY));
            final double maxY = Math.min(1, pixelCeil(centerY + extentY));
            final double minZ = Math.max(0, pixelFloor(centerZ - extentZ));
            final double maxZ = Math.min(1, pixelCeil(centerZ + extentZ));

            if (minX < maxX && minY < maxY && minZ < maxZ) {
                totalShape = Shapes.or(totalShape, Shapes.create(minX, minY, minZ, maxX, maxY, maxZ));
            }

            if (t == tMax) break;
            t = nextT;
        }

        if (!totalShape.isEmpty()) {
            return totalShape.optimize();
        }

        return createFallbackShape(pos, difference, extentX, extentY, extentZ);
    }

    private VoxelShape createFallbackShape(final BlockPos pos,
                                           final Vec3 direction,
                                           final double extentX,
                                           final double extentY,
                                           final double extentZ) {
        final Vec3 blockCenter = Vec3.atCenterOf(pos);
        final double mu = Mth.clamp(blockCenter.subtract(fromAttachment).dot(direction), 0, totalLength);
        final Vec3 center = fromAttachment.add(direction.scale(mu));

        final double centerX = center.x - pos.getX();
        final double centerY = center.y - pos.getY();
        final double centerZ = center.z - pos.getZ();

        final double minX = Math.max(0, pixelFloor(centerX - extentX));
        final double maxX = Math.min(1, pixelCeil(centerX + extentX));
        final double minY = Math.max(0, pixelFloor(centerY - extentY));
        final double maxY = Math.min(1, pixelCeil(centerY + extentY));
        final double minZ = Math.max(0, pixelFloor(centerZ - extentZ));
        final double maxZ = Math.min(1, pixelCeil(centerZ + extentZ));

        if (minX < maxX && minY < maxY && minZ < maxZ) {
            return Shapes.create(minX, minY, minZ, maxX, maxY, maxZ);
        }

        return Shapes.empty();
    }

    private double[] intersectAabbs(final Vec3 rayOrigin, final Vec3 rayDir, final AABB aabb) {
        double tMin = Double.NEGATIVE_INFINITY;
        double tMax = Double.POSITIVE_INFINITY;

        final double[] origin = {rayOrigin.x, rayOrigin.y, rayOrigin.z};
        final double[] dir = {rayDir.x, rayDir.y, rayDir.z};
        final double[] min = {aabb.minX, aabb.minY, aabb.minZ};
        final double[] max = {aabb.maxX, aabb.maxY, aabb.maxZ};

        for (int i = 0; i < 3; i++) {
            if (Math.abs(dir[i]) < EPSILON) {
                if (origin[i] < min[i] || origin[i] > max[i]) return null;
            } else {
                double t1 = (min[i] - origin[i]) / dir[i];
                double t2 = (max[i] - origin[i]) / dir[i];
                if (t1 > t2) {
                    final double temp = t1;
                    t1 = t2;
                    t2 = temp;
                }
                if (t1 > tMin) tMin = t1;
                if (t2 < tMax) tMax = t2;
                if (tMin > tMax) return null;
            }
        }
        return new double[]{tMin, tMax};
    }

    private BlockPos[] calculatePositions() {
        //If straight along a single axis, return a straight line of blocks between the two attachment points.
        final Vec3 differenceRaw = toAttachment.subtract(fromAttachment);
        this.totalLength = differenceRaw.length();
        if (totalLength < EPSILON) return new BlockPos[0];

        final Vec3 difference = differenceRaw.normalize();
        final boolean straightX = isEpsilon(difference.y) && isEpsilon(difference.z);
        final boolean straightY = isEpsilon(difference.x) && isEpsilon(difference.z);
        final boolean straightZ = isEpsilon(difference.x) && isEpsilon(difference.y);
        if (straightX || straightY || straightZ) {
            return straightLine(BlockPos.containing(fromAttachment), BlockPos.containing(toAttachment));
        }

        //Else, we're looking at actual collisions
        //Find two orthonormal vectors perpendicular to the line direction for the cross-section.
        //Pick a helper vector that is not parallel to the direction, then use cross products.
        final Vec3 helper = (Math.abs(difference.y()) > 0.999) ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        this.localXDirection = difference.cross(helper).normalize();
        if (isEpsilon(localXDirection)) {
            CreateBitsnBobs.LOGGER.error("Unexpected zero local X direction for strut line geometry between {} and {}, skipping shape generation", fromAttachment, toAttachment);
            return new BlockPos[0];
        }
        this.localYDirection = difference.cross(localXDirection).normalize();
        if (isEpsilon(localYDirection)) {
            CreateBitsnBobs.LOGGER.error("Unexpected zero local Y direction for strut line geometry between {} and {}, skipping shape generation", fromAttachment, toAttachment);
            return new BlockPos[0];
        }

        final boolean isHorizontal = isEpsilon(difference.y);
        //If we are along a flat plane we are checking against the width, otherwise its the height
        //Line definition:
        // L(mu) = fromAttatchment + difference * mu
        final float lineWidth = isHorizontal ? (SHAPE_SIZE_X_PIXELS / 16f) : (SHAPE_SIZE_Y_PIXELS / 16f);

        //Then, get the line in whole block units, (finer collisions are for the actual shape generation)
        //To be a lil lazy, ths just gets the whole aabb and iterates checking for collisions
        //Note this basically handles clipping attachments already since it cant exceed the bounds
        final BlockPos fromBlock = BlockPos.containing(fromAttachment);
        final BlockPos toBlock = BlockPos.containing(toAttachment);
        return BlockPos.betweenClosedStream(BlockPos.min(fromBlock, toBlock), BlockPos.max(fromBlock, toBlock))
                .filter((block) ->
                        satLineToSquare(fromAttachment, difference, block, lineWidth, localXDirection, localYDirection))
                .map(BlockPos::new)
                .toArray(BlockPos[]::new);
    }

    private boolean satLineToSquare(final Vec3 lineOrigin, final Vec3 lineDirection, final BlockPos block, final float lineWidth, final Vec3 localXDirection, final Vec3 localYDirection) {
        //Get the 4 vertices of the square cross section of the line at the point closest to the block center
        final Vec3 blockCenter = Vec3.atCenterOf(block);
        final Vec3 toBlock = blockCenter.subtract(lineOrigin);
        double mu = toBlock.dot(lineDirection);
        mu = Math.max(0, Math.min(this.totalLength, mu));
        final Vec3 closestPoint = lineOrigin.add(lineDirection.scale(mu));
        final Vec3 halfX = localXDirection.scale(lineWidth / 2);
        final Vec3 halfY = localYDirection.scale(lineWidth / 2);
        final Vec3[] vertices = new Vec3[]{
                closestPoint.add(halfX).add(halfY),
                closestPoint.subtract(halfX).subtract(halfY),
                closestPoint.add(halfX).subtract(halfY),
                closestPoint.subtract(halfX).add(halfY)
        };

        //Then, we can just do a SAT test between the square and the block (which is also a square)
        //The axes we need to test are the line direction, and the normals of the square (local x and y)
        //If any of these axes separate the two shapes, then there is no collision
        final Vec3 worldX = new Vec3(1, 0, 0);
        final Vec3 worldY = new Vec3(0, 1, 0);
        final Vec3 worldZ = new Vec3(0, 0, 1);

        return !separatingAxisTest(lineDirection, vertices, blockCenter) &&
                !separatingAxisTest(localXDirection, vertices, blockCenter) &&
                !separatingAxisTest(localYDirection, vertices, blockCenter) &&
                !separatingAxisTest(worldX, vertices, blockCenter) &&
                !separatingAxisTest(worldY, vertices, blockCenter) &&
                !separatingAxisTest(worldZ, vertices, blockCenter);
    }

    private boolean separatingAxisTest(final Vec3 lineDirection, final Vec3[] vertices, final Vec3 blockCenter) {
        //Project the vertices of the square onto the axis
        double minSquare = Double.POSITIVE_INFINITY;
        double maxSquare = Double.NEGATIVE_INFINITY;
        for (final Vec3 vertex : vertices) {
            final double projection = vertex.dot(lineDirection);
            minSquare = Math.min(minSquare, projection);
            maxSquare = Math.max(maxSquare, projection);
        }

        //Project the block onto the axis (since its a square, we can just project the center and then add/subtract the radius)
        final double blockProjection = blockCenter.dot(lineDirection);
        final double blockRadius = 0.5 * Math.abs(lineDirection.x()) + 0.5 * Math.abs(lineDirection.y()) + 0.5 * Math.abs(lineDirection.z());

        //Check for separation
        return maxSquare < blockProjection - blockRadius || minSquare > blockProjection + blockRadius;
    }

    private static BlockPos[] straightLine(final BlockPos from, final BlockPos to) {
        final Vec3i dominantAxis = new Vec3i((int) Math.signum(to.getX() - from.getX()), (int) Math.signum(to.getY() - from.getY()), (int) Math.signum(to.getZ() - from.getZ()));
        final int length = Math.max(Math.max(Math.abs(to.getX() - from.getX()), Math.abs(to.getY() - from.getY())), Math.abs(to.getZ() - from.getZ())) + 1;
        final BlockPos[] positions = new BlockPos[length];
        for (int i = 0; i < length; i++) {
            positions[i] = from.offset(dominantAxis.getX() * i, dominantAxis.getY() * i, dominantAxis.getZ() * i);
        }
        return positions;
    }

    private static boolean isEpsilon(final double value) {
        return value < EPSILON && value > -EPSILON;
    }

    private boolean isEpsilon(final Vec3 vector) {
        return isEpsilon(vector.x()) && isEpsilon(vector.y()) && isEpsilon(vector.z());
    }

    private static double pixelFloor(final double value) {
        return Math.floor(value * 16.0) / 16.0;
    }

    private static double pixelCeil(final double value) {
        return Math.ceil(value * 16.0) / 16.0;
    }

}