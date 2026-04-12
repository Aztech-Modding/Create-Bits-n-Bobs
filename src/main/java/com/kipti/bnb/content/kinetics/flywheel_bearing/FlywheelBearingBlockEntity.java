package com.kipti.bnb.content.kinetics.flywheel_bearing;

import com.cake.azimuth.lang.IncludeLangDefaults;
import com.cake.azimuth.lang.LangDefault;
import com.kipti.bnb.CreateBitsnBobs;
import com.kipti.bnb.content.kinetics.flywheel_bearing.contraption.InertControlledContraptionEntity;
import com.kipti.bnb.content.kinetics.flywheel_bearing.mechanics.FlywheelMovementMechanics;
import com.kipti.bnb.mixin_accessor.FlywheelAccessibleKineticNetwork;
import com.kipti.bnb.registry.core.BnbConfigs;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.contraptions.*;
import com.simibubi.create.content.contraptions.bearing.BearingBlock;
import com.simibubi.create.content.contraptions.bearing.BearingContraption;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.utility.CreateLang;
import com.simibubi.create.foundation.utility.ServerSpeedProvider;
import net.createmod.catnip.lang.Lang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Predominantly a copy of BearingBlockEntity with many adjustments and features cut for flywheel bearing behavior
 */
@IncludeLangDefaults({
        @LangDefault(key = "flywheel_stats", format = FlywheelBearingBlockEntity.TOOLTIP_FORMAT, value = "Flywheel Stats:"),
        @LangDefault(key = "angular_mass", format = FlywheelBearingBlockEntity.TOOLTIP_FORMAT, value = "Angular Mass:"),
        @LangDefault(key = "stored_stress", format = FlywheelBearingBlockEntity.TOOLTIP_FORMAT, value = "Stored Stress:"),
        @LangDefault(key = "kinetic_transfer", format = FlywheelBearingBlockEntity.TOOLTIP_FORMAT, value = "Kinetic Transfer:"),
        @LangDefault(key = "angular_mass.none", format = FlywheelBearingBlockEntity.TOOLTIP_FORMAT, value = "(none)"),
        @LangDefault(key = "angular_mass.super_light", format = FlywheelBearingBlockEntity.TOOLTIP_FORMAT, value = "(super light)"),
        @LangDefault(key = "angular_mass.light", format = FlywheelBearingBlockEntity.TOOLTIP_FORMAT, value = "(light)"),
        @LangDefault(key = "angular_mass.medium", format = FlywheelBearingBlockEntity.TOOLTIP_FORMAT, value = "(medium)"),
        @LangDefault(key = "angular_mass.heavy", format = FlywheelBearingBlockEntity.TOOLTIP_FORMAT, value = "(heavy)"),
        @LangDefault(key = "angular_mass.super_heavy", format = FlywheelBearingBlockEntity.TOOLTIP_FORMAT, value = "(super heavy)"),
        @LangDefault(key = "angular_mass.absurdly_heavy", format = FlywheelBearingBlockEntity.TOOLTIP_FORMAT, value = "(absurdly heavy)"),
        @LangDefault(key = "empty", format = FlywheelBearingBlockEntity.TOOLTIP_FORMAT, value = "(empty)"),
        @LangDefault(key = "full", format = FlywheelBearingBlockEntity.TOOLTIP_FORMAT, value = "(full)"),
})
public class FlywheelBearingBlockEntity extends GeneratingKineticBlockEntity implements IControlContraption, IDisplayAssemblyExceptions {

    public static final String TOOLTIP_FORMAT = "tooltip.bits_n_bobs.flywheel_bearing.%s";

    protected AssemblyException lastException;
    protected InertControlledContraptionEntity movedContraption;
    protected boolean checkAssemblyNextTick;
    protected float clientAngleDiff;
    protected boolean running;

    protected float clientFlywheelAbsorptionCapacityInNetwork;
    protected float clientFlywheelReleaseCapacityInNetwork;

    protected int lastGeneratorDirection = 1;

    protected FlywheelMovementMechanics flywheelMovement = new FlywheelMovementMechanics();

    public FlywheelBearingBlockEntity(final BlockEntityType<?> type, final BlockPos pos, final BlockState state) {
        super(type, pos, state);
        this.lazyTickRate = 5;
    }

    @Override
    public boolean addToGoggleTooltip(final List<Component> tooltip, final boolean isPlayerSneaking) {
        //Debug tooltips
//        tooltip.add(0, Component.literal(""));
//        final FlywheelAccessibleKineticNetwork net = getOrCreateFlywheelNetwork();
//        tooltip.add(1, Component.literal("Network flywheel absorb capacity " + (level.isClientSide ? clientFlywheelAbsorptionCapacityInNetwork + " (client)" : (net == null ? "null" : net.bits_n_bobs$getFlywheelStressAbsoptionCapacity()))));
//        tooltip.add(2, Component.literal("Network flywheel release capacity " + (level.isClientSide ? clientFlywheelReleaseCapacityInNetwork + " (client)" : (net == null ? "null" : net.bits_n_bobs$getFlywheelStressAbsoptionCapacity()))));
//        tooltip.add(3, Component.literal("This flywheel capacity " + getFlywheelStressAbsorptionCapacity()));
//        tooltip.add(4, Component.literal("Stored in this flywheel (sut) " + flywheelMovement.getStoredStressTicks()));
//        tooltip.add(5, Component.literal("Angular velocity " + (flywheelMovement.angularVelocity * 20) + " dps" + ((20 * 60 * flywheelMovement.angularVelocity) / 360) + " rpm"));
//        tooltip.add(6, Component.literal("StorageEnabled: " + BnbConfigs.server().FLYWHEEL_STORAGE_CAPACITY.get()));

        if (!this.running) {
            return super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        }

        Lang.builder(CreateBitsnBobs.MOD_ID)
                .add(Component.translatable("tooltip.bits_n_bobs.flywheel_bearing.flywheel_stats"))
                .forGoggles(tooltip);

        Lang.builder(CreateBitsnBobs.MOD_ID)
                .add(Component.translatable("tooltip.bits_n_bobs.flywheel_bearing.angular_mass"))
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);

        Lang.builder(CreateBitsnBobs.MOD_ID)
                .add(Component.literal(this.flywheelMovement.formatAngularMass() + " "))
                .style(ChatFormatting.AQUA)
                .add(this.flywheelMovement.getAngularMassDescription().copy().withStyle(ChatFormatting.DARK_GRAY))
                .forGoggles(tooltip, 1);

        if (BnbConfigs.server().FLYWHEEL_STORAGE_CAPACITY.get()) {
            Lang.builder(CreateBitsnBobs.MOD_ID)
                    .add(Component.translatable("tooltip.bits_n_bobs.flywheel_bearing.stored_stress"))
                    .style(ChatFormatting.GRAY)
                    .forGoggles(tooltip);
            final float currentStoredStress = this.flywheelMovement.currentStoredStressTicks;
            final float lastStoredStress = this.flywheelMovement.lastStoredStressTicks;
            final float maxStoredStress = this.flywheelMovement.getMaxStoredStressTicks();

            final int direction = Float.compare(currentStoredStress, lastStoredStress);
            final int changeStrength = Mth.clamp(
                    Math.round(1500 * Math.abs(currentStoredStress - lastStoredStress) / maxStoredStress),
                    1,
                    5
            );

            final int maxBars = 100;
            final int filledBars = Mth.clamp(Math.round(currentStoredStress / maxStoredStress * maxBars), 0, maxBars);
            Lang.builder(CreateBitsnBobs.MOD_ID)
                    .add(Component.literal("|".repeat(filledBars)))
                    .style(ChatFormatting.AQUA)
                    .add(Component.literal("|".repeat(maxBars - filledBars))
                                 .withStyle(ChatFormatting.DARK_GRAY))
                    .add(Component.literal(direction == 0 ? "" : direction > 0 ? " " + ">".repeat(changeStrength) : " " + "<".repeat(
                                    changeStrength))
                                 .withStyle(direction == 0 ? ChatFormatting.DARK_GRAY : ChatFormatting.AQUA))
                    .forGoggles(tooltip, 1);

            Lang.builder(CreateBitsnBobs.MOD_ID)
                    .add(Component.literal(String.format("%.1fsut", currentStoredStress)))
                    .style(ChatFormatting.AQUA)
                    .add(Component.literal(String.format("/%.1fsut ", maxStoredStress))
                                 .withStyle(ChatFormatting.GRAY))
                    .add((!this.flywheelMovement.canProvideStress() ? Component.translatable(
                            "tooltip.bits_n_bobs.flywheel_bearing.empty") :
                            this.flywheelMovement.canReceiveStress() ? Component.empty() : Component.translatable(
                                    "tooltip.bits_n_bobs.flywheel_bearing.full"))
                                 .withStyle(ChatFormatting.DARK_GRAY))
                    .forGoggles(tooltip, 1);

            Lang.builder(CreateBitsnBobs.MOD_ID)
                    .add(Component.translatable("tooltip.bits_n_bobs.flywheel_bearing.kinetic_transfer"))
                    .style(ChatFormatting.GRAY)
                    .forGoggles(tooltip);

            Lang.builder(CreateBitsnBobs.MOD_ID)
                    .add(Component.literal(String.format("%.1fsu", this.flywheelMovement.kineticTransfer)))
                    .style(ChatFormatting.AQUA)
                    .forGoggles(tooltip, 1);
        }

        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        this.addGenerationAsZeroIfNeeded(tooltip);
        return true;
    }

    private void addGenerationAsZeroIfNeeded(final List<Component> tooltip) {
        if (!IRotate.StressImpact.isEnabled() || !BnbConfigs.server().FLYWHEEL_STORAGE_CAPACITY.get())
            return;

        final float stressBase = this.calculateAddedStressCapacity();
        if (!Mth.equal(stressBase, 0))
            return;

        CreateLang.translate("gui.goggles.generator_stats")
                .forGoggles(tooltip);
        CreateLang.translate("tooltip.capacityProvided")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        CreateLang.number(0)
                .translate("generic.unit.stress")
                .style(ChatFormatting.AQUA)
                .space()
                .add(CreateLang.translate("gui.goggles.at_current_speed")
                             .style(ChatFormatting.DARK_GRAY))
                .forGoggles(tooltip, 1);

    }

    @Override
    public void addBehaviours(final List<BlockEntityBehaviour> behaviours) {

    }

    @Override
    public void remove() {
        if (!this.level.isClientSide)
            this.disassemble();
        super.remove();
    }

    @Override
    public void write(final CompoundTag compound, final HolderLookup.Provider registries, final boolean clientPacket) {
        compound.putBoolean("Running", this.running);

        compound.putFloat(
                "NetworkFlywheelAbsorptionCapacity",
                this.hasNetwork() ? this.getOrCreateFlywheelNetwork().bits_n_bobs$getFlywheelStressAbsoptionCapacity() : 0
        );
        compound.putFloat(
                "NetworkFlywheelReleaseCapacity",
                this.hasNetwork() ? this.getOrCreateFlywheelNetwork().bits_n_bobs$getFlywheelStressReleaseCapacity() : 0
        );

        compound.putInt("LastGeneratorDirection", this.lastGeneratorDirection);

        this.flywheelMovement.writeAdditional(compound);
        AssemblyException.write(compound, registries, this.lastException);
        super.write(compound, registries, clientPacket);
    }

    @Override
    protected void read(final CompoundTag compound,
                        final HolderLookup.Provider registries,
                        final boolean clientPacket) {
        if (this.wasMoved) {
            super.read(compound, registries, clientPacket);
            return;
        }

        this.clientFlywheelAbsorptionCapacityInNetwork = compound.getFloat("NetworkFlywheelAbsorptionCapacity");
        this.clientFlywheelReleaseCapacityInNetwork = compound.getFloat("NetworkFlywheelReleaseCapacity");

        final float angleBefore = this.flywheelMovement.angle;
        this.running = compound.getBoolean("Running");
        this.lastGeneratorDirection = compound.getInt("LastGeneratorDirection");
        this.flywheelMovement.readAdditional(compound, clientPacket);
        this.lastException = AssemblyException.read(compound, registries);
        super.read(compound, registries, clientPacket);
        if (!clientPacket)
            return;
        if (!this.running) {
            this.flywheelMovement.clientAngle = null;
            this.movedContraption = null;
        }
    }

    public float getInterpolatedAngle(float partialTicks) {
        if (this.isVirtual())
            return Mth.lerp(
                    partialTicks + .5f,
                    this.flywheelMovement.prevClientAngle,
                    this.flywheelMovement.clientAngle == null ? this.flywheelMovement.angle : this.flywheelMovement.clientAngle
            );
        if (this.movedContraption == null || this.movedContraption.isStalled() || !this.running)
            partialTicks = 0;
        return Mth.lerp(
                partialTicks,
                this.flywheelMovement.prevClientAngle,
                this.flywheelMovement.clientAngle == null ? this.flywheelMovement.angle : this.flywheelMovement.clientAngle
        );
    }

    @Override
    public void onSpeedChanged(final float prevSpeed) {
        super.onSpeedChanged(prevSpeed);
        this.checkAssemblyNextTick = true;

        if (this.movedContraption != null && Math.signum(prevSpeed) != Math.signum(this.getSpeed()) && prevSpeed != 0) {
            if (!this.movedContraption.isStalled()) {
                this.flywheelMovement.angle = Math.round(this.flywheelMovement.angle);
                this.applyRotation();
            }
            this.movedContraption.getContraption()
                    .stop(this.level);
        }
    }

    public float getAngularSpeed() {
        float speed = convertToAngular(this.flywheelMovement.angularVelocity);
        if (this.getSpeed() == 0)
            speed = 0;
        if (this.level.isClientSide) {
            speed *= ServerSpeedProvider.get();
            speed += this.clientAngleDiff / 3f;
        }
        return speed;
    }

    public void assemble() {
        if (!(this.level.getBlockState(this.worldPosition)
                .getBlock() instanceof FlywheelBearingBlock))
            return;

        final Direction direction = this.getBlockState().getValue(FlywheelBearingBlock.FACING);
        final BearingContraption contraption = new BearingContraption(false, direction);
        try {
            if (!contraption.assemble(this.level, this.worldPosition))
                return;

            this.lastException = null;
        } catch (final AssemblyException e) {
            this.lastException = e;
            this.sendData();
            return;
        }

        contraption.removeBlocksFromWorld(this.level, BlockPos.ZERO);
        this.movedContraption = InertControlledContraptionEntity.create(this.level, this, contraption);
        final BlockPos anchor = this.worldPosition.relative(direction);
        this.movedContraption.setPos(anchor.getX(), anchor.getY(), anchor.getZ());
        this.movedContraption.setRotationAxis(direction.getAxis());
        this.level.addFreshEntity(this.movedContraption);

        this.flywheelMovement.assemble(this, contraption);

        AllSoundEvents.CONTRAPTION_ASSEMBLE.playOnServer(this.level, this.worldPosition);

        this.running = true;
        this.flywheelMovement.zero();
        this.sendData();
        this.updateGeneratedRotation();
        this.updateFlywheelStressesInNetwork();
    }

    public void disassemble() {
        if (!this.running && this.movedContraption == null)
            return;
        this.flywheelMovement.zero();
        if (this.movedContraption != null) {
            this.movedContraption.setAngle(0);
            this.movedContraption.disassemble();
            AllSoundEvents.CONTRAPTION_DISASSEMBLE.playOnServer(this.level, this.worldPosition);
        }

        this.movedContraption = null;
        this.running = false;
        this.updateGeneratedRotation();
        this.updateFlywheelStressesInNetwork();
        this.checkAssemblyNextTick = false;
        this.sendData();
    }

    @Override
    public List<BlockPos> addPropagationLocations(final IRotate block,
                                                  final BlockState state,
                                                  final List<BlockPos> neighbours) {
        if (!ICogWheel.isLargeCog(state))
            return super.addPropagationLocations(block, state, neighbours);

        BlockPos.betweenClosedStream(new BlockPos(-1, -1, -1), new BlockPos(1, 1, 1))
                .forEach(offset -> {
                    if (offset.distSqr(BlockPos.ZERO) == 2)
                        neighbours.add(this.worldPosition.offset(offset));
                });
        return neighbours;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level.isClientSide)
            this.clientAngleDiff /= 2;

        if (!this.level.isClientSide && this.checkAssemblyNextTick) {
            this.checkAssemblyNextTick = false;
            if (this.running) {
                if (this.speed == 0 && (this.movedContraption == null || this.movedContraption.getContraption()
                        .getBlocks()
                        .isEmpty())) {
                    if (this.movedContraption != null)
                        this.movedContraption.getContraption()
                                .stop(this.level);
                    this.disassemble();
                    return;
                }
            } else {
                this.assemble();
            }
        }

        if (!this.running) {
            this.flywheelMovement.zero();
            return;
        }

        if (BnbConfigs.server().FLYWHEEL_STORAGE_CAPACITY.get()) {
            this.flywheelMovement.tickForStorageBehaviour(this);
        } else {
            this.flywheelMovement.tick(this);
        }
        this.applyRotation();
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (this.movedContraption != null && this.running && !this.level.isClientSide)
            this.sendData();
    }

    protected void applyRotation() {
        if (this.movedContraption == null)
            return;
        this.movedContraption.setAngle(this.level.isClientSide ? this.flywheelMovement.clientAngle : this.flywheelMovement.angle);
        final BlockState blockState = this.getBlockState();
        if (blockState.hasProperty(BlockStateProperties.FACING))
            this.movedContraption.setRotationAxis(blockState.getValue(BlockStateProperties.FACING)
                                                          .getAxis());
    }

    @Override
    public boolean addToTooltip(final List<Component> tooltip, final boolean isPlayerSneaking) {
        if (super.addToTooltip(tooltip, isPlayerSneaking))
            return true;
        if (isPlayerSneaking)
            return false;
        if (this.running)
            return false;
        final BlockState state = this.getBlockState();
        if (!(state.getBlock() instanceof BearingBlock))
            return false;

        final BlockState attachedState = this.level.getBlockState(this.worldPosition.relative(state.getValue(
                BearingBlock.FACING)));
        if (attachedState.canBeReplaced())
            return false;
        TooltipHelper.addHint(tooltip, "hint.empty_bearing");
        return true;
    }

    @Override
    public boolean isAttachedTo(final AbstractContraptionEntity contraption) {
        return this.movedContraption == contraption;
    }

    @Override
    public void attach(final ControlledContraptionEntity contraption) {
        final BlockState blockState = this.getBlockState();
        if (!(contraption.getContraption() instanceof BearingContraption))
            return;
        if (!(contraption instanceof final InertControlledContraptionEntity inertControlledContraptionEntity))
            return;
        if (!blockState.hasProperty(BearingBlock.FACING))
            return;

        this.movedContraption = inertControlledContraptionEntity;
        this.setChanged();
        final BlockPos anchor = this.worldPosition.relative(blockState.getValue(BearingBlock.FACING));
        this.movedContraption.setPos(anchor.getX(), anchor.getY(), anchor.getZ());
        this.running = true;
        if (!this.level.isClientSide) {
            this.sendData();
        }
    }

    @Override
    public void onStall() {
        // I dont think this should be possible but just handle it as normal
        if (!this.level.isClientSide)
            this.sendData();
    }

    @Override
    public boolean isValid() {
        return !this.isRemoved();
    }

    @Override
    public BlockPos getBlockPosition() {
        return this.worldPosition;
    }

    @Override
    public AssemblyException getLastAssemblyException() {
        return this.lastException;
    }

    public float getFlywheelStressDelta() {
        if (!this.hasNetwork() || !BnbConfigs.server().FLYWHEEL_STORAGE_CAPACITY.get()) {
            return 0;
        }

        final float flywheelAbsorptionCapacityInNetwork = this.level == null ? 0 :
                (this.level.isClientSide ? this.clientFlywheelAbsorptionCapacityInNetwork :
                        this.getOrCreateFlywheelNetwork().bits_n_bobs$getFlywheelStressAbsoptionCapacity());
        final float flywheelReleaseCapacityInNetwork = this.level == null ? 0 :
                (this.level.isClientSide ? this.clientFlywheelReleaseCapacityInNetwork :
                        this.getOrCreateFlywheelNetwork().bits_n_bobs$getFlywheelStressReleaseCapacity());

        final float stressDifferenceInNetwork = this.capacity - flywheelReleaseCapacityInNetwork - this.stress;

        final float flywheelAbsorptionStressCapacity = this.getFlywheelStressAbsorptionCapacity();
        final float flywheelReleaseStressCapacity = this.getFlywheelStressReleaseCapacity();

        if (stressDifferenceInNetwork > 0) {
            if (flywheelAbsorptionCapacityInNetwork == 0)
                return 0;

            return (flywheelAbsorptionStressCapacity / flywheelAbsorptionCapacityInNetwork) * stressDifferenceInNetwork;
        } else {
            if (flywheelReleaseCapacityInNetwork == 0)
                return 0;

            return (flywheelReleaseStressCapacity / flywheelReleaseCapacityInNetwork) * stressDifferenceInNetwork;
        }
    }

    protected FlywheelAccessibleKineticNetwork getOrCreateFlywheelNetwork() {
        return (FlywheelAccessibleKineticNetwork) this.getOrCreateNetwork();
    }

    public float getFlywheelStressAbsorptionCapacity() {
        return this.running ? this.flywheelMovement.getFlywheelStressCapacity() : 0;
    }

    public void updateFlywheelStressesInNetwork() {
        if (!BnbConfigs.server().FLYWHEEL_STORAGE_CAPACITY.get() || !this.hasNetwork())
            return;
        this.getOrCreateFlywheelNetwork().bits_n_bobs$updateFlywheelStresses();
    }

    @Override
    public float getGeneratedSpeed() {
        if (!BnbConfigs.server().FLYWHEEL_STORAGE_CAPACITY.get()) return 0;

        final float currentSpeed = this.getTheoreticalSpeed();

        if (currentSpeed != 0) {
            final int direction = currentSpeed > 0 ? 1 : -1;
            if (direction != this.lastGeneratorDirection) {
                this.lastGeneratorDirection = direction;
                this.sendData();
            }
        }

        return this.flywheelMovement.canProvideStress() ? (this.lastGeneratorDirection * 8) : 0;
    }

    @Override
    public float calculateAddedStressCapacity() {
        if (!BnbConfigs.server().FLYWHEEL_STORAGE_CAPACITY.get()) return 0;

        final float capacity = this.getFlywheelStressReleaseCapacity();
        this.lastCapacityProvided = capacity;
        final float currentSpeed = this.getGeneratedSpeed();
        return currentSpeed == 0 ? capacity : capacity / Math.abs(currentSpeed);
    }

    public float getFlywheelStressReleaseCapacity() {
        return this.flywheelMovement.canProvideStress() ? this.flywheelMovement.getMaxTransferCapacity() : 0;
    }

    public void updateFlywheelStressesFromNetwork() {
        this.sendData();
    }

    @Override
    public AABB getRenderBoundingBox() {
        return super.getRenderBoundingBox().inflate(1);
    }
}

