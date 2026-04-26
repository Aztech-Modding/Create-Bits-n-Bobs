package com.kipti.bnb.content.decoration.dyeable.simple;

import com.kipti.bnb.content.decoration.dyeable.BaseDyeableBehaviour;
import com.kipti.bnb.registry.content.BnbAdvancements;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public class SimpleDyeableBehaviour extends BaseDyeableBehaviour {

    public static final BehaviourType<SimpleDyeableBehaviour> TYPE = new BehaviourType<>("simple_dyeable");

    public SimpleDyeableBehaviour(final SmartBlockEntity be) {
        super(be);
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
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
            this.setColor(dyeItem.getDyeColor());
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }

}
