package com.kipti.bnb.mixin.flywheel_bearing;

import com.kipti.bnb.content.kinetics.flywheel_bearing.FlywheelBearingBlockEntity;
import com.kipti.bnb.mixin_accessor.FlywheelAccessibleKineticNetwork;
import com.kipti.bnb.registry.core.BnbConfigs;
import com.simibubi.create.content.kinetics.KineticNetwork;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import java.util.Map;

//TODO: silent add and remove handling, i think that can just be flat

///  dont actually need serialization in KBE, do, clear kinetic info. I think thats it, then go back and finish the flywheel mechanics
/// //Ok looking at this more, i think i can avoid getting the flywheel capacity from the block, i just get from the network directly :D

@Mixin(KineticNetwork.class)
public abstract class KineticNetworkMixin implements FlywheelAccessibleKineticNetwork {

    @Shadow
    public Map<KineticBlockEntity, Float> members;

    @Shadow
    public abstract void sync();

    @Shadow
    private float currentStress;

    /**
     * Tracking for how much stress within the network flywheels are able to take (excluding full bearings)
     * Unloaded flywheels are not tracked in the network
     */
    @Unique
    private float currentFlywheelStressAbsorptionCapacity = 0f;
    /**
     * Tracking for how much stress within the network flywheels specifically are able to generate
     * Unloaded flywheels are not tracked in the network
     */
    @Unique
    private float currentFlywheelStressReleaseCapacity = 0f;

    @Unique
    private boolean bits_n_bobs$flywheelCapacitiesAllowedInServer() {
        return BnbConfigs.server().FLYWHEEL_STORAGE_CAPACITY.get();
    }

    @Unique
    private float bits_n_bobs$calculateFlywheelStressAbsorptionCapacity() {
        if (!bits_n_bobs$flywheelCapacitiesAllowedInServer())
            return 0;

        float presentFlywheelStressCapacity = 0;
        for (final Iterator<KineticBlockEntity> iterator = members.keySet()
                .iterator(); iterator.hasNext(); ) {
            final KineticBlockEntity be = iterator.next();
            if (be.getLevel()
                    .getBlockEntity(be.getBlockPos()) != be) {
                iterator.remove();
                continue;
            }
            if (!(be instanceof final FlywheelBearingBlockEntity flywheelBearing))
                continue;
            presentFlywheelStressCapacity += flywheelBearing.getFlywheelStressAbsorptionCapacity();
        }
        return presentFlywheelStressCapacity;
    }

    @Unique
    private float bits_n_bobs$calculateFlywheelStressReleaseCapacity() {
        if (!bits_n_bobs$flywheelCapacitiesAllowedInServer())
            return 0;

        float presentFlywheelStressCapacity = 0;
        for (final Iterator<KineticBlockEntity> iterator = members.keySet()
                .iterator(); iterator.hasNext(); ) {
            final KineticBlockEntity be = iterator.next();
            if (be.getLevel()
                    .getBlockEntity(be.getBlockPos()) != be) {
                iterator.remove();
                continue;
            }
            if (!(be instanceof final FlywheelBearingBlockEntity flywheelBearing))
                continue;
            presentFlywheelStressCapacity += flywheelBearing.getFlywheelStressReleaseCapacity();
        }
        return presentFlywheelStressCapacity;
    }

    @Unique
    public void bits_n_bobs$updateFlywheelStresses() {
        final float newFlywheelStressAbsorptionCapacity = bits_n_bobs$calculateFlywheelStressAbsorptionCapacity();
        final float newFlywheelStressReleaseCapacity = bits_n_bobs$calculateFlywheelStressReleaseCapacity();
        if (currentFlywheelStressAbsorptionCapacity != newFlywheelStressAbsorptionCapacity ||
                currentFlywheelStressReleaseCapacity != newFlywheelStressReleaseCapacity) {
            currentFlywheelStressAbsorptionCapacity = newFlywheelStressAbsorptionCapacity;
            currentFlywheelStressReleaseCapacity = newFlywheelStressReleaseCapacity;
            bits_n_bob$syncToFlywheels();
        }
    }

    @Unique
    private void bits_n_bob$syncToFlywheels() {
        if (!bits_n_bobs$flywheelCapacitiesAllowedInServer())
            return;

        for (final Iterator<KineticBlockEntity> iterator = members.keySet()
                .iterator(); iterator.hasNext(); ) {
            final KineticBlockEntity be = iterator.next();
            if (be.getLevel()
                    .getBlockEntity(be.getBlockPos()) != be) {
                iterator.remove();
                continue;
            }
            if (!(be instanceof final FlywheelBearingBlockEntity flywheelBearing))
                continue;
            flywheelBearing.updateFlywheelStressesFromNetwork();
        }
    }

    @Inject(method = "updateNetwork", at = @At("HEAD"))
    public void bits_n_bobs$updateNetworkHead(final CallbackInfo ci) {
        if (!bits_n_bobs$flywheelCapacitiesAllowedInServer())
            return;

        final float flywheelCapacity = bits_n_bobs$calculateFlywheelStressAbsorptionCapacity();
        // trigger a sync if flywheel capacity has changed, by changing the stress capacity local, very cheeky, but faster than doing another injection at the end, and somehow cleaner
        if (currentFlywheelStressAbsorptionCapacity != flywheelCapacity) {
            currentFlywheelStressAbsorptionCapacity = flywheelCapacity;
            currentStress = -1;
        }
    }

    @Inject(method = "addSilently", at = @At("HEAD"))
    public void addSilently(final KineticBlockEntity be, final float lastCapacity, final float lastStress, final CallbackInfo ci) {
        if (!bits_n_bobs$flywheelCapacitiesAllowedInServer())
            return;

        if (members.containsKey(be))
            return;

        if (be instanceof final FlywheelBearingBlockEntity flywheelBearing) {
            currentFlywheelStressAbsorptionCapacity += flywheelBearing.getFlywheelStressAbsorptionCapacity();
            currentFlywheelStressReleaseCapacity += flywheelBearing.getFlywheelStressReleaseCapacity();
        }
    }

    @Inject(method = "add", at = @At("HEAD"))
    public void add(final KineticBlockEntity be, final CallbackInfo ci) {
        if (!bits_n_bobs$flywheelCapacitiesAllowedInServer())
            return;

        if (members.containsKey(be))
            return;

        if (be instanceof final FlywheelBearingBlockEntity flywheelBearing) {
            currentFlywheelStressAbsorptionCapacity += flywheelBearing.getFlywheelStressAbsorptionCapacity();
            currentFlywheelStressReleaseCapacity += flywheelBearing.getFlywheelStressReleaseCapacity();
        }
    }

    @Inject(method = "remove", at = @At("HEAD"))
    public void remove(final KineticBlockEntity be, final CallbackInfo ci) {
        if (!bits_n_bobs$flywheelCapacitiesAllowedInServer())
            return;

        if (!members.containsKey(be))
            return;

        if (be instanceof final FlywheelBearingBlockEntity flywheelBearing) {
            currentFlywheelStressAbsorptionCapacity -= flywheelBearing.getFlywheelStressAbsorptionCapacity();
            currentFlywheelStressReleaseCapacity -= flywheelBearing.getFlywheelStressReleaseCapacity();
        }
    }

    @Override
    public float bits_n_bobs$getFlywheelStressAbsoptionCapacity() {
        return currentFlywheelStressAbsorptionCapacity;
    }

    @Override
    public float bits_n_bobs$getFlywheelStressReleaseCapacity() {
        return currentFlywheelStressReleaseCapacity;
    }


}

