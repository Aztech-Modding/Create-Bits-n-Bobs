package com.kipti.bnb.mixin_accessor;

import net.createmod.catnip.data.Couple;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public interface ArticulatedBezierConnection {

    @NotNull Couple<Float> articulate$getTilt();

    float articulate$getEndpointTilt(boolean first);

    float articulate$getTiltAt(double t);

    void articulate$setTilt(@NotNull Couple<Float> tilt);

    @NotNull Couple<Vec3> articulate$getBaseNormals();

    void articulate$setBaseNormals(@NotNull Couple<Vec3> baseNormals);

}
