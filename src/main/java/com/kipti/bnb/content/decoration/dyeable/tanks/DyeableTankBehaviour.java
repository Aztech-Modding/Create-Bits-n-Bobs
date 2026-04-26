package com.kipti.bnb.content.decoration.dyeable.tanks;

import com.kipti.bnb.content.decoration.dyeable.BaseDyeableBehaviour;
import com.kipti.bnb.registry.content.BnbAdvancements;
import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.jetbrains.annotations.Nullable;

public class DyeableTankBehaviour extends BaseDyeableBehaviour {

    public static final BehaviourType<DyeableTankBehaviour> TYPE = new BehaviourType<>("dyeable_fluid_tank");

    @Nullable
    private GayDye gayDye;

    public DyeableTankBehaviour(final SmartBlockEntity be) {
        super(be);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.gayDye != null && this.gayDye.needsTicking()) {
            this.gayDye.tick();
            this.refreshRenderedModel();
        }
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

    @Nullable
    public DyeColor getDisplayedColor() {
        if (this.gayDye != null) {
            if (this.getBlockEntity() instanceof final FluidTankBlockEntity ftbe) {
                final int localY = this.getPos().subtract(ftbe.getController()).getY();
                return this.gayDye.getDisplayedColor(localY);
            }
        }
        return this.getColor();
    }

    @Override
    public void onItemUse(final PlayerInteractEvent.RightClickBlock event) {
        final ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof final DyeItem dyeItem)) {
            return;
        }

        if (!event.getLevel().isClientSide) {
            if (event.getEntity() instanceof final Player player) {
                BnbAdvancements.DYE_FLUID_COMPONENT.awardTo(player);
            }

            final DyeColor newColor = dyeItem.getDyeColor();
            if (event.getEntity().isShiftKeyDown()) {
                this.dyeSingle(newColor);
            } else {
                this.dyeEntireTank(newColor);
            }
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }

    private void dyeSingle(@Nullable final DyeColor color) {
        final FluidTankBlockEntity tankBE = (FluidTankBlockEntity) this.blockEntity;
        final FluidTankBlockEntity controllerBE = tankBE.getControllerBE();

        final boolean wasMulti = controllerBE != null && (controllerBE.getWidth() > 1 || controllerBE.getHeight() > 1);

        if (wasMulti) {
            ConnectivityHandler.splitMulti(controllerBE);
        }

        this.setColor(color);
        ConnectivityHandler.formMulti(tankBE);

        if (wasMulti && controllerBE != tankBE) {
            ConnectivityHandler.formMulti(controllerBE);
        }
    }

    public void setGayDye(@Nullable final GayDye gayDye) {
        this.gayDye = gayDye;
        this.refreshOrNotifyUpdate();
    }

    public void applyGayDyeToEntireTank(final GayDye gayDye) {
        final FluidTankBlockEntity tankBE = (FluidTankBlockEntity) this.blockEntity;
        final FluidTankBlockEntity controllerBE = tankBE.getControllerBE();

        if (controllerBE == null) {
            this.setGayDye(gayDye);
            return;
        }

        final Level level = this.getLevel();
        final BlockPos controllerPos = controllerBE.getBlockPos();

        for (int x = 0; x < controllerBE.getWidth(); x++) {
            for (int y = 0; y < controllerBE.getHeight(); y++) {
                for (int z = 0; z < controllerBE.getWidth(); z++) {
                    final BlockPos pos = controllerPos.offset(x, y, z);
                    final DyeableTankBehaviour behaviour = BlockEntityBehaviour.get(level, pos, TYPE);
                    if (behaviour != null) {
                        behaviour.setGayDye(gayDye);
                    }
                }
            }
        }
    }

    private void dyeEntireTank(@Nullable final DyeColor color) {
        final FluidTankBlockEntity tankBE = (FluidTankBlockEntity) this.blockEntity;
        final FluidTankBlockEntity controllerBE = tankBE.getControllerBE();

        if (controllerBE == null) {
            this.setColor(color);
            return;
        }

        final Level level = this.getLevel();
        final BlockPos controllerPos = controllerBE.getBlockPos();

        for (int x = 0; x < controllerBE.getWidth(); x++) {
            for (int y = 0; y < controllerBE.getHeight(); y++) {
                for (int z = 0; z < controllerBE.getWidth(); z++) {
                    final BlockPos pos = controllerPos.offset(x, y, z);
                    final DyeableTankBehaviour behaviour = BlockEntityBehaviour.get(level, pos, TYPE);
                    if (behaviour != null) {
                        behaviour.setColor(color);
                    }
                }
            }
        }
    }

    @Override
    protected void writeAdditionalDyeData(
            final CompoundTag nbt,
            final HolderLookup.Provider registries,
            final boolean clientPacket
    ) {
        if (this.gayDye != null) {
            final CompoundTag gay = new CompoundTag();
            this.gayDye.write(gay);
            nbt.put("Gay", gay);
        }
    }

    @Override
    protected boolean readAdditionalDyeData(
            final CompoundTag nbt,
            final HolderLookup.Provider registries,
            final boolean clientPacket
    ) {
        final GayDye previousGayDye = this.gayDye;
        if (nbt.contains("Gay")) {
            this.gayDye = GayDye.read(nbt.getCompound("Gay"));
        } else {
            this.gayDye = null;
        }
        return previousGayDye != this.gayDye;
    }

}
