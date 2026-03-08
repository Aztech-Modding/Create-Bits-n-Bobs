package com.kipti.bnb.content.articulate;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;

import java.util.function.UnaryOperator;

public final class ArticulatedTrackUtils {

    public static final float[] TILT_VALUES = {-25f, -20f, -15f, -10f, -5f, 0f, 5f, 10f, 15f, 20f, 25f};
    public static final float MAX_TILT_CHANGE_PER_BLOCK = 1.0f;
    private static final Vec3 WORLD_UP = new Vec3(0, 1, 0);

    private static final double VECTOR_EPSILON = 1.0e-7d;

    private ArticulatedTrackUtils() {
    }

    public static float snapToNearest(final float raw) {
        return TILT_VALUES[getNearestTiltIndex(raw)];
    }

    public static float nextTilt(final float current, final boolean forward) {
        final int currentIndex = getNearestTiltIndex(current);
        final int nextIndex;
        if (forward) {
            nextIndex = Math.min(currentIndex + 1, TILT_VALUES.length - 1);
        } else {
            nextIndex = Math.max(currentIndex - 1, 0);
        }
        return TILT_VALUES[nextIndex];
    }

    public static boolean isValidTilt(final float value) {
        for (final float allowedTilt : TILT_VALUES) {
            if (Float.compare(allowedTilt, value) == 0) {
                return true;
            }
        }
        return false;
    }

    public static boolean isValidTiltTransition(final float startTilt, final float endTilt, final float blockDistance) {
        if (blockDistance < 0f) {
            return false;
        }
        if (Float.compare(blockDistance, 0f) == 0) {
            return Float.compare(startTilt, endTilt) == 0;
        }
        return Math.abs(endTilt - startTilt) / blockDistance <= MAX_TILT_CHANGE_PER_BLOCK;
    }

    public static float interpolateTilt(final float t, final float startTilt, final float endTilt) {
        return Mth.lerp(t, startTilt, endTilt);
    }

    public static @NotNull Vec3 rotateFaceNormal(final @NotNull Vec3 faceNormal, final @NotNull Vec3 forward, final float tiltDegrees) {
        if (faceNormal == null) {
            return new Vec3(0, 1, 0);
        }
        if (forward == null) {
            return faceNormal;
        }
        if (Mth.equal(tiltDegrees, 0f)) {
            return faceNormal;
        }

        final Vec3 normalizedForward = forward.normalize();
        if (normalizedForward.lengthSqr() < VECTOR_EPSILON) {
            return faceNormal;
        }

        final double tiltRadians = tiltDegrees * Mth.DEG_TO_RAD;
        final double cosine = Math.cos(tiltRadians);
        final double sine = Math.sin(tiltRadians);
        return faceNormal.scale(cosine)
                .add(normalizedForward.cross(faceNormal).scale(sine))
                .add(normalizedForward.scale(normalizedForward.dot(faceNormal) * (1.0d - cosine)));
    }

    public static float extractTiltDegrees(final @NotNull Vec3 faceNormal, final @NotNull Vec3 forward) {
        final Vec3 normalizedForward = forward.normalize();
        if (normalizedForward.lengthSqr() < VECTOR_EPSILON || faceNormal.lengthSqr() < VECTOR_EPSILON) {
            return 0f;
        }

        final Vec3 lateral = normalizedForward.cross(WORLD_UP);
        if (lateral.lengthSqr() < VECTOR_EPSILON) {
            return 0f;
        }

        final Vec3 referenceNormal = lateral.normalize().cross(normalizedForward).normalize();
        final Vec3 normalizedFaceNormal = faceNormal.normalize();
        final double sine = normalizedForward.dot(referenceNormal.cross(normalizedFaceNormal));
        final double cosine = Mth.clamp(referenceNormal.dot(normalizedFaceNormal), -1.0d, 1.0d);
        return (float) Math.toDegrees(Math.atan2(sine, cosine));
    }

    public static @NotNull Quaternionf tiltQuaternion(final @NotNull Vec3 forward, final float tiltDegrees) {
        final Vec3 normalizedForward = forward.normalize();
        if (normalizedForward.lengthSqr() < VECTOR_EPSILON || Mth.equal(tiltDegrees, 0f)) {
            return new Quaternionf();
        }

        return new Quaternionf().rotateAxis(
                tiltDegrees * Mth.DEG_TO_RAD,
                (float) normalizedForward.x,
                (float) normalizedForward.y,
                (float) normalizedForward.z
        );
    }

    public static @NotNull UnaryOperator<Vec3> tiltTransform(final @NotNull Vec3 forward, final float tiltDegrees, final @NotNull Vec3 pivot) {
        return point -> rotateFaceNormal(point.subtract(pivot), forward, tiltDegrees).add(pivot);
    }

    /**
     * Canonical tilt contract:
     * tilt scalars are always expressed against the canonicalized forward axis, never against the raw axis direction.
     * That keeps one physical bank represented by one scalar sign even when endpoint axes face opposite directions.
     */
    public static @NotNull Vec3 rotateFaceNormalCanonical(final @NotNull Vec3 faceNormal, final @NotNull Vec3 forward, final float tiltDegrees) {
        return rotateFaceNormal(faceNormal, canonicalizeDirection(forward), tiltDegrees);
    }

    /**
     * Adjusts a tilt value so it's expressed relative to the canonical direction of the given axis.
     * If the axis needed to be flipped to its canonical form, the tilt sign is flipped too.
     */
    public static float tiltForCanonicalAxis(final float tiltDegrees, final @NotNull Vec3 axis) {
        final Vec3 canonical = canonicalizeDirection(axis);
        return canonical.dot(axis) < 0 ? -tiltDegrees : tiltDegrees;
    }

    /**
     * Creates a tilt transform using the canonical direction of the forward axis.
     */
    public static @NotNull UnaryOperator<Vec3> tiltTransformCanonical(final @NotNull Vec3 forward, final float tiltDegrees, final @NotNull Vec3 pivot) {
        return tiltTransform(canonicalizeDirection(forward), tiltDegrees, pivot);
    }

    /**
     * Extracts a tilt scalar using the canonical forward-axis contract.
     */
    public static float extractCanonicalTiltDegrees(final @NotNull Vec3 faceNormal, final @NotNull Vec3 forward) {
        return extractTiltDegrees(faceNormal, canonicalizeDirection(forward));
    }

    public static @NotNull Vec3 canonicalizeDirection(final @NotNull Vec3 direction) {
        if (direction.x > VECTOR_EPSILON) return direction;
        if (direction.x < -VECTOR_EPSILON) return direction.scale(-1);
        if (direction.z > VECTOR_EPSILON) return direction;
        if (direction.z < -VECTOR_EPSILON) return direction.scale(-1);
        if (direction.y > VECTOR_EPSILON) return direction;
        if (direction.y < -VECTOR_EPSILON) return direction.scale(-1);
        return direction;
    }

    private static int getNearestTiltIndex(final float value) {
        int nearestIndex = 0;
        float nearestDistance = Math.abs(value - TILT_VALUES[0]);

        for (int index = 1; index < TILT_VALUES.length; index++) {
            final float candidateDistance = Math.abs(value - TILT_VALUES[index]);
            if (candidateDistance < nearestDistance) {
                nearestDistance = candidateDistance;
                nearestIndex = index;
            }
        }

        return nearestIndex;
    }

}
