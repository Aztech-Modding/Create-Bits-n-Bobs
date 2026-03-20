package com.kipti.bnb.content.kinetics.cogwheel_chain.carriage;

import com.kipti.bnb.content.kinetics.cogwheel_chain.attachment.CogwheelChainAttachment;
import com.kipti.bnb.registry.content.BnbContraptionTypes;
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
 */
public class CogwheelChainCarriageContraption extends Contraption {

    public static final float FRONT_SHOE_OFFSET = 0.5f;
    public static final float BACK_SHOE_OFFSET = -0.5f;

    private CogwheelChainAttachment carriageAttachment;

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

        return this.searchMovedStructure(level, pos, null);
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
        }

        return tag;
    }

    @Override
    public void readNBT(final Level level, final CompoundTag nbt, final boolean spawnData) {
        if (nbt.contains("CenterAttachment")) {
            this.carriageAttachment = CogwheelChainAttachment.read(nbt.getCompound("CenterAttachment"));
        }
        super.readNBT(level, nbt, spawnData);
    }

    public CogwheelChainAttachment getCarriageAttachment() {
        return this.carriageAttachment;
    }
}
