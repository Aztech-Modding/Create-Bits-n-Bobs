package com.cake.azimuth.behaviour.render;

import com.cake.azimuth.behaviour.SuperBlockEntityBehaviour;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;

public class BlockEntityBehaviourRenderer<T extends SmartBlockEntity> {

    @SuppressWarnings("unchecked")
    public void castRenderSafe(final SuperBlockEntityBehaviour behaviour, final SmartBlockEntity blockEntity, final float partialTicks, final PoseStack ms, final MultiBufferSource buffer, final int light,
                               final int overlay) {
        T castBlockEntity = null;
        try {
            castBlockEntity = (T) blockEntity;
        } catch (final ClassCastException e) {
            throw new ClassCastException(
                    "BlockEntityBehaviourRenderer expected a block entity of type " +
                            getClass().getGenericSuperclass() +
                            " but got " +
                            blockEntity.getClass() +
                            ", renders must only ever be attached to compatible block entities " +
                            "(use SmartBlockEntity as your upper bound if you need it to be universal)!");
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
