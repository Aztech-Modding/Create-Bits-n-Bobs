package com.kipti.bnb.content.kinetics.chain_pulley;

import com.kipti.bnb.registry.client.BnbPartialModels;
import com.kipti.bnb.registry.client.BnbSpriteShifts;
import com.simibubi.create.content.contraptions.pulley.PulleyRenderer;
import com.simibubi.create.content.kinetics.base.ShaftVisual;
import com.simibubi.create.content.processing.burner.ScrollInstance;
import com.simibubi.create.foundation.render.AllInstanceTypes;
import com.simibubi.create.foundation.render.SpecialModels;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visual.ShaderLightVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import dev.engine_room.flywheel.lib.visual.util.InstanceRecycler;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.createmod.catnip.math.AngleHelper;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;

/**
 * Unused because the scrolling visuals don't have cutout support, and I cant be asked to figure it out and make a custom shader yet
 * TODO: use cutout supp cause it actually exists
 */
public class ChainPulleyVisual extends ShaftVisual<ChainPulleyBlockEntity> implements SimpleDynamicVisual, ShaderLightVisual {
    private final InstanceRecycler<ScrollInstance> belt;
    private final ScrollInstance halfBelt;

    private final ScrollInstance coil;

    private final TransformedInstance magnet;

    private final Matrix4fc cachedMagnetTransform;

    private float lastOffset = Float.NaN;

    private final long topSection;

    private long lastBottomSection;

    public ChainPulleyVisual(final VisualizationContext context, final ChainPulleyBlockEntity blockEntity, final float partialTick) {
        super(context, blockEntity, partialTick);

        final float blockStateAngle =
                AngleHelper.horizontalAngle(Direction.get(Direction.AxisDirection.POSITIVE, blockState.getValue(ChainPulleyBlock.HORIZONTAL_AXIS)));

        final Quaternionfc rotation = new Quaternionf().rotationY(Mth.DEG_TO_RAD * blockStateAngle);

        topSection = SectionPos.of(pos).asLong();

        belt = new InstanceRecycler<>(() -> context.instancerProvider()
                .instancer(AllInstanceTypes.SCROLLING, SpecialModels.flatLit(BnbPartialModels.CHAIN_ROPE))
                .createInstance()
                .rotation(rotation)
                .setSpriteShift(BnbSpriteShifts.CHAIN_ROPE));

        halfBelt = context.instancerProvider()
                .instancer(AllInstanceTypes.SCROLLING, SpecialModels.flatLit(BnbPartialModels.CHAIN_ROPE_HALF))
                .createInstance()
                .rotation(rotation)
                .setSpriteShift(BnbSpriteShifts.CHAIN_ROPE);

        coil = context.instancerProvider()
                .instancer(AllInstanceTypes.SCROLLING, Models.partial(BnbPartialModels.CHAIN_ROPE_COIL))
                .createInstance()
                .position(getVisualPosition())
                .rotation(rotation)
                .setSpriteShift(BnbSpriteShifts.CHAIN_PULLEY_COIL);

        coil.setChanged();

        magnet = context.instancerProvider()
                .instancer(InstanceTypes.TRANSFORMED, SpecialModels.flatLit(BnbPartialModels.CHAIN_PULLEY_MAGNET_NO_CHAIN))
                .createInstance();

        // Cache the magnet's transform to avoid recalculating this unchanging bit every frame
        magnet.setIdentityTransform()
                .translate(getVisualPosition())
                .center()
                .rotateYDegrees(blockStateAngle)
                .uncenter();

        cachedMagnetTransform = new Matrix4f(magnet.pose);

        animate(PulleyRenderer.getBlockEntityOffset(partialTick, blockEntity));
    }

    @Override
    public void updateLight(final float partialTick) {
        super.updateLight(partialTick);

        relight(coil);
    }

    @Override
    public void setSectionCollector(final SectionCollector sectionCollector) {
        super.setSectionCollector(sectionCollector);

        sectionCollector.sections(getLightSections(lastOffset));
    }

    @Override
    public void beginFrame(final DynamicVisual.Context ctx) {
        final float offset = PulleyRenderer.getBlockEntityOffset(ctx.partialTick(), blockEntity);
        if (!PulleyRenderer.isPulleyRunning(blockEntity)) {
            lastOffset = offset;
            belt.delete();
            halfBelt.setVisible(false);
            magnet.setVisible(false);
            animateCoil(offset);
            return;
        }
        animate(offset);
    }

    @Override
    protected void _delete() {
        super._delete();

        belt.delete();
        halfBelt.delete();
        coil.delete();
        magnet.delete();
    }

    private void animate(final float offset) {
        if (offset == lastOffset) {
            return;
        }
        lastOffset = offset;

        maybeUpdateSections(offset);

        animateCoil(offset);

        animateHalfBelt(offset);

        animateBelt(offset);

        animateMagnet(offset);
    }

    private void maybeUpdateSections(final float offset) {
        if (lightSections == null) {
            return;
        }
        if (lastBottomSection == SectionPos.offset(topSection, 0, -offset2SectionCount(offset), 0)) {
            return;
        }

        lightSections.sections(getLightSections(offset));
    }

    private void animateMagnet(final float offset) {
        magnet.setVisible(true);
        magnet.setTransform(cachedMagnetTransform)
                .translateY(-offset)
                .setChanged();
    }

    private void animateBelt(final float offset) {
        belt.resetCount();

        for (int i = 0; i < offset - .25f; i++) {
            final ScrollInstance segment = belt.get()
                    .position(getVisualPosition())
                    .shift(0, -(offset - i), 0);

            segment.offsetV = offset;

            segment.setChanged();
        }

        belt.discardExtra();
    }

    private void animateHalfBelt(final float offset) {
        final float f = offset % 1;
        if (f < .25f || f > .75f) {
            halfBelt.setVisible(true);
            halfBelt.position(getVisualPosition())
                    .shift(0, -(f > .75f ? f - 1 : f), 0);

            halfBelt.offsetV = offset;

            halfBelt.setChanged();
        } else {
            halfBelt.setVisible(false);
        }
    }

    private void animateCoil(final float offset) {
        coil.offsetV = -offset * 2;

        coil.setChanged();
    }

    private LongSet getLightSections(final float offset) {
        final LongArraySet out = new LongArraySet();

        final int sectionCount = offset2SectionCount(offset);

        for (int i = 0; i < sectionCount; i++) {
            out.add(SectionPos.offset(topSection, 0, -i, 0));
        }

        lastBottomSection = SectionPos.offset(topSection, 0, -sectionCount, 0);

        return out;
    }

    private static int offset2SectionCount(final float offset) {
        return (int) Math.ceil((offset + 1) / 16);
    }

}

