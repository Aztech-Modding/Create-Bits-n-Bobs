package com.kipti.bnb.foundation;

/**
 * Lerped float type util
 * Has 3 properties, overshoot, stiffness and approximate range to cap velocity within.
 */
public class ElasticFloat {

    private float previous;
    private float current;
    private float velocity;
    private float target;

    private final float overshoot;
    private final float stiffness;

    public ElasticFloat(final float initial, final float overshoot, final float stiffness) {
        this.current = initial;
        this.previous = initial;
        this.overshoot = overshoot;
        this.stiffness = stiffness;
    }

    public void tick() {
        final float overshoot = 0.6f;
        final float stiffness = 0.5f;
        final float range = 15f;
        velocity += (target - current) * overshoot;
        final float maxVelocity = range / 2f;
        velocity = Math.clamp(velocity, -maxVelocity, maxVelocity);
        velocity *= stiffness;
        previous = current;
        current += velocity;
    }

    public void setTarget(final float target) {
        this.target = target;
    }

    public float getCurrentValue(final float pt) {
        return previous + (current - previous) * pt;
    }
}

