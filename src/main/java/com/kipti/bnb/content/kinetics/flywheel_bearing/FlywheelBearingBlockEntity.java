package com.kipti.bnb.content.kinetics.flywheel_bearing;

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

import java.util.List;

/**
 * Predominantly a copy of BearingBlockEntity with many adjustments and features cut for flywheel bearing behavior
 */
public class FlywheelBearingBlockEntity extends GeneratingKineticBlockEntity implements IControlContraption, IDisplayAssemblyExceptions {

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
        lazyTickRate = 5;
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

        if (!running) {
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
                .add(Component.literal(flywheelMovement.formatAngularMass() + " "))
                .style(ChatFormatting.AQUA)
                .add(flywheelMovement.getAngularMassDescription().copy().withStyle(ChatFormatting.DARK_GRAY))
                .forGoggles(tooltip, 1);

        if (BnbConfigs.server().FLYWHEEL_STORAGE_CAPACITY.get()) {
            Lang.builder(CreateBitsnBobs.MOD_ID)
                    .add(Component.translatable("tooltip.bits_n_bobs.flywheel_bearing.stored_stress"))
                    .style(ChatFormatting.GRAY)
                    .forGoggles(tooltip);
            final float currentStoredStress = flywheelMovement.currentStoredStressTicks;
            final float lastStoredStress = flywheelMovement.lastStoredStressTicks;
            final float maxStoredStress = flywheelMovement.getMaxStoredStressTicks();

            final int direction = Float.compare(currentStoredStress, lastStoredStress);
            final int changeStrength = Mth.clamp(Math.round(1500 * Math.abs(currentStoredStress - lastStoredStress) / maxStoredStress), 1, 5);

            final int maxBars = 100;
            final int filledBars = Mth.clamp(Math.round(currentStoredStress / maxStoredStress * maxBars), 0, maxBars);
            Lang.builder(CreateBitsnBobs.MOD_ID)
                    .add(Component.literal("|".repeat(filledBars)))
                    .style(ChatFormatting.AQUA)
                    .add(Component.literal("|".repeat(maxBars - filledBars))
                            .withStyle(ChatFormatting.DARK_GRAY))
                    .add(Component.literal(direction == 0 ? "" : direction > 0 ? " " + ">".repeat(changeStrength) : " " + "<".repeat(changeStrength))
                            .withStyle(direction == 0 ? ChatFormatting.DARK_GRAY : ChatFormatting.AQUA))
                    .forGoggles(tooltip, 1);

            Lang.builder(CreateBitsnBobs.MOD_ID)
                    .add(Component.literal(String.format("%.1fsut", currentStoredStress)))
                    .style(ChatFormatting.AQUA)
                    .add(Component.literal(String.format("/%.1fsut ", maxStoredStress))
                            .withStyle(ChatFormatting.GRAY))
                    .add((!flywheelMovement.canProvideStress() ? Component.translatable("tooltip.bits_n_bobs.flywheel_bearing.empty") :
                            flywheelMovement.canReceiveStress() ? Component.empty() : Component.translatable("tooltip.bits_n_bobs.flywheel_bearing.full"))
                            .withStyle(ChatFormatting.DARK_GRAY))
                    .forGoggles(tooltip, 1);

            Lang.builder(CreateBitsnBobs.MOD_ID)
                    .add(Component.translatable("tooltip.bits_n_bobs.flywheel_bearing.kinetic_transfer"))
                    .style(ChatFormatting.GRAY)
                    .forGoggles(tooltip);

            Lang.builder(CreateBitsnBobs.MOD_ID)
                    .add(Component.literal(String.format("%.1fsu", flywheelMovement.kineticTransfer)))
                    .style(ChatFormatting.AQUA)
                    .forGoggles(tooltip, 1);
        }

        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        addGenerationAsZeroIfNeeded(tooltip);
        return true;
    }

    private void addGenerationAsZeroIfNeeded(final List<Component> tooltip) {
        if (!IRotate.StressImpact.isEnabled())
            return;

        final float stressBase = calculateAddedStressCapacity();
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
        if (!level.isClientSide)
            disassemble();
        super.remove();
    }

    @Override
    public void write(final CompoundTag compound, final HolderLookup.Provider registries, final boolean clientPacket) {
        compound.putBoolean("Running", running);

        compound.putFloat("NetworkFlywheelAbsorptionCapacity", hasNetwork() ? getOrCreateFlywheelNetwork().bits_n_bobs$getFlywheelStressAbsoptionCapacity() : 0);
        compound.putFloat("NetworkFlywheelReleaseCapacity", hasNetwork() ? getOrCreateFlywheelNetwork().bits_n_bobs$getFlywheelStressReleaseCapacity() : 0);

        compound.putInt("LastGeneratorDirection", lastGeneratorDirection);

        flywheelMovement.writeAdditional(compound);
        AssemblyException.write(compound, registries, lastException);
        super.write(compound, registries, clientPacket);
    }

    @Override
    protected void read(final CompoundTag compound, final HolderLookup.Provider registries, final boolean clientPacket) {
        if (wasMoved) {
            super.read(compound, registries, clientPacket);
            return;
        }

        if (clientPacket) {//TODO (?) implement network updates to the be so the client properly knows the correct information
            clientFlywheelAbsorptionCapacityInNetwork = compound.getFloat("NetworkFlywheelAbsorptionCapacity");
            clientFlywheelReleaseCapacityInNetwork = compound.getFloat("NetworkFlywheelReleaseCapacity");
        }

        final float angleBefore = flywheelMovement.angle;
        running = compound.getBoolean("Running");
        lastGeneratorDirection = compound.getInt("LastGeneratorDirection");
        flywheelMovement.readAdditional(compound, clientPacket);
        lastException = AssemblyException.read(compound, registries);
        super.read(compound, registries, clientPacket);
        if (!clientPacket)
            return;
        if (!running) {
            flywheelMovement.clientAngle = null;
            movedContraption = null;
        }
    }

    public float getInterpolatedAngle(float partialTicks) {
        if (isVirtual())
            return Mth.lerp(partialTicks + .5f, flywheelMovement.prevClientAngle, flywheelMovement.clientAngle == null ? flywheelMovement.angle : flywheelMovement.clientAngle);
        if (movedContraption == null || movedContraption.isStalled() || !running)
            partialTicks = 0;
        return Mth.lerp(partialTicks, flywheelMovement.prevClientAngle, flywheelMovement.clientAngle == null ? flywheelMovement.angle : flywheelMovement.clientAngle);
    }

    @Override
    public void onSpeedChanged(final float prevSpeed) {
        super.onSpeedChanged(prevSpeed);
        checkAssemblyNextTick = true;

        if (movedContraption != null && Math.signum(prevSpeed) != Math.signum(getSpeed()) && prevSpeed != 0) {
            if (!movedContraption.isStalled()) {
                flywheelMovement.angle = Math.round(flywheelMovement.angle);
                applyRotation();
            }
            movedContraption.getContraption()
                    .stop(level);
        }
    }

    public float getAngularSpeed() {
        float speed = convertToAngular(flywheelMovement.angularVelocity);
        if (getSpeed() == 0)
            speed = 0;
        if (level.isClientSide) {
            speed *= ServerSpeedProvider.get();
            speed += clientAngleDiff / 3f;
        }
        return speed;
    }

    public void assemble() {
        if (!(level.getBlockState(worldPosition)
                .getBlock() instanceof FlywheelBearingBlock))
            return;

        final Direction direction = getBlockState().getValue(FlywheelBearingBlock.FACING);
        final BearingContraption contraption = new BearingContraption(false, direction);
        try {
            if (!contraption.assemble(level, worldPosition))
                return;

            lastException = null;
        } catch (final AssemblyException e) {
            lastException = e;
            sendData();
            return;
        }

        contraption.removeBlocksFromWorld(level, BlockPos.ZERO);
        movedContraption = InertControlledContraptionEntity.create(level, this, contraption);
        final BlockPos anchor = worldPosition.relative(direction);
        movedContraption.setPos(anchor.getX(), anchor.getY(), anchor.getZ());
        movedContraption.setRotationAxis(direction.getAxis());
        level.addFreshEntity(movedContraption);

        flywheelMovement.assemble(this, contraption);

        AllSoundEvents.CONTRAPTION_ASSEMBLE.playOnServer(level, worldPosition);

        running = true;
        flywheelMovement.zero();
        sendData();
        updateGeneratedRotation();
        updateFlywheelStressesInNetwork();
    }

    public void disassemble() {
        if (!running && movedContraption == null)
            return;
        flywheelMovement.zero();
        if (movedContraption != null) {
            movedContraption.setAngle(0);
            movedContraption.disassemble();
            AllSoundEvents.CONTRAPTION_DISASSEMBLE.playOnServer(level, worldPosition);
        }

        movedContraption = null;
        running = false;
        updateGeneratedRotation();
        updateFlywheelStressesInNetwork();
        checkAssemblyNextTick = false;
        sendData();
    }

    @Override
    public List<BlockPos> addPropagationLocations(final IRotate block, final BlockState state, final List<BlockPos> neighbours) {
        if (!ICogWheel.isLargeCog(state))
            return super.addPropagationLocations(block, state, neighbours);

        BlockPos.betweenClosedStream(new BlockPos(-1, -1, -1), new BlockPos(1, 1, 1))
                .forEach(offset -> {
                    if (offset.distSqr(BlockPos.ZERO) == 2)
                        neighbours.add(worldPosition.offset(offset));
                });
        return neighbours;
    }

    @Override
    public void tick() {
        super.tick();

        if (level.isClientSide)
            clientAngleDiff /= 2;

        if (!level.isClientSide && checkAssemblyNextTick) {
            checkAssemblyNextTick = false;
            if (running) {
                if (speed == 0 && (movedContraption == null || movedContraption.getContraption()
                        .getBlocks()
                        .isEmpty())) {
                    if (movedContraption != null)
                        movedContraption.getContraption()
                                .stop(level);
                    disassemble();
                    return;
                }
            } else {
                assemble();
            }
        }

        if (!running) {
            flywheelMovement.zero();
            return;
        }

        if (BnbConfigs.server().FLYWHEEL_STORAGE_CAPACITY.get()) {
            flywheelMovement.tickForStorageBehaviour(this);
        } else {
            flywheelMovement.tick(this);
        }
        applyRotation();
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (movedContraption != null && running && !level.isClientSide)
            sendData();
    }

    protected void applyRotation() {
        if (movedContraption == null)
            return;
        movedContraption.setAngle(level.isClientSide ? flywheelMovement.clientAngle : flywheelMovement.angle);
        final BlockState blockState = getBlockState();
        if (blockState.hasProperty(BlockStateProperties.FACING))
            movedContraption.setRotationAxis(blockState.getValue(BlockStateProperties.FACING)
                    .getAxis());
    }

    @Override
    public boolean addToTooltip(final List<Component> tooltip, final boolean isPlayerSneaking) {
        if (super.addToTooltip(tooltip, isPlayerSneaking))
            return true;
        if (isPlayerSneaking)
            return false;
        if (running)
            return false;
        final BlockState state = getBlockState();
        if (!(state.getBlock() instanceof BearingBlock))
            return false;

        final BlockState attachedState = level.getBlockState(worldPosition.relative(state.getValue(BearingBlock.FACING)));
        if (attachedState.canBeReplaced())
            return false;
        TooltipHelper.addHint(tooltip, "hint.empty_bearing");
        return true;
    }

    @Override
    public boolean isAttachedTo(final AbstractContraptionEntity contraption) {
        return movedContraption == contraption;
    }

    @Override
    public void attach(final ControlledContraptionEntity contraption) {
        final BlockState blockState = getBlockState();
        if (!(contraption.getContraption() instanceof BearingContraption))
            return;
        if (!(contraption instanceof final InertControlledContraptionEntity inertControlledContraptionEntity))
            return;
        if (!blockState.hasProperty(BearingBlock.FACING))
            return;

        this.movedContraption = inertControlledContraptionEntity;
        setChanged();
        final BlockPos anchor = worldPosition.relative(blockState.getValue(BearingBlock.FACING));
        movedContraption.setPos(anchor.getX(), anchor.getY(), anchor.getZ());
        this.running = true;
        if (!level.isClientSide) {
            sendData();
        }
    }

    @Override
    public void onStall() {
        // I dont think this should be possible but just handle it as normal
        if (!level.isClientSide)
            sendData();
    }

    @Override
    public boolean isValid() {
        return !isRemoved();
    }

    @Override
    public BlockPos getBlockPosition() {
        return worldPosition;
    }

    @Override
    public AssemblyException getLastAssemblyException() {
        return lastException;
    }

    public float getFlywheelStressDelta() {
        if (!hasNetwork() || !BnbConfigs.server().FLYWHEEL_STORAGE_CAPACITY.get()) {
            return 0;
        }

        final float flywheelAbsorptionCapacityInNetwork = level == null ? 0 :
                (level.isClientSide ? clientFlywheelAbsorptionCapacityInNetwork :
                        getOrCreateFlywheelNetwork().bits_n_bobs$getFlywheelStressAbsoptionCapacity());
        final float flywheelReleaseCapacityInNetwork = level == null ? 0 :
                (level.isClientSide ? clientFlywheelReleaseCapacityInNetwork :
                        getOrCreateFlywheelNetwork().bits_n_bobs$getFlywheelStressReleaseCapacity());

        final float stressDifferenceInNetwork = capacity - flywheelReleaseCapacityInNetwork - stress;

        final float flywheelAbsorptionStressCapacity = getFlywheelStressAbsorptionCapacity();
        final float flywheelReleaseStressCapacity = getFlywheelStressReleaseCapacity();

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
        return (FlywheelAccessibleKineticNetwork) getOrCreateNetwork();
    }

    public float getFlywheelStressAbsorptionCapacity() {
        return running ? flywheelMovement.getFlywheelStressCapacity() : 0;
    }

    public void updateFlywheelStressesInNetwork() {
        if (!BnbConfigs.server().FLYWHEEL_STORAGE_CAPACITY.get() || !hasNetwork())
            return;
        getOrCreateFlywheelNetwork().bits_n_bobs$updateFlywheelStresses();
    }

    @Override
    public float getGeneratedSpeed() {
        if (!BnbConfigs.server().FLYWHEEL_STORAGE_CAPACITY.get()) return 0;

        final float currentSpeed = getTheoreticalSpeed();

        if (currentSpeed != 0) {
            final int direction = currentSpeed > 0 ? 1 : -1;
            if (direction != lastGeneratorDirection) {
                lastGeneratorDirection = direction;
                sendData();
            }
        }

        return flywheelMovement.canProvideStress() ? (lastGeneratorDirection * 8) : 0;
    }

    @Override
    public float calculateAddedStressCapacity() {
        if (!BnbConfigs.server().FLYWHEEL_STORAGE_CAPACITY.get()) return 0;

        final float capacity = getFlywheelStressReleaseCapacity();
        this.lastCapacityProvided = capacity;
        final float currentSpeed = getGeneratedSpeed();
        return currentSpeed == 0 ? capacity : capacity / Math.abs(currentSpeed);
    }

    public float getFlywheelStressReleaseCapacity() {
        return flywheelMovement.canProvideStress() ? flywheelMovement.getMaxTransferCapacity() : 0;
    }

    public void updateFlywheelStressesFromNetwork() {
        sendData();
    }

}

