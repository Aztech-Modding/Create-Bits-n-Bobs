package com.kipti.bnb.registry.compat;

import com.kipti.bnb.compat.computercraft.implementation.peripherals.HeadlampPeripheral;
import com.kipti.bnb.content.trinkets.light.headlamp.HeadlampBlockEntity;
import com.simibubi.create.compat.computercraft.implementation.peripherals.SyncedPeripheral;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

// THIS SHOULD GO INTO AZERBAIJAN TECHNOLOGY LIBRARY RAHHHHHHHHH
public class BnbComputerBehaviourPeripherals {

    public record ComputerBehaviourPeripheralType<T extends SmartBlockEntity>(
            InjectPriority injectPriority,
            Class<T> blockEntityType,
            Function<T, Supplier<SyncedPeripheral<T>>> supplierConstructor
    ) {
    }

    public enum InjectPriority {
        HIGH,
        NORMAL,
        LOW,
    }

    public static class PriorityPeripheralRegistry {

        private final Map<InjectPriority, List<ComputerBehaviourPeripheralType<?>>> entries;

        public PriorityPeripheralRegistry() {
            entries = new EnumMap<>(InjectPriority.class);
            for (final InjectPriority priority : InjectPriority.values()) {
                entries.put(priority, new ArrayList<>());
            }
        }

        public void add(final InjectPriority priority, final ComputerBehaviourPeripheralType<?> type) {
            entries.get(priority).add(type);
        }

        @SuppressWarnings("unchecked")
        public <T extends SmartBlockEntity> @Nullable ComputerBehaviourPeripheralType<T> getForBlockEntity(final T blockEntity) {
            for (final InjectPriority priority : InjectPriority.values()) {
                for (final ComputerBehaviourPeripheralType<?> type : entries.get(priority)) {
                    if (type.blockEntityType().isAssignableFrom(blockEntity.getClass())) {
                        return (ComputerBehaviourPeripheralType<T>) type;
                    }
                }
            }
            return null;
        }

    }

    public static final PriorityPeripheralRegistry COMPUTER_BEHAVIOUR_EXTRA_PERIPHERALS = new PriorityPeripheralRegistry();

    public static <T extends SmartBlockEntity> void register(final InjectPriority priority, final Class<T> blockEntityType, final Function<T, Supplier<SyncedPeripheral<T>>> supplierConstructor) {
        COMPUTER_BEHAVIOUR_EXTRA_PERIPHERALS.add(priority, new ComputerBehaviourPeripheralType<T>(priority, blockEntityType, supplierConstructor));
    }

    public static <T extends SmartBlockEntity> void register(final Class<T> blockEntityType, final Function<T, Supplier<SyncedPeripheral<T>>> supplierConstructor) {
        register(InjectPriority.NORMAL, blockEntityType, supplierConstructor);
    }

    static {
        register(HeadlampBlockEntity.class, be -> () -> new HeadlampPeripheral(be));
    }

}

