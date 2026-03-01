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
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ApiStatus.ScheduledForRemoval
public class GirderStrutModelBuilder extends BakedModelWrapper<BakedModel> {

    private static final ModelProperty<GirderStrutModelData> GIRDER_PROPERTY = new ModelProperty<>();
    private static final double SURFACE_OFFSET = (6 / 16f) + 1e-3;

    public GirderStrutModelBuilder(final BakedModel originalModel) {
        super(originalModel);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return false;
    }

    @Override
    public TriState useAmbientOcclusion(final BlockState state, final ModelData data, final RenderType renderType) {
        return TriState.FALSE;
    }

    @Override
    public boolean usesBlockLight() {
        return true;
    }

    @Override
    public List<BakedQuad> getQuads(final BlockState state, final Direction side, final RandomSource rand, final ModelData data, final RenderType renderType) {
        final List<BakedQuad> base = new ArrayList<>(super.getQuads(state, side, rand, data, renderType));
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
    public @NotNull ModelData getModelData(final BlockAndTintGetter level, final BlockPos pos, final BlockState state, final ModelData blockEntityData) {
        if (!(level.getBlockEntity(pos) instanceof final GirderStrutBlockEntity blockEntity)) {
            return ModelData.EMPTY;
        }
        blockEntity.connectionRenderBufferCache = null; // Invalidate cache on model data request
//        GirderStrutModelData data = GirderStrutModelData.collect(level, pos, state, blockEntity);
        return ModelData.builder()
//            .with(GIRDER_PROPERTY, data)
                .build();
    }

    static final class GirderStrutModelData {
        private final List<GirderConnection> connections;
        private final BlockPos pos;

        private GirderStrutModelData(final List<GirderConnection> connections, final BlockPos pos) {
            this.connections = connections;
            this.pos = pos;
        }

        static GirderStrutModelData collect(final BlockAndTintGetter level, final BlockPos pos, final BlockState state, final GirderStrutBlockEntity blockEntity) {
            if (!(state.getBlock() instanceof GirderStrutBlock)) {
                return new GirderStrutModelData(List.of(), pos);
            }
            final Direction facing = state.getValue(GirderStrutBlock.FACING);
            final Vec3 blockOrigin = Vec3.atLowerCornerOf(pos);
            final Vec3 facePoint = Vec3.atCenterOf(pos).relative(facing, -10 / 16f - 1e-3);
            final Vec3 thisSurface = Vec3.atCenterOf(pos).relative(facing, -SURFACE_OFFSET);

            final List<GirderConnection> connections = new ArrayList<>();

            for (BlockPos otherPos : blockEntity.getConnectionsCopy()) {
                otherPos = otherPos.offset(pos);
                final BlockState otherState = level.getBlockState(otherPos);
                if (!(otherState.getBlock() instanceof GirderStrutBlock)) {
                    continue;
                }
                final Direction otherFacing = otherState.getValue(GirderStrutBlock.FACING);
                final Vec3 otherSurface = Vec3.atCenterOf(otherPos).relative(otherFacing, -SURFACE_OFFSET);
                final Vec3 span = otherSurface.subtract(thisSurface);
                if (span.lengthSqr() < 1.0e-4) {
                    continue;
                }
                final Vec3 halfVector = span.scale(0.5);
                final double renderLength = halfVector.length() + 0.5f;
                if (renderLength <= 1.0e-4) {
                    continue;
                }

                final Vec3 direction = halfVector.normalize();
                final Vec3 startLocal = thisSurface.subtract(blockOrigin);
                final Vec3 planePointLocal = facePoint.subtract(blockOrigin);

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

        public BlockPos getPos() {
            return pos;
        }

        List<GirderConnection> connections() {
            return connections;
        }
    }

    static final class GirderConnection {
        private final Vec3 start;
        private final Vec3 direction;
        private final double renderLength;
        private final Vec3 surfacePlanePoint;
        private final Vec3 surfaceNormal;

        GirderConnection(final Vec3 start, final Vec3 direction, final double renderLength, final Vec3 surfacePlanePoint, final Vec3 surfaceNormal) {
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
