package com.kipti.bnb.content.decoration.girder_strut;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import net.neoforged.neoforge.common.util.TriState;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GirderStrutModelBuilder extends BakedModelWrapper<BakedModel> {

    private static final ModelProperty<GirderStrutModelData> GIRDER_PROPERTY = new ModelProperty<>();
    private static final double SURFACE_OFFSET = (6 / 16f) + 1e-3;

    public GirderStrutModelBuilder(BakedModel originalModel) {
        super(originalModel);
    }

    @Override
    public @NotNull ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData blockEntityData) {
        if (!(level.getBlockEntity(pos) instanceof GirderStrutBlockEntity blockEntity)) {
            return ModelData.EMPTY;
        }
        blockEntity.connectionRenderBufferCache = null; // Invalidate cache on model data request
//        GirderStrutModelData data = GirderStrutModelData.collect(level, pos, state, blockEntity);
        return ModelData.builder()
//            .with(GIRDER_PROPERTY, data)
                .build();
    }

    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand, ModelData data, RenderType renderType) {
        List<BakedQuad> base = new ArrayList<>(super.getQuads(state, side, rand, data, renderType));
//        if (renderType != null && renderType != RenderType.solid()) {
//            return base;
//        }
//        if (sideFactor != null) { //Fuck this shit took me way to long to figure out
//            return base;
//        }
//        if (!data.has(GIRDER_PROPERTY)) {
//            return base;
//        }
//        GirderStrutModelData girderData = data.get(GIRDER_PROPERTY);
//        if (girderData == null || girderData.connections().isEmpty()) {
//            return base;
//        }
//        for (GirderConnection connection : girderData.connections()) {
//            base.addAll(GirderStrutModelManipulator.bakeConnection(connection));
//        }
        return base;
    }

    @Override
    public TriState useAmbientOcclusion(BlockState state, ModelData data, RenderType renderType) {
        return TriState.FALSE;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean usesBlockLight() {
        return true;
    }

    static final class GirderStrutModelData {
        private final List<GirderConnection> connections;
        private final BlockPos pos;

        private GirderStrutModelData(List<GirderConnection> connections, BlockPos pos) {
            this.connections = connections;
            this.pos = pos;
        }

        public BlockPos getPos() {
            return pos;
        }

        List<GirderConnection> connections() {
            return connections;
        }

        static GirderStrutModelData collect(BlockAndTintGetter level, BlockPos pos, BlockState state, GirderStrutBlockEntity blockEntity) {
            if (!(state.getBlock() instanceof GirderStrutBlock)) {
                return new GirderStrutModelData(List.of(), pos);
            }
            Direction facing = state.getValue(GirderStrutBlock.FACING);
            Vec3 blockOrigin = Vec3.atLowerCornerOf(pos);
            Vec3 facePoint = Vec3.atCenterOf(pos).relative(facing, -10 / 16f - 1e-3);
            Vec3 thisSurface = Vec3.atCenterOf(pos).relative(facing, -SURFACE_OFFSET);

            List<GirderConnection> connections = new ArrayList<>();

            for (BlockPos otherPos : blockEntity.getConnectionsCopy()) {
                otherPos = otherPos.offset(pos);
                BlockState otherState = level.getBlockState(otherPos);
                if (!(otherState.getBlock() instanceof GirderStrutBlock)) {
                    continue;
                }
                Direction otherFacing = otherState.getValue(GirderStrutBlock.FACING);
                Vec3 otherSurface = Vec3.atCenterOf(otherPos).relative(otherFacing, -SURFACE_OFFSET);
                Vec3 span = otherSurface.subtract(thisSurface);
                if (span.lengthSqr() < 1.0e-4) {
                    continue;
                }
                Vec3 halfVector = span.scale(0.5);
                double renderLength = halfVector.length() + 0.5f;
                if (renderLength <= 1.0e-4) {
                    continue;
                }

                Vec3 direction = halfVector.normalize();
                Vec3 startLocal = thisSurface.subtract(blockOrigin);
                Vec3 planePointLocal = facePoint.subtract(blockOrigin);

                connections.add(new GirderConnection(
                        startLocal,
                        direction,
                        renderLength,
                        planePointLocal,
                        Vec3.atLowerCornerOf(facing.getNormal())
                ));
            }

            return new GirderStrutModelData(Collections.unmodifiableList(connections), pos);
        }
    }

    static final class GirderConnection {
        private final Vec3 start;
        private final Vec3 direction;
        private final double renderLength;
        private final Vec3 surfacePlanePoint;
        private final Vec3 surfaceNormal;

        GirderConnection(Vec3 start, Vec3 direction, double renderLength, Vec3 surfacePlanePoint, Vec3 surfaceNormal) {
            this.start = start;
            this.direction = direction;
            this.renderLength = renderLength;
            this.surfacePlanePoint = surfacePlanePoint;
            this.surfaceNormal = surfaceNormal;
        }

        Vec3 start() {
            return start;
        }

        Vec3 direction() {
            return direction;
        }

        double renderLength() {
            return renderLength;
        }

        Vec3 surfacePlanePoint() {
            return surfacePlanePoint;
        }

        Vec3 surfaceNormal() {
            return surfaceNormal;
        }
    }
}
