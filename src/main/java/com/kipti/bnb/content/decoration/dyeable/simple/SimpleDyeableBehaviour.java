package com.kipti.bnb.content.decoration.dyeable.simple;

import com.kipti.bnb.content.decoration.dyeable.BaseDyeableBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;

public class SimpleDyeableBehaviour extends BaseDyeableBehaviour {

    public static final BehaviourType<SimpleDyeableBehaviour> TYPE = new BehaviourType<>("simple_dyeable");

    public SimpleDyeableBehaviour(final SmartBlockEntity be) {
        super(be);
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

}
