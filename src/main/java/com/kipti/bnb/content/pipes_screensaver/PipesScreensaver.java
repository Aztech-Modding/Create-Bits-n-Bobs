package com.kipti.bnb.content.pipes_screensaver;

import com.kipti.bnb.content.dyeable_pipes.DyeablePipeBehaviour;
import com.mojang.brigadier.Command;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.*;

@EventBusSubscriber
public class PipesScreensaver {

    private static final BlockPos MIN = new BlockPos(-5, -48, -5);
    private static final BlockPos MAX = new BlockPos(4, -39, 4);
    private static final int MAX_ACTIVE_PATHS = 8;
    private static final int TICKS_PER_STEP = 2;
    private static final int HOLD_TICKS = 10;
    private static final int WAVE_GAP_TICKS = 14;
    private static final int MIN_PATH_DISTANCE = 8;
    private static final int MIN_SEGMENTS = 2;
    private static final int MAX_SEGMENTS = 6;
    private static final int MAX_GENERATION_ATTEMPTS = 160;

    private static final Direction[] DIRECTIONS = Direction.values();
    private static final Direction.Axis[] AXES = Direction.Axis.values();
    private static final DyeColor[] COLORS = {
            DyeColor.CYAN,
            DyeColor.LIGHT_BLUE,
            DyeColor.LIME,
            DyeColor.MAGENTA,
            DyeColor.ORANGE,
            DyeColor.PINK,
            DyeColor.PURPLE,
            DyeColor.RED,
            DyeColor.YELLOW
    };

    private static final Random RANDOM = new Random();
    private static final List<ActivePath> ACTIVE_PATHS = new ArrayList<>();
    private static final Set<BlockPos> RESERVED_POSITIONS = new HashSet<>();

    private static boolean running = true;
    private static int tickCounter;
    private static int waveGapTicks;

    private enum Phase {
        BUILDING,
        HOLDING,
        ERASING
    }

    private static final class ActivePath {
        private final List<BlockPos> positions;
        private final DyeColor color;
        private int builtCount;
        private int tailIndex;
        private int holdTicks;
        private Phase phase = Phase.BUILDING;

        private ActivePath(final List<BlockPos> positions, final DyeColor color) {
            this.positions = positions;
            this.color = color;
        }
    }

    @SubscribeEvent
    public static void onLevelTick(final LevelTickEvent.Post event) {
        final Level level = event.getLevel();
        if (level.isClientSide() || !level.dimension().equals(Level.OVERWORLD) || !running) {
            return;
        }

        tickCounter++;
        if (tickCounter % TICKS_PER_STEP != 0) {
            return;
        }

        if (ACTIVE_PATHS.isEmpty()) {
            if (waveGapTicks > 0) {
                waveGapTicks--;
            } else {
                spawnWave(level);
            }
        }

        final Iterator<ActivePath> iterator = ACTIVE_PATHS.iterator();
        while (iterator.hasNext()) {
            final ActivePath path = iterator.next();
            if (!advancePath(level, path)) {
                releasePath(path);
                iterator.remove();
            }
        }

        if (ACTIVE_PATHS.isEmpty() && waveGapTicks < 0) {
            waveGapTicks = WAVE_GAP_TICKS;
        }
    }

    public static int startDemo(final CommandSourceStack source) {
        running = true;
        waveGapTicks = 0;
        source.sendSuccess(() -> Component.literal("Pipe screensaver started."), true);
        return Command.SINGLE_SUCCESS;
    }

    public static int stopDemo(final CommandSourceStack source) {
        running = false;
        clearAllPlacedPipes(source.getServer().overworld());
        source.sendSuccess(() -> Component.literal("Pipe screensaver stopped and cleared."), true);
        return Command.SINGLE_SUCCESS;
    }

    private static void spawnWave(final Level level) {
        ACTIVE_PATHS.clear();
        RESERVED_POSITIONS.clear();

        int spawned = 0;
        int attempts = 0;
        while (spawned < MAX_ACTIVE_PATHS && attempts < MAX_ACTIVE_PATHS * 12) {
            final ActivePath path = generatePath(level);
            attempts++;
            if (path == null) {
                continue;
            }

            ACTIVE_PATHS.add(path);
            RESERVED_POSITIONS.addAll(path.positions);
            spawned++;
        }
    }

    private static boolean advancePath(final Level level, final ActivePath path) {
        switch (path.phase) {
            case BUILDING -> {
                if (!buildNextSegment(level, path)) {
                    clearBuiltSegments(level, path);
                    return false;
                }

                if (path.builtCount >= path.positions.size()) {
                    path.phase = Phase.HOLDING;
                    path.holdTicks = HOLD_TICKS;
                }
                return true;
            }
            case HOLDING -> {
                path.holdTicks--;
                if (path.holdTicks <= 0) {
                    path.phase = Phase.ERASING;
                }
                return true;
            }
            case ERASING -> {
                eraseTail(level, path);
                return path.tailIndex < path.builtCount;
            }
        }

        return false;
    }

    private static boolean buildNextSegment(final Level level, final ActivePath path) {
        final BlockPos pos = path.positions.get(path.builtCount);
        if (!canUse(level, pos)) {
            return false;
        }

        level.setBlock(pos, AllBlocks.FLUID_PIPE.getDefaultState(), 3);
        applyColor(level, pos, path.color);
        refreshTouchedPipes(level, pos);
        path.builtCount++;
        return true;
    }

    private static void eraseTail(final Level level, final ActivePath path) {
        if (path.tailIndex >= path.builtCount) {
            return;
        }

        clearPipe(level, path.positions.get(path.tailIndex));
        path.tailIndex++;
    }

    private static void clearBuiltSegments(final Level level, final ActivePath path) {
        for (int index = path.tailIndex; index < path.builtCount; index++) {
            clearPipe(level, path.positions.get(index));
        }
    }

    private static void clearPipe(final Level level, final BlockPos pos) {
        if (level.getBlockState(pos).is(AllBlocks.FLUID_PIPE.get())) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            refreshTouchedPipes(level, pos);
        }
    }

    private static void clearAllPlacedPipes(final ServerLevel level) {
        ACTIVE_PATHS.clear();
        RESERVED_POSITIONS.clear();
        waveGapTicks = 0;

        for (int x = MIN.getX(); x <= MAX.getX(); x++) {
            for (int y = MIN.getY(); y <= MAX.getY(); y++) {
                for (int z = MIN.getZ(); z <= MAX.getZ(); z++) {
                    clearPipe(level, new BlockPos(x, y, z));
                }
            }
        }
    }

    private static void releasePath(final ActivePath path) {
        RESERVED_POSITIONS.removeAll(path.positions);
    }

    private static ActivePath generatePath(final Level level) {
        for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++) {
            final BlockPos start = randomBoundaryPosition();
            final BlockPos end = randomBoundaryPosition();
            final int distance = manhattanDistance(start, end);
            final int differingAxes = differingAxes(start, end);

            if (start.equals(end) || differingAxes < 2 || distance < MIN_PATH_DISTANCE) {
                continue;
            }

            final int minSegments = Math.max(MIN_SEGMENTS, differingAxes);
            final int maxSegments = Math.min(MAX_SEGMENTS, distance);
            if (minSegments > maxSegments) {
                continue;
            }

            final int segmentCount = randomBetween(minSegments, maxSegments);
            final List<Direction.Axis> axisSequence = buildAxisSequence(start, end, segmentCount);
            if (axisSequence == null) {
                continue;
            }

            final List<BlockPos> positions = tracePositions(start, end, axisSequence);
            if (positions == null || !isPathUsable(level, positions)) {
                continue;
            }

            return new ActivePath(positions, COLORS[RANDOM.nextInt(COLORS.length)]);
        }

        return null;
    }

    private static List<Direction.Axis> buildAxisSequence(
            final BlockPos start,
            final BlockPos end,
            final int segmentCount
    ) {
        final int[] deltas = absoluteDeltas(start, end);

        for (int attempt = 0; attempt < 32; attempt++) {
            final int[] segmentCounts = allocateSegmentCounts(deltas, segmentCount);
            if (segmentCounts == null) {
                continue;
            }

            final List<Direction.Axis> sequence = new ArrayList<>(segmentCount);
            if (appendAxes(sequence, segmentCounts, -1)) {
                return sequence;
            }
        }

        return null;
    }

    private static int[] allocateSegmentCounts(final int[] deltas, final int segmentCount) {
        final int[] counts = new int[AXES.length];
        int remaining = segmentCount;

        for (int axis = 0; axis < AXES.length; axis++) {
            if (deltas[axis] > 0) {
                counts[axis] = 1;
                remaining--;
            }
        }

        if (remaining < 0) {
            return null;
        }

        while (remaining > 0) {
            final List<Integer> candidates = new ArrayList<>();
            for (int axis = 0; axis < AXES.length; axis++) {
                if (counts[axis] < deltas[axis]) {
                    candidates.add(axis);
                }
            }

            if (candidates.isEmpty()) {
                return null;
            }

            final int axis = candidates.get(RANDOM.nextInt(candidates.size()));
            counts[axis]++;
            remaining--;
        }

        return canAlternate(counts) ? counts : null;
    }

    private static boolean appendAxes(
            final List<Direction.Axis> sequence,
            final int[] remainingCounts,
            final int previousAxis
    ) {
        if (sum(remainingCounts) == 0) {
            return true;
        }

        final List<Integer> candidates = new ArrayList<>();
        for (int axis = 0; axis < remainingCounts.length; axis++) {
            if (remainingCounts[axis] > 0 && axis != previousAxis) {
                candidates.add(axis);
            }
        }

        Collections.shuffle(candidates, RANDOM);
        candidates.sort((left, right) -> Integer.compare(remainingCounts[right], remainingCounts[left]));

        for (final int axis : candidates) {
            remainingCounts[axis]--;
            sequence.add(AXES[axis]);
            if (appendAxes(sequence, remainingCounts, axis)) {
                return true;
            }
            sequence.remove(sequence.size() - 1);
            remainingCounts[axis]++;
        }

        return false;
    }

    private static List<BlockPos> tracePositions(
            final BlockPos start,
            final BlockPos end,
            final List<Direction.Axis> axisSequence
    ) {
        final int[] signedDeltas = signedDeltas(start, end);
        final int[] axisCounts = new int[AXES.length];
        for (final Direction.Axis axis : axisSequence) {
            axisCounts[axisIndex(axis)]++;
        }

        @SuppressWarnings("unchecked") final Deque<Integer>[] axisLengths = new Deque[AXES.length];
        for (int axis = 0; axis < AXES.length; axis++) {
            axisLengths[axis] = new ArrayDeque<>();
            final int partCount = axisCounts[axis];
            if (partCount == 0) {
                continue;
            }

            for (final int length : partition(Math.abs(signedDeltas[axis]), partCount)) {
                axisLengths[axis].addLast(length);
            }
        }

        final List<BlockPos> positions = new ArrayList<>(manhattanDistance(start, end) + 1);
        final Set<BlockPos> visited = new HashSet<>();
        BlockPos current = start;
        positions.add(current);
        visited.add(current);

        for (final Direction.Axis axis : axisSequence) {
            final int axisIndex = axisIndex(axis);
            final Direction direction = directionFor(axis, Integer.signum(signedDeltas[axisIndex]));
            final int length = axisLengths[axisIndex].removeFirst();

            for (int step = 0; step < length; step++) {
                current = current.relative(direction);
                if (!visited.add(current)) {
                    return null;
                }
                positions.add(current);
            }
        }

        return current.equals(end) ? positions : null;
    }

    private static int[] partition(final int total, final int parts) {
        final int[] lengths = new int[parts];
        Arrays.fill(lengths, 1);

        int remaining = total - parts;
        while (remaining > 0) {
            lengths[RANDOM.nextInt(parts)]++;
            remaining--;
        }

        return lengths;
    }

    private static boolean isPathUsable(final Level level, final List<BlockPos> positions) {
        for (final BlockPos pos : positions) {
            if (!inBounds(pos) || RESERVED_POSITIONS.contains(pos) || !canUse(level, pos)) {
                return false;
            }
        }

        return true;
    }

    private static boolean canUse(final Level level, final BlockPos pos) {
        return level.isEmptyBlock(pos);
    }

    private static void refreshTouchedPipes(final Level level, final BlockPos center) {
        refreshPipe(level, center);
        for (final Direction direction : DIRECTIONS) {
            refreshPipe(level, center.relative(direction));
        }
    }

    private static void refreshPipe(final Level level, final BlockPos pos) {
        final BlockState state = level.getBlockState(pos);
        if (state.is(AllBlocks.FLUID_PIPE.get())) {
            DyeablePipeBehaviour.refreshPipeState(level, pos, state, false);
        }
    }

    private static void applyColor(final Level level, final BlockPos pos, final DyeColor color) {
        final BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof final SmartBlockEntity smartBlockEntity) {
            final DyeablePipeBehaviour behaviour = smartBlockEntity.getBehaviour(DyeablePipeBehaviour.TYPE);
            if (behaviour != null) {
                behaviour.setColor(color);
            }
        }
    }

    private static BlockPos randomBoundaryPosition() {
        int x = randomBetween(MIN.getX(), MAX.getX());
        int y = randomBetween(MIN.getY(), MAX.getY());
        int z = randomBetween(MIN.getZ(), MAX.getZ());

        switch (DIRECTIONS[RANDOM.nextInt(DIRECTIONS.length)]) {
            case DOWN -> y = MIN.getY();
            case UP -> y = MAX.getY();
            case NORTH -> z = MIN.getZ();
            case SOUTH -> z = MAX.getZ();
            case WEST -> x = MIN.getX();
            case EAST -> x = MAX.getX();
        }

        return new BlockPos(x, y, z);
    }

    private static int randomBetween(final int min, final int max) {
        return min + RANDOM.nextInt(max - min + 1);
    }

    private static int[] absoluteDeltas(final BlockPos start, final BlockPos end) {
        final int[] deltas = signedDeltas(start, end);
        for (int axis = 0; axis < deltas.length; axis++) {
            deltas[axis] = Math.abs(deltas[axis]);
        }
        return deltas;
    }

    private static int[] signedDeltas(final BlockPos start, final BlockPos end) {
        return new int[]{
                end.getX() - start.getX(),
                end.getY() - start.getY(),
                end.getZ() - start.getZ()
        };
    }

    private static boolean canAlternate(final int[] counts) {
        final int total = sum(counts);
        int largest = 0;
        for (final int count : counts) {
            largest = Math.max(largest, count);
        }
        return largest <= total - largest + 1;
    }

    private static int sum(final int[] values) {
        int total = 0;
        for (final int value : values) {
            total += value;
        }
        return total;
    }

    private static int axisIndex(final Direction.Axis axis) {
        return switch (axis) {
            case X -> 0;
            case Y -> 1;
            case Z -> 2;
        };
    }

    private static int differingAxes(final BlockPos start, final BlockPos end) {
        int count = 0;
        if (start.getX() != end.getX()) {
            count++;
        }
        if (start.getY() != end.getY()) {
            count++;
        }
        if (start.getZ() != end.getZ()) {
            count++;
        }
        return count;
    }

    private static int manhattanDistance(final BlockPos start, final BlockPos end) {
        return Math.abs(end.getX() - start.getX())
                + Math.abs(end.getY() - start.getY())
                + Math.abs(end.getZ() - start.getZ());
    }

    private static Direction directionFor(final Direction.Axis axis, final int sign) {
        return switch (axis) {
            case X -> sign >= 0 ? Direction.EAST : Direction.WEST;
            case Y -> sign >= 0 ? Direction.UP : Direction.DOWN;
            case Z -> sign >= 0 ? Direction.SOUTH : Direction.NORTH;
        };
    }

    private static boolean inBounds(final BlockPos pos) {
        return pos.getX() >= MIN.getX() && pos.getX() <= MAX.getX()
                && pos.getY() >= MIN.getY() && pos.getY() <= MAX.getY()
                && pos.getZ() >= MIN.getZ() && pos.getZ() <= MAX.getZ();
    }
}
