package com.kipti.bnb.mixin.articulate;

import com.kipti.bnb.content.articulate.ArticulatedTrackPlacementPlan;
import com.kipti.bnb.mixin_accessor.ArticulatedTrackPlacementPlanHolder;
import com.simibubi.create.content.trains.track.BezierConnection;
import com.simibubi.create.content.trains.track.TrackPlacement;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.outliner.Outliner;
import net.createmod.catnip.theme.Color;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TrackPlacement.class)
public abstract class TrackPlacementClientMixin {

    @Shadow
    private static LerpedFloat animation;

    @Shadow
    private static int lastLineCount;

    @Unique
    private static final String articulate$previewKey = "articulate_curve";

    @Unique
    private static int articulate$lastPreviewLineCount = 0;

    @Inject(method = "clientTick", at = @At("TAIL"))
    private static void articulate$drawPlacementPlanPreview(final CallbackInfo ci) {
        final TrackPlacement.PlacementInfo info = TrackPlacement.cached;
        final ArticulatedTrackPlacementPlan plan = info == null
                ? null
                : ((ArticulatedTrackPlacementPlanHolder) info).articulate$getPlacementPlan();
        if (plan == null || plan.isEmpty()) {
            articulate$clearPlacementPlanPreview();
            return;
        }

        articulate$clearDefaultPreview();

        final int color = Color.mixColors(0xEA5C2B, 0x95CD41, animation.getValue());
        final float s = animation.getValue() * 7 / 8f + 1 / 8f;
        final float lw = animation.getValue() * 1 / 16f + 1 / 16f;
        final Vec3 up = new Vec3(0, 4 / 16f, 0);
        int lineId = 0;

        for (final ArticulatedTrackPlacementPlan.Segment segment : plan.segments()) {
            final BezierConnection curve = segment.curve();
            final int segmentCount = curve.getSegmentCount();
            if (segmentCount <= 0) {
                continue;
            }

            Vec3 previous1 = null;
            Vec3 previous2 = null;
            final Vec3 end1 = curve.starts.getFirst();
            final Vec3 end2 = curve.starts.getSecond();
            final Vec3 finish1 = end1.add(curve.axes.getFirst().scale(curve.getHandleLength()));
            final Vec3 finish2 = end2.add(curve.axes.getSecond().scale(curve.getHandleLength()));

            for (int index = 0; index <= segmentCount; index++) {
                final float t = index / (float) segmentCount;
                final Vec3 result = VecHelper.bezier(end1, end2, finish1, finish2, t);
                final Vec3 derivative = VecHelper.bezierDerivative(end1, end2, finish1, finish2, t).normalize();
                final Vec3 normal = curve.getNormal(t).cross(derivative).scale(15 / 16f);
                final Vec3 rail1 = result.add(normal).add(up);
                final Vec3 rail2 = result.subtract(normal).add(up);

                if (previous1 != null) {
                    final Vec3 middle1 = rail1.add(previous1).scale(0.5f);
                    final Vec3 middle2 = rail2.add(previous2).scale(0.5f);
                    Outliner.getInstance()
                            .showLine(Pair.of(articulate$previewKey, lineId++), VecHelper.lerp(s, middle1, previous1),
                                    VecHelper.lerp(s, middle1, rail1))
                            .colored(color)
                            .disableLineNormals()
                            .lineWidth(lw);
                    Outliner.getInstance()
                            .showLine(Pair.of(articulate$previewKey, lineId++), VecHelper.lerp(s, middle2, previous2),
                                    VecHelper.lerp(s, middle2, rail2))
                            .colored(color)
                            .disableLineNormals()
                            .lineWidth(lw);
                }

                previous1 = rail1;
                previous2 = rail2;
            }
        }

        for (int index = lineId; index < articulate$lastPreviewLineCount; index++) {
            Outliner.getInstance().remove(Pair.of(articulate$previewKey, index));
        }
        articulate$lastPreviewLineCount = lineId;
    }

    @Unique
    private static void articulate$clearDefaultPreview() {
        for (int index = 1; index <= 4; index++) {
            Outliner.getInstance().remove(Pair.of("start", index));
        }
        for (int index = 0; index <= lastLineCount; index++) {
            Outliner.getInstance().remove(Pair.of("curve", index * 2));
            Outliner.getInstance().remove(Pair.of("curve", index * 2 + 1));
        }
        lastLineCount = 0;
    }

    @Unique
    private static void articulate$clearPlacementPlanPreview() {
        for (int index = 0; index < articulate$lastPreviewLineCount; index++) {
            Outliner.getInstance().remove(Pair.of(articulate$previewKey, index));
        }
        articulate$lastPreviewLineCount = 0;
    }

}
