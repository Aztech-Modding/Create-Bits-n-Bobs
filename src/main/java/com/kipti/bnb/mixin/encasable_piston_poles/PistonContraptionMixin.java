package com.kipti.bnb.mixin.encasable_piston_poles;

import com.kipti.bnb.content.encased_blocks.piston_pole.EncasedPistonExtensionPoleBlock;
import com.kipti.bnb.registry.BnbBlocks;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.api.contraption.ContraptionType;
import com.simibubi.create.content.contraptions.AssemblyException;
import com.simibubi.create.content.contraptions.TranslatingContraption;
import com.simibubi.create.content.contraptions.piston.PistonContraption;
import com.simibubi.create.content.contraptions.piston.PistonExtensionPoleBlock;
import com.simibubi.create.foundation.utility.BlockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PistonContraption.class)
public class PistonContraptionMixin extends TranslatingContraption {

    @Shadow
    protected Direction orientation;

    @WrapOperation(method = "collectExtensions", at = @At(value = "INVOKE", ordinal = 1, target = "Lcom/simibubi/create/content/contraptions/piston/PistonExtensionPoleBlock$PlacementHelper;matchesAxis(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction$Axis;)Z"))
    private boolean collectExtensions(final PistonExtensionPoleBlock.PlacementHelper instance, final BlockState state, final Direction.Axis axis, final Operation<Boolean> original) {
        return original.call(instance, state, axis) ||
                (BnbBlocks.ENCASED_PISTON_EXTENSION_POLE.isIn(state) && state.getValue(EncasedPistonExtensionPoleBlock.FACING).getAxis() == axis && !state.getValue(EncasedPistonExtensionPoleBlock.EMPTY));
    }

    @WrapOperation(method = "collectExtensions", at = @At(value = "NEW", ordinal = 4, target = "(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplate$StructureBlockInfo;"))
    private StructureTemplate.StructureBlockInfo collectExtensions(final BlockPos pos, BlockState state, final CompoundTag nbt, final Operation<StructureTemplate.StructureBlockInfo> original) {
        if (BnbBlocks.ENCASED_PISTON_EXTENSION_POLE.isIn(state))
            state = BlockHelper.copyProperties(state, AllBlocks.PISTON_EXTENSION_POLE.getDefaultState());
        return original.call(pos, state, nbt);
    }

    @Inject(method = "customBlockPlacement", at = @At("HEAD"), cancellable = true)
    protected void customBlockPlacement(final LevelAccessor world, final BlockPos pos, final BlockState state, final CallbackInfoReturnable<Boolean> cir) {
        final BlockState existingState = world.getBlockState(pos);
        if (BnbBlocks.ENCASED_PISTON_EXTENSION_POLE.isIn(existingState) && state.is(AllBlocks.PISTON_EXTENSION_POLE)) {
            if (existingState.getValue(EncasedPistonExtensionPoleBlock.EMPTY)) {
                world.setBlock(pos, existingState.setValue(EncasedPistonExtensionPoleBlock.EMPTY, false), Block.UPDATE_CLIENTS);
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "customBlockRemoval", at = @At("HEAD"), cancellable = true)
    protected void customBlockRemoval(final LevelAccessor world, final BlockPos pos, final BlockState state, final CallbackInfoReturnable<Boolean> cir) {
        final BlockState existingState = world.getBlockState(pos);
        if (BnbBlocks.ENCASED_PISTON_EXTENSION_POLE.isIn(existingState) && state.is(AllBlocks.PISTON_EXTENSION_POLE)) {
            if (!existingState.getValue(EncasedPistonExtensionPoleBlock.EMPTY)) {
                world.setBlock(pos, existingState.setValue(EncasedPistonExtensionPoleBlock.EMPTY, true), Block.UPDATE_CLIENTS);
                cir.setReturnValue(true);
            }
        }
    }

    @Shadow
    @Override
    public boolean assemble(final Level world, final BlockPos pos) throws AssemblyException {
        return false;
    }

    @Shadow
    @Override
    public ContraptionType getType() {
        return null;
    }

}
