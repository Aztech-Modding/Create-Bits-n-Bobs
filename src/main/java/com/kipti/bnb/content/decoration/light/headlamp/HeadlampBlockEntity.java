package com.kipti.bnb.content.decoration.light.headlamp;

import com.kipti.bnb.registry.BnbBlocks;
import com.kipti.bnb.registry.BnbShapes;
import com.simibubi.create.AllShapes;
import com.simibubi.create.api.schematic.requirement.SpecialBlockEntityItemRequirement;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class HeadlampBlockEntity extends SmartBlockEntity implements SpecialBlockEntityItemRequirement {

    private final int[] activePlacements = new int[9];

    public HeadlampBlockEntity(final BlockEntityType<?> type, final BlockPos pos, final BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(final List<BlockEntityBehaviour> behaviours) {

    }

    public List<HeadlampPlacement> getExistingPlacements() {
        final List<HeadlampPlacement> placements = new ArrayList<>();
        for (int i = 0; i < activePlacements.length; i++) {
            if (activePlacements[i] != 0) {
                placements.add(HeadlampPlacement.values()[i]);
            }
        }
        return placements;
    }

    @Override
    public void tick() {
        super.tick();
    }

    /**
     * Returns true if the headlamp was successfully placed into the block.
     */
    public boolean placeHeadlampIntoBlock(
            final Vec3 position,
            final Direction clickedDirection
    ) {
        final Vec3 localPosition = getLocalSurfacePosition(position, clickedDirection);

        final HeadlampPlacement placement = getClosestHeadlampPlacement(localPosition);
        final List<HeadlampPlacement> existingPlacements = getExistingPlacements();

        if (placement.hasCollisionWith(existingPlacements)) {
            return false;
        }
        final int index = placement.ordinal();
        this.activePlacements[index] = 1;
        sendData();
        return true;
    }

    public boolean canPlaceHeadlampIntoBlock(
            final Vec3 position,
            final Direction clickedDirection
    ) {
        final Vec3 localPosition = getLocalSurfacePosition(position, clickedDirection);

        final HeadlampPlacement placement = getClosestHeadlampPlacement(localPosition);
        final List<HeadlampPlacement> existingPlacements = getExistingPlacements();

        return !placement.hasCollisionWith(existingPlacements);
    }

    public void placeDyeColorIntoBlock(final DyeColor dyeColor, final Vec3 position, final Direction value) {
        final Vec3 localPosition = getLocalSurfacePosition(position, value);

        final HeadlampPlacement placement = getClosestExistingHeadlampPlacement(localPosition);
        if (placement == null) {
            return; // No existing placement found
        }
        final int index = placement.ordinal();

        final int i = dyeColor.ordinal() + 2;
        if (activePlacements[index] == i) {
            for (int j = 0; j < activePlacements.length; j++) {
                if (activePlacements[j] != i && activePlacements[j] != 0) {
                    return;
                }
            }
            tryExtendPlaceDyeColorIntoFullBlock(
                    dyeColor, getBlockState().getValue(HeadlampBlock.FACING), new ArrayList<>(List.of(getBlockPos())), new ArrayList<>()
            );
            return; // No change in color
        }
        this.activePlacements[index] = i; // 0 - no color, 1 - no dye, 2 - red, etc.
        sendData();
    }

    private void tryExtendPlaceDyeColorIntoFullBlock(final DyeColor dyeColor, final Direction facing, final List<BlockPos> frontier, final List<BlockPos> visited) {
        if (frontier.isEmpty()) {
            return; // No frontier to process
        }
        if (visited.size() > 32) {
            return;
        }
        final BlockPos currentPos = frontier.remove(0);
        if (visited.contains(currentPos)) {
            tryExtendPlaceDyeColorIntoFullBlock(dyeColor, facing, frontier, visited);
            return; // Already visited this relativePos
        }
        visited.add(currentPos);

        if (level.getBlockEntity(currentPos) instanceof final HeadlampBlockEntity otherHeadlamp) {
            if (otherHeadlamp.placeDyeColorIntoFullBlock(dyeColor)) {
                return;
            }
        }

        // Check adjacent blocks
        final List<Direction> directions = List.of(
                Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.UP, Direction.DOWN
        ).stream().filter(d -> d.getAxis() != facing.getAxis()).toList();
        for (final Direction direction : directions) {
            frontier.add(currentPos.relative(direction));
        }

        // Recursion :D
        tryExtendPlaceDyeColorIntoFullBlock(dyeColor, facing, frontier, visited);
    }

    public boolean removeNearestHeadlamp(final Vec3 subtract, final Direction value) {
        final Vec3 localPosition = getLocalSurfacePosition(subtract, value);

        final HeadlampPlacement closestPlacement = getClosestExistingHeadlampPlacement(localPosition);
        if (closestPlacement == null) {
            return false; // No existing placement found
        }
        final int index = closestPlacement.ordinal();
        this.activePlacements[index] = 0; // Remove the headlamp
        sendData();
        if (getExistingPlacements().isEmpty()) {
            level.removeBlock(worldPosition, false);
        }
        return true;
    }

    private static @NotNull Vec3 getLocalSurfacePosition(final Vec3 position, final Direction value) {
        // Transform the relativePos into a point on the xz plane, where x = leftright, z = updown.
        final Vector3f jomlLocalPosition = value.getRotation().transformInverse(new Vector3f((float) position.x, (float) position.y, (float) position.z));
        final Vec3 localPosition = new Vec3(
                jomlLocalPosition.x,
                jomlLocalPosition.y,
                jomlLocalPosition.z
        );
        return localPosition;
    }

    @Override
    protected void read(final CompoundTag tag, final HolderLookup.Provider registries, final boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        if (!tag.contains("activePlacements", 11)) {
            return;
        }
        final int[] placements = tag.getIntArray("activePlacements");
        if (placements.length != activePlacements.length) {
            throw new IllegalStateException("Active placements length mismatch: expected " + activePlacements.length + ", got " + placements.length);
        }
        System.arraycopy(placements, 0, activePlacements, 0, placements.length);
        if (clientPacket) {
            requestModelDataUpdate();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 16);
        }
    }

    @Override
    protected void write(final CompoundTag tag, final HolderLookup.Provider registries, final boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putIntArray("activePlacements", activePlacements);
    }

    /**
     * Target on surface is a point on the xz plane, x = leftright, z = updown.
     */
    public static HeadlampPlacement getClosestHeadlampPlacement(
            final Vec3 targetOnSurface
    ) {
        return new HeadlampPlacement(
                getAlignmentFromOffset(targetOnSurface.z),
                getAlignmentFromOffset(targetOnSurface.x)
        );
    }

    private @Nullable HeadlampPlacement getClosestExistingHeadlampPlacement(final Vec3 localPosition) {
        HeadlampPlacement closestPlacement = null;
        double closestDistance = Double.MAX_VALUE;
        for (final HeadlampPlacement placement : HeadlampPlacement.values()) {
            if (this.activePlacements[placement.ordinal()] == 0) {
                continue;
            }

            final double distance = Math.abs(localPosition.x - placement.horizontalAlignment.getOffset()) +
                    Math.abs(localPosition.z - placement.verticalAlignment.getOffset());

            if (distance < closestDistance) {
                closestDistance = distance;
                closestPlacement = placement;
            }
        }
        return closestPlacement;
    }

    private static HeadlampAlignment getAlignmentFromOffset(
            final double offset
    ) {
        if (offset < HeadlampAlignment.RIGHT_OR_BOTTOM.getOffset() / 2f) {
            return HeadlampAlignment.RIGHT_OR_BOTTOM;
        } else if (offset > HeadlampAlignment.LEFT_OR_TOP.getOffset() / 2f) {
            return HeadlampAlignment.LEFT_OR_TOP;
        } else {
            return HeadlampAlignment.MIDDLE;
        }
    }

    public int[] getActivePlacements() {
        return this.activePlacements;
    }

    public VoxelShape getShape(final BlockState state, final BlockGetter level, final BlockPos pos, final CollisionContext context) {
        final AllShapes.Builder builder = new AllShapes.Builder(Shapes.empty());
        final List<HeadlampPlacement> placements = getExistingPlacements();

        if (placements.isEmpty()) {
            return Shapes.block();
        }

        for (final HeadlampPlacement placement : placements) {
            if (activePlacements[placement.ordinal()] != 0) {
                builder.add(BnbShapes.cuboid(
                        4 + placement.horizontalAlignment.pixelOffset, 0, 4 + placement.verticalAlignment.pixelOffset,
                        12 + placement.horizontalAlignment.pixelOffset, 7, 12 + placement.verticalAlignment.pixelOffset
                ));
            }
        }
        return builder.forDirectional().get(state.getValue(HeadlampBlock.FACING));
    }

    public boolean placeDyeColorIntoFullBlock(final DyeColor dyeColor) {
        boolean placedAny = false;
        final int toAdd = dyeColor.ordinal() + 2;
        for (int i = 0; i < HeadlampPlacement.values().length; i++) {
            if (this.activePlacements[i] == 0 || this.activePlacements[i] == toAdd) {
                continue; // Skip placements without headlamps
            }
            placedAny = true;
            this.activePlacements[i] = toAdd; // 0 - no color, 1 - no dye, 2 - red, etc.
        }
        if (!placedAny) {
            return false; // No headlamps to place dye color into
        }
        sendData();
        return true;
    }

    public enum HeadlampAlignment {
        RIGHT_OR_BOTTOM(-4),
        MIDDLE(0),
        LEFT_OR_TOP(4);
        private final int pixelOffset;

        HeadlampAlignment(final int pixelOffset) {
            this.pixelOffset = pixelOffset;
        }

        public int getPixelOffset() {
            return pixelOffset;
        }

        public double getOffset() {
            return pixelOffset / 16.0;
        }

        public boolean collidesWith(final HeadlampAlignment horizontalAlignment) {
            return this == horizontalAlignment || this == MIDDLE || horizontalAlignment == MIDDLE;
        }
    }

    public record HeadlampPlacement(
            HeadlampAlignment verticalAlignment,
            HeadlampAlignment horizontalAlignment
    ) {
        private static final HeadlampPlacement[] VALUES = generateValues();

        private static HeadlampPlacement[] generateValues() {
            final List<HeadlampPlacement> placements = new ArrayList<>();
            for (final HeadlampAlignment vertical : HeadlampAlignment.values()) {
                for (final HeadlampAlignment horizontal : HeadlampAlignment.values()) {
                    placements.add(new HeadlampPlacement(vertical, horizontal));
                }
            }
            return placements.toArray(new HeadlampPlacement[0]);
        }

        public static HeadlampPlacement[] values() {
            return VALUES;
        }

        public boolean hasCollisionWith(final List<HeadlampPlacement> existingPlacements) {
            for (final HeadlampPlacement placement : existingPlacements) {
                if (placement.horizontalAlignment.collidesWith(horizontalAlignment) &&
                        placement.verticalAlignment.collidesWith(verticalAlignment)) {
                    return true;
                }
            }
            return false;
        }

        public int ordinal() {
            for (int i = 0; i < VALUES.length; i++) {
                if (VALUES[i].equals(this)) {
                    return i;
                }
            }
            throw new IllegalStateException("HeadlampPlacement not found in values array: " + this);
        }
    }

    @Override
    public ItemRequirement getRequiredItems(final BlockState state) {
        int numberOfHeadlamps = -1;
        for (final int placement : activePlacements)
            if (placement != 0)
                numberOfHeadlamps++;

        if (numberOfHeadlamps <= 0) {
            return ItemRequirement.NONE;
        }

        return new ItemRequirement(
                ItemRequirement.ItemUseType.CONSUME,
                BnbBlocks.HEADLAMP.asItem().getDefaultInstance().copyWithCount(numberOfHeadlamps)
        );
    }
}
