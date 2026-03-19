package com.kipti.bnb.content.kinetics.cogwheel_chain.segment;

import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.RenderedChainPathNode;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Constructs a {@link List} of {@link CogwheelChainSegment} from rendered chain path nodes.
 * <p>
 * Consecutive path nodes sharing the same {@code relativePos} (i.e., on the same cogwheel block)
 * produce {@link CogwheelChainSegment.SegmentType#NODE_ARC NODE_ARC} segments, while consecutive
 * nodes with differing positions produce
 * {@link CogwheelChainSegment.SegmentType#BETWEEN_NODES BETWEEN_NODES} segments.
 */
public final class CogwheelChainSegmentBuilder {

    private CogwheelChainSegmentBuilder() {
    }

    /**
     * Builds a segment list from the given rendered path nodes.
     *
     * @param renderedNodes the ordered rendered chain path nodes (looping chain)
     * @return an unmodifiable list of segments covering the entire chain path
     */
    public static List<CogwheelChainSegment> buildSegments(final List<RenderedChainPathNode> renderedNodes) {
        if (renderedNodes.size() < 2) {
            return Collections.emptyList();
        }

        final int size = renderedNodes.size();
        final List<CogwheelChainSegment> segments = new ArrayList<>(size);
        float cumulativeDist = 0f;

        for (int i = 0; i < size; i++) {
            final RenderedChainPathNode nodeA = renderedNodes.get(i);
            final RenderedChainPathNode nodeB = renderedNodes.get((i + 1) % size);

            final Vec3 posA = nodeA.getPosition();
            final Vec3 posB = nodeB.getPosition();

            final CogwheelChainSegment.SegmentType segmentType = determineSegmentType(nodeA, nodeB);
            final float segmentLength = (float) posA.distanceTo(posB);

            segments.add(new CogwheelChainSegment(
                    posA, posB, segmentType,
                    cumulativeDist, cumulativeDist + segmentLength
            ));

            cumulativeDist += segmentLength;
        }

        return Collections.unmodifiableList(segments);
    }

    private static CogwheelChainSegment.SegmentType determineSegmentType(final RenderedChainPathNode nodeA,
                                                                         final RenderedChainPathNode nodeB) {
        if (nodeA.relativePos().equals(nodeB.relativePos())) {
            return CogwheelChainSegment.SegmentType.NODE_ARC;
        }
        return CogwheelChainSegment.SegmentType.BETWEEN_NODES;
    }
}
