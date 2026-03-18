package com.kipti.bnb.content.decoration.grating;

import net.createmod.catnip.placement.IPlacementHelper;
import net.createmod.catnip.placement.PlacementOffset;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;

public interface IGratingPanel {

    @MethodsReturnNonnullByDefault
    class PlacementHelper implements IPlacementHelper {
        @Override
        public Predicate<ItemStack> getItemPredicate() {
            return itemStack -> (itemStack.getItem() instanceof final BlockItem blockItem && blockItem.getBlock() instanceof IGratingPanel);
        }

        @Override
        public Predicate<BlockState> getStatePredicate() {
            return blockState -> blockState.getBlock() instanceof IGratingPanel;
        }

        @Override
        public PlacementOffset getOffset(@NotNull final Player player, @NotNull final Level world, final BlockState state, @NotNull final BlockPos pos,
                                         final BlockHitResult ray) {
            final List<Direction> directions = IPlacementHelper.orderedByDistanceExceptAxis(pos, ray.getLocation(),
                    state.getValue(GratingPanelBlock.FACING)
                            .getAxis(),
                    dir -> world.getBlockState(pos.relative(dir))
                            .canBeReplaced());

            if (directions.isEmpty())
                return PlacementOffset.fail();
            else {
                return PlacementOffset.success(pos.relative(directions.getFirst()),
                        s -> s.setValue(GratingPanelBlock.FACING, state.getValue(GratingPanelBlock.FACING)));
            }
        }
    }

}
