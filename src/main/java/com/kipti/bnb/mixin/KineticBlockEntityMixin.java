package com.kipti.bnb.mixin;

import com.kipti.bnb.content.kinetics.gigantic_cogwheel.GiganticCogwheelBlock;
import com.simibubi.create.Create;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = KineticBlockEntity.class, remap = false)
public class KineticBlockEntityMixin {

    @Inject(method = "addPropagationLocations", at = @At("RETURN"), cancellable = true)
    private void injectGiganticCogLocations(IRotate block, BlockState state,
                                            List<BlockPos> neighbours,
                                            CallbackInfoReturnable<List<BlockPos>> cir) {

        KineticBlockEntity self = (KineticBlockEntity)(Object) this;
        Level level = self.getLevel();
        BlockPos worldPosition = self.getBlockPos();

        if (!(block instanceof ICogWheel cog) || cog.isLargeCog()) return;
        if (level == null) return;

        Direction.Axis axis = cog.getRotationAxis(state);
        List<BlockPos> result = cir.getReturnValue();

        for (Direction dir : Direction.values()) {
            if (dir.getAxis() == axis) continue;

            BlockPos targetPos = worldPosition.relative(dir, 3);
            BlockState targetState = level.getBlockState(targetPos);

            if (!(targetState.getBlock() instanceof GiganticCogwheelBlock gigantic)) continue;
            if (gigantic.getRotationAxis(targetState) != axis) continue;

            if (!result.contains(targetPos))
                result.add(targetPos);
        }

        cir.setReturnValue(result);
    }

    @Inject(method = "propagateRotationTo", at = @At("HEAD"), cancellable = true)
    private void injectGiganticCogPropagation(KineticBlockEntity target,
                                              BlockState stateFrom, BlockState stateTo,
                                              BlockPos diff,
                                              boolean connectedViaAxes, boolean connectedViaCogs,
                                              CallbackInfoReturnable<Float> cir) {

        if (!(stateFrom.getBlock() instanceof ICogWheel cog) || cog.isLargeCog()) return;
        if (!(stateTo.getBlock() instanceof GiganticCogwheelBlock gigantic)) return;

        KineticBlockEntity self = (KineticBlockEntity)(Object) this;
        Direction.Axis axis = gigantic.getRotationAxis(stateTo);
        if (cog.getRotationAxis(stateFrom) != axis) return;

        BlockPos selfPos = self.getBlockPos();
        BlockPos targetPos = target.getBlockPos();
        BlockPos d = targetPos.subtract(selfPos);

        if (!isValidDiff(axis, d)) return;

        Create.LOGGER.info("[GiganticCog] MIXIN propagateRotationTo: small->gigantic ratio applied");
        cir.setReturnValue(-1f / 5f);
    }

    private static boolean isValidDiff(Direction.Axis axis, BlockPos diff) {
        return switch (axis) {
            case X -> diff.getX() == 0 && (Math.abs(diff.getY()) == 3 && diff.getZ() == 0 || Math.abs(diff.getZ()) == 3 && diff.getY() == 0);
            case Y -> diff.getY() == 0 && (Math.abs(diff.getX()) == 3 && diff.getZ() == 0 || Math.abs(diff.getZ()) == 3 && diff.getX() == 0);
            case Z -> diff.getZ() == 0 && (Math.abs(diff.getX()) == 3 && diff.getY() == 0 || Math.abs(diff.getY()) == 3 && diff.getX() == 0);
        };
    }
}