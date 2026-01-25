package com.kipti.bnb.foundation.client;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;

/**
 * TODO: implement compat with sable, but needs to be released since devs wont have
 */
public class ShipyardLodHelper {

    public static boolean isProbablyRenderingInShipyard(final BlockPos containing) {
        return containing.distSqr(Vec3i.ZERO) >= 2_000_000d * 2_000_000d;
    }
}
