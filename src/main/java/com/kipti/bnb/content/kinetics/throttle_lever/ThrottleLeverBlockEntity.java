package com.kipti.bnb.content.kinetics.throttle_lever;

import com.kipti.bnb.foundation.ElasticFloat;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class ThrottleLeverBlockEntity extends GeneratingKineticBlockEntity {

    private final ElasticFloat currentPower = new ElasticFloat(0, 0.4f, 0.6f);

    public ThrottleLeverBlockEntity(final BlockEntityType<?> typeIn, final BlockPos pos, final BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    public void tick() {
        super.tick();
        currentPower.setTarget((float) Math.floor((AnimationTickHolder.getTicks() / 15f) % 16));
        currentPower.tick();
        updateGeneratedRotation();
    }

    @Override
    public float getGeneratedSpeed() {
        return 16f;
    }

    @Override
    public float calculateAddedStressCapacity() {
        return 16f;
    }

    public float getCurrentPower(final float pt) {
        return currentPower.getCurrentValue(pt);
    }
}

