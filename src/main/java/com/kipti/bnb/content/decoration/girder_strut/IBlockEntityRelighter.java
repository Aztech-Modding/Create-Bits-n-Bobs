package com.kipti.bnb.content.decoration.girder_strut;

import com.kipti.bnb.content.decoration.girder_strut.geometry.GirderGeometry;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public interface IBlockEntityRelighter {

    float ADJACENT_BLOCK_TOLERANCE = 0.3f;

    BlockAndTintGetter getLevel();

    BlockPos getBlockPos();

    default Function<Vector3f, Integer> createLighter() {
        return createLighter(getBlockPos());
    }

    default Function<Vector3f, Integer> createLighter(BlockPos blockPos) {
        return (position) -> {
            if (getLevel() == null) return GirderGeometry.DEFAULT_LIGHT;
            Matrix4f lightTransform = new Matrix4f().translate(blockPos.getX(), blockPos.getY(), blockPos.getZ());
            Vector3f lightPosition = lightTransform.transformPosition(position, new Vector3f());
            List<BlockPos> positions = getClosePositions(lightPosition.x, lightPosition.y, lightPosition.z);
            return positions
                    .stream()
                    .map(p -> LevelRenderer.getLightColor(getLevel(), p))
                    .reduce(0, IBlockEntityRelighter::maximizeLight);
        };
    }

    default Function<Vector3f, Integer> createGlobalLighter() {
        return (position) -> {
            if (getLevel() == null) return GirderGeometry.DEFAULT_LIGHT;
            List<BlockPos> positions = getClosePositions(position.x, position.y, position.z);
            return positions
                    .stream()
                    .map(p -> LevelRenderer.getLightColor(getLevel(), p))
                    .reduce(0, IBlockEntityRelighter::maximizeLight);
        };
    }

    static Function<Vector3f, Integer> createGlobalLighter(BlockEntity be) {
        return (position) -> {
            if (be.getLevel() == null) return GirderGeometry.DEFAULT_LIGHT;
            List<BlockPos> positions = getClosePositions(position.x, position.y, position.z);
            return positions
                    .stream()
                    .map(p -> LevelRenderer.getLightColor(be.getLevel(), p))
                    .reduce(0, IBlockEntityRelighter::maximizeLight);
        };
    }

    private static List<BlockPos> getClosePositions(float x, float y, float z) {
        float fx = x - Math.round(x);
        float fy = y - Math.round(y);
        float fz = z - Math.round(z);
        BlockPos base = new BlockPos((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
        List<BlockPos> positions = new ArrayList<>();
        positions.add(base);
        if (Math.abs(fx) < ADJACENT_BLOCK_TOLERANCE) {
            positions.add(base.relative(fx > 0 ? Direction.WEST : Direction.EAST));
        }
        if (Math.abs(fy) < ADJACENT_BLOCK_TOLERANCE) {
            positions.add(base.relative(fy > 0 ? Direction.DOWN : Direction.UP));
        }
        if (Math.abs(fz) < ADJACENT_BLOCK_TOLERANCE) {
            positions.add(base.relative(fz > 0 ? Direction.NORTH : Direction.SOUTH));
        }
        return positions;
    }

    static int maximizeLight(int lightA, int lightB) {
        int blockA = lightA & 0xFFFF;
        int skyA = (lightA >>> 16) & 0xFFFF;
        int blockB = lightB & 0xFFFF;
        int skyB = (lightB >>> 16) & 0xFFFF;
        int block = Math.max(blockA, blockB);
        int sky = Math.max(skyA, skyB);
        return (sky << 16) | block;
    }

}

