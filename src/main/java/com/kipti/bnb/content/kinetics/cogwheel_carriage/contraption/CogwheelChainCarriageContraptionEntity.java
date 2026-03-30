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

    protected static final float CLIENT_CHASING_RATE_CHANGE = 0.015f;
    protected static final float CLIENT_CHASING_MAX = 0.3f;
    private static final float MAX_CLIENT_SERVER_DIFF = 0.5f;

    protected boolean disassembleNextTick = false;

    /**
     * The client will chase the chain attachment dist (previous from server + predicted velocity) by moderating its own velocity + or - 10%
     */
    protected float clientChasingChainAttachmentDist = 0.0f;
    protected float currentClientChasingRate = 1.0f;

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
                            this.getAttachment().getDist()
                    )
            );
        }
    }

    private void tickClient() {
        final CogwheelChainAttachment attachment = this.getAttachment();
        final float velocity = attachment.getDistPerTick(this.level());

        this.clientChasingChainAttachmentDist = attachment.wrapDist(
                this.level(),
                this.clientChasingChainAttachmentDist + (velocity * this.currentClientChasingRate)
        );

        final float serverDist = this.getAttachment().getDist();
        final float clientServerDiff = attachment.getMinWrappedDist(
                this.level(), this.clientChasingChainAttachmentDist,
                serverDist
        );

        if (clientServerDiff > MAX_CLIENT_SERVER_DIFF) { //Quiet so we dont fill the hard drive of anyone with a shitty internet with 5 million logs
//            CreateBitsnBobs.LOGGER.info("Client too far from server! Correcting. Diff: {}", clientServerDiff);
            this.clientChasingChainAttachmentDist = (this.clientChasingChainAttachmentDist + serverDist) / 2f;
        }

        if (Math.abs(velocity) < 0.0001f) {
            if (clientServerDiff > 0.25f) { // Use a kind of big epsilon here since being 0.25f blocks off maybe doesent matter
                final float diff = serverDist - this.clientChasingChainAttachmentDist;
                this.clientChasingChainAttachmentDist = attachment.wrapDist(
                        this.level(),
                        this.clientChasingChainAttachmentDist + Math.clamp(diff, -0.015f, 0.015f)
                );
            }
            this.currentClientChasingRate = 1.0f;
        } else {
            final boolean isAhead = attachment.isWrappedDistFurther(
                    this.level(),
                    this.clientChasingChainAttachmentDist,
                    serverDist,
                    Math.signum(velocity)
            );

            final float errorAdjustment = Math.min(clientServerDiff * 2.0f, CLIENT_CHASING_MAX);

            float targetRate = 1.0f;

            if (clientServerDiff > 0.005f) {
                targetRate = isAhead ? (1.0f - errorAdjustment) : (1.0f + errorAdjustment);
            }

            final float lerpResistance = 0.85f;
            this.currentClientChasingRate = this.currentClientChasingRate * lerpResistance + targetRate * (1.0f - lerpResistance);
        }
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

        final CogwheelChainAttachment attachment = this.getAttachment();
        final float previousDist = attachment.getDist();
        attachment.tick(level);

        if (this.isVerticalSegmentBlocked(level, attachment.getDist(), previousDist)) {
            attachment.setDist(previousDist);
            this.notifyVerticalBlock(level);
            return;
        }

        this.updatePositionFromChain(level);
        this.updateYawFromChain(level);
    }

    private boolean isVerticalSegmentBlocked(final Level level, final float dist, final float previousDist) {
        final float velocity = dist - previousDist;
        final CogwheelChainSegment advancingSegment = this.getAttachment().getSegmentAtOffset(level, velocity);
        final CogwheelChainSegment currentSegment = this.getAttachment().getSegmentAtOffset(level, previousDist - dist);

        if (advancingSegment == null || currentSegment == null) return false;

        return advancingSegment.hasVerticalDisplacement() && currentSegment.hasVerticalDisplacement();
    }

    private void notifyVerticalBlock(final Level level) {
        //TODO: copy train tooltiop logic
    }

    private void updatePositionFromChain(final Level level) {
        final CogwheelChainAttachment attachment = this.getAttachment();
        final float offset = this.getChainRenderOffset(level, attachment);
        final Vec3 newPos = attachment
                .getCurrentPosition(level, offset);
        this.setPos(newPos.x, newPos.y - 1.5, newPos.z);
    }

    private float getChainRenderOffset(final Level level, final CogwheelChainAttachment attachment) {
        return level.isClientSide ? this.clientChasingChainAttachmentDist - attachment.getDist() : 0;
    }

    private void updateYawFromChain(final Level level) {
        final CogwheelChainAttachment attachment = this.getAttachment();
        final float offset = this.getChainRenderOffset(level, attachment);
        final Vec3 direction = attachment.getCurrentDirection(level, offset);

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
        compound.putDouble("ChainAttachmentDist", this.getAttachment().getDist());
    }

    @Override
    protected void readAdditional(final CompoundTag compound, final boolean spawnPacket) {
        super.readAdditional(compound, spawnPacket);
        this.getAttachment().setDist(compound.getFloat("ChainAttachmentDist"));
        if (spawnPacket) {
            this.clientChasingChainAttachmentDist = compound.getFloat("ChainAttachmentDist");
        }
    }

    public void setDistFromServer(final float dist) {
        this.getAttachment().setDist(dist);
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
