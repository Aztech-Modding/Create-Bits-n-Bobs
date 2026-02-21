package com.kipti.bnb.mixin.encasable_piston_poles;

import com.simibubi.create.content.contraptions.piston.PistonExtensionPoleBlock;
import com.simibubi.create.content.decoration.encasing.EncasableBlock;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.WrenchableDirectionalBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PistonExtensionPoleBlock.class)
public class PistonExtensionPoleBlockMixin extends WrenchableDirectionalBlock implements IWrenchable, SimpleWaterloggedBlock, EncasableBlock {

    public PistonExtensionPoleBlockMixin(final Properties properties) {
        super(properties);
    }

    @Inject(method = "useItemOn", at = @At("RETURN"), cancellable = true)
    protected void useItemOn(final ItemStack stack, final BlockState state, final Level level, final BlockPos pos, final Player player, final InteractionHand hand, final BlockHitResult hitResult, final CallbackInfoReturnable<ItemInteractionResult> cir) {
        if (cir.getReturnValue() == ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION)
            cir.setReturnValue(EncasableBlock.super.tryEncase(state, level, pos, stack, player, hand, hitResult));
    }

}

