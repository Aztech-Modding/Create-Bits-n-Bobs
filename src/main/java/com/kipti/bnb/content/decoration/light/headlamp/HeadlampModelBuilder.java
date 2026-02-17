package com.kipti.bnb.content.decoration.light.headlamp;

import com.kipti.bnb.content.decoration.light.founation.LightBlock;
import com.kipti.bnb.registry.BnbPartialModels;
import com.simibubi.create.foundation.model.BakedQuadHelper;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import net.neoforged.neoforge.common.util.TriState;
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
    private static final int PLACEMENT_COUNT = 9;

    public HeadlampModelBuilder(final BakedModel originalModel) {
        super(originalModel);
    }

    @Override
    public @NotNull ModelData getModelData(final BlockAndTintGetter world, final BlockPos pos, final BlockState state, final ModelData blockEntityData) {
        final HeadlampModelData data = new HeadlampModelData();

        int[] activePlacements = new int[PLACEMENT_COUNT];

        if (world.getBlockEntity(pos) instanceof final HeadlampBlockEntity headlampBlockEntity) {
            activePlacements = headlampBlockEntity.getActivePlacements();
            data.setCcAddressingView(headlampBlockEntity.getCCLightAddressingView());
        }

        data.setActivePlacements(activePlacements);

        return ModelData.builder()
                .with(HEADLAMP_PROPERTY, data)
                .build();
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(final BlockState state, final Direction side, final RandomSource rand, final ModelData data, final RenderType renderType) {
        if (data.has(HEADLAMP_PROPERTY)) {
            final List<BakedQuad> model = new ArrayList<>(super.getQuads(state, side, rand, data, renderType));
            final HeadlampModelData headlampModelData = data.get(HEADLAMP_PROPERTY);
            if (headlampModelData == null) {
            return model;
            }
            final HeadlampBlockEntity.HeadlampPlacement[] placements = HeadlampBlockEntity.HeadlampPlacement.values();
            final int[] activePlacements = headlampModelData.getActivePlacements();
            final Direction facing = state.getValue(HeadlampBlock.FACING);
            for (int i = 0; i < placements.length; i++) {
            final HeadlampBlockEntity.HeadlampPlacement placement = placements[i];
            final int placementValue = activePlacements[i];
                final TriState ccAddressing = headlampModelData.getCcAddressingView() == null ? TriState.DEFAULT :
                        headlampModelData.getCcAddressingView().getCCAddressingForIndex(placement);

                if (placementValue != 0) {
                    final boolean shouldDisplayOn = ccAddressing == TriState.DEFAULT ? LightBlock.shouldUseOnLightModel(state) : ccAddressing == TriState.TRUE;
                final HeadlampRenderCache.QuadCacheKey cacheKey = new HeadlampRenderCache.QuadCacheKey(
                    facing,
                    placement.ordinal(),
                    placementValue,
                    shouldDisplayOn,
                    side,
                    renderType
                );
                model.addAll(HeadlampRenderCache.getOrCreateQuads(cacheKey, () -> transformQuadsForLamp(
                    (shouldDisplayOn ? BnbPartialModels.HEADLAMP_ON : BnbPartialModels.HEADLAMP_OFF).get()
                        .getQuads(state, side, rand, data, renderType),
                    HeadlampRenderCache.getTransform(facing, placement),
                    placementValue
                )));
                }
            }
            return model;
        }
        return Collections.emptyList();
    }

        private List<BakedQuad> transformQuadsForLamp(final List<BakedQuad> quads, final Matrix4f transform, final int placementValue) {
        @Nullable final DyeColor color = placementValue == 1 ? null : DyeColor.values()[Math.clamp(placementValue - 2, 0, DyeColor.values().length - 1)];
        final List<BakedQuad> transformedQuads = new ArrayList<>();
        for (final BakedQuad quad : quads) {
            final int[] vertices = quad.getVertices();
            final int[] transformedVertices = Arrays.copyOf(vertices, vertices.length);

            final TextureAtlasSprite oldSprite = quad.getSprite();
            final TextureAtlasSprite newSprite = HeadlampRenderCache.getTintedSprite(oldSprite, color);

            final Vec3 quadNormal = Vec3.atLowerCornerOf(quad.getDirection()
                    .getNormal());
            final Vector3f quadNormalJoml = transform.transformDirection((float) quadNormal.x, (float) quadNormal.y, (float) quadNormal.z, new Vector3f());

            final Vector3f vertexJoml = new Vector3f();
            final Vector3f normalJoml = new Vector3f();

            for (int i = 0; i < vertices.length / BakedQuadHelper.VERTEX_STRIDE; i++) {
                final Vec3 vertex = BakedQuadHelper.getXYZ(vertices, i);
                final Vec3 normal = BakedQuadHelper.getNormalXYZ(vertices, i);
                float uvX = BakedQuadHelper.getU(vertices, i);
                float uvY = BakedQuadHelper.getV(vertices, i);

                if (!oldSprite.equals(newSprite)) {
                    uvX = (uvX - oldSprite.getU0()) / (oldSprite.getU1() - oldSprite.getU0()) * (newSprite.getU1() - newSprite.getU0()) + newSprite.getU0();
                    uvY = (uvY - oldSprite.getV0()) / (oldSprite.getV1() - oldSprite.getV0()) * (newSprite.getV1() - newSprite.getV0()) + newSprite.getV0();
                }

                transform.transformPosition((float) vertex.x, (float) vertex.y, (float) vertex.z, vertexJoml);
                transform.transformDirection((float) normal.x, (float) normal.y, (float) normal.z, normalJoml);

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
        @Nullable CCLightAddressing.View ccAddressingView;

        public void setActivePlacements(final int[] activePlacements) {
            if (activePlacements.length != PLACEMENT_COUNT) {
                throw new IllegalArgumentException("Active placements array must have length " + PLACEMENT_COUNT);
            }
            this.activePlacements = activePlacements;
        }

        public void setCcAddressingView(@Nullable CCLightAddressing.View ccAddressingView) {
            this.ccAddressingView = ccAddressingView;
        }

        public int[] getActivePlacements() {
            return activePlacements;
        }

        public @Nullable CCLightAddressing.View getCcAddressingView() {
            return ccAddressingView;
        }
    }

}
