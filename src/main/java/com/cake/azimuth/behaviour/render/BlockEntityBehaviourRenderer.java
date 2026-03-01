package com.cake.azimuth.behaviour.render;

import com.cake.azimuth.behaviour.SuperBlockEntityBehaviour;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;

public abstract class BlockEntityBehaviourRenderer<T extends SmartBlockEntity> {

    @SuppressWarnings("unchecked")
    public void castRenderSafe(final SuperBlockEntityBehaviour behaviour, final SmartBlockEntity blockEntity, final float partialTicks, final PoseStack ms, final MultiBufferSource buffer, final int light,
                               final int overlay) {
        T castBlockEntity = null;
        try {
            castBlockEntity = (T) blockEntity;
        } catch (final ClassCastException e) {
            throw new ClassCastException(
                    "BlockEntityBehaviourRenderer expected a block entity of a certain type, but got " +
                            blockEntity.getClass() +
                            ", which was not within the bounds of this (" + this + ") renderer!");
        } finally {
            if (castBlockEntity != null) {
                renderSafe(behaviour, castBlockEntity, partialTicks, ms, buffer, light, overlay);
            }
        }
    }

    public void renderSafe(final SuperBlockEntityBehaviour behaviour, final T blockEntity, final float partialTicks, final PoseStack ms, final MultiBufferSource buffer, final int light,
                           final int overlay) {
    }

}
