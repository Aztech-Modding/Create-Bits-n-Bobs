package com.kipti.bnb.content.kinetics.cogwheel_chain.carriage;

import com.kipti.bnb.content.kinetics.cogwheel_chain.attachment.CogwheelChainAttachments;
import com.simibubi.create.api.contraption.ContraptionType;
import com.simibubi.create.content.contraptions.AssemblyException;
import com.simibubi.create.content.contraptions.Contraption;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

/**
 * Contraption that rides along a cogwheel chain drive. The carriage collects
 * blocks at its anchor position into a rigid body constrained to the chain's
 * horizontal segments.
 *
 * <p>Two logical "shoe" attachment points at {@value #FRONT_SHOE_OFFSET} and
 * {@value #BACK_SHOE_OFFSET} blocks from the center define the carriage footprint
 * on the chain and are used to enforce the vertical-displacement constraint.</p>
 *
 * <p>TODO: Register the {@link ContraptionType} via
 * {@code Registry.registerForHolder(CreateBuiltInRegistries.CONTRAPTION_TYPE, ...)}
 * and assign it to {@link #TYPE}.</p>
 */
public class CogwheelChainCarriageContraption extends Contraption {

    public static final float FRONT_SHOE_OFFSET = 0.5f;
    public static final float BACK_SHOE_OFFSET = -0.5f;

    /**
     * TODO: Assign during registration, e.g.:
     * <pre>{@code
     * TYPE = Registry.registerForHolder(
     *     CreateBuiltInRegistries.CONTRAPTION_TYPE,
     *     CreateBitsnBobs.asResource("cogwheel_chain_carriage"),
     *     new ContraptionType(CogwheelChainCarriageContraption::new)
     * ).value();
     * }</pre>
     */
    public static ContraptionType TYPE;

    private CogwheelChainAttachments centerAttachment;

    public CogwheelChainCarriageContraption() {
    }

    public CogwheelChainCarriageContraption(final CogwheelChainAttachments centerAttachment) {
        this.centerAttachment = centerAttachment;
    }

    @Override
    public boolean assemble(final Level level, final BlockPos pos) throws AssemblyException {
        if (this.centerAttachment == null || !this.centerAttachment.isValid(level)) {
            return false;
        }

        if (!this.searchMovedStructure(level, pos, null)) {
            return false;
        }

        return true;
    }

    @Override
    public boolean canBeStabilized(final Direction facing, final BlockPos localPos) {
        return false;
    }

    @Override
    public ContraptionType getType() {
        return TYPE;
    }

    @Override
    public CompoundTag writeNBT(final HolderLookup.Provider registries, final boolean spawnPacket) {
        CompoundTag tag = super.writeNBT(registries, spawnPacket);

        if (this.centerAttachment != null) {
            CompoundTag attachmentTag = new CompoundTag();
            this.centerAttachment.write(attachmentTag);
            tag.put("CenterAttachment", attachmentTag);
        }

        return tag;
    }

    @Override
    public void readNBT(final Level level, final CompoundTag nbt, final boolean spawnData) {
        if (nbt.contains("CenterAttachment")) {
            this.centerAttachment = CogwheelChainAttachments.read(nbt.getCompound("CenterAttachment"));
        }
        super.readNBT(level, nbt, spawnData);
    }

    public CogwheelChainAttachments getCenterAttachment() {
        return this.centerAttachment;
    }
}
