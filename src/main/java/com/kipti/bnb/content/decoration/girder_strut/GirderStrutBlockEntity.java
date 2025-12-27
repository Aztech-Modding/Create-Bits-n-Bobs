package com.kipti.bnb.content.decoration.girder_strut;

import com.simibubi.create.api.schematic.requirement.SpecialBlockEntityItemRequirement;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

//TODO: impl SpecialBlockEntityItemRequirement
public class GirderStrutBlockEntity extends SmartBlockEntity implements IBlockEntityRelighter, SpecialBlockEntityItemRequirement {

    private final Set<BlockPos> connections = new HashSet<>();
    public @Nullable SuperByteBuffer connectionRenderBufferCache;

    public GirderStrutBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void addConnection(BlockPos other) {
        if (!other.equals(getBlockPos()) && connections.add(other.immutable().subtract(getBlockPos()))) {
            setChanged();
            sendData();
            notifyModelChange();
        }
    }

    public void removeConnection(BlockPos pos) {
        if (connections.remove(pos.subtract(getBlockPos()))) {
            setChanged();
            sendData();
            notifyModelChange();
        }
    }

    public boolean hasConnectionTo(BlockPos pos) {
        return connections.contains(pos.subtract(getBlockPos()));
    }

    public int connectionCount() {
        return connections.size();
    }

    public Set<BlockPos> getConnectionsCopy() {
        return Set.copyOf(connections);
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        ListTag list = new ListTag();
        for (BlockPos p : connections) {
            CompoundTag ct = new CompoundTag();
            ct.putInt("X", p.getX());
            ct.putInt("Y", p.getY());
            ct.putInt("Z", p.getZ());
            list.add(ct);
        }
        tag.put("Connections", list);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        connections.clear();
        if (tag.contains("Connections", Tag.TAG_LIST)) {
            ListTag list = tag.getList("Connections", Tag.TAG_COMPOUND);
            for (Tag t : list) {
                if (t instanceof CompoundTag ct) {
                    connections.add(new BlockPos(ct.getInt("X"), ct.getInt("Y"), ct.getInt("Z")));
                }
            }
        }
        if (clientPacket) {
            notifyModelChange();
        }
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

    private void notifyModelChange() {
        if (level != null) {
            if (level.isClientSide) {
                requestModelDataUpdate();
            }
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

}
