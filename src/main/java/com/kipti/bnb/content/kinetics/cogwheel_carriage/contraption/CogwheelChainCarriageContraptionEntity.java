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
import org.jetbrains.annotations.Nullable;

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
    private boolean hasPendingChainAttachmentDist;
    private float pendingChainAttachmentDist;

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

    @Nullable
    public CogwheelChainAttachment getAttachment() {
        if (!(this.contraption instanceof final CogwheelChainCarriageContraption cccc)) {
            return null;
        }
        final CogwheelChainAttachment attachment = cccc.getCarriageAttachment();
        if (attachment == null) return null;
        this.applyPendingAttachmentDist(attachment);
        return attachment;
    }

    @Override
    protected void tickContraption() {
        super.tickContraption();
        if (!this.level().isClientSide) {
            this.tickServer();
        }

        if (this.isRemoved()) return;

        final CogwheelChainAttachment attachment = this.getAttachment();
        if (attachment == null || !attachment.isValid(this.level())) return;

        if (!this.tickCommon(attachment)) return;
        if (this.level().isClientSide) {
            this.tickClient(attachment);
        }
        this.tickActors();
        if (!this.level().isClientSide) {
            this.tickServerSync(attachment);
        }
    }

    private void tickServerSync(final CogwheelChainAttachment attachment) {
        if (this.tickCount % 60 == 0) {
            CatnipServices.NETWORK.sendToClientsTrackingEntity(
                    this,
                    new CogwheelChainCarriageUpdateDistPacket(
                            this.getId(),
                            attachment.getDist()
                    )
            );
        }
    }

    private void tickClient(final CogwheelChainAttachment attachment) {
        final float velocity = attachment.getDistPerTick(this.level());

        this.clientChasingChainAttachmentDist = attachment.wrapDist(
                this.level(),
                this.clientChasingChainAttachmentDist + (velocity * this.currentClientChasingRate)
        );

        final float serverDist = attachment.getDist();
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
        final CogwheelChainAttachment attachment = this.getAttachment();
        if (attachment == null) return;

        if (!attachment.isValid(level) || this.disassembleNextTick) {
            this.disassemble();
            this.discard();
        }
    }

    private boolean tickCommon(final CogwheelChainAttachment attachment) {
        final Level level = this.level();

        final float previousDist = attachment.getDist();
        attachment.tick(level);

        if (!attachment.isValid(level)) {
            return false;
        }

        if (this.isVerticalSegmentBlocked(level, attachment, attachment.getDist(), previousDist)) {
            attachment.setDist(previousDist);
            this.notifyVerticalBlock(level);
            return true;
        }

        if (!this.updatePositionFromChain(level, attachment)) {
            return false;
        }
        this.updateYawFromChain(level, attachment);
        return true;
    }

    private boolean isVerticalSegmentBlocked(final Level level,
                                             final CogwheelChainAttachment attachment,
                                             final float dist,
                                             final float previousDist) {
        final float velocity = dist - previousDist;
        final CogwheelChainSegment advancingSegment = attachment.getSegmentAtOffset(level, velocity);
        final CogwheelChainSegment currentSegment = attachment.getSegmentAtOffset(level, previousDist - dist);

        if (advancingSegment == null || currentSegment == null) return false;

        return advancingSegment.hasVerticalDisplacement() && currentSegment.hasVerticalDisplacement();
    }

    private void notifyVerticalBlock(final Level level) {
        //TODO: copy train tooltiop logic
    }

    private boolean updatePositionFromChain(final Level level, final CogwheelChainAttachment attachment) {
        final float offset = this.getChainRenderOffset(level, attachment);
        final Vec3 newPos = attachment
                .getCurrentPositionIfPresent(level, offset);
        if (newPos == null) return false;
        this.setPos(newPos.x, newPos.y - 1.5, newPos.z);
        return true;
    }

    private float getChainRenderOffset(final Level level, final CogwheelChainAttachment attachment) {
        return level.isClientSide ? this.clientChasingChainAttachmentDist - attachment.getDist() : 0;
    }

    private void updateYawFromChain(final Level level, final CogwheelChainAttachment attachment) {
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
        final CogwheelChainAttachment attachment = this.getAttachment();
        if (attachment != null) {
            compound.putFloat("ChainAttachmentDist", attachment.getDist());
        } else if (this.hasPendingChainAttachmentDist) {
            compound.putFloat("ChainAttachmentDist", this.pendingChainAttachmentDist);
        }
    }

    @Override
    protected void readAdditional(final CompoundTag compound, final boolean spawnPacket) {
        super.readAdditional(compound, spawnPacket);
        if (compound.contains("ChainAttachmentDist")) {
            final float attachmentDist = compound.getFloat("ChainAttachmentDist");
            this.setAttachmentDist(attachmentDist);
            if (spawnPacket) {
                this.clientChasingChainAttachmentDist = attachmentDist;
            }
        }
    }

    public void setDistFromServer(final float dist) {
        final CogwheelChainAttachment attachment = this.getAttachment();
        if (attachment == null || !attachment.isValid(this.level())) return;
        attachment.setDist(dist);
    }

    private void setAttachmentDist(final float dist) {
        final CogwheelChainAttachment attachment = this.getAttachment();
        if (attachment == null) {
            this.pendingChainAttachmentDist = dist;
            this.hasPendingChainAttachmentDist = true;
            return;
        }
        attachment.setDist(dist);
    }

    private void applyPendingAttachmentDist(final CogwheelChainAttachment attachment) {
        if (!this.hasPendingChainAttachmentDist) return;
        attachment.setDist(this.pendingChainAttachmentDist);
        this.hasPendingChainAttachmentDist = false;
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
