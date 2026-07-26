package com.kipti.bnb.content.decoration.dyeable.simple;

import com.kipti.bnb.content.decoration.dyeable.BaseDyeableBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public class SimpleDyeableBehaviour extends BaseDyeableBehaviour {

    public static final BehaviourType<SimpleDyeableBehaviour> TYPE = new BehaviourType<>("simple_dyeable");

    public SimpleDyeableBehaviour(final SmartBlockEntity be) {
        super(be);
    }

    @Nullable
    public static DyeColor getDyeColor(final BlockEntity blockEntity) {
        final Level level = blockEntity.getLevel();
        if (level == null) {
            return null;
        }

        return getDyeColor(level, blockEntity.getBlockPos());
    }

    @Nullable
    public static DyeColor getDyeColor(final BlockGetter level, final BlockPos pos) {
        final SimpleDyeableBehaviour behaviour = get(level, pos, TYPE);
        if (behaviour == null) {
            return null;
        }

        return behaviour.getColor();
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

}
