package com.kipti.bnb.content.kinetics.cogwheel_carriage.contraption;

import com.kipti.bnb.CreateBitsnBobs;
import com.kipti.bnb.content.kinetics.cogwheel_chain.attachment.CogwheelChainAttachment;
import com.kipti.bnb.registry.content.BnbContraptionTypes;
import com.simibubi.create.api.contraption.ContraptionType;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.AssemblyException;
import com.simibubi.create.content.contraptions.Contraption;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

import java.util.Queue;

public class CogwheelChainCarriageContraption extends Contraption {

    private CogwheelChainAttachment carriageAttachment;
    private Direction facing;

    public CogwheelChainCarriageContraption(final CogwheelChainAttachment centerAttachment) {
        this.carriageAttachment = centerAttachment;
    }

    public CogwheelChainCarriageContraption() {
    }

    @Override
    public boolean assemble(final Level level, final BlockPos pos) throws AssemblyException {
        if (this.carriageAttachment == null || !this.carriageAttachment.isValid(level)) {
            return false;
        }
        this.facing = level.getBlockState(pos).getValue(BlockStateProperties.HORIZONTAL_FACING);
        if (!this.searchMovedStructure(level, pos, null))
            return false;
        this.startMoving(level);
        this.expandBoundsAroundAxis(Direction.Axis.Y);
        return !this.blocks.isEmpty();
    }

    @Override
    public void onEntityInitialize(final Level world, final AbstractContraptionEntity contraptionEntity) {
        super.onEntityInitialize(world, contraptionEntity);
        if (contraptionEntity instanceof final CogwheelChainCarriageContraptionEntity cccce) {
            if (this.facing == null) {
                return;
            }
            cccce.setInitialOrientation(this.facing);
        }
    }

    @Override
    protected boolean addToInitialFrontier(final Level world,
                                           final BlockPos pos,
                                           final Direction forcedDirection,
                                           final Queue<BlockPos> frontier) throws AssemblyException {
        frontier.add(pos.relative(Direction.DOWN));
        return true;
    }

    @Override
    protected boolean isAnchoringBlockAt(final BlockPos pos) {
        return false; //Include anchor in assembly
    }

    @Override
    public boolean canBeStabilized(final Direction facing, final BlockPos localPos) {
        return false;
    }

    @Override
    public ContraptionType getType() {
        return BnbContraptionTypes.COGWHEEL_CHAIN_CARRIAGE.value();
    }

    @Override
    public CompoundTag writeNBT(final HolderLookup.Provider registries, final boolean spawnPacket) {
        final CompoundTag tag = super.writeNBT(registries, spawnPacket);

        if (this.carriageAttachment != null) {
            final CompoundTag attachmentTag = new CompoundTag();
            this.carriageAttachment.write(attachmentTag);
            tag.put("CenterAttachment", attachmentTag);
            if (this.facing != null) {
                tag.putInt("AnchorFacing", this.facing.ordinal());
            }
        }

        return tag;
    }

    @Override
    public void readNBT(final Level level, final CompoundTag nbt, final boolean spawnData) {
        if (nbt.contains("CenterAttachment")) {
            this.carriageAttachment = CogwheelChainAttachment.read(nbt.getCompound("CenterAttachment"));
            final Direction anchorFacing = readHorizontalDirection(nbt, "AnchorFacing");
            if (anchorFacing != null) {
                this.facing = anchorFacing;
            }
        }
        super.readNBT(level, nbt, spawnData);
    }

    @Nullable
    private static Direction readHorizontalDirection(final CompoundTag nbt, final String key) {
        if (!nbt.contains(key)) {
            return null;
        }
        final int directionIndex = nbt.getInt(key);
        final Direction[] directions = Direction.values();
        if (directionIndex < 0 || directionIndex >= directions.length) {
            warnInvalidHorizontalDirection(key, directionIndex);
            return null;
        }
        final Direction direction = directions[directionIndex];
        if (direction.getAxis().isVertical()) {
            warnInvalidHorizontalDirection(key, directionIndex);
            return null;
        }
        return direction;
    }

    private static void warnInvalidHorizontalDirection(final String key, final int directionIndex) {
        CreateBitsnBobs.LOGGER.warn(
                "Ignoring invalid {} value {} while reading cogwheel chain carriage contraption",
                key,
                directionIndex
        );
    }

    @Nullable
    public CogwheelChainAttachment getCarriageAttachment() {
        return this.carriageAttachment;
    }
}
