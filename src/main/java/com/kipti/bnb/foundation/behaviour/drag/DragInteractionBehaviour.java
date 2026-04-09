package com.kipti.bnb.foundation.behaviour.drag;

import com.cake.azimuth.behaviour.SuperBlockEntityBehaviour;
import com.kipti.bnb.foundation.client.ElasticFloat;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

import java.util.function.Consumer;

/** Reusable behaviour that converts right-click drag gestures into a discrete integer value with debounced commits. */
public class DragInteractionBehaviour extends SuperBlockEntityBehaviour {

    public static final BehaviourType<DragInteractionBehaviour> TYPE = new BehaviourType<>("drag_interaction");

    private int value;
    private int targetValue;
    private ElasticFloat animatedValue;

    private int ticksSinceFirstChange;
    private int ticksSinceLastUpdate;
    private boolean initialCommitDone;

    private int min;
    private int max;
    private int initialDelay;
    private int updateInterval;
    private Consumer<Integer> onValueCommitted;

    public DragInteractionBehaviour(final SmartBlockEntity be) {
        super(be);
        this.value = 0;
        this.targetValue = 0;
        this.animatedValue = new ElasticFloat(0f, 0.6f, 0.5f);
        this.ticksSinceFirstChange = -1;
        this.ticksSinceLastUpdate = 0;
        this.initialCommitDone = false;
        this.min = 0;
        this.max = 15;
        this.initialDelay = 3;
        this.updateInterval = 5;
        this.onValueCommitted = v -> {};
    }

    public DragInteractionBehaviour withRange(final int min, final int max) {
        this.min = min;
        this.max = max;
        return this;
    }

    public DragInteractionBehaviour withDebounce(final int initialDelay, final int updateInterval) {
        this.initialDelay = initialDelay;
        this.updateInterval = updateInterval;
        return this;
    }

    public DragInteractionBehaviour withCallback(final Consumer<Integer> onValueCommitted) {
        this.onValueCommitted = onValueCommitted;
        return this;
    }

    public void updateTargetValue(final int newTarget) {
        this.targetValue = Math.max(this.min, Math.min(this.max, newTarget));
    }

    @Override
    public void tick() {
        super.tick();
        this.animatedValue.setTarget(this.targetValue);
        this.animatedValue.tick();

        if (this.isServerLevel()) {
            this.tickDebounce();
        }
    }

    private void tickDebounce() {
        if (this.targetValue != this.value) {
            if (this.ticksSinceFirstChange == -1) {
                this.ticksSinceFirstChange = 0;
            }
            this.ticksSinceFirstChange++;
            this.ticksSinceLastUpdate++;

            boolean ready = this.initialCommitDone
                    ? this.ticksSinceLastUpdate >= this.updateInterval
                    : this.ticksSinceFirstChange >= this.initialDelay;

            if (ready) {
                this.value = this.targetValue;
                this.ticksSinceLastUpdate = 0;
                this.initialCommitDone = true;
                this.onValueCommitted.accept(this.value);
                this.sendData();
            }
        } else {
            this.ticksSinceFirstChange = -1;
            this.initialCommitDone = false;
        }
    }

    public float getAnimatedValue(final float partialTick) {
        return this.animatedValue.getCurrentValue(partialTick);
    }

    public int getValue() {
        return this.value;
    }

    public int getMin() {
        return this.min;
    }

    public int getMax() {
        return this.max;
    }

    @Override
    public void write(final CompoundTag compound, final HolderLookup.Provider registries, final boolean clientPacket) {
        super.write(compound, registries, clientPacket);
        compound.putInt("DragValue", this.value);
    }

    @Override
    public void read(final CompoundTag compound, final HolderLookup.Provider registries, final boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        this.value = compound.getInt("DragValue");
        this.targetValue = this.value;
    }

    @Override
    public BehaviourType<DragInteractionBehaviour> getType() {
        return TYPE;
    }
}
