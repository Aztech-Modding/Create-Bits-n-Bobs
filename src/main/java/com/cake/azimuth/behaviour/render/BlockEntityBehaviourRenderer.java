package com.cake.azimuth.behaviour.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;

public class BlockEntityBehaviourRenderer<T extends SmartBlockEntity> {

    protected void renderSafe(T blockEntity, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light,
                              int overlay) {
    }

}
