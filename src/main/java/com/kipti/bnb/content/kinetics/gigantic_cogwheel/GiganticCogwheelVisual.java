package com.kipti.bnb.content.kinetics.gigantic_cogwheel;

import com.simibubi.create.content.kinetics.base.KineticBlockEntityVisual;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.foundation.render.AllInstanceTypes;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.model.baked.BakedModelBuilder;
import dev.engine_room.flywheel.lib.util.RendererReloadCache;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Consumer;

/**
 * Handles Flywheel-based visualization for the gigantic cogwheel, managing
 * rotating instances and model caching based on the cogwheel's material.
 */
public class GiganticCogwheelVisual extends KineticBlockEntityVisual<GiganticCogwheelBlockEntity> {

    private static final RendererReloadCache<ModelKey, Model> MODEL_CACHE = new RendererReloadCache<>(
            GiganticCogwheelVisual::createModel);

    protected BlockState lastMaterial;
    protected RotatingInstance rotatingModel;

    public GiganticCogwheelVisual(final VisualizationContext context,
                                  final GiganticCogwheelBlockEntity blockEntity,
                                  final float partialTick) {
        super(context, blockEntity, partialTick);
        this.setupInstance();
    }

    private void setupInstance() {
        this.lastMaterial = this.blockEntity.material;
        this.rotatingModel = this.instancerProvider().instancer(
                        AllInstanceTypes.ROTATING,
                        MODEL_CACHE.get(new ModelKey(this.blockEntity.material))
                )
                .createInstance();
        this.rotatingModel.setup(this.blockEntity)
                .setPosition(this.getVisualPosition())
                .rotateToFace(this.rotationAxis())
                .setChanged();
        this.relight(this.rotatingModel);
    }

    @Override
    public void update(final float pt) {
        if (this.lastMaterial != this.blockEntity.material) {
            this.rotatingModel.delete();
            this.setupInstance();
        } else {
            this.rotatingModel.setup(this.blockEntity).setChanged();
        }
    }

    @Override
    public void updateLight(final float partialTick) {
        this.relight(this.rotatingModel);
    }

    @Override
    protected void _delete() {
        this.rotatingModel.delete();
    }

    @Override
    public void collectCrumblingInstances(final Consumer<Instance> consumer) {
        consumer.accept(this.rotatingModel);
    }

    private static Model createModel(final ModelKey key) {
        final BakedModel model = GiganticCogwheelRenderer.generateModel(key.material());
        return new BakedModelBuilder(model).build();
    }

    public record ModelKey(BlockState material) {
    }
}
