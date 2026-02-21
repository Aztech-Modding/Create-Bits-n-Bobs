package com.kipti.bnb.mixin.glowing_belts;

import com.kipti.bnb.registry.compat.BnbCreateBlockEdits;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.content.kinetics.belt.BeltBlock;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltHelper;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.GlowInkSacItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BeltBlock.class)
public class BeltBlockMixin extends HorizontalKineticBlock implements IBE<BeltBlockEntity> {

    public BeltBlockMixin(final Properties properties) {
        super(properties);
    }

    @Inject(method = "createBlockStateDefinition", at = @At("TAIL"))
    private void bits_n_bobs$createBlockStateDefinitionWithGlowingProperty(final StateDefinition.Builder<Block, BlockState> builder, final CallbackInfo ci) {
        builder.add(BnbCreateBlockEdits.GLOWING);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void bits_n_bobs$constructorWithDefaultGlowing(final BlockBehaviour.Properties properties, final CallbackInfo ci) {
        registerDefaultState(
                defaultBlockState()
                        .setValue(BnbCreateBlockEdits.GLOWING, false)
        );
    }

    @Inject(method = "useItemOn", at = @At("RETURN"), cancellable = true)
    private void bits_n_bobs$constructorWithDefaultGlowing(final ItemStack stack,
                                                           final BlockState state,
                                                           final Level level,
                                                           final BlockPos pos,
                                                           final Player player,
                                                           final InteractionHand hand,
                                                           final BlockHitResult hitResult,
                                                           final CallbackInfoReturnable<ItemInteractionResult> cir) {
        final ItemInteractionResult result = cir.getReturnValue();
        if (result != ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION && state.hasProperty(BnbCreateBlockEdits.GLOWING))
            return;

        final boolean isGlowSac = stack.getItem() instanceof GlowInkSacItem;
        if (!isGlowSac) return;

        final boolean stateIsGlowing = state.getValue(BnbCreateBlockEdits.GLOWING);

        if (!stateIsGlowing)
            withBlockEntityDo(level, pos, be -> {
                for (final BlockPos blockPos : BeltBlock.getBeltChain(level, be.getController())) {
                    final BeltBlockEntity belt = BeltHelper.getSegmentBE(level, blockPos);
                    if (belt == null)
                        continue;
                    level.setBlock(blockPos, level.getBlockState(blockPos).setValue(BnbCreateBlockEdits.GLOWING, true), Block.UPDATE_ALL | Block.UPDATE_MOVE_BY_PISTON);
                }
            });
        cir.setReturnValue(ItemInteractionResult.SUCCESS);
    }

    @Shadow
    @Override
    public Direction.Axis getRotationAxis(final BlockState state) {
        return null;
    }

    @Shadow
    @Override
    public Class<BeltBlockEntity> getBlockEntityClass() {
        return null;
    }

    @Shadow
    @Override
    public BlockEntityType<? extends BeltBlockEntity> getBlockEntityType() {
        return null;
    }

}

