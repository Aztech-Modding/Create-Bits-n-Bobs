package com.kipti.bnb.content.kinetics.cogwheel_chain.carriage;

import com.kipti.bnb.content.kinetics.cogwheel_chain.attachment.CogwheelChainAttachment;
import com.kipti.bnb.content.kinetics.cogwheel_chain.attachment.CogwheelChainAttachmentHelper;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.contraptions.AssemblyException;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Block entity for the cogwheel chain carriage. Stores a chain attachment
 * resolved at activation time and uses it to assemble a contraption.
 *
 * <p>The attachment is not ticked here — the block entity is stationary.
 * Once assembled, the {@link CogwheelChainCarriageContraptionEntity} owns
 * the attachment and drives movement along the chain.</p>
 */
public class CogwheelChainCarriageBlockEntity extends SmartBlockEntity {

    private @Nullable AssemblyException lastException;
    public boolean assembleNextTick;

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
    }

    private void assemble() {
        final CogwheelChainAttachment attachment = CogwheelChainAttachmentHelper.findNearestAttachment(this.level, this.getBlockPos().getCenter());
        if (attachment == null) {
            this.lastException = new AssemblyException("bits_n_bobs.no_chain_to_attatch_to");
            return;
        }

        final CogwheelChainCarriageContraption contraption =
                new CogwheelChainCarriageContraption(attachment);
        try {
            if (!contraption.assemble(this.level, this.worldPosition)) {
                return;
            }
            this.lastException = null;
        } catch (final AssemblyException e) {
            this.lastException = e;
            this.sendData();
            return;
        }

        contraption.removeBlocksFromWorld(this.level, BlockPos.ZERO);

        final CogwheelChainCarriageContraptionEntity entity =
                CogwheelChainCarriageContraptionEntity.create(this.level, contraption);

        final Vec3 anchor = Vec3.atBottomCenterOf(this.worldPosition);
        entity.setPos(anchor.x, anchor.y, anchor.z);

        this.level.addFreshEntity(entity);
        AllSoundEvents.CONTRAPTION_ASSEMBLE.playOnServer(this.level, this.worldPosition);
    }

    @Nullable
    public AssemblyException getLastException() {
        return this.lastException;
    }

    @Override
    protected void write(final CompoundTag tag, final HolderLookup.Provider registries, final boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        AssemblyException.write(tag, registries, this.lastException);
    }

    @Override
    protected void read(final CompoundTag tag, final HolderLookup.Provider registries, final boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        this.lastException = AssemblyException.read(tag, registries);
    }
}
