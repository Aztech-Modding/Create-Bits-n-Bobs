package com.kipti.bnb.content.kinetics.cogwheel_chain.shape;

import dev.ryanhcode.sable.companion.ClientSubLevelAccess;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ChainCoordinateSpace {

    private final BlockPos controllerPos;
    private final Vec3 controllerBase;
    private final @Nullable Pose3dc pose;

    private ChainCoordinateSpace(final BlockPos controllerPos, final @Nullable Pose3dc pose) {
        this.controllerPos = controllerPos;
        this.controllerBase = Vec3.atLowerCornerOf(controllerPos);
        this.pose = pose;
    }

    public static ChainCoordinateSpace forLogical(final Level level, final BlockPos controllerPos) {
        final SubLevelAccess access = SableCompanion.INSTANCE.getContaining(level, controllerPos);
        return new ChainCoordinateSpace(controllerPos, access == null ? null : access.logicalPose());
    }

    public static ChainCoordinateSpace forRender(final Level level, final BlockPos controllerPos) {
        final SubLevelAccess access = SableCompanion.INSTANCE.getContaining(level, controllerPos);
        final Pose3dc pose = access instanceof final ClientSubLevelAccess client
                ? client.renderPose()
                : access == null ? null : access.logicalPose();
        return new ChainCoordinateSpace(controllerPos, pose);
    }

    public BlockPos getControllerPos() {
        return this.controllerPos;
    }

    public Vec3 toLocal(final Vec3 worldPos) {
        final Vec3 plotPos = this.pose == null ? worldPos : this.pose.transformPositionInverse(worldPos);
        return plotPos.subtract(this.controllerBase);
    }

    public Vec3 toWorld(final Vec3 localPos) {
        final Vec3 plotPos = localPos.add(this.controllerBase);
        return this.pose == null ? plotPos : this.pose.transformPosition(plotPos);
    }
}
