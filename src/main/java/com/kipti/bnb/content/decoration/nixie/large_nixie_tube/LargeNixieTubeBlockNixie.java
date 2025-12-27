package com.kipti.bnb.content.decoration.nixie.large_nixie_tube;

import com.kipti.bnb.content.decoration.nixie.foundation.*;
import com.kipti.bnb.registry.BnbBlockEntities;
import com.kipti.bnb.registry.BnbBlocks;
import com.kipti.bnb.registry.BnbShapes;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LargeNixieTubeBlockNixie extends GenericNixieDisplayBlock implements IBE<GenericNixieDisplayBlockEntity>, IWrenchable, IGenericNixieDisplayBlock, DyeProviderBlock {

    final @Nullable DyeColor dyeColor;

    public LargeNixieTubeBlockNixie(final Properties p_52591_, @Nullable final DyeColor dyeColor) {
        super(p_52591_);
        this.dyeColor = dyeColor;
    }

    @Override
    public ItemStack getCloneItemStack(final BlockState state, final HitResult target, final LevelReader level, final BlockPos pos, final Player player) {
        return BnbBlocks.LARGE_NIXIE_TUBE.asItem().getDefaultInstance();
    }

    @Override
    protected ItemInteractionResult useItemOn(final ItemStack stack, final BlockState state, final Level level, final BlockPos pos, final Player player, final InteractionHand hand, final BlockHitResult hitResult) {
        final ItemStack heldItem = player.getItemInHand(hand);
        if (heldItem.getItem() instanceof final DyeItem dyeItem && dyeItem.getDyeColor() != dyeColor) {
            if (!level.isClientSide) {
                final GenericNixieDisplayBlockEntity be = (GenericNixieDisplayBlockEntity) level.getBlockEntity(pos);
                be.applyToEachElementOfThisStructure((display) -> {
                    final DyeColor newColor = dyeItem.getDyeColor();
                    final BlockState newState = BnbBlocks.DYED_LARGE_NIXIE_TUBE.get(newColor).getDefaultState()
                            .setValue(FACING, display.getBlockState().getValue(FACING))
                            .setValue(ORIENTATION, display.getBlockState().getValue(ORIENTATION))
                            .setValue(LIT, display.getBlockState().getValue(LIT));
                    level.setBlockAndUpdate(display.getBlockPos(), newState);
                    final GenericNixieDisplayBlockEntity newBe = (GenericNixieDisplayBlockEntity) level.getBlockEntity(display.getBlockPos());
                    newBe.inheritDataFrom(display);
                });
            }
            return ItemInteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected VoxelShape getShape(final BlockState state, final BlockGetter level, final BlockPos pos, final CollisionContext context) {
        final Direction frontTarget = DoubleOrientedDirections.getFront(state.getValue(FACING), state.getValue(ORIENTATION));
        final boolean isFront = frontTarget.getAxis() == state.getValue(ORIENTATION).getAxis();
        return isFront ? BnbShapes.LARGE_NIXIE_TUBE_SIDE.get(state.getValue(FACING))
                : BnbShapes.LARGE_NIXIE_TUBE_FRONT.get(state.getValue(FACING));
    }

    @Override
    public Class<GenericNixieDisplayBlockEntity> getBlockEntityClass() {
        return GenericNixieDisplayBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends GenericNixieDisplayBlockEntity> getBlockEntityType() {
        return BnbBlockEntities.GENERIC_NIXIE_DISPLAY.get();
    }

    public DyeColor getDyeColor() {
        return dyeColor != null ? dyeColor : DyeColor.ORANGE;
    }

    public List<GenericNixieDisplayBlockEntity.ConfigurableDisplayOptions> getPossibleDisplayOptions() {
        return List.of(GenericNixieDisplayBlockEntity.ConfigurableDisplayOptions.NONE, GenericNixieDisplayBlockEntity.ConfigurableDisplayOptions.ALWAYS_UP);
    }

}
