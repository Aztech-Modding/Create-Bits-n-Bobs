package com.kipti.bnb.content.kinetics.cogwheel_chain.riding;

import com.kipti.bnb.mixin.PlayerSkyhookRendererAccessor;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Keeps BnB hanging players merged into Create's shared skyhook renderer state.
 */
public class PlayerSkyhookRendererBridge {

    private static final Set<UUID> bnbHangingPlayers = new HashSet<>();

    public static void updateBnbHangingPlayers(final Collection<UUID> uuids) {
        final Set<UUID> hangingPlayers = PlayerSkyhookRendererAccessor.bits_n_bobs$getHangingPlayers();
        hangingPlayers.removeAll(PlayerSkyhookRendererBridge.bnbHangingPlayers);
        PlayerSkyhookRendererBridge.bnbHangingPlayers.clear();
        PlayerSkyhookRendererBridge.bnbHangingPlayers.addAll(uuids);
        hangingPlayers.addAll(PlayerSkyhookRendererBridge.bnbHangingPlayers);
    }

    public static void restoreBnbHangingPlayers() {
        PlayerSkyhookRendererAccessor.bits_n_bobs$getHangingPlayers()
                .addAll(PlayerSkyhookRendererBridge.bnbHangingPlayers);
    }
}
