package com.kipti.bnb.mixin.articulate;

import com.simibubi.create.content.trains.track.BezierConnection;
import com.simibubi.create.content.trains.track.TrackPlacement;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TrackPlacement.PlacementInfo.class)
public interface TrackPlacementInfoAccessorMixin {

    @Accessor("curve")
    BezierConnection articulate$getCurve();

    @Accessor("curve")
    void articulate$setCurve(BezierConnection curve);

    @Accessor("valid")
    boolean articulate$isValid();

    @Accessor("valid")
    void articulate$setValid(boolean valid);

    @Accessor("message")
    String articulate$getMessage();

    @Accessor("message")
    void articulate$setMessage(String message);

    @Accessor("end1Extent")
    int articulate$getEnd1Extent();

    @Accessor("end2Extent")
    int articulate$getEnd2Extent();

    @Accessor("end1Extent")
    void articulate$setEnd1Extent(int extent);

    @Accessor("end2Extent")
    void articulate$setEnd2Extent(int extent);

    @Accessor("axis1")
    Vec3 articulate$getAxis1();

    @Accessor("axis2")
    Vec3 articulate$getAxis2();

    @Accessor("pos1")
    BlockPos articulate$getPos1();

    @Accessor("pos2")
    BlockPos articulate$getPos2();

    @Accessor("end1")
    Vec3 articulate$getEnd1();

    @Accessor("end2")
    Vec3 articulate$getEnd2();

    @Accessor("normal1")
    Vec3 articulate$getNormal1();

    @Accessor("normal2")
    Vec3 articulate$getNormal2();

}
