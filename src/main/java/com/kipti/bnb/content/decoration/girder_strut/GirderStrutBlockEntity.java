package com.kipti.bnb.content.decoration.girder_strut;

import com.kipti.bnb.content.decoration.girder_strut.connection.GirderConnectionNode;
import com.simibubi.create.api.contraption.transformable.TransformableBlockEntity;
import com.simibubi.create.content.contraptions.StructureTransform;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

//TODO: impl SpecialBlockEntityItemRequirement
public class GirderStrutBlockEntity extends SmartBlockEntity implements IBlockEntityRelighter, TransformableBlockEntity {

    private final Set<GirderConnectionNode> connections = new HashSet<>();
    private final Set<GirderConnectionNode> registeredConnections = new HashSet<>();
    private final Set<BlockPos> unresolvedLegacyConnections = new HashSet<>();
    public @Nullable SuperByteBuffer connectionRenderBufferCache;

    public GirderStrutBlockEntity(final BlockEntityType<?> type, final BlockPos pos, final BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide) {
            final StrutModelType modelType = getBlockState().getBlock() instanceof GirderStrutBlock block ? block.getModelType() : BnbStrutModels.NORMAL;
            for (final GirderConnectionNode data : connections) {
                if (registeredConnections.add(data)) {
                    com.kipti.bnb.content.decoration.girder_strut.structure.GirderStrutStructureShapes.registerConnection(
                            level, getBlockPos(), getAttachmentDirection(),
                            data.absoluteFrom(getBlockPos()), data.peerFacing(), modelType);
                }
            }
            tryResolveLegacyConnections();
        }
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        tryResolveLegacyConnections();
    }

    @Override
    public void remove() {
        super.remove();
        if (level != null && !level.isClientSide) {
            for (final GirderConnectionNode data : registeredConnections) {
                com.kipti.bnb.content.decoration.girder_strut.structure.GirderStrutStructureShapes.unregisterConnection(
                        level, getBlockPos(), data.absoluteFrom(getBlockPos()));
            }
            registeredConnections.clear();
        }
    }

    public void addConnection(final BlockPos other, final Direction otherFacing) {
        final GirderConnectionNode data = GirderConnectionNode.fromAbsolute(getBlockPos(), other, otherFacing);
        if (!other.equals(getBlockPos()) && connections.add(data)) {
            if (level != null && !level.isClientSide) {
                if (registeredConnections.add(data)) {
                    final StrutModelType modelType = getBlockState().getBlock() instanceof GirderStrutBlock block ? block.getModelType() : BnbStrutModels.NORMAL;
                    com.kipti.bnb.content.decoration.girder_strut.structure.GirderStrutStructureShapes.registerConnection(
                            level, getBlockPos(), getAttachmentDirection(), other, otherFacing, modelType);
                }
            }
            setChanged();
            sendData();
            notifyModelChange();
        }
    }

    public void removeConnection(final BlockPos pos) {
        GirderConnectionNode toRemove = null;
        final BlockPos relative = pos.subtract(getBlockPos());
        for (final GirderConnectionNode data : connections) {
            if (data.relativeOffset().equals(relative)) {
                toRemove = data;
                break;
            }
        }
        if (toRemove != null && connections.remove(toRemove)) {
            if (level != null && !level.isClientSide) {
                if (registeredConnections.remove(toRemove)) {
                    com.kipti.bnb.content.decoration.girder_strut.structure.GirderStrutStructureShapes.unregisterConnection(
                            level, getBlockPos(), pos);
                }
            }
            setChanged();
            sendData();
            notifyModelChange();
        }
    }

    public boolean hasConnectionTo(final BlockPos pos) {
        final BlockPos relative = pos.subtract(getBlockPos());
        for (final GirderConnectionNode data : connections) {
            if (data.relativeOffset().equals(relative)) {
                return true;
            }
        }
        return false;
    }

    public int connectionCount() {
        return connections.size();
    }

    public Set<GirderConnectionNode> getConnectionsCopy() {
        return Set.copyOf(connections);
    }

    @Override
    protected void write(final CompoundTag tag, final HolderLookup.Provider registries, final boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        final ListTag list = new ListTag();
        for (final GirderConnectionNode p : connections) {
            final CompoundTag ct = new CompoundTag();
            ct.putInt("X", p.relativeOffset().getX());
            ct.putInt("Y", p.relativeOffset().getY());
            ct.putInt("Z", p.relativeOffset().getZ());
            ct.putInt("Facing", p.peerFacing().get3DDataValue());
            list.add(ct);
        }
        for (final BlockPos unresolvedOffset : unresolvedLegacyConnections) {
            final CompoundTag ct = new CompoundTag();
            ct.putInt("X", unresolvedOffset.getX());
            ct.putInt("Y", unresolvedOffset.getY());
            ct.putInt("Z", unresolvedOffset.getZ());
            list.add(ct);
        }
        tag.put("Connections", list);
    }

    @Override
    protected void read(final CompoundTag tag, final HolderLookup.Provider registries, final boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        connections.clear();
        unresolvedLegacyConnections.clear();
        if (tag.contains("Connections", Tag.TAG_LIST)) {
            final ListTag list = tag.getList("Connections", Tag.TAG_COMPOUND);
            for (final Tag t : list) {
                if (t instanceof final CompoundTag ct) {
                    final BlockPos offset = new BlockPos(ct.getInt("X"), ct.getInt("Y"), ct.getInt("Z"));
                    if (ct.contains("Facing", Tag.TAG_INT)) {
                        final Direction facing = Direction.from3DDataValue(ct.getInt("Facing"));
                        connections.add(new GirderConnectionNode(offset, facing));
                    } else {
                        unresolvedLegacyConnections.add(offset);
                    }
                }
            }
        }
        if (clientPacket) {
            notifyModelChange();
        }
    }

    @Override
    public void addBehaviours(final List<BlockEntityBehaviour> behaviours) {
    }

    private void notifyModelChange() {
        if (level != null) {
            if (level.isClientSide) {
                requestModelDataUpdate();
            }
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    public void transform(final BlockEntity blockEntity, final StructureTransform transform) {
        final List<GirderConnectionNode> newConnections = connections.stream()
                .map(data -> {
                    final BlockPos transformedOffset = transform.applyWithoutOffset(data.relativeOffset());
                    final Direction transformedFacing = transform.rotateFacing(data.peerFacing());
                    return new GirderConnectionNode(transformedOffset, transformedFacing);
                })
                .filter(data -> !data.relativeOffset().equals(BlockPos.ZERO))
                .toList();
        connections.clear();

        connections.addAll(newConnections);

        if (!unresolvedLegacyConnections.isEmpty()) {
            final Set<BlockPos> transformedLegacy = new HashSet<>();
            for (final BlockPos unresolved : unresolvedLegacyConnections) {
                final BlockPos transformedOffset = transform.applyWithoutOffset(unresolved);
                if (!transformedOffset.equals(BlockPos.ZERO)) {
                    transformedLegacy.add(transformedOffset);
                }
            }
            unresolvedLegacyConnections.clear();
            unresolvedLegacyConnections.addAll(transformedLegacy);
        }
    }

    private void tryResolveLegacyConnections() {
        if (level == null || level.isClientSide || unresolvedLegacyConnections.isEmpty()) {
            return;
        }

        final Set<BlockPos> resolvedOffsets = new HashSet<>();
        for (final BlockPos unresolvedOffset : unresolvedLegacyConnections) {
            final BlockPos otherPosition = getBlockPos().offset(unresolvedOffset);
            Direction otherFacing = null;

            if (level.getBlockEntity(otherPosition) instanceof final GirderStrutBlockEntity otherBlockEntity) {
                otherFacing = otherBlockEntity.getAttachmentDirection();
            } else if (level.getBlockState(otherPosition).getBlock() instanceof GirderStrutBlock) {
                otherFacing = level.getBlockState(otherPosition).getValue(GirderStrutBlock.FACING);
            }

            if (otherFacing != null) {
                addConnection(otherPosition, otherFacing);
                resolvedOffsets.add(unresolvedOffset);
            }
        }

        if (!resolvedOffsets.isEmpty()) {
            unresolvedLegacyConnections.removeAll(resolvedOffsets);
            setChanged();
            sendData();
        }
    }

    public Vec3 getAttachment() {
        //TODO: use this in renderer to ensure parity always, even if its the same rn
        return Vec3.atCenterOf(getBlockPos()).relative(getBlockState().getValue(GirderStrutBlock.FACING), -0.4);
    }

    public Direction getAttachmentDirection() {
        return getBlockState().getValue(GirderStrutBlock.FACING);
    }
}

