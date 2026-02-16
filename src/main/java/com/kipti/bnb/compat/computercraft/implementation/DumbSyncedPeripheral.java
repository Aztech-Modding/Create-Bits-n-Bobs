package com.kipti.bnb.compat.computercraft.implementation;

import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;

/**
 * It's like {@link com.simibubi.create.compat.computercraft.implementation.peripherals.SyncedPeripheral} but not smart.
 */
public interface DumbSyncedPeripheral<BE extends BlockEntity> extends IPeripheral {
	List<IComputerAccess> computers = new LinkedList<>();

	@NotNull
	@Override
	String getType();

	@Nullable
	@Override
	BE getTarget();

	@Override
	default void attach(@NotNull IComputerAccess computer) {
		computers.add(computer);
	}

	@Override
	default void detach(@NotNull IComputerAccess computer) {
		computers.removeIf(p -> (p.getID() == computer.getID()));
	}

	@Override
	default boolean equals(@Nullable IPeripheral other) {
		return other == this && other.getType().equals(getType()) && other.getTarget() == this.getTarget();
	}

}
