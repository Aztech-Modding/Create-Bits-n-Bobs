package com.kipti.bnb.content.decoration.dyeable.fluid_tank;

import com.cake.azimuth.behaviour.SuperBlockEntityBehaviour;
import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.jetbrains.annotations.Nullable;

public class DyeableFluidTankBehaviour extends SuperBlockEntityBehaviour {

    public static final BehaviourType<DyeableFluidTankBehaviour> TYPE = new BehaviourType<>("dyeable_fluid_tank");

    @Nullable
    private DyeColor color;

    public DyeableFluidTankBehaviour(final SmartBlockEntity be) {
        super(be);
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

    @Nullable
    public DyeColor getColor() {
        return this.color;
    }

    public void applyColorClientOnly(@Nullable final DyeColor color) {
        if (this.color == color) {
            return;
        }
        this.color = color;
        this.refreshRenderedModel();
    }

    public void setColor(@Nullable final DyeColor color) {
        if (this.color == color) {
            return;
        }
        this.color = color;
        if (this.hasLevel() && this.getLevel().isClientSide) {
            this.refreshRenderedModel();
        } else {
            this.blockEntity.notifyUpdate();
        }
    }

    @Override
    public void onItemUse(final PlayerInteractEvent.RightClickBlock event) {
        final ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof final DyeItem dyeItem)) {
            return;
        }

        if (!event.getLevel().isClientSide) {
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
                    final DyeableFluidTankBehaviour behaviour = BlockEntityBehaviour.get(level, pos, TYPE);
                    if (behaviour != null) {
                        behaviour.setColor(color);
                    }
                }
            }
        }
    }

    @Override
    public void write(final CompoundTag nbt, final HolderLookup.Provider registries, final boolean clientPacket) {
        super.write(nbt, registries, clientPacket);
        if (this.color != null) {
            nbt.putInt("DyeColor", this.color.getId());
        }
    }

    @Override
    public void read(final CompoundTag nbt, final HolderLookup.Provider registries, final boolean clientPacket) {
        super.read(nbt, registries, clientPacket);
        final DyeColor previousColor = this.color;
        if (nbt.contains("DyeColor")) {
            this.color = DyeColor.byId(nbt.getInt("DyeColor"));
        } else {
            this.color = null;
        }

        if (clientPacket && previousColor != this.color) {
            this.refreshRenderedModel();
        }
    }

    public void refreshRenderedModel() {
        this.blockEntity.requestModelDataUpdate();
        if (this.hasLevel()) {
            final Level level = this.getLevel();
            final BlockPos pos = this.getPos();
            level.sendBlockUpdated(pos, this.getBlockState(), this.getBlockState(), 16);
        }
    }
}
