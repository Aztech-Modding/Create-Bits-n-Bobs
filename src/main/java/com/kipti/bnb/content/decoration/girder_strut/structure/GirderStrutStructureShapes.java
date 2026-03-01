package com.kipti.bnb.content.decoration.girder_strut.structure;

import com.cake.azimuth.utility.ChunkedMap;
import com.kipti.bnb.content.decoration.girder_strut.GirderStrutBlockEntity;
import com.kipti.bnb.foundation.caching.LevelSafeStorage;
import net.minecraft.world.level.ChunkPos;

/**
 * Handler and storage for all the connections between girder struts in a level.
 * The connections are stored as pairs of block entities, and the structure blocks are generated based on these connections.
 */
public class GirderStrutStructureShapes {

    LevelSafeStorage<ChunkedMap<GirderStrutConnectionShape>> shapeStorage = new LevelSafeStorage<>(ChunkedMap::new);


    private record GirderStrutConnectionShape(GirderStrutBlockEntity from,
                                              GirderStrutBlockEntity to,
                                              BlockyStrutLineGeometry geometry) implements ChunkedMap.IChunkedObject {
        @Override
        public ChunkPos[] getChunks() {
            return new ChunkPos[0];
        }
    }
}
