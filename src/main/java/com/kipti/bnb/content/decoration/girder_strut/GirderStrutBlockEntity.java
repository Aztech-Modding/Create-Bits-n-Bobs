package com.kipti.bnb.content.decoration.girder_strut;

import com.simibubi.create.api.contraption.transformable.TransformableBlockEntity;
import com.simibubi.create.api.schematic.requirement.SpecialBlockEntityItemRequirement;
import com.simibubi.create.content.contraptions.StructureTransform;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

//TODO: impl SpecialBlockEntityItemRequirement
public class GirderStrutBlockEntity extends SmartBlockEntity implements IBlockEntityRelighter, SpecialBlockEntityItemRequirement, TransformableBlockEntity {

    private final Set<BlockPos> connections = new HashSet<>();
    public @Nullable SuperByteBuffer connectionRenderBufferCache;

    public GirderStrutBlockEntity(final BlockEntityType<?> type, final BlockPos pos, final BlockState state) {
        super(type, pos, state);
    }

    public void addConnection(final BlockPos other) {
        if (!other.equals(getBlockPos()) && connections.add(other.immutable().subtract(getBlockPos()))) {
            setChanged();
            sendData();
            notifyModelChange();
        }
    }

    public void removeConnection(final BlockPos pos) {
        if (connections.remove(pos.subtract(getBlockPos()))) {
            setChanged();
            sendData();
            notifyModelChange();
        }
    }

    public boolean hasConnectionTo(final BlockPos pos) {
        return connections.contains(pos.subtract(getBlockPos()));
    }

    public int connectionCount() {
        return connections.size();
    }

    public Set<BlockPos> getConnectionsCopy() {
        return Set.copyOf(connections);
    }

    @Override
    protected void write(final CompoundTag tag, final HolderLookup.Provider registries, final boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        final ListTag list = new ListTag();
        for (final BlockPos p : connections) {
            final CompoundTag ct = new CompoundTag();
            ct.putInt("X", p.getX());
            ct.putInt("Y", p.getY());
            ct.putInt("Z", p.getZ());
            list.add(ct);
        }
        tag.put("Connections", list);
    }

    @Override
    protected void read(final CompoundTag tag, final HolderLookup.Provider registries, final boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        connections.clear();
        if (tag.contains("Connections", Tag.TAG_LIST)) {
            final ListTag list = tag.getList("Connections", Tag.TAG_COMPOUND);
            for (final Tag t : list) {
                if (t instanceof final CompoundTag ct) {
                    connections.add(new BlockPos(ct.getInt("X"), ct.getInt("Y"), ct.getInt("Z")));
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
        final List<BlockPos> newConnections = connections.stream()
                .map(transform::applyWithoutOffset)
                .filter(p -> !p.equals(getBlockPos()))
                .toList();
        connections.clear();

        connections.addAll(newConnections);
    }
}

