package com.kipti.bnb.compat.computercraft.implementation.peripherals;

import com.kipti.bnb.content.decoration.light.headlamp.HeadlampBlockEntity;
import com.simibubi.create.compat.computercraft.implementation.peripherals.SyncedPeripheral;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

public class HeadlampPeripheral extends SyncedPeripheral<HeadlampBlockEntity> {

    public HeadlampPeripheral(final HeadlampBlockEntity blockEntity) {
        super(blockEntity);
    }

    @LuaFunction
    public final void setLamp(final ILuaContext context, final int x, final int y, final boolean onOff) throws LuaException {
        if (x < -32 || x > 33 || y < -32 || y > 33) {
            throw new LuaException("x and y must be between 32 and 33 inclusive");
        }
        context.issueMainThreadTask(() -> {
            if (x < 0 || x > 1 || y < 0 || y > 1) {
                final @Nullable HeadlampBlockEntity localBlockEntity = blockEntity.searchForHeadlampAtOffset(x >> 1, y >> 1);
                if (localBlockEntity == null) {
                    throw new LuaException("No headlamp found at the given offset coordinates " + x + ", " + y);
                }
                performSetLamp(localBlockEntity, x & 1, y & 1, onOff);
                return null;
            }
            performSetLamp(blockEntity, x, y, onOff);
            return null;
        });
        return;
    }

    private void performSetLamp(final HeadlampBlockEntity blockEntity, int x, int y, boolean onOff) {
        blockEntity.getOrCreateAddressing().setLocalMaskValue(new Vector2i(x, y), onOff);
        blockEntity.sendData();
    }

    @NotNull
    @Override
    public String getType() {
        return "Create_Bits_N_Bobs_Headlamp";
    }

}
