package com.kipti.bnb.foundation.client;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;

/**
 * TODO: implement compat with sable, but needs to be released since devs wont have
 */
public class ShipyardHelper {

    /**
     * Fuck up behaviour for stuff absurdly far out, idk if this may break other shit, but this breaks the least amount of shit
     */
    public static boolean isProbablyInShipyard(final BlockPos containing) {
        return containing.distSqr(Vec3i.ZERO) >= 2_000_000d * 2_000_000d;
    }
}

