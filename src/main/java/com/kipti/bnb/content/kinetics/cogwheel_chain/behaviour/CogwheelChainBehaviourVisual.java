package com.kipti.bnb.content.kinetics.cogwheel_chain.behaviour;

import com.cake.azimuth.behaviour.extensions.RenderedBehaviourExtension;
import com.cake.struts.content.IAntiClippedShadowLighter;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.CogwheelChain;
import com.kipti.bnb.content.kinetics.cogwheel_chain.render.ChainQuadBuilder;
import com.kipti.bnb.content.kinetics.cogwheel_chain.render.CogwheelChainRenderGeometryBuilder;
import com.kipti.bnb.content.kinetics.cogwheel_chain.render.CogwheelChainRenderGeometryBuilder.ChainSegment;
import com.kipti.bnb.content.kinetics.cogwheel_chain.types.CogwheelChainType;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.processing.burner.ScrollTransformedInstance;
import com.simibubi.create.foundation.render.AllInstanceTypes;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.vertex.MutableVertexList;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.material.Materials;
import dev.engine_room.flywheel.lib.material.SimpleMaterial;
import dev.engine_room.flywheel.lib.model.QuadMesh;
import dev.engine_room.flywheel.lib.model.SingleMeshModel;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.joml.Vector4fc;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

//TODO: fix light updates
public class CogwheelChainBehaviourVisual extends RenderedBehaviourExtension.BehaviourVisual {

    private final VisualizationContext context;
    private final KineticBlockEntity kineticBlockEntity;
    private final CogwheelChainBehaviour cogwheelChainBehaviour;

    @Nullable
    private ScrollTransformedInstance chainInstance;

    private int chainSignature = Integer.MIN_VALUE;
    private float textureSquish = 1;
    private float lastSpeedV = Float.NaN;
    private int lastPackedLight = Integer.MIN_VALUE;

    public CogwheelChainBehaviourVisual(final VisualizationContext context, final KineticBlockEntity kineticBlockEntity, final CogwheelChainBehaviour cogwheelChainBehaviour, final AbstractBlockEntityVisual<?> parentVisual) {
        super(parentVisual);
        this.context = context;
        this.kineticBlockEntity = kineticBlockEntity;
        this.cogwheelChainBehaviour = cogwheelChainBehaviour;

        rebuildMeshIfNeeded(true);
        updateLight(0);
        update(0);
    }

    @Override
    public void update(final float partialTick) {
        rebuildMeshIfNeeded(false);

        if (chainInstance == null) {
            return;
        }

        final float rotationsPerTick = cogwheelChainBehaviour.getChainRotationFactor() * kineticBlockEntity.getSpeed() / (60 * 20);
        final float speedV = (float) (Math.PI * 2 * rotationsPerTick * textureSquish);

        if (!Mth.equal(lastSpeedV, speedV)) {
            chainInstance.speed(0, speedV);
            chainInstance.scaleU = 0;
            chainInstance.scaleV = 1;
            chainInstance.offsetU = 0;
            chainInstance.offsetV = 0;
            chainInstance.diffU = 0;
            chainInstance.diffV = 0;
            chainInstance.setChanged();
            lastSpeedV = speedV;
        }
    }

    @Override
    public void updateLight(final float partialTick) {
        if (chainInstance == null || kineticBlockEntity.getLevel() == null) {
            return;
        }

        final int packedLight = LevelRenderer.getLightColor(kineticBlockEntity.getLevel(), kineticBlockEntity.getBlockPos());
        if (packedLight != lastPackedLight) {
            chainInstance.light(packedLight);
            chainInstance.setChanged();
            lastPackedLight = packedLight;
        }
    }

    @Override
    public void collectCrumblingInstances(final Consumer<Instance> consumer) {
        if (chainInstance != null) {
            consumer.accept(chainInstance);
        }
    }

    @Override
    public void delete() {
        deleteInstance();
    }

    private void rebuildMeshIfNeeded(final boolean force) {
        final CogwheelChain chain = cogwheelChainBehaviour.getControlledChain();
        if (chain == null) {
            deleteInstance();
            chainSignature = Integer.MIN_VALUE;
            return;
        }

        final CogwheelChainType chainType = chain.getChainType();
        final boolean flipInsideOutside = chainType.getRenderType().usesConsistentInsideOutside() && chain.shouldFlipInsideOutside();
        final int newSignature = Objects.hash(chain.hashCode(), chainType.getKey(), flipInsideOutside);
        if (!force && newSignature == chainSignature && chainInstance != null) {
            return;
        }

        final List<ChainSegment> segments = CogwheelChainRenderGeometryBuilder.buildSegments(chain, Vec3.ZERO);
        final double totalChainDistance = segments.stream().mapToDouble(ChainSegment::distance).sum();

        if (totalChainDistance <= 1e-4) {
            deleteInstance();
            chainSignature = newSignature;
            return;
        }

        textureSquish = (float) (Math.ceil(totalChainDistance) / totalChainDistance);
        final Function<Vector3f, Integer> lighter = IAntiClippedShadowLighter.createGlobalLighter(kineticBlockEntity);

        final CogwheelChainMesh mesh = new CogwheelChainMesh(
                segments,
                chainType,
                flipInsideOutside,
                textureSquish,
                kineticBlockEntity.getBlockPos().getX(),
                kineticBlockEntity.getBlockPos().getY(),
                kineticBlockEntity.getBlockPos().getZ(),
                lighter
        );
        // SQUARE shapes form a closed tube — enable backface culling to avoid rendering
        // the interior faces. CROSS shapes need both sides visible.
        final boolean isCross = chainType.getRenderType().getVertexShape() == CogwheelChainType.VertexShape.CROSS;
        final SimpleMaterial material = SimpleMaterial.builderOf(Materials.CUTOUT_MIPPED_BLOCK)
                .texture(chainType.getRenderTexture())
                .backfaceCulling(!isCross)
                .build();

        deleteInstance();

        chainInstance = context.instancerProvider()
                .instancer(AllInstanceTypes.SCROLLING_TRANSFORMED, new SingleMeshModel(mesh, material))
                .createInstance();
        chainInstance
                .setIdentityTransform()
                .translate(getVisualPosition().getX(), getVisualPosition().getY(), getVisualPosition().getZ());

        chainInstance.overlay(OverlayTexture.NO_OVERLAY);
        chainInstance.setChanged();
        chainSignature = newSignature;
        lastSpeedV = Float.NaN;
        lastPackedLight = Integer.MIN_VALUE;
    }

    private void deleteInstance() {
        if (chainInstance != null) {
            chainInstance.delete();
            chainInstance = null;
        }
    }

    private static final class CogwheelChainMesh implements QuadMesh {

        private final List<Vertex> vertices;
        private final Vector4fc boundingSphere;

        private CogwheelChainMesh(final List<ChainSegment> segments,
                                  final CogwheelChainType type,
                                  final boolean flipInsideOutside,
                                  final float textureSquish,
                                  final int baseX,
                                  final int baseY,
                                  final int baseZ,
                                  final Function<Vector3f, Integer> lighter) {
            final ArrayList<Vertex> builtVertices = new ArrayList<>();
            final Bounds bounds = new Bounds();
            final CogwheelChainType.ChainRenderInfo chainRenderInfo = type.getRenderType();

            for (final ChainSegment segment : segments) {
                List<Vec3> destinationPoints = CogwheelChainRenderGeometryBuilder.getEndPointsForChainJoint(
                        segment.from(),
                        segment.to(),
                        segment.postTo(),
                        chainRenderInfo,
                        segment.toCogwheelAxis()
                );
                final List<Vec3> sourcePoints = CogwheelChainRenderGeometryBuilder.getEndPointsForChainJoint(
                        segment.preFrom(),
                        segment.from(),
                        segment.to(),
                        chainRenderInfo,
                        segment.fromCogwheelAxis()
                );

                destinationPoints = CogwheelChainRenderGeometryBuilder.getPointsInClosestOrder(destinationPoints, sourcePoints);

                final float minV = (float) (segment.uvStart() * textureSquish);
                final float maxV = (float) ((segment.uvStart() + segment.distance()) * textureSquish);

                ChainQuadBuilder.buildSegmentFaces(destinationPoints, sourcePoints, chainRenderInfo, minV, maxV, flipInsideOutside,
                        (x, y, z, u, v, nx, ny, nz) -> {
                            final int light = lighter.apply(new Vector3f(x + baseX, y + baseY, z + baseZ));
                            builtVertices.add(new Vertex(x, y, z, u, v, nx, ny, nz, light));
                            bounds.include(x, y, z);
                        });
            }

            this.vertices = builtVertices;
            this.boundingSphere = bounds.toBoundingSphere(builtVertices);
        }

        @Override
        public int vertexCount() {
            return vertices.size();
        }

        @Override
        public void write(final MutableVertexList vertexList) {
            for (int i = 0; i < vertices.size(); i++) {
                final Vertex vertex = vertices.get(i);
                vertexList.x(i, vertex.x);
                vertexList.y(i, vertex.y);
                vertexList.z(i, vertex.z);
                vertexList.u(i, vertex.u);
                vertexList.v(i, vertex.v);
                vertexList.normalX(i, 0);
                vertexList.normalY(i, 1);
                vertexList.normalZ(i, 0);
                vertexList.r(i, 1);
                vertexList.g(i, 1);
                vertexList.b(i, 1);
                vertexList.a(i, 1);
                vertexList.light(i, vertex.light);
                vertexList.overlay(i, OverlayTexture.NO_OVERLAY);
            }
        }

        @Override
        public Vector4fc boundingSphere() {
            return boundingSphere;
        }

        private record Vertex(float x, float y, float z, float u, float v, float nx, float ny, float nz, int light) {
        }

        private static final class Bounds {
            private float minX = Float.POSITIVE_INFINITY;
            private float minY = Float.POSITIVE_INFINITY;
            private float minZ = Float.POSITIVE_INFINITY;
            private float maxX = Float.NEGATIVE_INFINITY;
            private float maxY = Float.NEGATIVE_INFINITY;
            private float maxZ = Float.NEGATIVE_INFINITY;

            void include(final float x, final float y, final float z) {
                if (x < minX) minX = x;
                if (y < minY) minY = y;
                if (z < minZ) minZ = z;
                if (x > maxX) maxX = x;
                if (y > maxY) maxY = y;
                if (z > maxZ) maxZ = z;
            }

            Vector4fc toBoundingSphere(final List<Vertex> vertices) {
                if (vertices.isEmpty()) {
                    return new Vector4f(0, 0, 0, 0.01f);
                }

                final float cx = (minX + maxX) * 0.5f;
                final float cy = (minY + maxY) * 0.5f;
                final float cz = (minZ + maxZ) * 0.5f;
                float r2 = 0;
                for (final Vertex vertex : vertices) {
                    final float dx = vertex.x - cx;
                    final float dy = vertex.y - cy;
                    final float dz = vertex.z - cz;
                    r2 = Math.max(r2, dx * dx + dy * dy + dz * dz);
                }

                return new Vector4f(cx, cy, cz, (float) Math.sqrt(r2));
            }
        }
    }
}
