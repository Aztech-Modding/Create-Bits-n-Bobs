package com.kipti.bnb.content.decoration.girder_strut;

import com.mojang.blaze3d.vertex.*;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperBufferFactory;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

public class GirderStrutBlockEntityRenderer extends SmartBlockEntityRenderer<GirderStrutBlockEntity> {


    public GirderStrutBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(GirderStrutBlockEntity blockEntity, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        super.renderSafe(blockEntity, partialTicks, ms, buffer, light, overlay);

        StrutModelType modelType;
        if (blockEntity.getBlockState().getBlock() instanceof GirderStrutBlock girderStrutBlock) {
            modelType = girderStrutBlock.getModelType();
        } else {
            return;
        }

        // Fast, pure partial based approach, which actually looks ok so ill leave it for fast graphics
        if (Minecraft.getInstance().options.graphicsMode().get() == GraphicsStatus.FAST) {
            // Render the girder strut segment
            for (BlockPos pos : blockEntity.getConnectionsCopy()) {
                pos = pos.offset(blockEntity.getBlockPos());
                BlockState state = blockEntity.getLevel().getBlockState(pos);
                if (!
                        (state.getBlock() instanceof GirderStrutBlock)) {
                    continue; // Skip if the block is not a Girder Strut
                }

                Vec3i relative = pos.subtract(blockEntity.getBlockPos());
                // Calculate the length of the strut segment based on the distance to the connected block
                Vec3 thisAttachment = Vec3.atCenterOf(blockEntity.getBlockPos()).relative(blockEntity.getBlockState().getValue(GirderStrutBlock.FACING), -0.4);
                BlockState otherState = blockEntity.getLevel().getBlockState(pos);
                Vec3 otherAttachment = Vec3.atCenterOf(pos).relative(otherState.getValue(GirderStrutBlock.FACING), -0.4);

                double length = thisAttachment.distanceTo(otherAttachment);
                int segments = (int) Math.ceil(length);
                double lengthOffset = (length - segments) / 2.0;

                // Render the segments of the girder strut
                ms.pushPose();

                Vec3 relativeVec = otherAttachment.subtract(thisAttachment);
                float distHorizontal = (float) Math.sqrt(relativeVec.x() * relativeVec.x() + relativeVec.z() * relativeVec.z());
                double yRot = distHorizontal == 0 ? 0 : Math.atan2(relativeVec.x(), relativeVec.z());
                double xRot = (float) Math.atan2(relativeVec.y(), distHorizontal);

                TransformStack.of(ms)
                        .translate(Vec3.atLowerCornerOf(blockEntity.getBlockState().getValue(GirderStrutBlock.FACING).getNormal()).scale(-0.4))
                        .center()
                        .rotateY((float) yRot)
                        .rotateX(-(float) xRot)
                        .uncenter();

                ms.translate(0, 0, lengthOffset + 0.5); // Adjust the translation based on segment length
                if (getRenderPriority(relative) > getRenderPriority(relative.multiply(-1))) {
                    renderSegments(state, modelType.getPartialModel(), ms, segments, buffer, blockEntity.getLevel() == null ? light : LevelRenderer.getLightColor(blockEntity.getLevel(), pos));
                }
                ms.popPose();
            }
        } else { //use GirderStrutModelManipulator
            if (blockEntity.connectionRenderBufferCache == null) {
                GirderStrutModelBuilder.GirderStrutModelData connectionData = GirderStrutModelBuilder.GirderStrutModelData.collect(blockEntity.getLevel(), blockEntity.getBlockPos(), blockEntity.getBlockState(), blockEntity);
                List<Consumer<BufferBuilder>> quads = connectionData.connections()
                        .stream()
                        .flatMap(c -> GirderStrutModelManipulator.bakeConnectionToConsumer(c, modelType, blockEntity.createLighter()).stream())
                        .toList();

                BufferBuilder builder = new BufferBuilder(new ByteBufferBuilder(256), VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);

                for (Consumer<BufferBuilder> quad : quads) {
                    quad.accept(builder);
                }
                MeshData meshData = builder.build();

                if (meshData == null) return;

                blockEntity.connectionRenderBufferCache = SuperBufferFactory.getInstance().create(meshData);
            }
            blockEntity.connectionRenderBufferCache.renderInto(ms, buffer.getBuffer(RenderType.solid()));
        }

    }


    protected void renderSegments(BlockState state, PartialModel model, PoseStack ms, int length, MultiBufferSource buffer, int light) {
        // Render the segments of the girder strut
        for (int i = 0; i < length; i++) {
            ms.pushPose();
            ms.translate(0, 0, i); // Adjust the translation based on segment height
            CachedBuffers.partial(model, state)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderType.solid()));
            ms.popPose();
        }
    }

    /**
     * Used to track which one of the two is more positive
     */
    protected int getRenderPriority(Vec3i relative) {
        return relative.getY() * 10000 + relative.getX() * 100 + relative.getZ();
    }

    @Override
    public @NotNull AABB getRenderBoundingBox(@NotNull GirderStrutBlockEntity blockEntity) {
        return super.getRenderBoundingBox(blockEntity).inflate(10);
    }

    @Override
    public boolean shouldRender(final GirderStrutBlockEntity blockEntity, final Vec3 cameraPos) {
        return true;
    }

    @Override
    public boolean shouldRenderOffScreen(final GirderStrutBlockEntity blockEntity) {
        return true;
    }

}
