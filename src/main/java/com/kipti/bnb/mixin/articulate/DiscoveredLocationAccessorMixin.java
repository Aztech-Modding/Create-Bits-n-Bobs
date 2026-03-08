package com.kipti.bnb.mixin.articulate;

import com.simibubi.create.content.trains.graph.TrackNodeLocation;
import com.simibubi.create.content.trains.track.BezierConnection;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TrackNodeLocation.DiscoveredLocation.class)
public interface DiscoveredLocationAccessorMixin {

    @Accessor("normal")
    Vec3 articulate$getNormal();

    @Accessor("turn")
    @Nullable BezierConnection articulate$getTurn();

}
