package com.kipti.bnb.content.decoration.girder_strut.structure;

import com.cake.azimuth.utility.ChunkedMap;
import com.kipti.bnb.foundation.caching.LevelSafeStorage;
import com.kipti.bnb.registry.content.blocks.deco.BnbDecorativeBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.*;

/**
 * Handler and storage for all the connections between girder struts in a level.
 */
public class GirderStrutStructureShapes {

    private static final LevelSafeStorage<ShapeRegistry> STORAGE = new LevelSafeStorage<>(ShapeRegistry::new);

    public record ConnectionKey(BlockPos a, BlockPos b) {
        public ConnectionKey {
            if (a.compareTo(b) > 0) {
                final BlockPos temp = a;
                a = b;
                b = temp;
            }
        }
    }

    public static void registerConnection(final Level level, final BlockPos from, final Direction fromFacing, final BlockPos to, final Direction toFacing, final com.kipti.bnb.content.decoration.girder_strut.StrutModelType modelType) {
        STORAGE.getForLevel(level).registerConnection(level, from, fromFacing, to, toFacing, modelType);
    }

    public static void unregisterConnection(final Level level, final BlockPos a, final BlockPos b) {
        STORAGE.getForLevel(level).unregisterConnection(level, new ConnectionKey(a, b));
    }

    public static VoxelShape getShape(final Level level, final BlockPos pos) {
        return STORAGE.getForLevel(level).getShape(pos);
    }

    public static Set<ConnectionKey> getConnectionsAt(final Level level, final BlockPos pos) {
        return STORAGE.getForLevel(level).getConnectionsAt(pos);
    }

    public static void removePositionData(final Level level, final BlockPos pos) {
        STORAGE.getForLevel(level).removePositionData(pos);
    }

    public static boolean hasPositionData(final Level level, final BlockPos pos) {
        return STORAGE.getForLevel(level).hasPositionData(pos);
    }

    public static boolean placeStructureBlockIfPossible(final Level level, final BlockPos pos) {
        final BlockState currentState = level.getBlockState(pos);
        if (currentState.getBlock() == BnbDecorativeBlocks.GIRDER_STRUT_STRUCTURE.get()) {
            return true;
        }
        if (!currentState.canBeReplaced()) {
            return false;
        }

        final FluidState fluidState = level.getFluidState(pos);
        final boolean waterlogged = fluidState.getType() == Fluids.WATER;
        BlockState structureState = BnbDecorativeBlocks.GIRDER_STRUT_STRUCTURE.getDefaultState();
        if (structureState.hasProperty(BlockStateProperties.WATERLOGGED)) {
            structureState = structureState.setValue(BlockStateProperties.WATERLOGGED, waterlogged);
        }

        return level.setBlock(pos, structureState, Block.UPDATE_ALL);
    }

    private static class ShapeRegistry {
        private final ChunkedMap<GirderStrutConnectionShape> connectionsByChunk;
        private final Map<ConnectionKey, GirderStrutConnectionShape> connectionsByKey = new HashMap<>();
        private final Map<BlockPos, PositionData> shapesByPosition = new HashMap<>();

        private ShapeRegistry(final Level level) {
            this.connectionsByChunk = new ChunkedMap<>(level) {
                @Override
                protected void onChunkEvicted(final ChunkPos chunk, final List<GirderStrutConnectionShape> evictedObjects) {
                    for (final GirderStrutConnectionShape connectionShape : evictedObjects) {
                        for (final BlockPos position : connectionShape.geometry().getPositions()) {
                            if (new ChunkPos(position).equals(chunk)) {
                                final PositionData positionData = shapesByPosition.get(position);
                                if (positionData != null) {
                                    positionData.remove(connectionShape.key());
                                    if (positionData.perConnectionShapes.isEmpty()) {
                                        shapesByPosition.remove(position);
                                    }
                                }
                            }
                        }
                    }
                }
            };
        }

        public void registerConnection(final Level level, final BlockPos from, final Direction fromFacing, final BlockPos to, final Direction toFacing, final com.kipti.bnb.content.decoration.girder_strut.StrutModelType modelType) {
            final ConnectionKey key = new ConnectionKey(from, to);
            if (connectionsByKey.containsKey(key)) {
                return;
            }

            final BlockyStrutLineGeometry geometry = new BlockyStrutLineGeometry(from, fromFacing, to, toFacing, modelType.shapeSizeXPixels(), modelType.shapeSizeYPixels());
            final Set<ChunkPos> chunks = new HashSet<>();
            for (final BlockPos pos : geometry.getPositions()) {
                chunks.add(new ChunkPos(pos));
            }
            final GirderStrutConnectionShape shapeObj = new GirderStrutConnectionShape(key, geometry, chunks.toArray(new ChunkPos[0]));
            connectionsByKey.put(key, shapeObj);
            connectionsByChunk.add(shapeObj);

            for (final BlockPos pos : geometry.getPositions()) {
                final VoxelShape shape = geometry.getShapeForPosition(pos);
                if (shape.isEmpty()) continue;

                final PositionData pd = shapesByPosition.computeIfAbsent(pos, p -> new PositionData());
                final boolean wasEmpty = pd.perConnectionShapes.isEmpty();
                pd.add(key, shape);

                // Only place structure blocks at non-endpoint positions
                if (!pos.equals(from) && !pos.equals(to) && wasEmpty) {
                    placeStructureBlockIfPossible(level, pos);
                }
            }
        }

        public void unregisterConnection(final Level level, final ConnectionKey key) {
            final GirderStrutConnectionShape found = connectionsByKey.remove(key);
            if (found == null) return;

            connectionsByChunk.remove(found);

            for (final BlockPos pos : found.geometry().getPositions()) {
                final PositionData pd = shapesByPosition.get(pos);
                if (pd != null) {
                    pd.remove(key);
                    if (pd.perConnectionShapes.isEmpty()) {
                        shapesByPosition.remove(pos);
                        // If it's our structure block, remove it
                        if (level.getBlockState(pos).getBlock() == BnbDecorativeBlocks.GIRDER_STRUT_STRUCTURE.get()) {
                            level.removeBlock(pos, false);
                        }
                    }
                }
            }
        }

        public VoxelShape getShape(final BlockPos pos) {
            final PositionData pd = shapesByPosition.get(pos);
            return pd == null ? Shapes.empty() : pd.mergedShape;
        }

        public Set<ConnectionKey> getConnectionsAt(final BlockPos pos) {
            final PositionData pd = shapesByPosition.get(pos);
            return pd == null ? Set.of() : Set.copyOf(pd.perConnectionShapes.keySet());
        }

        public void removePositionData(final BlockPos pos) {
            shapesByPosition.remove(pos);
        }

        public boolean hasPositionData(final BlockPos pos) {
            return shapesByPosition.containsKey(pos);
        }
    }

    private static class PositionData {
        private final Map<ConnectionKey, VoxelShape> perConnectionShapes = new HashMap<>();
        private VoxelShape mergedShape = Shapes.empty();

        public void add(final ConnectionKey key, final VoxelShape shape) {
            perConnectionShapes.put(key, shape);
            recompute();
        }

        public void remove(final ConnectionKey key) {
            perConnectionShapes.remove(key);
            recompute();
        }

        private void recompute() {
            mergedShape = Shapes.empty();
            for (final VoxelShape shape : perConnectionShapes.values()) {
                mergedShape = Shapes.or(mergedShape, shape);
            }
            mergedShape = mergedShape.optimize();
        }
    }

    private record GirderStrutConnectionShape(ConnectionKey key,
                                              BlockyStrutLineGeometry geometry,
                                              ChunkPos[] chunks) implements ChunkedMap.IChunkedObject {
        @Override
        public ChunkPos[] getChunks() {
            return chunks;
        }
    }
}
