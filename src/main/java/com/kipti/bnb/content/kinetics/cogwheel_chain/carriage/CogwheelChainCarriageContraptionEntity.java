package com.kipti.bnb.content.kinetics.cogwheel_chain.carriage;

import com.kipti.bnb.content.kinetics.cogwheel_chain.attachment.CogwheelChainAttachment;
import com.kipti.bnb.content.kinetics.cogwheel_chain.segment.CogwheelChainSegment;
import com.kipti.bnb.registry.content.BnbEntityTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.StructureTransform;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
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
 */
public class CogwheelChainCarriageContraptionEntity extends AbstractContraptionEntity {

    private float prevYaw;
    private float yaw;

    public CogwheelChainCarriageContraptionEntity(final EntityType<?> type, final Level level) {
        super(type, level);
    }

    /**
     * Factory method for spawning the entity with a pre-assembled contraption.
     */
    public static CogwheelChainCarriageContraptionEntity create(final Level level,
                                                                final CogwheelChainCarriageContraption contraption) {
        final CogwheelChainCarriageContraptionEntity entity =
                new CogwheelChainCarriageContraptionEntity(BnbEntityTypes.COGWHEEL_CHAIN_CARRIAGE_CONTRAPTION.get(), level);
        entity.setContraption(contraption);
        return entity;
    }

    public CogwheelChainAttachment getAttachment() {
        if (!(this.contraption instanceof final CogwheelChainCarriageContraption cccc)) {
            throw new IllegalStateException("A CogwheelChainCarriageContraptionEntity must have a CogwheelChainCarriageContraption contraption!");
        }
        return cccc.getCarriageAttachment();
    }

    @Override
    protected void tickContraption() {
        if (!this.level().isClientSide) {
            this.tickServer();
        } else {
            this.tickClient();
        }
    }

    private void tickServer() {
        final Level level = this.level();

        if (!this.getAttachment().isValid(level)) {
            this.disassemble();
            this.discard();
            return;
        }

        final float previousDist = this.getAttachment().getDist();
        this.getAttachment().tick(level);

        if (this.isVerticalSegmentBlocked(level)) {
            this.getAttachment().setDist(previousDist);
            this.notifyVerticalBlock(level);
            return;
        }

        this.updatePositionFromChain(level);
        this.updateYawFromChain(level);
    }

    private boolean isVerticalSegmentBlocked(final Level level) {
        final CogwheelChainSegment frontSegment = this.getAttachment().getSegmentAtOffset(
                level, CogwheelChainCarriageContraption.FRONT_SHOE_OFFSET);
        final CogwheelChainSegment backSegment = this.getAttachment().getSegmentAtOffset(
                level, CogwheelChainCarriageContraption.BACK_SHOE_OFFSET);

        final boolean frontBlocked = frontSegment != null && frontSegment.hasVerticalDisplacement();
        final boolean backBlocked = backSegment != null && backSegment.hasVerticalDisplacement();

        return frontBlocked || backBlocked;
    }

    private void notifyVerticalBlock(final Level level) {
        //TODO: copy train tooltiop logic
    }

    private void updatePositionFromChain(final Level level) {
        final Vec3 newPos = this.getAttachment().getCurrentPosition(level);
        this.setPos(newPos.x, newPos.y, newPos.z);
    }

    private void updateYawFromChain(final Level level) {
        final Vec3 frontPos = this.getAttachment().getCurrentPosition(
                level, CogwheelChainCarriageContraption.FRONT_SHOE_OFFSET);
        final Vec3 backPos = this.getAttachment().getCurrentPosition(
                level, CogwheelChainCarriageContraption.BACK_SHOE_OFFSET);
        final Vec3 direction = frontPos.subtract(backPos);

        if (direction.horizontalDistanceSqr() > 1e-8) {
            final float targetYaw = (float) Math.toDegrees(Math.atan2(direction.x, direction.z));
            this.prevYaw = this.yaw;
            this.yaw = targetYaw;
        }
    }

    private void tickClient() {
        this.prevYaw = this.yaw;
    }

    @Override
    public Vec3 applyRotation(final Vec3 localPos, final float partialTicks) {
        final float interpolatedYaw = this.getInterpolatedYaw(partialTicks);
        return VecHelper.rotate(localPos, -interpolatedYaw, Direction.Axis.Y);
    }

    @Override
    public Vec3 reverseRotation(final Vec3 localPos, final float partialTicks) {
        final float interpolatedYaw = this.getInterpolatedYaw(partialTicks);
        return VecHelper.rotate(localPos, interpolatedYaw, Direction.Axis.Y);
    }

    @Override
    public ContraptionRotationState getRotationState() {
        final ContraptionRotationState state = new ContraptionRotationState();
        state.yRotation = -this.yaw;
        return state;
    }

    @Override
    protected StructureTransform makeStructureTransform() {
        final BlockPos offset = BlockPos.containing(this.getAnchorVec().add(0.5, 0.5, 0.5));
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
        final float interpolatedYaw = this.getInterpolatedYaw(partialTicks);
        matrixStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(interpolatedYaw));
    }

    @Override
    protected void writeAdditional(final CompoundTag compound,
                                   final HolderLookup.Provider registries,
                                   final boolean spawnPacket) {
        super.writeAdditional(compound, registries, spawnPacket);
        compound.putFloat("Yaw", this.yaw);
    }

    @Override
    protected void readAdditional(final CompoundTag compound, final boolean spawnData) {
        super.readAdditional(compound, spawnData);
        this.yaw = compound.getFloat("Yaw");
        this.prevYaw = this.yaw;
    }

    public float getInterpolatedYaw(final float partialTicks) {
        return partialTicks == 1.0f
                ? this.yaw
                : AngleHelper.angleLerp(partialTicks, this.prevYaw, this.yaw);
    }
}
