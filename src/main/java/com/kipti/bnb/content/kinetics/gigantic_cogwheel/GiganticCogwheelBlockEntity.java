package com.kipti.bnb.content.kinetics.gigantic_cogwheel;

import com.simibubi.create.Create;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class GiganticCogwheelBlockEntity extends KineticBlockEntity {

    public GiganticCogwheelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        setLazyTickRate(3); // check frequently to catch newly placed small cogs
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        Create.LOGGER.info("[GiganticCog] lazyTick fired at {}", worldPosition);
        if (level == null || level.isClientSide) return;
        BlockState state = getBlockState();
        if (!(state.getBlock() instanceof GiganticCogwheelBlock gigantic)) return;
        Direction.Axis axis = gigantic.getRotationAxis(state);
        for (Direction dir : Direction.values()) {
            if (dir.getAxis() == axis) continue;
            BlockPos targetPos = worldPosition.relative(dir, 3);
            BlockState targetState = level.getBlockState(targetPos);
            if (!(targetState.getBlock() instanceof ICogWheel cog)) continue;
            if (cog.isLargeCog()) continue;
            if (cog.getRotationAxis(targetState) != axis) continue;
            BlockEntity be = level.getBlockEntity(targetPos);
            Create.LOGGER.info("[GiganticCog] Found small cog at {} hasSource={}", targetPos, be instanceof KineticBlockEntity kbe2 ? kbe2.hasSource() : "notKBE");
            if (be instanceof KineticBlockEntity kbe) {
                if (kbe.hasSource() && !this.hasSource()) {
                    Create.LOGGER.info("[GiganticCog] Small cog has source but we don't — nudging small cog");
                    kbe.detachKinetics();
                    kbe.updateSpeed = true;
                    kbe.attachKinetics();
                    return;
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Add the 3-blocks-away small cog positions to THIS block's scan list.
    // This handles: when the gigantic cog is placed near an existing small cog,
    // OR when the gigantic cog is the source propagating outward.
    // -------------------------------------------------------------------------

    @Override
    public List<BlockPos> addPropagationLocations(IRotate block, BlockState state, List<BlockPos> neighbours) {
        List<BlockPos> result = super.addPropagationLocations(block, state, neighbours);

        Create.LOGGER.info("[GiganticCog] addPropagationLocations called on {}", worldPosition);

        if (!(block instanceof GiganticCogwheelBlock gigantic) || level == null)
            return result;

        Direction.Axis axis = gigantic.getRotationAxis(state);

        for (Direction dir : Direction.values()) {
            if (dir.getAxis() == axis) continue;

            BlockPos targetPos = worldPosition.relative(dir, 3);
            BlockState targetState = level.getBlockState(targetPos);
            Create.LOGGER.info("[GiganticCog] Checking {} (axis={}) found={}", targetPos, axis, targetState.getBlock());

            if (!(targetState.getBlock() instanceof ICogWheel cog)) continue;
            if (cog.isLargeCog()) continue;
            if (cog.getRotationAxis(targetState) != axis) continue;

            if (!result.contains(targetPos)) {
                Create.LOGGER.info("[GiganticCog] Adding small cog at {} to propagation list", targetPos);
                result.add(targetPos);
            }
        }

        return result;
    }

    // -------------------------------------------------------------------------
    // isCustomConnection — called by RotationPropagator.isConnected() from
    // BOTH directions. Returning true here means the propagator will call
    // getRotationSpeedModifier (which calls propagateRotationTo) for this pair.
    // This also handles the case where the small cog is the one being scanned
    // and finds the gigantic cog in ITS neighbour list via this symmetric check.
    // -------------------------------------------------------------------------

    @Override
    public boolean isCustomConnection(KineticBlockEntity other, BlockState state, BlockState otherState) {
        Create.LOGGER.info("[GiganticCog] isCustomConnection called: from={} to={}", state.getBlock(), otherState.getBlock());
        // Case 1: we are the gigantic cog, other is a small cog
        if (state.getBlock() instanceof GiganticCogwheelBlock gigantic
                && otherState.getBlock() instanceof ICogWheel cog
                && !cog.isLargeCog()) {

            Direction.Axis axis = gigantic.getRotationAxis(state);
            if (cog.getRotationAxis(otherState) != axis) return false;

            BlockPos diff = other.getBlockPos().subtract(worldPosition);
            return isValidDiff(axis, diff);
        }

        // Case 2: we are something else, other is the gigantic cog
        // (symmetric — RotationPropagator checks isConnected both ways)
        if (otherState.getBlock() instanceof GiganticCogwheelBlock gigantic
                && state.getBlock() instanceof ICogWheel cog
                && !cog.isLargeCog()) {

            Direction.Axis axis = gigantic.getRotationAxis(otherState);
            if (cog.getRotationAxis(state) != axis) return false;

            BlockPos diff = worldPosition.subtract(other.getBlockPos());
            return isValidDiff(axis, diff);
        }

        return false;
    }

    // -------------------------------------------------------------------------
    // propagateRotationTo — the actual ratio. Called by getRotationSpeedModifier
    // which is called inside isConnected and also during propagation itself.
    // -------------------------------------------------------------------------

    @Override
    public float propagateRotationTo(KineticBlockEntity target,
                                     BlockState stateFrom, BlockState stateTo,
                                     BlockPos diff,
                                     boolean connectedViaAxes, boolean connectedViaCogs) {

        Create.LOGGER.info("[GiganticCog] propagateRotationTo called: from={} to={} diff={}", stateFrom.getBlock(), stateTo.getBlock(), diff);

        // Case 1: Gigantic -> Small (5x speed up)
        if (stateFrom.getBlock() instanceof GiganticCogwheelBlock gigantic
                && stateTo.getBlock() instanceof ICogWheel cog
                && !cog.isLargeCog()) {

            Direction.Axis axis = gigantic.getRotationAxis(stateFrom);
            if (cog.getRotationAxis(stateTo) != axis) return 0;
            if (!isValidDiff(axis, diff)) return 0;

            return -5.0f;
        }

        // Case 2: Small -> Gigantic (1/5 speed down)
        if (stateFrom.getBlock() instanceof ICogWheel cog
                && !cog.isLargeCog()
                && stateTo.getBlock() instanceof GiganticCogwheelBlock gigantic) {

            Direction.Axis axis = gigantic.getRotationAxis(stateTo);
            if (cog.getRotationAxis(stateFrom) != axis) return 0;
            if (!isValidDiff(axis, diff)) return 0;

            return -1f / 5;
        }

        return 0;
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private static boolean isValidDiff(Direction.Axis axis, BlockPos diff) {
        // The small cog must be exactly 3 blocks away perpendicular to the rotation axis
        return switch (axis) {
            case X -> diff.getX() == 0 && (Math.abs(diff.getY()) == 3 && diff.getZ() == 0 || Math.abs(diff.getZ()) == 3 && diff.getY() == 0);
            case Y -> diff.getY() == 0 && (Math.abs(diff.getX()) == 3 && diff.getZ() == 0 || Math.abs(diff.getZ()) == 3 && diff.getX() == 0);
            case Z -> diff.getZ() == 0 && (Math.abs(diff.getX()) == 3 && diff.getY() == 0 || Math.abs(diff.getY()) == 3 && diff.getX() == 0);
        };
    }

    // -------------------------------------------------------------------------
    // Expand render bounding box to cover the full 3x3 footprint
    // -------------------------------------------------------------------------

    @Override
    protected AABB createRenderBoundingBox() {
        return new AABB(worldPosition).inflate(1.5);
    }
}