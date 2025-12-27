package com.kipti.bnb.content.decoration.light.headlamp;

import com.kipti.bnb.CreateBitsnBobs;
import com.kipti.bnb.content.decoration.light.founation.LightBlock;
import com.kipti.bnb.registry.BnbPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.model.BakedQuadHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class HeadlampModelBuilder extends BakedModelWrapper<BakedModel> {

    private static final ModelProperty<HeadlampModelData> HEADLAMP_PROPERTY = new ModelProperty<>();

    public HeadlampModelBuilder(final BakedModel originalModel) {
        super(originalModel);
    }

    @Override
    public @NotNull ModelData getModelData(final BlockAndTintGetter world, final BlockPos pos, final BlockState state, final ModelData blockEntityData) {
        final HeadlampModelData data = new HeadlampModelData();

        final int[] activePlacements = new int[9];

        if (world.getBlockEntity(pos) instanceof final HeadlampBlockEntity headlampBlockEntity) {
            final int[] existingPlacements = headlampBlockEntity.getActivePlacements();
            System.arraycopy(existingPlacements, 0, activePlacements, 0, existingPlacements.length);
        }

        data.setActivePlacements(activePlacements);

        return ModelData.builder()
                .with(HEADLAMP_PROPERTY, data)
                .build();
    }

    @Override
    public List<BakedQuad> getQuads(final BlockState state, final Direction side, final RandomSource rand, final ModelData data, final RenderType renderType) {
        if (data.has(HEADLAMP_PROPERTY)) {
            final List<BakedQuad> model = new ArrayList<>(super.getQuads(state, side, rand, data, renderType));
            final HeadlampModelData headlampModelData = data.get(HEADLAMP_PROPERTY);
            for (int i = 0; i < HeadlampBlockEntity.HeadlampPlacement.values().length; i++) {
                final HeadlampBlockEntity.HeadlampPlacement placement = HeadlampBlockEntity.HeadlampPlacement.values()[i];
                final int placementValue = headlampModelData.getActivePlacements()[i];
                if (placementValue != 0) {
                    final PoseStack poseStack = new PoseStack();
                    poseStack.translate(0.5f, 0.5f, 0.5f);
                    poseStack.mulPose(state.getValue(HeadlampBlock.FACING).getRotation());
                    poseStack.translate(-0.5f, -0.5f, -0.5f);
                    poseStack.translate(placement.horizontalAlignment().getOffset(), 0, placement.verticalAlignment().getOffset());

                    model.addAll(transformQuadsForLamp((LightBlock.shouldUseOnLightModel(state) ? BnbPartialModels.HEADLAMP_ON : BnbPartialModels.HEADLAMP_OFF).get()
                            .getQuads(state, side, rand, data, renderType), poseStack, placementValue));
                }
            }
            return model;
        }
        return Collections.emptyList();
    }

    private List<BakedQuad> transformQuadsForLamp(final List<BakedQuad> quads, final PoseStack poseStack, final int placementValue) {
        @Nullable final DyeColor color = placementValue == 1 ? null : DyeColor.values()[Math.clamp(placementValue - 2, 0, DyeColor.values().length - 1)];
        final Matrix4f pose = poseStack.last().pose();
        final List<BakedQuad> transformedQuads = new ArrayList<>();
        for (final BakedQuad quad : quads) {
            final int[] vertices = quad.getVertices();
            final int[] transformedVertices = Arrays.copyOf(vertices, vertices.length);

            final TextureAtlasSprite oldSprite = quad.getSprite();
            final boolean oldSpriteIsBlockTexture = oldSprite.contents().name().equals(CreateBitsnBobs.asResource("block/headlight/headlight"));
            final boolean oldSpriteIsOffLampTexture = oldSprite.contents().name().equals(CreateBitsnBobs.asResource("block/headlight/headlight_off"));
            final TextureAtlasSprite newSprite = oldSpriteIsBlockTexture || color == null ? oldSprite :
                    Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(
                            CreateBitsnBobs.asResource("block/headlight/headlight_" + (oldSpriteIsOffLampTexture ? "off" : "on") + "_" + color.getName())
                    );

            final Vec3 quadNormal = Vec3.atLowerCornerOf(quad.getDirection()
                    .getNormal());
            final Vector3f quadNormalJoml = pose.transformDirection((float) quadNormal.x, (float) quadNormal.y, (float) quadNormal.z, new Vector3f());

            for (int i = 0; i < vertices.length / BakedQuadHelper.VERTEX_STRIDE; i++) {
                final Vec3 vertex = BakedQuadHelper.getXYZ(vertices, i);
                final Vec3 normal = BakedQuadHelper.getNormalXYZ(vertices, i);
                float uvX = BakedQuadHelper.getU(vertices, i);
                float uvY = BakedQuadHelper.getV(vertices, i);

                if (!oldSprite.equals(newSprite)) {
                    uvX = (uvX - oldSprite.getU0()) / (oldSprite.getU1() - oldSprite.getU0()) * (newSprite.getU1() - newSprite.getU0()) + newSprite.getU0();
                    uvY = (uvY - oldSprite.getV0()) / (oldSprite.getV1() - oldSprite.getV0()) * (newSprite.getV1() - newSprite.getV0()) + newSprite.getV0();
                }

                final Vector3f vertexJoml = pose.transformPosition((float) vertex.x, (float) vertex.y, (float) vertex.z, new Vector3f());
                final Vector3f normalJoml = pose.transformDirection((float) normal.x, (float) normal.y, (float) normal.z, new Vector3f());

                BakedQuadHelper.setXYZ(transformedVertices, i, new Vec3(vertexJoml.x, vertexJoml.y, vertexJoml.z));
                BakedQuadHelper.setNormalXYZ(transformedVertices, i, new Vec3(normalJoml.x, normalJoml.y, normalJoml.z));
                BakedQuadHelper.setU(transformedVertices, i, uvX);
                BakedQuadHelper.setV(transformedVertices, i, uvY);
            }

            final Direction newNormal = Direction.fromDelta(Math.round(quadNormalJoml.x), Math.round(quadNormalJoml.y), Math.round(quadNormalJoml.z));
            transformedQuads.add(new BakedQuad(transformedVertices,
                    quad.getTintIndex(),
                    newNormal,
                    newSprite,
                    false
            ));
        }
        return transformedQuads;
    }

    private static class HeadlampModelData {
        int[] activePlacements;

        public void setActivePlacements(final int[] activePlacements) {
            if (activePlacements.length != 9) {
                throw new IllegalArgumentException("Active placements array must have length " + this.activePlacements.length);
            }
            this.activePlacements = activePlacements;
        }

        public int[] getActivePlacements() {
            return activePlacements;
        }
    }

}
