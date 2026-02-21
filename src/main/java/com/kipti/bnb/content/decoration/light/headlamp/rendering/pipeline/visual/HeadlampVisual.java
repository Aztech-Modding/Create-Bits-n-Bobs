package com.kipti.bnb.content.decoration.light.headlamp.rendering.pipeline.visual;

import com.kipti.bnb.content.decoration.light.headlamp.CCLightAddressing;
import com.kipti.bnb.content.decoration.light.headlamp.HeadlampBlock;
import com.kipti.bnb.content.decoration.light.headlamp.HeadlampBlockEntity;
import com.kipti.bnb.content.decoration.light.headlamp.rendering.HeadlampConstants;
import com.kipti.bnb.registry.BnbInstanceTypes;
import com.kipti.bnb.registry.BnbMaterials;
import com.kipti.bnb.registry.BnbPartialModels;
import com.kipti.bnb.registry.BnbSpriteShifts;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.model.baked.BakedModelBuilder;
import dev.engine_room.flywheel.lib.util.RendererReloadCache;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import net.createmod.catnip.render.SpriteShiftEntry;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector2i;

import java.util.function.Consumer;

public class HeadlampVisual extends AbstractBlockEntityVisual<HeadlampBlockEntity> {

    private static final RendererReloadCache<String, Model> MODEL_CACHE = new RendererReloadCache<>(key -> switch (key) {
        case "base" -> Models.partial(BnbPartialModels.HEADLAMP_INSTANCE_BASE);
        case "top" -> new BakedModelBuilder(BnbPartialModels.HEADLAMP_INSTANCE_OFF.get())
                .materialFunc((renderType, ao) -> BnbMaterials.HEADLAMP_NO_DIFFUSE_MATERIAL)
                .build();
        default -> throw new IllegalArgumentException(key);
    });
    private final LampInstance[] lamps = new LampInstance[HeadlampConstants.PLACEMENT_COUNT];
    private long lastRenderState = Long.MIN_VALUE;

    public HeadlampVisual(final VisualizationContext ctx, final HeadlampBlockEntity blockEntity, final float partialTick) {
        super(ctx, blockEntity, partialTick);
        update(partialTick);
    }

    @Override
    public void update(final float partialTick) {
        final long currentState = blockEntity.getRenderStateAsLong();
        if (currentState == lastRenderState) {
            return;
        }

        final int onOffBits = (int) (currentState & 0xF);
        final HeadlampBlockEntity.HeadlampPlacement[] allPlacements = HeadlampBlockEntity.HeadlampPlacement.values();

        for (int i = 0; i < HeadlampConstants.PLACEMENT_COUNT; i++) {
            final int placementValue = (int) ((currentState >> (HeadlampConstants.RENDER_STATE_ON_OFF_BITS + i * HeadlampConstants.RENDER_STATE_SLOT_BITS)) & HeadlampConstants.SLOT_VALUE_MASK);

            if (placementValue == 0) {
                if (lamps[i] != null) {
                    lamps[i].delete();
                    lamps[i] = null;
                }
                continue;
            }

            final HeadlampBlockEntity.HeadlampPlacement placement = allPlacements[i];
            final boolean isOn = getLightOnOffState(onOffBits, placement);
            final DyeColor color = placementValue == 1 ? null :
                    DyeColor.values()[Math.clamp(placementValue - HeadlampConstants.DYE_COLOR_OFFSET, 0, DyeColor.values().length - 1)];

            if (lamps[i] == null) {
                lamps[i] = new LampInstance(visualizationContext, placement, isOn, color);
            } else {
                lamps[i].update(isOn, color);
            }
        }

        lastRenderState = currentState;
    }

    private static boolean getLightOnOffState(final int onOffBits, final HeadlampBlockEntity.HeadlampPlacement placement) {
        final Vector2i coord = CCLightAddressing.getLocalMaskCoordinateForPlacement(placement);
        return CCLightAddressing.getMaskValue((byte) onOffBits, coord);
    }

    @Override
    protected void _delete() {
        for (int i = 0; i < lamps.length; i++) {
            if (lamps[i] != null) {
                lamps[i].delete();
                lamps[i] = null;
            }
        }
    }

    public static Model baseModel() {
        return MODEL_CACHE.get("base");
    }

    public static Model topModel() {
        return MODEL_CACHE.get("top");
    }

    @Override
    public void updateLight(final float partialTick) {
        for (final LampInstance lamp : lamps) {
            if (lamp != null) {
                lamp.updateLight();
            }
        }
    }

    @Override
    public void collectCrumblingInstances(final Consumer<@Nullable Instance> consumer) {
        for (final LampInstance lamp : lamps) {
            if (lamp != null) {
                lamp.collectCrumblingInstances(consumer);
            }
        }
    }

    /**
     * Holder of the base and single top instance for a single lamp placement.
     * The top instance always uses the "off" model as its base geometry; sprite shifts
     * handle switching to on/dyed variants (off→on, off→off-dye, off→on-dye).
     */
    private class LampInstance {
        private final TransformedInstance base;
        private final ShiftTransformedInstance top;
        private boolean wasOn;
        private DyeColor currentColor;

        public LampInstance(final VisualizationContext ctx, final HeadlampBlockEntity.HeadlampPlacement placement, final boolean isOn, final @Nullable DyeColor color) {
            this.base = ctx.instancerProvider().instancer(BnbInstanceTypes.SHIFT_TRANSFORMED, HeadlampVisual.baseModel()).createInstance();
            this.top = ctx.instancerProvider().instancer(BnbInstanceTypes.SHIFT_TRANSFORMED, HeadlampVisual.topModel()).createInstance();

            this.wasOn = isOn;

            updateTransform(placement);
            updateSpriteShift(isOn, color);
            updateLight();
        }

        private void updateTransform(final HeadlampBlockEntity.HeadlampPlacement placement) {
            final float tx = (float) placement.horizontalAlignment().getOffset();
            final float tz = (float) placement.verticalAlignment().getOffset();
            final Direction facing = blockEntity.getBlockState().getValue(HeadlampBlock.FACING);

            applyTransform(base, tx, tz, facing);
            applyTopTransform(top, tx, tz, facing);
        }

        /**
         * Sets the appropriate sprite shift on the top instance based on on/off state and color.
         * All shifts are from the off (undyed) base texture.
         */
        private void updateSpriteShift(final boolean isOn, final @Nullable DyeColor color) {
            currentColor = color;

            final SpriteShiftEntry shift;
            if (color != null) {
                shift = isOn
                        ? BnbSpriteShifts.HEADLAMP_ON_SPRITE_SHIFTS.get(color)
                        : BnbSpriteShifts.HEADLAMP_OFF_SPRITE_SHIFTS.get(color);
            } else if (isOn) {
                shift = BnbSpriteShifts.HEADLAMP_ON_UNDYED_SPRITE_SHIFT;
            } else {
                shift = null; // off + undyed = base texture, no shift needed
            }

            top.setSpriteShift(shift);

            if (isOn) {
                top.light(LightTexture.FULL_BRIGHT);
            }
            top.setChanged();
        }

        public void updateLight() {
            relight(base);
            if (!wasOn) {
                relight(top);
            }
        }

        private void applyTransform(final TransformedInstance instance, final float tx, final float tz, final Direction facing) {
            instance.setIdentityTransform()
                    .translate(getVisualPosition());
            applySurfaceRotation(instance, facing);
            instance.translate(tx, 0, tz)
                    .setChanged();
        }

        private void applyTopTransform(final ShiftTransformedInstance instance, final float tx, final float tz, final Direction facing) {
            instance.setIdentityTransform()
                    .translate(getVisualPosition());
            applySurfaceRotation(instance, facing);
            instance.translate(tx, 0, tz)
                    .setChanged();
        }

        private void applySurfaceRotation(final TransformedInstance instance, final Direction facing) {
            if (facing == Direction.UP) {
                return;
            }
            final Quaternionf rotation = facing.getRotation();
            final Quaternionf upRotation = Direction.UP.getRotation();
            upRotation.invert();
            instance.translate(0.5f, 0.5f, 0.5f)
                    .rotate(upRotation.mul(rotation))
                    .translate(-0.5f, -0.5f, -0.5f);
        }

        public void update(final boolean isOn, final @Nullable DyeColor color) {
            if (isOn != wasOn || currentColor != color) {
                updateSpriteShift(isOn, color);
                wasOn = isOn;
            }
        }

        public void collectCrumblingInstances(final Consumer<@Nullable Instance> consumer) {
            consumer.accept(base);
            consumer.accept(top);
        }

        public void delete() {
            base.delete();
            top.delete();
        }
    }

}
