package com.kipti.bnb.compat.computercraft.peripherals;

import com.kipti.bnb.compat.computercraft.implementation.DumbSyncedPeripheral;
import com.kipti.bnb.content.decoration.light.headlamp.HeadlampBlockEntity;
import com.simibubi.create.compat.computercraft.implementation.peripherals.SyncedPeripheral;
import com.simibubi.create.content.contraptions.elevator.ElevatorContactBlockEntity;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HeadlampPeripheral implements DumbSyncedPeripheral<HeadlampBlockEntity> {
    private final HeadlampBlockEntity be;

    public HeadlampPeripheral(HeadlampBlockEntity be) {
        this.be = be;
    }


    @LuaFunction(mainThread = true)
    public void setColor(int index, String color) throws LuaException {
        Level level = be.getLevel();
        if (level == null) return;

        if (index < 0 || index >= HeadlampBlockEntity.HeadlampPlacement.values().length)
            throw new LuaException("Invalid headlamp index");

        be.setColor(index, color);
    }

    @LuaFunction(mainThread = true)
    public String getColor(int index) throws LuaException {
        Level level = be.getLevel();
        if (level == null) return null;

        if (index < 0 || index >= HeadlampBlockEntity.HeadlampPlacement.values().length)
            throw new LuaException("Invalid headlamp index");

        if (be.getColor(index) != null)
            return be.getColor(index).getName();
        else
            return null;
    }


    @NotNull
    @Override
    public String getType() {
        return "bnb_headlamp";
    }

    @Override
    public @Nullable HeadlampBlockEntity getTarget() {
        return be;
    }

}