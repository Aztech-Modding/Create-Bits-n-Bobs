package com.kipti.bnb.content.kinetics.cogwheel_chain.carriage;

import com.kipti.bnb.content.kinetics.cogwheel_chain.attachment.CogwheelChainAttachments;
import com.kipti.bnb.content.kinetics.cogwheel_chain.segment.CogwheelChainSegment;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.StructureTransform;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Entity that carries a {@link CogwheelChainCarriageContraption} along a cogwheel chain.
 * Movement is driven by the chain's linear speed; the entity cannot traverse segments
 * with vertical displacement.
 *
 * <p>Yaw is derived from the vector between the front and back shoe positions
 * (at ±{@value CogwheelChainCarriageContraption#FRONT_SHOE_OFFSET} blocks from center).</p>
 *
 * <p>TODO: Register the {@link EntityType} via the BnB registrate in
 * {@code BnbEntityTypes}, following the pattern of
 * {@code INERT_CONTROLLED_CONTRAPTION}.</p>
 */
public class CogwheelChainCarriageContraptionEntity extends AbstractContraptionEntity {

    private static final double ACTIONBAR_RANGE_SQ = 64.0 * 64.0;

    private CogwheelChainAttachments centerAttachment;
    private float prevYaw;
    private float yaw;

    public CogwheelChainCarriageContraptionEntity(final EntityType<?> type, final Level level) {
        super(type, level);
    }

    /**
     * Factory method for spawning the entity with a pre-assembled contraption.
     *
     * <p>TODO: Replace the {@code entityType} parameter with the registered
     * {@code BnbEntityTypes.COGWHEEL_CHAIN_CARRIAGE_CONTRAPTION.get()} once
     * the entity type is registered.</p>
     */
    public static CogwheelChainCarriageContraptionEntity create(final Level level,
                                                                 final EntityType<?> entityType,
                                                                 final CogwheelChainCarriageContraption contraption,
                                                                 final CogwheelChainAttachments attachment) {
        CogwheelChainCarriageContraptionEntity entity =
                new CogwheelChainCarriageContraptionEntity(entityType, level);
        entity.centerAttachment = attachment;
        entity.setContraption(contraption);
        return entity;
    }

    @Override
    protected void tickContraption() {
        if (this.centerAttachment == null) {
            return;
        }

        if (!this.level().isClientSide) {
            this.tickServer();
        } else {
            this.tickClient();
        }
    }

    private void tickServer() {
        Level level = this.level();

        if (!this.centerAttachment.isValid(level)) {
            this.disassemble();
            this.discard();
            return;
        }

        float previousDist = this.centerAttachment.getDist();
        this.centerAttachment.tick(level);

        if (this.isVerticalSegmentBlocked(level)) {
            this.centerAttachment.setDist(previousDist);
            this.notifyVerticalBlock(level);
            return;
        }

        this.updatePositionFromChain(level);
        this.updateYawFromChain(level);
    }

    private boolean isVerticalSegmentBlocked(final Level level) {
        CogwheelChainSegment frontSegment = this.centerAttachment.getSegmentAtOffset(
                level, CogwheelChainCarriageContraption.FRONT_SHOE_OFFSET);
        CogwheelChainSegment backSegment = this.centerAttachment.getSegmentAtOffset(
                level, CogwheelChainCarriageContraption.BACK_SHOE_OFFSET);

        boolean frontBlocked = frontSegment != null && frontSegment.hasVerticalDisplacement();
        boolean backBlocked = backSegment != null && backSegment.hasVerticalDisplacement();

        return frontBlocked || backBlocked;
    }

    private void notifyVerticalBlock(final Level level) {
        Component message = Component.translatable(
                "bits_n_bobs.cogwheel_chain.carriage.vertical_blocked");

        for (Player player : level.players()) {
            if (player.distanceToSqr(this) < ACTIONBAR_RANGE_SQ) {
                player.displayClientMessage(message, true);
            }
        }
    }

    private void updatePositionFromChain(final Level level) {
        Vec3 newPos = this.centerAttachment.getCurrentPosition(level);
        this.setPos(newPos.x, newPos.y, newPos.z);
    }

    private void updateYawFromChain(final Level level) {
        Vec3 frontPos = this.centerAttachment.getCurrentPosition(
                level, CogwheelChainCarriageContraption.FRONT_SHOE_OFFSET);
        Vec3 backPos = this.centerAttachment.getCurrentPosition(
                level, CogwheelChainCarriageContraption.BACK_SHOE_OFFSET);
        Vec3 direction = frontPos.subtract(backPos);

        if (direction.horizontalDistanceSqr() > 1e-8) {
            float targetYaw = (float) Math.toDegrees(Math.atan2(direction.x, direction.z));
            this.prevYaw = this.yaw;
            this.yaw = targetYaw;
        }
    }

    private void tickClient() {
        this.prevYaw = this.yaw;
    }

    @Override
    public Vec3 applyRotation(final Vec3 localPos, final float partialTicks) {
        float interpolatedYaw = this.getInterpolatedYaw(partialTicks);
        return VecHelper.rotate(localPos, -interpolatedYaw, Direction.Axis.Y);
    }

    @Override
    public Vec3 reverseRotation(final Vec3 localPos, final float partialTicks) {
        float interpolatedYaw = this.getInterpolatedYaw(partialTicks);
        return VecHelper.rotate(localPos, interpolatedYaw, Direction.Axis.Y);
    }

    @Override
    public ContraptionRotationState getRotationState() {
        ContraptionRotationState state = new ContraptionRotationState();
        state.yRotation = -this.yaw;
        return state;
    }

    @Override
    protected StructureTransform makeStructureTransform() {
        BlockPos offset = BlockPos.containing(this.getAnchorVec().add(0.5, 0.5, 0.5));
        return new StructureTransform(offset, 0, -this.yaw, 0);
    }

    @Override
    protected float getStalledAngle() {
        return this.yaw;
    }

    @Override
    protected void handleStallInformation(final double x, final double y, final double z,
                                           final float angle) {
        this.yaw = angle;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void applyLocalTransforms(final PoseStack matrixStack, final float partialTicks) {
        float interpolatedYaw = this.getInterpolatedYaw(partialTicks);
        matrixStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(interpolatedYaw));
    }

    @Override
    protected void writeAdditional(final CompoundTag compound,
                                    final HolderLookup.Provider registries,
                                    final boolean spawnPacket) {
        super.writeAdditional(compound, registries, spawnPacket);

        if (this.centerAttachment != null) {
            CompoundTag attachmentTag = new CompoundTag();
            this.centerAttachment.write(attachmentTag);
            compound.put("CenterAttachment", attachmentTag);
        }

        compound.putFloat("Yaw", this.yaw);
    }

    @Override
    protected void readAdditional(final CompoundTag compound, final boolean spawnData) {
        super.readAdditional(compound, spawnData);

        if (compound.contains("CenterAttachment")) {
            this.centerAttachment = CogwheelChainAttachments.read(
                    compound.getCompound("CenterAttachment"));
        }

        this.yaw = compound.getFloat("Yaw");
        this.prevYaw = this.yaw;
    }

    public CogwheelChainAttachments getCenterAttachment() {
        return this.centerAttachment;
    }

    public float getYaw() {
        return this.yaw;
    }

    public float getInterpolatedYaw(final float partialTicks) {
        return partialTicks == 1.0f
                ? this.yaw
                : AngleHelper.angleLerp(partialTicks, this.prevYaw, this.yaw);
    }
}
