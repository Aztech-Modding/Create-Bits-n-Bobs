package com.kipti.bnb.content.decoration.girder_strut;

import com.kipti.bnb.content.decoration.girder_strut.cap.GirderCapAccumulator;
import com.kipti.bnb.content.decoration.girder_strut.geometry.GirderGeometry;
import com.kipti.bnb.content.decoration.girder_strut.mesh.GirderMeshQuad;
import com.kipti.bnb.content.decoration.girder_strut.mesh.GirderSegmentMesh;
import com.kipti.bnb.registry.BnbBlocks;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

@OnlyIn(Dist.CLIENT)
final class GirderStrutModelManipulator {

    private static Map<StrutModelType, GirderSegmentMesh> segmentMeshes = new HashMap<>();

    private GirderStrutModelManipulator() {
    }

//    static List<BakedQuad> bakeConnection(GirderStrutModelBuilder.GirderConnection connection) {
//        if (connection.renderLength() <= GirderGeometry.EPSILON) {
//            return List.of();
//        }
//
//        GirderSegmentMesh mesh = getSegmentMesh();
//        List<GirderMeshQuad> quads = mesh.forLength((float) connection.renderLength());
//
//        Vec3 dir = connection.direction();
//        double distHorizontal = Math.sqrt(dir.x * dir.x + dir.z * dir.z);
//        float yRot = distHorizontal == 0 ? 0f : (float) Math.atan2(dir.x, dir.z);
//        float xRot = (float) Math.atan2(dir.y, distHorizontal);
//
//        PoseStack poseStack = new PoseStack();
//        poseStack.translate(connection.start().x, connection.start().y, connection.start().z);
//        poseStack.mulPose(new Quaternionf().rotationY(yRot));
//        poseStack.mulPose(new Quaternionf().rotationX(-xRot));
//        poseStack.translate(-0.5f, -0.5f, -0.5f);
//
//        PoseStack.Pose last = poseStack.last();
//        Matrix4f pose = new Matrix4f(last.pose());
//        Matrix3f normalMatrix = new Matrix3f(last.normal());
//
//        Vector3f planePoint = toVector3f(connection.surfacePlanePoint());
//        Vector3f planeNormal = toVector3f(connection.surfaceNormal());
//        if (planeNormal.lengthSquared() > GirderGeometry.EPSILON) {
//            planeNormal.normalize();
//        }
//
//        List<BakedQuad> bakedQuads = new ArrayList<>();
//        GirderCapAccumulator capAccumulator = new GirderCapAccumulator(INDUSTRIAL_IRON_BLOCK);
//        for (GirderMeshQuad quad : quads) {
//            quad.transformAndEmit(pose, normalMatrix, planePoint, planeNormal, capAccumulator, bakedQuads);
//        }
//        capAccumulator.emitCaps(planePoint, planeNormal, bakedQuads);
//        return bakedQuads;
//    }

    static List<Consumer<BufferBuilder>> bakeConnectionToConsumer(GirderStrutModelBuilder.GirderConnection connection, StrutModelType modelType, Function<Vector3f, Integer> lightFunction) {
        if (connection.renderLength() <= GirderGeometry.EPSILON) {
            return List.of();
        }

        GirderSegmentMesh mesh = getSegmentMesh(modelType);
        List<GirderMeshQuad> quads = mesh.forLength((float) connection.renderLength());

        Vec3 dir = connection.direction();
        double distHorizontal = Math.sqrt(dir.x * dir.x + dir.z * dir.z);
        float yRot = distHorizontal == 0 ? 0f : (float) Math.atan2(dir.x, dir.z);
        float xRot = (float) Math.atan2(dir.y, distHorizontal);

        PoseStack poseStack = new PoseStack();
        poseStack.translate(connection.start().x, connection.start().y, connection.start().z);
        poseStack.mulPose(new Quaternionf().rotationY(yRot));
        poseStack.mulPose(new Quaternionf().rotationX(-xRot));
        poseStack.translate(-0.5f, -0.5f, -0.5f);

        PoseStack.Pose last = poseStack.last();
        Matrix4f pose = new Matrix4f(last.pose());
        Matrix3f normalMatrix = new Matrix3f(last.normal());

        Vector3f planePoint = toVector3f(connection.surfacePlanePoint());
        Vector3f planeNormal = toVector3f(connection.surfaceNormal());
        if (planeNormal.lengthSquared() > GirderGeometry.EPSILON) {
            planeNormal.normalize();
        }

        List<Consumer<BufferBuilder>> quadConsumer = new ArrayList<>();
        GirderCapAccumulator capAccumulator = new GirderCapAccumulator(modelType.getCapTexture());
        for (GirderMeshQuad quad : quads) {
            quad.transformAndEmitToConsumer(pose, normalMatrix, planePoint, planeNormal, capAccumulator, quadConsumer, lightFunction);
        }
        capAccumulator.emitCapsToConsumer(planeNormal, quadConsumer, lightFunction);
        return quadConsumer;
    }

    private static GirderSegmentMesh getSegmentMesh(StrutModelType modelType) {
        GirderSegmentMesh girderSegmentMesh = segmentMeshes.get(modelType);
        if (girderSegmentMesh == null) {
            BakedModel bakedModel = modelType.getPartialModel().get();
            List<BakedQuad> bakedQuads = new ArrayList<>();
            RandomSource random = RandomSource.create();
            bakedQuads.addAll(bakedModel.getQuads(
                    BnbBlocks.GIRDER_STRUT.get().defaultBlockState(), //No affect on the quads, so ignore modelType
                    null,
                    random,
                    ModelData.EMPTY,
                    null
            ));
            segmentMeshes.put(modelType, girderSegmentMesh = new GirderSegmentMesh(bakedQuads));
        }
        return girderSegmentMesh;
    }

    private static Vector3f toVector3f(Vec3 vec) {
        return new Vector3f((float) vec.x, (float) vec.y, (float) vec.z);
    }
}
