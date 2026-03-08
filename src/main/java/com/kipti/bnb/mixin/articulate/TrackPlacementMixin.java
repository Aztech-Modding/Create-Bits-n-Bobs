package com.kipti.bnb.mixin.articulate;

import com.kipti.bnb.CreateBitsnBobs;
import com.kipti.bnb.content.articulate.ArticulatedTrackBehaviour;
import com.kipti.bnb.content.articulate.ArticulatedTrackLogic;
import com.kipti.bnb.content.articulate.ArticulatedTrackPlacementPlan;
import com.kipti.bnb.content.articulate.ArticulatedTrackUtils;
import com.kipti.bnb.mixin_accessor.ArticulatedBezierConnection;
import com.kipti.bnb.mixin_accessor.ArticulatedTrackPlacementPlanHolder;
import com.kipti.bnb.registry.core.BnbDataComponents;
import com.simibubi.create.content.trains.track.*;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;
import com.simibubi.create.foundation.utility.BlockHelper;
import net.createmod.catnip.data.Couple;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
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

import java.util.*;

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
        final ArticulatedTrackPlacementPlanHolder planHolder = (ArticulatedTrackPlacementPlanHolder) info;
        planHolder.articulate$setPlacementPlan(null);
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
//        final float normalizedStartTilt = ArticulatedTrackUtils.tiltForCanonicalAxis(startTilt, access.articulate$getAxis1());
        final float normalizedStartTilt = startTilt;
        final float normalizedEndTilt = endTilt;

        if (articulate$hasTiltedJunction(level, access.articulate$getPos1(), normalizedStartTilt)
                || articulate$hasTiltedJunction(level, access.articulate$getPos2(), normalizedEndTilt)) {
            access.articulate$setValid(false);
            access.articulate$setMessage("track.turn_start");
            cir.setReturnValue(info);
            return;
        }

        final double totalLength = articulate$getConnectionLength(access);
        final boolean tooSteep = !ArticulatedTrackUtils.isValidTiltTransition(normalizedStartTilt, normalizedEndTilt, (float) totalLength);
        final ArticulatedTrackPlacementPlan plan = articulate$buildPlacementPlan(level, info, state1, state2,
                normalizedStartTilt, normalizedEndTilt, totalLength);
        planHolder.articulate$setPlacementPlan(plan);

        if (tooSteep) {
            access.articulate$setValid(false);
            access.articulate$setMessage("track.too_steep");
            if (!simulate) {
                cir.setReturnValue(info);
                return;
            }
        }

        articulate$executePlacementPlan(level, info, plan, simulate);
        cir.setReturnValue(info);
    }

    @Inject(method = "placeTracks", at = @At("RETURN"))
    private static void articulate$applyPlacedTilts(final Level level, final TrackPlacement.PlacementInfo info, final BlockState state1,
                                                    final BlockState state2, final BlockPos targetPos1, final BlockPos targetPos2,
                                                    final boolean simulate,
                                                    final CallbackInfoReturnable<TrackPlacement.PlacementInfo> cir) {
        // Segmented articulate placement now applies true Bezier tilt on placement and
        // only keeps endpoint block tilt where that representation is still meaningful.
    }

    @Unique
    private static ArticulatedTrackPlacementPlan articulate$buildPlacementPlan(final Level level, final TrackPlacement.PlacementInfo info,
                                                                               final BlockState state1, final BlockState state2,
                                                                               final float startTilt, final float endTilt,
                                                                               final double totalLength) {
        final TrackPlacementInfoAccessorMixin access = (TrackPlacementInfoAccessorMixin) info;
        final BlockPos pos1 = access.articulate$getPos1();
        final BlockPos pos2 = access.articulate$getPos2();
        final Vec3 axis1 = access.articulate$getAxis1();
        final Vec3 axis2 = access.articulate$getAxis2();
        final Vec3 end1 = articulate$resolveTrackCurveStart(level, pos1, state1, axis1, access.articulate$getEnd1());
        final Vec3 end2 = articulate$resolveTrackCurveStart(level, pos2, state2, axis2, access.articulate$getEnd2());
        final Vec3 normal1 = articulate$resolveTrackNormal(level, pos1, state1, access.articulate$getNormal1());
        final Vec3 normal2 = articulate$resolveTrackNormal(level, pos2, state2, access.articulate$getNormal2());
        final Vec3 normedAxis1 = axis1.normalize();
        final Vec3 normedAxis2 = axis2.normalize();
        final BezierConnection baseCurve = access.articulate$getCurve();
        final List<ArticulatedTrackPlacementPlan.Segment> segments = new ArrayList<>(3);

        if (baseCurve == null) {
            segments.add(new ArticulatedTrackPlacementPlan.Segment(
                    articulate$createStraightSegment(
                            Couple.create(pos1, pos2),
                            Couple.create(end1, end2),
                            Couple.create(normedAxis1, normedAxis2),
                            Couple.create(normal1, normal2),
                            startTilt,
                            endTilt,
                            articulate$heldGirder.get(),
                            info.trackMaterial
                    ),
                    state1,
                    state2
            ));
            return new ArticulatedTrackPlacementPlan(segments, pos1, pos2, startTilt, endTilt);
        }

        final BezierConnection middleCurve = baseCurve.clone();
        final ArticulatedBezierConnection articulatedMiddle = (ArticulatedBezierConnection) middleCurve;
        final Couple<Vec3> middleBaseNormals = articulatedMiddle.articulate$getBaseNormals();
        final Vec3 entryStraightJoinAxis = ArticulatedTrackLogic.outwardStraightJoinAxis(middleCurve.axes.getFirst());
        final Vec3 exitStraightJoinAxis = ArticulatedTrackLogic.outwardStraightJoinAxis(middleCurve.axes.getSecond());
        final ArticulatedTrackLogic.PlacementSegmentPlan segmentPlan = ArticulatedTrackLogic.planSegmentBreakpoints(
                totalLength,
                access.articulate$getEnd1Extent(),
                axis1.length(),
                access.articulate$getEnd2Extent(),
                axis2.length(),
                startTilt,
                endTilt
        );

        if (segmentPlan.hasEntryStraight()) {
            segments.add(new ArticulatedTrackPlacementPlan.Segment(
                    articulate$createStraightSegment(
                            Couple.create(pos1, middleCurve.bePositions.getFirst()),
                            Couple.create(end1, middleCurve.starts.getFirst()),
                            Couple.create(normedAxis1, entryStraightJoinAxis),
                            Couple.create(normal1, middleBaseNormals.getFirst()),
                            startTilt,
                            segmentPlan.curveStartTilt(),
                            middleCurve.hasGirder,
                            info.trackMaterial
                    ),
                    state1,
                    state1
            ));
        }

        articulatedMiddle.articulate$setTilt(Couple.create(segmentPlan.curveStartTilt(), segmentPlan.curveEndTilt()));
        segments.add(new ArticulatedTrackPlacementPlan.Segment(middleCurve, state1, state2));

        if (segmentPlan.hasExitStraight()) {
            segments.add(new ArticulatedTrackPlacementPlan.Segment(
                    articulate$createStraightSegment(
                            Couple.create(middleCurve.bePositions.getSecond(), pos2),
                            Couple.create(middleCurve.starts.getSecond(), end2),
                            Couple.create(exitStraightJoinAxis, normedAxis2),
                            Couple.create(middleBaseNormals.getSecond(), normal2),
                            segmentPlan.curveEndTilt(),
                            endTilt,
                            middleCurve.hasGirder,
                            info.trackMaterial
                    ),
                    state2,
                    state2
            ));
        }

        return new ArticulatedTrackPlacementPlan(segments, pos1, pos2, startTilt, endTilt);
    }

    @Unique
    private static BezierConnection articulate$createStraightSegment(final Couple<BlockPos> positions,
                                                                     final Couple<Vec3> starts,
                                                                     final Couple<Vec3> axes,
                                                                     final Couple<Vec3> normals,
                                                                     final float startTilt,
                                                                     final float endTilt,
                                                                     final boolean girder,
                                                                     final TrackMaterial material) {
        final BezierConnection segment = new BezierConnection(positions, starts, axes, normals, true, girder, material);
        ((ArticulatedBezierConnection) segment).articulate$setTilt(Couple.create(startTilt, endTilt));
        return segment;
    }

    @Unique
    private static void articulate$executePlacementPlan(final Level level, final TrackPlacement.PlacementInfo info,
                                                        final ArticulatedTrackPlacementPlan plan, final boolean simulate) {
        info.requiredTracks = articulate$calculateRequiredTracks(level, plan);
        if (simulate) {
            return;
        }

        final Map<BlockPos, BlockState> endpointStates = new LinkedHashMap<>();
        for (final ArticulatedTrackPlacementPlan.Segment segment : plan.segments()) {
            endpointStates.putIfAbsent(segment.curve().bePositions.getFirst(), segment.startState());
            endpointStates.putIfAbsent(segment.curve().bePositions.getSecond(), segment.endState());
        }

        final Set<BlockPos> changedPositions = new HashSet<>();
        for (final Map.Entry<BlockPos, BlockState> entry : endpointStates.entrySet()) {
            if (articulate$placeCurveEndpoint(level, entry.getKey(), entry.getValue(), info.trackMaterial)) {
                changedPositions.add(entry.getKey());
            }
        }

        final Set<BlockPos> smoothingPositions = new HashSet<>();
        for (final ArticulatedTrackPlacementPlan.Segment segment : plan.segments()) {
            final BezierConnection curve = segment.curve();
            final BlockEntity te1 = level.getBlockEntity(curve.bePositions.getFirst());
            final BlockEntity te2 = level.getBlockEntity(curve.bePositions.getSecond());
            if (!(te1 instanceof final TrackBlockEntity tte1) || !(te2 instanceof final TrackBlockEntity tte2)) {
                continue;
            }

            final ArticulatedTrackBehaviour behaviour1 = ArticulatedTrackBehaviour.get(te1);
            final ArticulatedTrackBehaviour behaviour2 = ArticulatedTrackBehaviour.get(te2);
            if (behaviour1 == null || behaviour2 == null) {
                continue;
            }
            if (((Object) segment.curve()) instanceof final ArticulatedBezierConnection articulatedSegment) {
                final Couple<Float> tilts = articulatedSegment.articulate$getTilt();
                behaviour1.setTiltDegrees(tilts.getFirst());
                behaviour2.setTiltDegrees(tilts.getSecond());
            }

            tte1.addConnection(curve);
            tte2.addConnection(curve.secondary());

            smoothingPositions.add(tte1.getBlockPos());
            smoothingPositions.add(tte2.getBlockPos());
        }

        for (final BlockPos blockPos : smoothingPositions) {
            final BlockEntity blockEntity = level.getBlockEntity(blockPos);
            if (blockEntity instanceof final TrackBlockEntity trackBlockEntity) {
                trackBlockEntity.tilt.tryApplySmoothing();
            }
        }

        articulate$applyPlanEndpointTilt(level, plan.startPos(), plan.startTilt(), changedPositions);
        articulate$applyPlanEndpointTilt(level, plan.endPos(), plan.endTilt(), changedPositions);

        for (final BlockPos blockPos : changedPositions) {
            final BlockState blockState = level.getBlockState(blockPos);
            if (blockState.getBlock() instanceof TrackBlock) {
                level.scheduleTick(blockPos, blockState.getBlock(), 1);
            }
        }
    }

    @Unique
    private static int articulate$calculateRequiredTracks(final Level level, final ArticulatedTrackPlacementPlan plan) {
        int requiredTracks = 0;
        final Set<BlockPos> countedPlacements = new HashSet<>();

        for (final ArticulatedTrackPlacementPlan.Segment segment : plan.segments()) {
            final BezierConnection curve = segment.curve();
            requiredTracks += articulate$countEndpointPlacement(level, curve.bePositions.getFirst(), countedPlacements);
            requiredTracks += articulate$countEndpointPlacement(level, curve.bePositions.getSecond(), countedPlacements);
            if (articulate$requiresTrackItemsForCurve(level, curve)) {
                requiredTracks += curve.getTrackItemCost();
            }
        }

        return requiredTracks;
    }

    @Unique
    private static int articulate$countEndpointPlacement(final Level level, final BlockPos pos, final Set<BlockPos> countedPlacements) {
        if (!countedPlacements.add(pos)) {
            return 0;
        }

        final BlockState stateAtPos = level.getBlockState(pos);
        return stateAtPos.canBeReplaced() || stateAtPos.is(BlockTags.FLOWERS) ? 1 : 0;
    }

    @Unique
    private static boolean articulate$requiresTrackItemsForCurve(final Level level, final BezierConnection curve) {
        final BlockEntity te1 = level.getBlockEntity(curve.bePositions.getFirst());
        final BlockEntity te2 = level.getBlockEntity(curve.bePositions.getSecond());
        if (!(te1 instanceof final TrackBlockEntity tte1) || !(te2 instanceof TrackBlockEntity)) {
            return true;
        }

        return !tte1.getConnections().containsKey(curve.bePositions.getSecond());
    }

    @Unique
    private static boolean articulate$placeCurveEndpoint(final Level level, final BlockPos pos,
                                                         final BlockState referenceState, final TrackMaterial material) {
        final BlockState normalizedReferenceState = articulate$normalizeEndpointReferenceState(referenceState);
        final BlockState stateAtPos = level.getBlockState(pos);
        BlockState toPlace = BlockHelper.copyProperties(normalizedReferenceState, material.getBlock().defaultBlockState());
        boolean canPlace = stateAtPos.canBeReplaced() || stateAtPos.is(BlockTags.FLOWERS);

        if (stateAtPos.getBlock() instanceof final ITrackBlock trackAtPos) {
            toPlace = trackAtPos.overlay(level, pos, stateAtPos, toPlace);
            canPlace = true;
        }

        if (!canPlace) {
            return false;
        }

        if (toPlace.hasProperty(TrackBlock.HAS_BE)) {
            toPlace = toPlace.setValue(TrackBlock.HAS_BE, true);
        }

        level.setBlock(pos, ProperWaterloggedBlock.withWater(level, toPlace, pos), Block.UPDATE_ALL);
        final ArticulatedTrackBehaviour behaviour = ArticulatedTrackBehaviour.get(level.getBlockEntity(pos));
        return true;
    }

    @Unique
    private static BlockState articulate$normalizeEndpointReferenceState(final BlockState referenceState) {
        if (!referenceState.hasProperty(TrackBlock.SHAPE)) {
            return referenceState;
        }

        return switch (referenceState.getValue(TrackBlock.SHAPE)) {
            case TE, TW -> referenceState.setValue(TrackBlock.SHAPE, TrackShape.XO);
            case TN, TS -> referenceState.setValue(TrackBlock.SHAPE, TrackShape.ZO);
            default -> referenceState;
        };
    }

    @Unique
    private static void articulate$applyPlanEndpointTilt(final Level level, final BlockPos pos, final float tiltDegrees,
                                                         final Set<BlockPos> changedPositions) {
        if (Float.compare(tiltDegrees, 0f) == 0) {
            return;
        }

        if (articulate$applyTiltToBlock(level, pos, tiltDegrees)) {
            changedPositions.add(pos);
        }
    }

    @Unique
    private static boolean articulate$applyTiltToBlock(final Level level, final BlockPos blockPos, final float tiltDegrees) {
        BlockState blockState = level.getBlockState(blockPos);
        if (!(blockState.getBlock() instanceof TrackBlock) || !blockState.hasProperty(TrackBlock.HAS_BE)) {
            if (Float.compare(tiltDegrees, 0f) != 0) {
                CreateBitsnBobs.LOGGER.warn("Failed to apply articulate tilt {} at {} because the block has no track block entity support", tiltDegrees, blockPos);
            }
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
            CreateBitsnBobs.LOGGER.warn("Failed to apply articulate tilt {} at {} because articulated track behaviour was unavailable", tiltDegrees, blockPos);
            return false;
        }

        behaviour.setTiltDegrees(tiltDegrees);
        return true;
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
        final BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof ITrackBlock) {
            final BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof TrackBlockEntity) {
                return ArticulatedTrackBehaviour.getTiltDegrees(blockEntity);
            }
            return 0f;
        }

        return articulate$heldTilt.get();
    }

    @Unique
    private static Vec3 articulate$resolveTrackCurveStart(final Level level, final BlockPos pos, final BlockState state,
                                                          final Vec3 axis, final Vec3 fallbackEnd) {
        if (state.getBlock() instanceof final ITrackBlock trackBlock && axis != null) {
            return trackBlock.getCurveStart(level, pos, state, axis);
        }
        if (fallbackEnd != null) {
            return fallbackEnd;
        }

        if (state.getBlock() instanceof final ITrackBlock trackBlock) {
            return Vec3.atBottomCenterOf(pos).add(0, trackBlock.getElevationAtCenter(level, pos, state), 0);
        }

        return VecHelper.getCenterOf(pos);
    }

    @Unique
    private static Vec3 articulate$resolveTrackNormal(final Level level, final BlockPos pos, final BlockState state,
                                                      final Vec3 fallbackNormal) {
        if (state.getBlock() instanceof final ITrackBlock trackBlock) {
            return trackBlock.getUpNormal(level, pos, state).normalize();
        }
        if (fallbackNormal != null) {
            return fallbackNormal.normalize();
        }

        return new Vec3(0, 1, 0);
    }

    @Unique
    private static float articulate$resolveStartTilt(final Level level, final BlockPos pos) {
        return ArticulatedTrackBehaviour.getTiltDegrees(level.getBlockEntity(pos));
    }

}
