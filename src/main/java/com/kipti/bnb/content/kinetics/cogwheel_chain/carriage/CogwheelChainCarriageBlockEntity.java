package com.kipti.bnb.content.kinetics.cogwheel_chain.carriage;

import com.kipti.bnb.content.kinetics.cogwheel_chain.attachment.CogwheelChainAttachments;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Block entity for the cogwheel chain carriage. Manages the chain attachment
 * state and triggers contraption assembly/disassembly.
 *
 * <p>When a player activates the carriage block, the attachment is resolved
 * and {@link #assembleNextTick} is set. On the next server tick the entity
 * attempts to build a contraption from the blocks below.</p>
 *
 * <p>TODO: Register in BnbBlockEntities.</p>
 */
public class CogwheelChainCarriageBlockEntity extends SmartBlockEntity {

    private static final String ATTACHMENT_TAG = "ChainAttachment";
    private static final String ASSEMBLED_TAG = "Assembled";

    @Nullable
    private CogwheelChainAttachments attachment;
    public boolean assembleNextTick;
    private boolean assembled;

    public CogwheelChainCarriageBlockEntity(final BlockEntityType<?> type,
                                             final BlockPos pos,
                                             final BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(final List<BlockEntityBehaviour> behaviours) {
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level == null || this.level.isClientSide)
            return;

        if (this.assembleNextTick) {
            this.assembleNextTick = false;
            this.assemble();
        }

        if (this.assembled && this.attachment != null) {
            this.tickAttachment();
        }
    }

    private void tickAttachment() {
        if (!this.attachment.isValid(this.level)) {
            this.disassemble();
            return;
        }
        this.attachment.tick(this.level);
    }

    /**
     * Assembles blocks below into a contraption that rides along the chain.
     *
     * <p>TODO: Create and spawn {@code CogwheelChainCarriageContraptionEntity}.
     * Follow Create's assembly pattern: build contraption, remove blocks from
     * world, create controlled contraption entity, add to level.</p>
     */
    private void assemble() {
        if (this.attachment == null)
            return;
        if (!this.attachment.isValid(this.level))
            return;

        this.assembled = true;
        this.sendData();
    }

    /**
     * Disassembles the contraption and detaches from the chain.
     */
    public void disassemble() {
        this.assembled = false;
        this.attachment = null;
        this.assembleNextTick = false;
        this.sendData();
    }

    public void setAttachment(final CogwheelChainAttachments attachment) {
        this.attachment = attachment;
        this.sendData();
    }

    @Nullable
    public CogwheelChainAttachments getAttachment() {
        return this.attachment;
    }

    public boolean isAssembled() {
        return this.assembled;
    }

    @Override
    protected void write(final CompoundTag tag, final HolderLookup.Provider registries, final boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putBoolean(ASSEMBLED_TAG, this.assembled);
        if (this.attachment != null) {
            final CompoundTag attachmentTag = new CompoundTag();
            this.attachment.write(attachmentTag);
            tag.put(ATTACHMENT_TAG, attachmentTag);
        }
    }

    @Override
    protected void read(final CompoundTag tag, final HolderLookup.Provider registries, final boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        this.assembled = tag.getBoolean(ASSEMBLED_TAG);
        if (tag.contains(ATTACHMENT_TAG)) {
            this.attachment = CogwheelChainAttachments.read(tag.getCompound(ATTACHMENT_TAG));
        } else {
            this.attachment = null;
        }
    }
}
