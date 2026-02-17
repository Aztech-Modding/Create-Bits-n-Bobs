package com.kipti.bnb.content.decoration.light.headlamp.rendering.pipeline.visual;

import com.kipti.bnb.content.decoration.light.headlamp.HeadlampBlockEntity;
import com.kipti.bnb.registry.BnbInstanceTypes;
import com.kipti.bnb.registry.BnbMaterials;
import com.kipti.bnb.registry.BnbPartialModels;
import com.kipti.bnb.registry.BnbSpriteShifts;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.model.baked.BakedModelBuilder;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class HeadlampVisual extends AbstractBlockEntityVisual<HeadlampBlockEntity> {
    private final ShiftTransformedInstance headlampTop;
    private final TransformedInstance headlampBase;

    public HeadlampVisual(final VisualizationContext ctx, final HeadlampBlockEntity blockEntity, final float partialTick) {
        super(ctx, blockEntity, partialTick);
        final Model headlampTopModel = new BakedModelBuilder(BnbPartialModels.HEADLAMP_INSTANCE_ON.get())
                .materialFunc((renderType, ao) -> BnbMaterials.HEADLAMP_MATERIAL)
                .build();
        final Model headlampBaseModel = Models.partial(BnbPartialModels.HEADLAMP_INSTANCE_BASE);

        headlampTop = instancerProvider().instancer(BnbInstanceTypes.SHIFT_TRANSFORMED, headlampTopModel)
                .createInstance();
        headlampBase = instancerProvider().instancer(InstanceTypes.TRANSFORMED, headlampBaseModel)
                .createInstance();

        headlampTop.setIdentityTransform()
                .translate(getVisualPosition());
        headlampBase.setIdentityTransform()
                .translate(getVisualPosition());

        headlampTop.setSpriteShift(BnbSpriteShifts.HEADLAMP_ON_SPRITE_SHIFTS.get(DyeColor.LIGHT_BLUE));
    }

    @Override
    public void collectCrumblingInstances(final Consumer<@Nullable Instance> consumer) {
        consumer.accept(headlampTop);
        consumer.accept(headlampBase);
    }

    @Override
    public void update(final float partialTick) {
        super.update(partialTick);
    }

    @Override
    public void updateLight(final float partialTick) {
        relight(headlampBase);
        relight(headlampTop);
    }

    @Override
    protected void _delete() {
        headlampBase.delete();
        headlampTop.delete();
    }
}
