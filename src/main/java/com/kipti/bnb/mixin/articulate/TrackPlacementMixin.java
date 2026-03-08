package com.kipti.bnb.mixin.articulate;

import com.kipti.bnb.content.articulate.ArticulatedTrackBehaviour;
import com.kipti.bnb.content.articulate.ArticulatedTrackUtils;
import com.kipti.bnb.mixin_accessor.ArticulatedBezierConnection;
import com.kipti.bnb.registry.core.BnbDataComponents;
import com.simibubi.create.content.trains.track.*;
import net.createmod.catnip.data.Couple;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Mixin(TrackPlacement.class)
public abstract class TrackPlacementMixin {

    @Unique
    private static final ThreadLocal<Float> articulate$heldTilt = ThreadLocal.withInitial(() -> 0f);

    @Unique
    private static final ThreadLocal<Boolean> articulate$heldGirder = ThreadLocal.withInitial(() -> false);

    @Inject(method = "tryConnect", at = @At("HEAD"))
    private static void articulate$captureHeldTilt(final Level level, final Player player, final BlockPos pos2, final BlockState state2,
                                                   final ItemStack stack, final boolean girder, final boolean maximiseTurn,
                                                   final CallbackInfoReturnable<TrackPlacement.PlacementInfo> cir) {
        final float rawTilt = stack.getOrDefault(BnbDataComponents.TRACK_TILT, 0f);
        articulate$heldTilt.set(ArticulatedTrackUtils.isValidTilt(rawTilt) ? rawTilt : ArticulatedTrackUtils.snapToNearest(rawTilt));
        articulate$heldGirder.set(girder);
    }

    @Inject(method = "tryConnect", at = @At("RETURN"))
    private static void articulate$clearHeldTilt(final Level level, final Player player, final BlockPos pos2, final BlockState state2,
                                                 final ItemStack stack, final boolean girder, final boolean maximiseTurn,
                                                 final CallbackInfoReturnable<TrackPlacement.PlacementInfo> cir) {
        articulate$heldTilt.remove();
        articulate$heldGirder.remove();
    }

    @Inject(method = "placeTracks", at = @At("HEAD"), cancellable = true)
    private static void articulate$prepareTiltData(final Level level, final TrackPlacement.PlacementInfo info, final BlockState state1,
                                                   final BlockState state2, final BlockPos targetPos1, final BlockPos targetPos2,
                                                   final boolean simulate,
                                                   final CallbackInfoReturnable<TrackPlacement.PlacementInfo> cir) {
        final TrackPlacementInfoAccessorMixin access = (TrackPlacementInfoAccessorMixin) info;
        if (!access.articulate$isValid()) {
            cir.setReturnValue(info);
            return;
        }

        final float startTilt = articulate$resolveStartTilt(level, access.articulate$getPos1());
        final float endTilt = articulate$resolveEndTilt(level, access.articulate$getPos2());
        if (Float.compare(startTilt, 0f) == 0 && Float.compare(endTilt, 0f) == 0) {
            return;
        }

        // Normalize tilt signs relative to canonical axis direction
        // This ensures +15° always means the same physical rotation direction
        final float normalizedStartTilt = ArticulatedTrackUtils.tiltForCanonicalAxis(startTilt, access.articulate$getAxis1());
        final float normalizedEndTilt = ArticulatedTrackUtils.tiltForCanonicalAxis(endTilt, access.articulate$getAxis2());

        if (articulate$hasTiltedJunction(level, access.articulate$getPos1(), normalizedStartTilt)
                || articulate$hasTiltedJunction(level, access.articulate$getPos2(), normalizedEndTilt)) {
            access.articulate$setValid(false);
            access.articulate$setMessage("track.turn_start");
            cir.setReturnValue(info);
            return;
        }

        final double totalLength = articulate$getConnectionLength(access);
        if (!ArticulatedTrackUtils.isValidTiltTransition(normalizedStartTilt, normalizedEndTilt, (float) totalLength)) {
            access.articulate$setValid(false);
            access.articulate$setMessage("track.too_steep");
            cir.setReturnValue(info);
            return;
        }

        final BezierConnection curve = access.articulate$getCurve();
        if (curve != null) {
            // Real curve (not straight) -- set tilt on it and let normal placeTracks handle the rest
            final double curveStartDistance = access.articulate$getEnd1Extent() * access.articulate$getAxis1().length();
            final double curveEndDistance = totalLength - access.articulate$getEnd2Extent() * access.articulate$getAxis2().length();
            final float curveStartTilt = totalLength <= 0d
                    ? normalizedStartTilt
                    : ArticulatedTrackUtils.interpolateTilt((float) Mth.clamp(curveStartDistance / totalLength, 0.0d, 1.0d), normalizedStartTilt, normalizedEndTilt);
            final float curveEndTilt = totalLength <= 0d
                    ? normalizedEndTilt
                    : ArticulatedTrackUtils.interpolateTilt((float) Mth.clamp(curveEndDistance / totalLength, 0.0d, 1.0d), normalizedStartTilt, normalizedEndTilt);
            ((ArticulatedBezierConnection) curve).articulate$setTilt(Couple.create(curveStartTilt, curveEndTilt));
            return;
        }

        // Straight segment with non-zero tilt: create degenerate BezierConnection
        // Tilt values are already normalized relative to canonical axis direction
        articulate$handleTiltedStraightPlacement(level, info, state1, state2, simulate, normalizedStartTilt, normalizedEndTilt, totalLength);
        cir.setReturnValue(info);
    }

    @Inject(method = "placeTracks", at = @At("RETURN"))
    private static void articulate$applyPlacedTilts(final Level level, final TrackPlacement.PlacementInfo info, final BlockState state1,
                                                    final BlockState state2, final BlockPos targetPos1, final BlockPos targetPos2,
                                                    final boolean simulate,
                                                    final CallbackInfoReturnable<TrackPlacement.PlacementInfo> cir) {
        if (simulate || level.isClientSide) {
            return;
        }

        final TrackPlacementInfoAccessorMixin access = (TrackPlacementInfoAccessorMixin) info;
        if (!access.articulate$isValid()) {
            return;
        }

        final float startTilt = articulate$resolveStartTilt(level, access.articulate$getPos1());
        final float endTilt = articulate$resolveEndTilt(level, access.articulate$getPos2());
        if (Float.compare(startTilt, 0f) == 0 && Float.compare(endTilt, 0f) == 0) {
            return;
        }

        // Normalize tilt signs relative to canonical axis direction
        final float normalizedStartTilt = ArticulatedTrackUtils.tiltForCanonicalAxis(startTilt, access.articulate$getAxis1());
        final float normalizedEndTilt = ArticulatedTrackUtils.tiltForCanonicalAxis(endTilt, access.articulate$getAxis2());

        final double totalLength = articulate$getConnectionLength(access);
        final Map<BlockPos, Double> blockDistances = articulate$collectPlacedBlockDistances(access, totalLength);
        final Set<BlockPos> changedPositions = new HashSet<>();

        for (final Map.Entry<BlockPos, Double> entry : blockDistances.entrySet()) {
            final BlockPos blockPos = entry.getKey();
            final float targetTilt = totalLength <= 0d
                    ? normalizedEndTilt
                    : ArticulatedTrackUtils.interpolateTilt((float) Mth.clamp(entry.getValue() / totalLength, 0.0d, 1.0d), normalizedStartTilt, normalizedEndTilt);
            final float snappedTilt = ArticulatedTrackUtils.isValidTilt(targetTilt) ? targetTilt : ArticulatedTrackUtils.snapToNearest(targetTilt);
            if (articulate$applyTiltToBlock(level, blockPos, snappedTilt)) {
                changedPositions.add(blockPos);
            }
        }

        for (final BlockPos blockPos : changedPositions) {
            final BlockState blockState = level.getBlockState(blockPos);
            if (blockState.getBlock() instanceof TrackBlock) {
                level.scheduleTick(blockPos, blockState.getBlock(), 1);
            }
        }
    }

    @Unique
    private static void articulate$handleTiltedStraightPlacement(final Level level, final TrackPlacement.PlacementInfo info,
                                                                 final BlockState state1, final BlockState state2,
                                                                 final boolean simulate,
                                                                 final float startTilt, final float endTilt,
                                                                 final double totalLength) {
        final TrackPlacementInfoAccessorMixin access = (TrackPlacementInfoAccessorMixin) info;
        final BlockPos pos1 = access.articulate$getPos1();
        final BlockPos pos2 = access.articulate$getPos2();
        final Vec3 end1 = access.articulate$getEnd1() != null ? access.articulate$getEnd1() : VecHelper.getCenterOf(pos1);
        final Vec3 end2 = access.articulate$getEnd2() != null ? access.articulate$getEnd2() : VecHelper.getCenterOf(pos2);
        final Vec3 normal1 = access.articulate$getNormal1() != null ? access.articulate$getNormal1() : new Vec3(0, 1, 0);
        final Vec3 normal2 = access.articulate$getNormal2() != null ? access.articulate$getNormal2() : new Vec3(0, 1, 0);
        final Vec3 normedAxis1 = access.articulate$getAxis1().normalize();
        final Vec3 normedAxis2 = access.articulate$getAxis2().normalize();
        final boolean girder = articulate$heldGirder.get();

        // Create degenerate BezierConnection (parallel axes -> straight line with smooth tilt interpolation)
        final BezierConnection degenerateCurve = new BezierConnection(
                Couple.create(pos1, pos2),
                Couple.create(end1, end2),
                Couple.create(normedAxis1, normedAxis2),
                Couple.create(normal1, normal2),
                true, girder, info.trackMaterial
        );

        ((ArticulatedBezierConnection) degenerateCurve).articulate$setTilt(
                Couple.create(startTilt, endTilt)
        );

        // Compute required tracks from curve segment count
        final int segmentCount = degenerateCurve.getSegmentCount();
        info.requiredTracks = (segmentCount + 1) / 2;

        // Store curve on PlacementInfo (used for client-side outline rendering)
        access.articulate$setCurve(degenerateCurve);
        access.articulate$setEnd1Extent(0);
        access.articulate$setEnd2Extent(0);

        if (simulate) {
            return;
        }

        // Ensure endpoint blocks exist with HAS_BE=true
        articulate$ensureEndpointBlock(level, pos1, state1, info.trackMaterial);
        articulate$ensureEndpointBlock(level, pos2, state2, info.trackMaterial);

        // Apply tilt BEFORE storing connection (setTiltDegrees rejects blocks with >1 connections)
        articulate$applyTiltToBlock(level, pos1, startTilt);
        articulate$applyTiltToBlock(level, pos2, endTilt);

        // Store connection in both endpoint TrackBlockEntities
        final BlockEntity te1 = level.getBlockEntity(pos1);
        final BlockEntity te2 = level.getBlockEntity(pos2);
        if (te1 instanceof final TrackBlockEntity tte1 && te2 instanceof final TrackBlockEntity tte2) {
            tte1.addConnection(degenerateCurve);
            tte2.addConnection(degenerateCurve.secondary());
            tte1.tilt.tryApplySmoothing();
            tte2.tilt.tryApplySmoothing();
        }
    }

    @Unique
    private static void articulate$ensureEndpointBlock(final Level level, final BlockPos pos,
                                                       final BlockState referenceState, final TrackMaterial material) {
        final BlockState stateAtPos = level.getBlockState(pos);
        if (stateAtPos.getBlock() instanceof TrackBlock) {
            if (stateAtPos.hasProperty(TrackBlock.HAS_BE) && !stateAtPos.getValue(TrackBlock.HAS_BE)) {
                level.setBlock(pos, stateAtPos.setValue(TrackBlock.HAS_BE, true), Block.UPDATE_ALL);
            }
        } else {
            final BlockState newState = material.getBlock().defaultBlockState();
            if (newState.hasProperty(TrackBlock.HAS_BE)) {
                level.setBlock(pos, newState.setValue(TrackBlock.HAS_BE, true), Block.UPDATE_ALL);
            }
        }
    }

    @Unique
    private static boolean articulate$applyTiltToBlock(final Level level, final BlockPos blockPos, final float tiltDegrees) {
        BlockState blockState = level.getBlockState(blockPos);
        if (!(blockState.getBlock() instanceof TrackBlock) || !blockState.hasProperty(TrackBlock.HAS_BE)) {
            return false;
        }

        if (Float.compare(tiltDegrees, 0f) != 0 && !blockState.getValue(TrackBlock.HAS_BE)) {
            level.setBlock(blockPos, blockState.setValue(TrackBlock.HAS_BE, true), Block.UPDATE_ALL);
            blockState = level.getBlockState(blockPos);
        } else if (Float.compare(tiltDegrees, 0f) == 0 && !blockState.getValue(TrackBlock.HAS_BE)) {
            return false;
        }

        final ArticulatedTrackBehaviour behaviour = ArticulatedTrackBehaviour.get(level.getBlockEntity(blockPos));
        if (behaviour == null) {
            return false;
        }

        behaviour.setTiltDegrees(tiltDegrees);
        return true;
    }

    @Unique
    private static Map<BlockPos, Double> articulate$collectPlacedBlockDistances(final TrackPlacementInfoAccessorMixin info, final double totalLength) {
        final Map<BlockPos, Double> distances = new LinkedHashMap<>();
        final BezierConnection curve = info.articulate$getCurve();

        for (final boolean first : Iterate.trueAndFalse) {
            final int extent = first ? info.articulate$getEnd1Extent() : info.articulate$getEnd2Extent();
            final int count = curve != null ? extent + 1 : extent;
            if (count <= 0) {
                continue;
            }

            final Vec3 axis = first ? info.articulate$getAxis1() : info.articulate$getAxis2();
            final BlockPos pos = first ? info.articulate$getPos1() : info.articulate$getPos2();
            final double axisLength = axis.length();

            for (int index = 0; index < count; index++) {
                final BlockPos targetPos = pos.offset(BlockPos.containing(axis.scale(index)));
                final double distance = first ? index * axisLength : totalLength - index * axisLength;
                distances.putIfAbsent(targetPos, distance);
            }
        }

        return distances;
    }

    @Unique
    private static double articulate$getConnectionLength(final TrackPlacementInfoAccessorMixin info) {
        final BezierConnection curve = info.articulate$getCurve();
        if (curve == null) {
            return VecHelper.getCenterOf(info.articulate$getPos1()).distanceTo(VecHelper.getCenterOf(info.articulate$getPos2()));
        }

        return curve.getLength()
                + info.articulate$getEnd1Extent() * info.articulate$getAxis1().length()
                + info.articulate$getEnd2Extent() * info.articulate$getAxis2().length();
    }

    @Unique
    private static boolean articulate$hasTiltedJunction(final Level level, final BlockPos pos, final float tilt) {
        if (Float.compare(tilt, 0f) == 0) {
            return false;
        }

        final BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof final TrackBlockEntity trackBlockEntity && trackBlockEntity.getConnections().size() > 1;
    }

    @Unique
    private static float articulate$resolveEndTilt(final Level level, final BlockPos pos) {
        final BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TrackBlockEntity) {
            final float existingTilt = ArticulatedTrackBehaviour.getTiltDegrees(blockEntity);
            if (Float.compare(existingTilt, 0f) != 0) {
                return existingTilt;
            }
        }
        return articulate$heldTilt.get();
    }

    @Unique
    private static float articulate$resolveStartTilt(final Level level, final BlockPos pos) {
        return ArticulatedTrackBehaviour.getTiltDegrees(level.getBlockEntity(pos));
    }

}