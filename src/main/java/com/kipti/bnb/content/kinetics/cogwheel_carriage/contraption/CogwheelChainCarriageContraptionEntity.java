package com.kipti.bnb.content.kinetics.cogwheel_carriage.contraption;

import com.kipti.bnb.content.kinetics.cogwheel_chain.attachment.CogwheelChainAttachment;
import com.kipti.bnb.content.kinetics.cogwheel_chain.segment.CogwheelChainSegment;
import com.kipti.bnb.network.packets.to_client.CogwheelChainCarriageUpdateDistPacket;
import com.kipti.bnb.registry.content.BnbEntityTypes;
import com.simibubi.create.content.contraptions.OrientedContraptionEntity;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class CogwheelChainCarriageContraptionEntity extends OrientedContraptionEntity {

    public static final float SHOE_OFFSET = 0.5f;

    protected static final double CLIENT_CHASING_RATE_CHANGE = 0.015;
    protected static final double CLIENT_CHASING_MAX = 0.3;

    protected boolean disassembleNextTick = false;

    protected double chainAttachmentDist = 0f;
    /**
     * The client will chase the chain attachment dist (previous from server + predicted velocity) by moderating its own velocity + or - 10%
     */
    protected double clientChasingChainAttachmentDist = 0f;
    protected double currentClientChasingRate = 1.0f;

    protected Vec3 lastFrontShoeDir = Vec3.ZERO;
    protected Vec3 lastBackShoeDir = Vec3.ZERO;

    protected Vec3 frontShoeDir = Vec3.ZERO;
    protected Vec3 backShoeDir = Vec3.ZERO;

    public CogwheelChainCarriageContraptionEntity(final EntityType<?> type, final Level level) {
        super(type, level);
    }

    /**
     * Factory method for spawning the entity with a pre-assembled contraption.
     */
    public static CogwheelChainCarriageContraptionEntity create(final Level level,
                                                                final CogwheelChainCarriageContraption contraption) {
        final CogwheelChainCarriageContraptionEntity entity =
                new CogwheelChainCarriageContraptionEntity(
                        BnbEntityTypes.COGWHEEL_CHAIN_CARRIAGE_CONTRAPTION.get(),
                        level
                );
        entity.setContraption(contraption);
        return entity;
    }

    public CogwheelChainAttachment getAttachment() {
        if (!(this.contraption instanceof final CogwheelChainCarriageContraption cccc)) {
            throw new IllegalStateException(
                    "A CogwheelChainCarriageContraptionEntity must have a CogwheelChainCarriageContraption contraption!");
        }
        return cccc.getCarriageAttachment();
    }

    @Override
    protected void tickContraption() {
        super.tickContraption();
        if (!this.level().isClientSide) {
            this.tickServer();
        }

        if (!this.getAttachment().isValid(this.level())) return;

        this.tickCommon();
        if (this.level().isClientSide) {
            this.tickClient();
        }
        this.tickActors();
        if (!this.level().isClientSide) {
            this.tickServerSync();
        }
    }

    private void tickServerSync() {
        if (this.tickCount % 60 == 0) {
            CatnipServices.NETWORK.sendToClientsTrackingEntity(
                    this,
                    new CogwheelChainCarriageUpdateDistPacket(
                            this.getId(),
                            this.chainAttachmentDist
                    )
            );
        }
    }

    private void tickClient() {
        final CogwheelChainAttachment attachment = this.getAttachment();
        final float velocity = attachment.getDistPerTick(this.level());

        this.clientChasingChainAttachmentDist += velocity * this.currentClientChasingRate;
        if (this.clientChasingChainAttachmentDist > this.chainAttachmentDist) {
            this.currentClientChasingRate -= CLIENT_CHASING_RATE_CHANGE;
        } else if (this.clientChasingChainAttachmentDist < this.chainAttachmentDist) {
            this.currentClientChasingRate += CLIENT_CHASING_RATE_CHANGE;
        }

        this.currentClientChasingRate = Math.max(
                1.0 - CLIENT_CHASING_MAX,
                Math.min(1.0 + CLIENT_CHASING_MAX, this.currentClientChasingRate)
        );

        this.lastFrontShoeDir = this.frontShoeDir;
        this.lastBackShoeDir = this.backShoeDir;

        this.frontShoeDir = attachment.getCurrentDirection(this.level(), SHOE_OFFSET);
        this.backShoeDir = attachment.getCurrentDirection(this.level(), -SHOE_OFFSET);
    }

    private void tickServer() {
        final Level level = this.level();
        if (!this.getAttachment().isValid(level) || this.disassembleNextTick) {
            this.disassemble();
            this.discard();
        }
    }

    private void tickCommon() {
        final Level level = this.level();

        final CogwheelChainAttachment dist = this.getAttachment();
        final float previousDist = dist.getDist();
        dist.tick(level);
        this.chainAttachmentDist = dist.getDist();

        if (this.isVerticalSegmentBlocked(level)) {
            dist.setDist(previousDist);
            this.notifyVerticalBlock(level);
            return;
        }

        this.updatePositionFromChain(level);
        this.updateYawFromChain(level);
    }

    private boolean isVerticalSegmentBlocked(final Level level) {
        final CogwheelChainSegment frontSegment = this.getAttachment().getSegmentAtOffset(level, SHOE_OFFSET);
        final CogwheelChainSegment backSegment = this.getAttachment().getSegmentAtOffset(level, -SHOE_OFFSET);

        final boolean frontBlocked = frontSegment != null && frontSegment.hasVerticalDisplacement();
        final boolean backBlocked = backSegment != null && backSegment.hasVerticalDisplacement();

        return frontBlocked || backBlocked;
    }

    private void notifyVerticalBlock(final Level level) {
        //TODO: copy train tooltiop logic
    }

    private void updatePositionFromChain(final Level level) {
        final CogwheelChainAttachment attachment = this.getAttachment();
        final Vec3 newPos = attachment
                .getCurrentPosition(level, SHOE_OFFSET)
                .lerp(
                        attachment.getCurrentPosition(level, -SHOE_OFFSET), 0.5
                );
        this.setPos(newPos.x, newPos.y - 0.5, newPos.z);
    }

    private void updateYawFromChain(final Level level) {
        final Vec3 frontPos = this.getAttachment().getCurrentPosition(
                level, SHOE_OFFSET);
        final Vec3 backPos = this.getAttachment().getCurrentPosition(
                level, -SHOE_OFFSET);
        final Vec3 direction = frontPos.subtract(backPos);

        if (direction.horizontalDistanceSqr() > 1e-8) {
            this.prevYaw = this.yaw;
            this.yaw = (float) Math.toDegrees(Math.atan2(direction.z, direction.x)) - 90;
        }
    }

    public float getInterpolatedYaw(final float partialTicks) {
        return AngleHelper.angleLerp(partialTicks, this.prevYaw, this.yaw);
    }

    @Override
    protected void writeAdditional(final CompoundTag compound,
                                   final HolderLookup.Provider registries,
                                   final boolean spawnPacket) {
        super.writeAdditional(compound, registries, spawnPacket);
        compound.putDouble("ChainAttachmentDist", this.chainAttachmentDist);
    }

    @Override
    protected void readAdditional(final CompoundTag compound, final boolean spawnPacket) {
        super.readAdditional(compound, spawnPacket);
        this.chainAttachmentDist = compound.getDouble("ChainAttachmentDist");
    }

    public void setDistFromServer(final double dist) {
        this.chainAttachmentDist = dist;
    }

    public Vec3 getFrontShoeDir(final float partialTicks) {
        return this.lastFrontShoeDir.lerp(this.frontShoeDir, partialTicks);
    }

    public Vec3 getBackShoeDir(final float partialTicks) {
        return this.lastBackShoeDir.lerp(this.backShoeDir, partialTicks);
    }

    public void disassembleNextTick() {
        this.disassembleNextTick = true;
    }

}
