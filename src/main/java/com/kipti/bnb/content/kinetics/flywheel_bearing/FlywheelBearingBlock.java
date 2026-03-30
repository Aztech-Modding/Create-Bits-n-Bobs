package com.kipti.bnb.content.kinetics.flywheel_bearing;

import com.kipti.bnb.registry.content.BnbBlockEntities;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

public class FlywheelBearingBlock extends DirectionalKineticBlock implements IBE<FlywheelBearingBlockEntity>, ICogWheel {

    public FlywheelBearingBlock(final Properties properties) {
        super(properties);
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(final @NotNull ItemStack stack,
                                                       final @NotNull BlockState state,
                                                       final @NotNull Level level,
                                                       final @NotNull BlockPos pos,
                                                       final Player player,
                                                       final @NotNull InteractionHand hand,
                                                       final @NotNull BlockHitResult hitResult) {
        if (!player.mayBuild())
            return ItemInteractionResult.FAIL;
        if (player.isShiftKeyDown())
            return ItemInteractionResult.FAIL;
        if (stack.isEmpty()) {
            if (level.isClientSide)
                return ItemInteractionResult.SUCCESS;
            this.withBlockEntityDo(
                    level, pos, be -> {
                        if (be.running) {
                            be.disassemble();
                        } else {
                            be.checkAssemblyNextTick = true;
                        }
                    }
            );
            return ItemInteractionResult.SUCCESS;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public Direction.Axis getRotationAxis(final BlockState state) {
        return state.getValue(FACING).getAxis();
    }

    @Override
    public boolean isLargeCog() {
        return true;
    }

    @Override
    public Class<FlywheelBearingBlockEntity> getBlockEntityClass() {
        return FlywheelBearingBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends FlywheelBearingBlockEntity> getBlockEntityType() {
        return BnbBlockEntities.FLYWHEEL_BEARING.get();
    }

}

