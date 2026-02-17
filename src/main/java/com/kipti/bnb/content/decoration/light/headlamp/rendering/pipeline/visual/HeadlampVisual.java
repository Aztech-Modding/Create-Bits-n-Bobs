package com.kipti.bnb.content.decoration.light.headlamp.rendering.pipeline.visual;

import com.kipti.bnb.content.decoration.light.headlamp.CCLightAddressing;
import com.kipti.bnb.content.decoration.light.headlamp.HeadlampBlockEntity;
import com.kipti.bnb.content.decoration.light.headlamp.rendering.HeadlampConstants;
import com.kipti.bnb.registry.BnbInstanceTypes;
import com.kipti.bnb.registry.BnbPartialModels;
import com.kipti.bnb.registry.BnbSpriteShifts;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.api.material.Transparency;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.material.SimpleMaterial;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.model.baked.BakedModelBuilder;
import dev.engine_room.flywheel.lib.util.RendererReloadCache;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

import java.util.function.Consumer;

public class HeadlampVisual extends AbstractBlockEntityVisual<HeadlampBlockEntity> {

    private final LampInstance[] lamps = new LampInstance[HeadlampConstants.PLACEMENT_COUNT];
    private long lastRenderState = Long.MIN_VALUE;

    private static final RendererReloadCache<String, Model> MODEL_CACHE = new RendererReloadCache<>(key -> {
//        Material material = SimpleMaterial.builder()
//                .transparency(Transparency.ORDER_INDEPENDENT)
//                .mipmap(false)
//                .blur(false)
//                .backfaceCulling(true)
//                .polygonOffset(true)
//                .build();
        Material material = SimpleMaterial.builder()
                .transparency(Transparency.ORDER_INDEPENDENT) // or ADDITIVE
                .mipmap(false)
                .blur(false)
                .backfaceCulling(true)
//                .diffuse(false)
                .polygonOffset(true)         // Keeps the overlay bias we added before
//                .writeMask(WriteMask.COLOR)  // <--- CRITICAL FIX: Disables depth writing
//                .depthTest(DepthTest.LEQUAL)
                .build();

        return switch (key) {
            case "base" -> Models.partial(BnbPartialModels.HEADLAMP_INSTANCE_BASE);
            case "on" -> new BakedModelBuilder(BnbPartialModels.HEADLAMP_INSTANCE_ON.get())
                    .materialFunc((renderType, ao) -> material)
                    .build();
            case "off" -> new BakedModelBuilder(BnbPartialModels.HEADLAMP_INSTANCE_OFF.get())
                    .materialFunc((renderType, ao) -> material)
                    .build();
            default -> throw new IllegalArgumentException(key);
        };
    });

    public HeadlampVisual(final VisualizationContext ctx, final HeadlampBlockEntity blockEntity, final float partialTick) {
        super(ctx, blockEntity, partialTick);
        update(partialTick);
    }

    public static Model baseModel() {
        return MODEL_CACHE.get("base");
    }

    public static Model topModelOn() {
        return MODEL_CACHE.get("on");
    }

    public static Model topModelOff() {
        return MODEL_CACHE.get("off");
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
                lamps[i].update(placement, isOn, color);
            }
        }

        lastRenderState = currentState;
    }

    private static boolean getLightOnOffState(final int onOffBits, final HeadlampBlockEntity.HeadlampPlacement placement) {
        final Vector2i coord = CCLightAddressing.getLocalMaskCoordinateForPlacement(placement);
        return CCLightAddressing.getMaskValue((byte) onOffBits, coord);
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

    @Override
    protected void _delete() {
        for (int i = 0; i < lamps.length; i++) {
            if (lamps[i] != null) {
                lamps[i].delete();
                lamps[i] = null;
            }
        }
    }

    private class LampInstance {
        private final TransformedInstance base;
        private ShiftTransformedInstance top;
        private boolean wasOn;

        public LampInstance(final VisualizationContext ctx, final HeadlampBlockEntity.HeadlampPlacement placement, final boolean isOn, final @Nullable DyeColor color) {
            this.base = ctx.instancerProvider().instancer(BnbInstanceTypes.SHIFT_TRANSFORMED, HeadlampVisual.baseModel()).createInstance();
            this.wasOn = isOn;
            createTopInstance(ctx, isOn);

            updateTransform(placement);
            updateColor(color, isOn);
            updateLight(); // Initialize light
        }

        private void createTopInstance(final VisualizationContext ctx, final boolean isOn) {
            final Model model = isOn ? HeadlampVisual.topModelOn() : HeadlampVisual.topModelOff();
            this.top = ctx.instancerProvider().instancer(BnbInstanceTypes.SHIFT_TRANSFORMED, model).createInstance();
        }

        public void update(final HeadlampBlockEntity.HeadlampPlacement placement, final boolean isOn, final @Nullable DyeColor color) {
            if (isOn != wasOn) {
                top.delete();
                createTopInstance(visualizationContext, isOn);
                wasOn = isOn;
                // Re-apply light to new instance
                relight(top);
            }
            updateTransform(placement);
            updateColor(color, isOn);
        }

        private void updateTransform(final HeadlampBlockEntity.HeadlampPlacement placement) {
            // Calculate base transform
            final float tx = (float) placement.horizontalAlignment().getOffset();
            final float tz = (float) placement.verticalAlignment().getOffset();

            // Set base transform
            base.setIdentityTransform()
                    .translate(getVisualPosition())
                    .translate(tx, 0, tz);

            // Set top transform
            top.setIdentityTransform()
                    .translate(getVisualPosition())
                    .translate(tx, 0, tz)
                    .translate(0.5f, 0.5f, 0.5f)
                    .scale(1f)
                    .translate(-0.5f, -0.5f, -0.5f);

            top.setChanged();
            base.setChanged();
        }

        private void updateColor(final @Nullable DyeColor color, final boolean isOn) {
            if (color != null) {
                top.setSpriteShift(isOn ? BnbSpriteShifts.HEADLAMP_ON_SPRITE_SHIFTS.get(color) : BnbSpriteShifts.HEADLAMP_OFF_SPRITE_SHIFTS.get(color));
            } else {
                top.setSpriteShift(null);
            }
        }

        public void updateLight() {
            relight(base);
            relight(top);
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
