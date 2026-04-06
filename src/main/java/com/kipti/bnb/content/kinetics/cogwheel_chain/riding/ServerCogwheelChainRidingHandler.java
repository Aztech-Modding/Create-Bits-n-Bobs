package com.kipti.bnb.content.kinetics.cogwheel_chain.riding;

import java.util.Map;
import java.util.UUID;

import com.kipti.bnb.network.packets.to_client.CogwheelChainRidingBroadcastPacket;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.world.entity.player.Player;

public class ServerCogwheelChainRidingHandler {

	private static final Object2IntMap<UUID> ridingPlayers = new Object2IntOpenHashMap<>();
	private static int ticks;

	public static void handleTTLPacket(Player player) {
		int count = ridingPlayers.size();
		ridingPlayers.put(player.getUUID(), 20);
		if (ridingPlayers.size() != count)
			sync();
	}

	public static void handleStopRidingPacket(Player player) {
		if (ridingPlayers.removeInt(player.getUUID()) != 0)
			sync();
	}

	public static void tick() {
		ticks++;
		int before = ridingPlayers.size();
		ObjectIterator<Object2IntMap.Entry<UUID>> iterator = ridingPlayers.object2IntEntrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<UUID, Integer> entry = iterator.next();
			int newTTL = entry.getValue() - 1;
			if (newTTL <= 0) {
				iterator.remove();
			} else {
				entry.setValue(newTTL);
			}
		}
		int after = ridingPlayers.size();
		if (Math.floorMod(ticks, 10) != 0 && before == after)
			return;
		sync();
	}

	private static void sync() {
		CatnipServices.NETWORK.sendToAllClients(new CogwheelChainRidingBroadcastPacket(ridingPlayers.keySet()));
	}
}
