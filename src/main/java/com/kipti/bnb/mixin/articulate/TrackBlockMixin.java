package com.kipti.bnb.mixin.articulate;

import com.kipti.bnb.content.articulate.ArticulatedTrackBehaviour;
import com.kipti.bnb.content.articulate.ArticulatedTrackUtils;
import com.kipti.bnb.registry.core.BnbDataComponents;
import com.simibubi.create.content.trains.graph.TrackNodeLocation;
import com.simibubi.create.content.trains.track.TrackBlock;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.List;

@Mixin(TrackBlock.class)
public abstract class TrackBlockMixin {

    @Inject(method = "setPlacedBy", at = @At("HEAD"))
    private void articulate$forceBlockEntityForTilt(final Level level, final BlockPos pos, final BlockState state,
                                                    @Nullable final LivingEntity placer, final ItemStack stack,
                                                    final CallbackInfo ci) {
        if (level.isClientSide()) {
            return;
        }
        final float tilt = stack.getOrDefault(BnbDataComponents.TRACK_TILT, 0f);
        if (Float.compare(tilt, 0f) == 0) {
            return;
        }
        if (!state.hasProperty(TrackBlock.HAS_BE) || state.getValue(TrackBlock.HAS_BE)) {
            return;
        }

        level.setBlock(pos, state.setValue(TrackBlock.HAS_BE, true), Block.UPDATE_ALL);
        final var newBE = level.getBlockEntity(pos);
        if (newBE instanceof final SmartBlockEntity smartBE) {
            smartBE.initialize();
            ((SmartBlockEntityAccessorMixin) smartBE).articulate$setInitialized(true);
        }
        final ArticulatedTrackBehaviour behaviour = ArticulatedTrackBehaviour.get(newBE);
        if (behaviour != null) {
            behaviour.setTiltDegrees(tilt);
            behaviour.refreshRenderedModel();
        }
    }

    @Inject(method = "getConnected", at = @At("RETURN"))
    private void articulate$rotateConnectedNormals(final BlockGetter world, final BlockPos pos, final BlockState state,
                                                   final boolean linear, final TrackNodeLocation connectedTo,
                                                   final CallbackInfoReturnable<Collection<TrackNodeLocation.DiscoveredLocation>> cir) {
        final float tiltDegrees = ArticulatedTrackBehaviour.getTiltDegrees(world.getBlockEntity(pos));
        if (Float.compare(tiltDegrees, 0f) == 0) {
            return;
        }

        final List<Vec3> trackAxes = ((TrackBlock) (Object) this).getTrackAxes(world, pos, state);
        if (trackAxes.size() != 1) {
            return;
        }

        final Vec3 trackAxis = trackAxes.get(0);
        for (final TrackNodeLocation.DiscoveredLocation location : cir.getReturnValue()) {
            if (((DiscoveredLocationAccessorMixin) location).articulate$getTurn() != null) {
                continue;
            }
            final Vec3 baseNormal = ((DiscoveredLocationAccessorMixin) location).articulate$getNormal();
            location.withNormal(ArticulatedTrackUtils.rotateFaceNormalCanonical(baseNormal, trackAxis, tiltDegrees));
        }
    }

}
