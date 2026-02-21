package com.kipti.bnb.content.decoration.weathered_girder;

import com.kipti.bnb.registry.content.blocks.deco.BnbDecorativeBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.decoration.girder.GirderBlock;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.outliner.Outliner;
import net.createmod.catnip.placement.IPlacementHelper;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Exact mirror pretty much of {@link WeatheredGirderWrenchBehaviour} but with the occasional reference changed to the weathered variant
 */
public class WeatheredGirderWrenchBehaviour {

    @OnlyIn(Dist.CLIENT)
    public static void tick() {
        final Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || !(mc.hitResult instanceof final BlockHitResult result))
            return;

        final ClientLevel world = mc.level;
        final BlockPos pos = result.getBlockPos();
        final Player player = mc.player;
        final ItemStack heldItem = player.getMainHandItem();

        if (player.isShiftKeyDown())
            return;

        if (!BnbDecorativeBlocks.WEATHERED_METAL_GIRDER.has(world.getBlockState(pos)))
            return;

        if (!AllItems.WRENCH.isIn(heldItem))
            return;

        final Pair<Direction, WeatheredGirderWrenchBehaviour.Action> dirPair = getDirectionAndAction(result, world, pos);
        if (dirPair == null)
            return;

        final Vec3 center = VecHelper.getCenterOf(pos);
        final Vec3 edge = center.add(Vec3.atLowerCornerOf(dirPair.getFirst()
                        .getNormal())
                .scale(0.4));
        final Direction.Axis[] axes = Arrays.stream(Iterate.axes)
                .filter(axis -> axis != dirPair.getFirst()
                        .getAxis())
                .toArray(Direction.Axis[]::new);

        double normalMultiplier = dirPair.getSecond() == WeatheredGirderWrenchBehaviour.Action.PAIR ? 4 : 1;
        final Vec3 corner1 = edge
                .add(Vec3.atLowerCornerOf(Direction.fromAxisAndDirection(axes[0], Direction.AxisDirection.POSITIVE)
                                .getNormal())
                        .scale(0.3))
                .add(Vec3.atLowerCornerOf(Direction.fromAxisAndDirection(axes[1], Direction.AxisDirection.POSITIVE)
                                .getNormal())
                        .scale(0.3))
                .add(Vec3.atLowerCornerOf(dirPair.getFirst()
                                .getNormal())
                        .scale(0.1 * normalMultiplier));

        normalMultiplier = dirPair.getSecond() == WeatheredGirderWrenchBehaviour.Action.HORIZONTAL ? 9 : 2;
        final Vec3 corner2 = edge
                .add(Vec3.atLowerCornerOf(Direction.fromAxisAndDirection(axes[0], Direction.AxisDirection.NEGATIVE)
                                .getNormal())
                        .scale(0.3))
                .add(Vec3.atLowerCornerOf(Direction.fromAxisAndDirection(axes[1], Direction.AxisDirection.NEGATIVE)
                                .getNormal())
                        .scale(0.3))
                .add(Vec3.atLowerCornerOf(dirPair.getFirst()
                                .getOpposite()
                                .getNormal())
                        .scale(0.1 * normalMultiplier));

        Outliner.getInstance().showAABB("girderWrench", new AABB(corner1, corner2))
                .lineWidth(1 / 32f)
                .colored(new Color(127, 127, 127));
    }

    @Nullable
    private static Pair<Direction, WeatheredGirderWrenchBehaviour.Action> getDirectionAndAction(final BlockHitResult result, final Level world, final BlockPos pos) {
        final List<Pair<Direction, WeatheredGirderWrenchBehaviour.Action>> validDirections = getValidDirections(world, pos);

        if (validDirections.isEmpty())
            return null;

        final List<Direction> directions = IPlacementHelper.orderedByDistance(pos, result.getLocation(),
                validDirections.stream()
                        .map(Pair::getFirst)
                        .toList());

        if (directions.isEmpty())
            return null;

        final Direction dir = directions.get(0);
        return validDirections.stream()
                .filter(pair -> pair.getFirst() == dir)
                .findFirst()
                .orElseGet(() -> Pair.of(dir, WeatheredGirderWrenchBehaviour.Action.SINGLE));
    }

    public static List<Pair<Direction, WeatheredGirderWrenchBehaviour.Action>> getValidDirections(final BlockGetter level, final BlockPos pos) {
        final BlockState blockState = level.getBlockState(pos);

        if (!BnbDecorativeBlocks.WEATHERED_METAL_GIRDER.has(blockState))
            return Collections.emptyList();

        return Arrays.stream(Iterate.directions)
                .<Pair<Direction, WeatheredGirderWrenchBehaviour.Action>>mapMulti((direction, consumer) -> {
                    final BlockState other = level.getBlockState(pos.relative(direction));

                    if (!blockState.getValue(GirderBlock.X) && !blockState.getValue(GirderBlock.Z))
                        return;

                    // up and down
                    if (direction.getAxis() == Direction.Axis.Y) {
                        // no other girder in target dir
                        if (!BnbDecorativeBlocks.WEATHERED_METAL_GIRDER.has(other)) {
                            if (!blockState.getValue(GirderBlock.X) ^ !blockState.getValue(GirderBlock.Z))
                                consumer.accept(Pair.of(direction, WeatheredGirderWrenchBehaviour.Action.SINGLE));
                            return;
                        }
                        // this girder is a pole or cross
                        if (blockState.getValue(GirderBlock.X) == blockState.getValue(GirderBlock.Z))
                            return;
                        // other girder is a pole or cross
                        if (other.getValue(GirderBlock.X) == other.getValue(GirderBlock.Z))
                            return;
                        // toggle up/down connection for both
                        consumer.accept(Pair.of(direction, WeatheredGirderWrenchBehaviour.Action.PAIR));

                        return;
                    }

//					if (BnbBlocksBootstrap.WEATHERED_METAL_GIRDER.has(other))
//						consumer.accept(Pair.of(direction, Action.HORIZONTAL));

                })
                .toList();
    }

    public static boolean handleClick(final Level level, final BlockPos pos, final BlockState state, final BlockHitResult result) {
        final Pair<Direction, WeatheredGirderWrenchBehaviour.Action> dirPair = getDirectionAndAction(result, level, pos);
        if (dirPair == null)
            return false;
        if (level.isClientSide)
            return true;
        if (!state.getValue(GirderBlock.X) && !state.getValue(GirderBlock.Z))
            return false;

        final Direction dir = dirPair.getFirst();

        final BlockPos otherPos = pos.relative(dir);
        final BlockState other = level.getBlockState(otherPos);

        if (dir == Direction.UP) {
            level.setBlock(pos, postProcess(state.cycle(GirderBlock.TOP)), 2 | 16);
            if (dirPair.getSecond() == WeatheredGirderWrenchBehaviour.Action.PAIR && BnbDecorativeBlocks.WEATHERED_METAL_GIRDER.has(other))
                level.setBlock(otherPos, postProcess(other.cycle(GirderBlock.BOTTOM)), 2 | 16);
            return true;
        }

        if (dir == Direction.DOWN) {
            level.setBlock(pos, postProcess(state.cycle(GirderBlock.BOTTOM)), 2 | 16);
            if (dirPair.getSecond() == WeatheredGirderWrenchBehaviour.Action.PAIR && BnbDecorativeBlocks.WEATHERED_METAL_GIRDER.has(other))
                level.setBlock(otherPos, postProcess(other.cycle(GirderBlock.TOP)), 2 | 16);
            return true;
        }

//		if (dirPair.getSecond() == Action.HORIZONTAL) {
//			BooleanProperty property = dir.getAxis() == Direction.Axis.X ? GirderBlock.X : GirderBlock.Z;
//			level.setBlock(pos, state.cycle(property), 2 | 16);
//
//			return true;
//		}

        return true;
    }

    private static BlockState postProcess(final BlockState newState) {
        if (newState.getValue(GirderBlock.TOP) && newState.getValue(GirderBlock.BOTTOM))
            return newState;
        if (newState.getValue(GirderBlock.AXIS) != Direction.Axis.Y)
            return newState;
        return newState.setValue(GirderBlock.AXIS, newState.getValue(GirderBlock.X) ? Direction.Axis.X : Direction.Axis.Z);
    }

    private enum Action {
        SINGLE, PAIR, HORIZONTAL
    }
}

