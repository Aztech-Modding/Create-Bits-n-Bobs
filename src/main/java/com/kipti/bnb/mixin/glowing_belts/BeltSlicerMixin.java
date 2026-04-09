package com.kipti.bnb.mixin.glowing_belts;

import com.kipti.bnb.registry.compat.BnbCreateBlockEdits;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.simibubi.create.content.kinetics.belt.BeltSlicer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BeltSlicer.class)
public class BeltSlicerMixin {

    @Inject(method = "useConnector", at = @At("HEAD"))
    private static void bits_n_bobs$getValueInjectFor(final BlockState state,
                                                      final Level world,
                                                      final BlockPos pos,
                                                      final Player player,
                                                      final InteractionHand handIn,
                                                      final BlockHitResult hit,
                                                      final BeltSlicer.Feedback feedBack,
                                                      final CallbackInfoReturnable<ItemInteractionResult> cir,
                                                      @Share("isGlowing") final LocalBooleanRef isGlowing) {
        isGlowing.set(state.hasProperty(BnbCreateBlockEdits.GLOWING) && state.getValue(BnbCreateBlockEdits.GLOWING));
    }


    @Inject(method = "useConnector", at = @At(value = "FIELD", target = "Lcom/simibubi/create/content/kinetics/belt/BeltBlockEntity;color:Ljava/util/Optional;", ordinal = 1))
    private static void bits_n_bobs$addGlowingStateToNewBlock(final BlockState state,
                                                              final Level world,
                                                              final BlockPos pos,
                                                              final Player player,
                                                              final InteractionHand handIn,
                                                              final BlockHitResult hit,
                                                              final BeltSlicer.Feedback feedBack,
                                                              final CallbackInfoReturnable<ItemInteractionResult> cir,
                                                              @Share("isGlowing") final LocalBooleanRef isGlowing,
                                                              @Local(name = "blockPos", ordinal = 3) final BlockPos blockPos) {
        world.setBlock(blockPos, world.getBlockState(blockPos).setValue(BnbCreateBlockEdits.GLOWING, isGlowing.get()), Block.UPDATE_ALL | Block.UPDATE_MOVE_BY_PISTON);
    }

    @WrapOperation(method = "useConnector", at = @At(value = "INVOKE", ordinal = 1, target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private static boolean bits_n_bobs$addGlowingStateToMergedBlock(final Level instance,
                                                                    final BlockPos pos,
                                                                    final BlockState newState,
                                                                    final int flags,
                                                                    final Operation<Boolean> original,
                                                                    @Share("isGlowing") final LocalBooleanRef isGlowing) {
        return original.call(instance, pos, newState.setValue(BnbCreateBlockEdits.GLOWING, isGlowing.get()), flags);
    }

}

