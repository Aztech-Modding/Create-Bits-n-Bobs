package com.kipti.bnb.compat.computercraft.implementation.peripherals;

import com.kipti.bnb.content.decoration.light.headlamp.HeadlampBlockEntity;
import com.kipti.bnb.foundation.caching.LevelSafeStorage;
import com.kipti.bnb.foundation.caching.LevelSimpleCache;
import com.kipti.bnb.foundation.caching.SimpleCache;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * Holds
 */
public class HeadlampQueuedOperationHandler {

    private static final int MASK_BITS = 0b1111;
    private static final int STATE_SHIFT = 4;

    public static final LevelSimpleCache<BlockPos, HeadlampBlockEntity> HEADLAMP_CACHE =
            new LevelSimpleCache<>(() -> new SimpleCache<>(256, BlockEntity::isRemoved));
    private static final LevelSafeStorage<Long2ByteOpenHashMap> QUEUED_MASK_CHANGES =
            new LevelSafeStorage<>(() -> new Long2ByteOpenHashMap(256));

    public static void queueMaskChange(final Level level, final BlockPos pos, final int localX, final int localY, final boolean onOff) {
        if (level == null || level.isClientSide()) {
            return;
        }
        queueMaskChange(level, pos, encodeMaskChange(localX, localY, onOff));
    }

    private static void queueMaskChange(final Level level, final BlockPos pos, final byte change) {
        final Long2ByteOpenHashMap map = QUEUED_MASK_CHANGES.getForLevel(level);
        synchronized (map) {
            final long key = pos.asLong();
            final byte existing = map.get(key);
            map.put(key, (byte) (existing | change));
        }
    }

    private static byte encodeMaskChange(final int localX, final int localY, final boolean onOff) {
        final int index = localX + localY * 2;
        final int mask = 1 << index;
        final int states = onOff ? (1 << (index + STATE_SHIFT)) : 0;
        return (byte) (mask | states);
    }

    private static void applyMaskChange(final HeadlampBlockEntity blockEntity, final byte change) {
        final int mask = change & MASK_BITS;
        if (mask == 0) {
            return;
        }
        final int states = (change >> STATE_SHIFT) & MASK_BITS;
        final var addressing = blockEntity.getOrCreateAddressing();
        byte newMask = addressing.getMask();
        boolean changed = false;
        for (int i = 0; i < 4; i++) {
            if ((mask & (1 << i)) == 0) {
                continue;
            }
            final boolean onOff = (states & (1 << i)) != 0;
            if (onOff) {
                newMask = (byte) (newMask | (1 << i));
            } else {
                newMask = (byte) (newMask & ~(1 << i));
            }
            changed = true;
        }
        if (changed) {
            addressing.setMask(newMask);
            blockEntity.sendData();
        }
    }

    public static void applyQueuedChanges(final Level level) {
        if (level == null || level.isClientSide()) {
            return;
        }
        final Long2ByteOpenHashMap map = QUEUED_MASK_CHANGES.getForLevel(level);
        final Long2ByteOpenHashMap snapshot;
        synchronized (map) {
            if (map.isEmpty()) {
                return;
            }
            snapshot = new Long2ByteOpenHashMap(map);
            map.clear();
        }

        final SimpleCache<BlockPos, HeadlampBlockEntity> cache = HEADLAMP_CACHE.getCacheForLevel(level);
        for (final Long2ByteMap.Entry entry : snapshot.long2ByteEntrySet()) {
            final BlockPos pos = BlockPos.of(entry.getLongKey());
            HeadlampBlockEntity blockEntity = cache.get(pos);
            if (blockEntity == null || blockEntity.isRemoved()) {
                final BlockEntity raw = level.getBlockEntity(pos);
                if (raw instanceof final HeadlampBlockEntity headlamp) {
                    blockEntity = headlamp;
                    cache.put(pos, headlamp);
                } else {
                    continue;
                }
            }
            applyMaskChange(blockEntity, entry.getByteValue());
        }
    }

    @EventBusSubscriber
    public static class HeadlampQueuedOperationEvents {
        @SubscribeEvent
        public static void onLevelTick(final LevelTickEvent.Post event) {
            final Level level = event.getLevel();
            if (!level.isClientSide()) {
                HeadlampQueuedOperationHandler.applyQueuedChanges(level);
            }
        }
    }


}
