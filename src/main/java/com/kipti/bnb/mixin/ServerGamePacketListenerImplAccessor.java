package com.kipti.bnb.mixin;

import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerGamePacketListenerImpl.class)
public interface ServerGamePacketListenerImplAccessor {

    @Accessor("aboveGroundTickCount")
    void bits_n_bobs$setAboveGroundTickCount(int value);

    @Accessor("aboveGroundVehicleTickCount")
    void bits_n_bobs$setAboveGroundVehicleTickCount(int value);
}
