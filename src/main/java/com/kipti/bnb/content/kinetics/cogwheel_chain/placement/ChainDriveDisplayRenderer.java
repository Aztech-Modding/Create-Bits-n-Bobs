package com.kipti.bnb.content.kinetics.cogwheel_chain.placement;

import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.PlacingCogwheelChain;
import net.createmod.catnip.outliner.Outliner;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Shared display primitives for chain-drive placement and partial-edit previews.
 * <p>
 * Every method accepts an explicit colour so callers decide valid/invalid styling.
 */
public class ChainDriveDisplayRenderer {

    public static final int VALID_COLOUR = 0x95CD41;
    public static final int INVALID_COLOUR = 0xFF5D5D;
    public static final int MAX_PLACEMENT_RANGE = PlacingCogwheelChain.MAX_CHAIN_BOUNDS;

    private static final float PARTICLE_DENSITY = 0.1f;
    private static final float CONNECTION_LINE_WIDTH = 2f / 16f;
    private static final float OUTLINE_LINE_WIDTH = 1f / 16f;

    public static void renderParticlesBetween(final ClientLevel level, final Vec3 from, final Vec3 to, final int colour) {
        final Vec3 delta = to.subtract(from);
        final double length = delta.length();
        if (length < 1.0E-3 || length > 256) {
            return;
        }

        final float r = ((colour >> 16) & 0xFF) / 255f;
        final float g = ((colour >> 8) & 0xFF) / 255f;
        final float b = (colour & 0xFF) / 255f;

        final Vec3 dir = delta.normalize();
        final double step = 0.25;

        for (double t = 0; t <= length; t += step) {
            if (level.getRandom().nextFloat() > PARTICLE_DENSITY) {
                continue;
            }
            final Vec3 lerped = from.add(dir.scale(t));
            level.addParticle(
                    new DustParticleOptions(new Vector3f(r, g, b), 1), true,
                    lerped.x, lerped.y, lerped.z, 0, 0, 0
            );
        }
    }

    public static void renderBlockOutline(final ClientLevel level, final BlockPos pos,
                                          final BlockState placementState, final int colour,
                                          final String keyPrefix) {
        final AtomicInteger counter = new AtomicInteger(0);
        placementState.getShape(level, pos).forAllEdges((fx, fy, fz, tx, ty, tz) ->
                Outliner.getInstance().showLine(
                                keyPrefix + "_" + counter.getAndIncrement(),
                                new Vec3(fx, fy, fz).add(Vec3.atLowerCornerOf(pos)),
                                new Vec3(tx, ty, tz).add(Vec3.atLowerCornerOf(pos)))
                        .colored(colour)
                        .lineWidth(OUTLINE_LINE_WIDTH));
    }

    public static void renderBlockOutline(final ClientLevel level, final BlockPos pos,
                                          final BlockState placementState, final int colour) {
        renderBlockOutline(level, pos, placementState, colour, "chain_outline_" + pos);
    }

    public static void renderBlockOutline(final ClientLevel level, final BlockPos pos, final int colour) {
        final BlockState state = level.getBlockState(pos);
        renderBlockOutline(level, pos, state, colour, "chain_outline_" + pos);
    }

    public static void renderConnectionSegment(final String key,
                                               final ChainPlacementPathDisplayHelper.DisplayedSegment segment,
                                               final int colour) {
        Outliner.getInstance().showLine(key, segment.from(), segment.to())
                .colored(colour)
                .lineWidth(CONNECTION_LINE_WIDTH);
    }

    public static void renderConnectionLine(final String key, final Vec3 from, final Vec3 to, final int colour) {
        Outliner.getInstance().showLine(key, from, to)
                .colored(colour)
                .lineWidth(CONNECTION_LINE_WIDTH);
    }
}
