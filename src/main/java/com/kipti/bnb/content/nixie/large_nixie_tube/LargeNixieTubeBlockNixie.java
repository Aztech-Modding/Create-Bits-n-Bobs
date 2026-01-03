package com.kipti.bnb.content.nixie.large_nixie_tube;

import com.kipti.bnb.content.nixie.foundation.*;
import com.kipti.bnb.registry.BnbBlockEntities;
import com.kipti.bnb.registry.BnbBlocks;
import com.kipti.bnb.registry.BnbShapes;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import net.createmod.catnip.placement.IPlacementHelper;
import net.createmod.catnip.placement.PlacementHelpers;
import net.createmod.catnip.placement.PlacementOffset;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public class LargeNixieTubeBlockNixie extends GenericNixieDisplayBlock implements IBE<GenericNixieDisplayBlockEntity>, IWrenchable, IGenericNixieDisplayBlock, DyeProviderBlock {

    private final @Nullable DyeColor dyeColor;
    private static final int placementHelperId = PlacementHelpers.register(new PlacementHelper());

    public LargeNixieTubeBlockNixie(final Properties p_52591_, @Nullable final DyeColor dyeColor) {
        super(p_52591_);
        this.dyeColor = dyeColor;
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        return BnbBlocks.LARGE_NIXIE_TUBE.asItem().getDefaultInstance();
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand interactionHand, BlockHitResult hitResult) {
        final ItemStack heldItem = player.getItemInHand(interactionHand);
        final IPlacementHelper placementHelper = PlacementHelpers.get(placementHelperId);
        if (!player.isShiftKeyDown() && player.mayBuild()) {
            if (placementHelper.matchesItem(stack)) {
                placementHelper.getOffset(player, level, state, pos, hitResult)
                        .placeInWorld(level, (BlockItem) stack.getItem(), player, hand, hitResult);
                return InteractionResult.SUCCESS;
            }
        }
        return super.use(state, level, pos, player, interactionHand, hitResult);
    }

    @Override
    public VoxelShape getShape(final BlockState state, final BlockGetter level, final BlockPos pos, final CollisionContext context) {
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

    @MethodsReturnNonnullByDefault
    private static class PlacementHelper implements IPlacementHelper {
        @Override
        public Predicate<ItemStack> getItemPredicate() {
            return BnbBlocks.LARGE_NIXIE_TUBE::isIn;
        }

        @Override
        public Predicate<BlockState> getStatePredicate() {
            return s -> s.getBlock() instanceof LargeNixieTubeBlockNixie;
        }

        @Override
        public PlacementOffset getOffset(@NotNull final Player player, @NotNull final Level world, final BlockState state, @NotNull final BlockPos pos, final BlockHitResult ray) {
            final List<Direction> directions = IPlacementHelper.orderedByDistanceOnlyAxis(pos, ray.getLocation(),
                    DoubleOrientedDirections.getLeft(state.getValue(LargeNixieTubeBlockNixie.ORIENTATION), state.getValue(LargeNixieTubeBlockNixie.FACING)).getAxis(), dir -> world.getBlockState(pos.relative(dir)).canBeReplaced());

            if (directions.isEmpty()) {
                return PlacementOffset.fail();
            } else {
                final BlockPos newPos = pos.relative(directions.get(0));

                return PlacementOffset.success(newPos, sourceState -> state);
            }
        }
    }

}
