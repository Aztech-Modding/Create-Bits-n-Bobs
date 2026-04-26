package com.kipti.bnb.content.decoration.dyeable;

import com.cake.azimuth.behaviour.SuperBlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public abstract class BaseDyeableBehaviour extends SuperBlockEntityBehaviour {

    @Nullable
    private DyeColor color;

    protected BaseDyeableBehaviour(final SmartBlockEntity be) {
        super(be);
    }

    @Nullable
    public DyeColor getColor() {
        return this.color;
    }

    public void applyColorClientOnly(@Nullable final DyeColor color) {
        if (this.color == color) {
            return;
        }
        this.color = color;
        this.refreshRenderedModel();
    }

    public void setColor(@Nullable final DyeColor color) {
        if (this.color == color) {
            return;
        }
        this.color = color;
        this.onColorChanged(color);
        this.refreshOrNotifyUpdate();
    }

    protected void onColorChanged(@Nullable final DyeColor color) {
    }

    protected void refreshOrNotifyUpdate() {
        if (this.hasLevel() && this.getLevel().isClientSide) {
            this.refreshRenderedModel();
        } else {
            this.blockEntity.notifyUpdate();
        }
    }

    @Override
    public void write(final CompoundTag nbt, final HolderLookup.Provider registries, final boolean clientPacket) {
        super.write(nbt, registries, clientPacket);
        this.writeDyeColor(nbt);
        this.writeAdditionalDyeData(nbt, registries, clientPacket);
    }

    protected void writeDyeColor(final CompoundTag nbt) {
        if (this.color != null) {
            nbt.putInt("DyeColor", this.color.getId());
        }
    }

    protected void writeAdditionalDyeData(
            final CompoundTag nbt,
            final HolderLookup.Provider registries,
            final boolean clientPacket
    ) {
    }

    @Override
    public void read(final CompoundTag nbt, final HolderLookup.Provider registries, final boolean clientPacket) {
        super.read(nbt, registries, clientPacket);
        final boolean dyeColorChanged = this.readDyeColor(nbt);
        final boolean additionalDyeDataChanged = this.readAdditionalDyeData(nbt, registries, clientPacket);
        if (clientPacket && (dyeColorChanged || additionalDyeDataChanged)) {
            this.refreshRenderedModel();
        }
    }

    protected boolean readDyeColor(final CompoundTag nbt) {
        final DyeColor previousColor = this.color;
        if (nbt.contains("DyeColor")) {
            this.color = DyeColor.byId(nbt.getInt("DyeColor"));
        } else {
            this.color = null;
        }
        return previousColor != this.color;
    }

    protected boolean readAdditionalDyeData(
            final CompoundTag nbt,
            final HolderLookup.Provider registries,
            final boolean clientPacket
    ) {
        return false;
    }

    public void refreshRenderedModel() {
        this.blockEntity.requestModelDataUpdate();
        if (this.hasLevel()) {
            final Level level = this.getLevel();
            final BlockPos pos = this.getPos();
            level.sendBlockUpdated(pos, this.getBlockState(), this.getBlockState(), 16);
        }
    }

}
