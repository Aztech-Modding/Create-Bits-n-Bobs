package com.kipti.bnb.content.kinetics.cogwheel_chain.graph;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

public record PlacingCogwheelNode(BlockPos pos, Direction.Axis rotationAxis, boolean isLarge,
                                  boolean hasSmallCogwheelOffset) {

    public static final Codec<PlacingCogwheelNode> CODEC = RecordCodecBuilder.create(
            p_337946_ -> p_337946_.group(
                            BlockPos.CODEC.fieldOf("pos").forGetter(PlacingCogwheelNode::pos),
                            Direction.Axis.CODEC.fieldOf("rotationAxis").forGetter(PlacingCogwheelNode::rotationAxis),
                            Codec.BOOL.fieldOf("isLarge").forGetter(PlacingCogwheelNode::isLarge),
                            Codec.BOOL.fieldOf("hasSmallCogwheelOffset").forGetter(PlacingCogwheelNode::hasSmallCogwheelOffset)
                    )
                    .apply(p_337946_, PlacingCogwheelNode::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, PlacingCogwheelNode> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            PlacingCogwheelNode::pos,
            CatnipStreamCodecs.AXIS,
            PlacingCogwheelNode::rotationAxis,
            ByteBufCodecs.BOOL,
            PlacingCogwheelNode::isLarge,
            ByteBufCodecs.BOOL,
            PlacingCogwheelNode::hasSmallCogwheelOffset,
            PlacingCogwheelNode::new
    );

    public Vec3 center() {
        return this.pos.getCenter();
    }

    public Vec3 rotationAxisVec() {
        return Vec3.atLowerCornerOf(Direction.fromAxisAndDirection(this.rotationAxis, Direction.AxisDirection.POSITIVE).getNormal());
    }

}

