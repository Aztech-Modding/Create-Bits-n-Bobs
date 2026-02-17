package com.kipti.bnb.compat.computercraft.implementation.peripherals;

import com.kipti.bnb.content.decoration.light.headlamp.HeadlampBlock;
import com.kipti.bnb.content.decoration.light.headlamp.HeadlampBlockEntity;
import com.simibubi.create.compat.computercraft.implementation.peripherals.SyncedPeripheral;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class HeadlampPeripheral extends SyncedPeripheral<HeadlampBlockEntity> {

    public HeadlampPeripheral(final HeadlampBlockEntity blockEntity) {
        super(blockEntity);
    }

    @LuaFunction
    public final void setLamp(final ILuaContext context, final int x, final int y, final boolean onOff) throws LuaException {
//        if (x < -32 || x > 33 || y < -32 || y > 33) {
//            throw new LuaException("x and y must be between 32 and 33 inclusive");
//        }
        final Level level = blockEntity.getLevel();
        if (level == null || level.isClientSide()) {
            return;
        }

        final int localX = x & 1;
        final int localY = y & 1;
        final int offsetX = x >> 1;
        final int offsetY = y >> 1;
        final BlockPos targetPos = (offsetX == 0 && offsetY == 0)
                ? blockEntity.getBlockPos()
                : getTargetPosForOffset(blockEntity, offsetX, offsetY);

        HeadlampQueuedOperationHandler.queueMaskChange(level, targetPos, localX, localY, onOff);
    }

    private static BlockPos getTargetPosForOffset(final HeadlampBlockEntity blockEntity, final int x, final int y) {
        final Direction facing = blockEntity.getBlockState().getValue(HeadlampBlock.FACING);
        final boolean isVertical = facing.get2DDataValue() == -1;
        final boolean isDown = facing == Direction.DOWN;
        final Direction horizontalAxis = isVertical
                ? Direction.fromAxisAndDirection(Direction.Axis.X, Direction.AxisDirection.NEGATIVE)
                : facing.getClockWise();
        final Direction verticalAxis = isVertical
                ? Direction.fromAxisAndDirection(Direction.Axis.Z, isDown ? Direction.AxisDirection.POSITIVE : Direction.AxisDirection.NEGATIVE)
                : Direction.UP;
        return blockEntity.getBlockPos().relative(horizontalAxis, x).relative(verticalAxis, y);
    }

    @NotNull
    @Override
    public String getType() {
        return "Create_Bits_N_Bobs_Headlamp";
    }

}
