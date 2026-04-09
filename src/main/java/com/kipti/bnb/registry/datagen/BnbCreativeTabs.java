package com.kipti.bnb.registry.datagen;

import com.kipti.bnb.CreateBitsnBobs;
import com.kipti.bnb.registry.content.BnbItems;
import com.kipti.bnb.registry.content.blocks.BnbKineticBlocks;
import com.kipti.bnb.registry.content.blocks.BnbTrinketBlocks;
import com.kipti.bnb.registry.core.BnbFeatureFlag;
import com.kipti.bnb.registry.worldgen.BnbPaletteStoneTypes;
import com.simibubi.create.AllCreativeModeTabs;
import com.simibubi.create.content.decoration.encasing.EncasedBlock;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Supplier;

public class BnbCreativeTabs {

    private static final DeferredRegister<CreativeModeTab> REGISTER =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CreateBitsnBobs.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> BASE_CREATIVE_TAB = REGISTER.register("bnb_based",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("tab." + CreateBitsnBobs.MOD_ID + ".base"))
                    .withTabsBefore(AllCreativeModeTabs.PALETTES_CREATIVE_TAB.getId())
                    .icon(BnbKineticBlocks.SMALL_FLANGED_COGWHEEL::asStack)
                    .displayItems((p, o) -> buildCreativeTabContents(p, o, () -> BnbCreativeTabs.BASE_CREATIVE_TAB)).build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> PALETTES_CREATIVE_TAB = REGISTER.register("bnb_palettes",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("tab." + CreateBitsnBobs.MOD_ID + ".deco"))
                    .withTabsBefore(BnbCreativeTabs.BASE_CREATIVE_TAB.getId())
                    .icon(() -> BnbPaletteStoneTypes.ASURINE.getVariants().registeredBlocks.getFirst().asStack())
                    .displayItems((p, o) -> buildCreativeTabContents(p, o, () -> BnbCreativeTabs.PALETTES_CREATIVE_TAB)).build());

    private static boolean matchesBlockFilter(final BlockItem item) {
        if (BnbTrinketBlocks.CHAIRS.contains(item.getBlock()) && !BnbTrinketBlocks.CHAIRS.get(DyeColor.RED).is(item.getBlock()))
            return false;

        return !(item.getBlock() instanceof EncasedBlock);
    }

    private static boolean matchesSearchOnlyBlockFilter(final BlockItem item) {
        return BnbTrinketBlocks.CHAIRS.contains(item.getBlock()) && !BnbTrinketBlocks.CHAIRS.get(DyeColor.RED).is(item.getBlock());
    }

    @ApiStatus.Internal
    public static void register(final IEventBus modEventBus) {
        REGISTER.register(modEventBus);
    }

    private static void buildCreativeTabContents(final CreativeModeTab.ItemDisplayParameters parameters, final CreativeModeTab.Output output, final Supplier<DeferredHolder<CreativeModeTab, CreativeModeTab>> tabToGet) {
        for (final RegistryEntry<Item, Item> item : CreateBitsnBobs.REGISTRATE.getAll(Registries.ITEM)) {
            if (!CreateRegistrate.isInCreativeTab(item, tabToGet.get()) || !(item.get() instanceof final BlockItem blockItem) || !BnbFeatureFlag.isEnabled(blockItem))
                continue;

            if (matchesSearchOnlyBlockFilter(blockItem))
                output.accept(item.get(), CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
            else if (matchesBlockFilter(blockItem))
                output.accept(item.get());
        }

        for (final RegistryEntry<Item, Item> item : CreateBitsnBobs.REGISTRATE.getAll(Registries.ITEM)) {
            if (!CreateRegistrate.isInCreativeTab(item, tabToGet.get()) || (item.get() instanceof BlockItem))
                continue;

            if (matchesItemFilter(item.get()) && BnbFeatureFlag.isEnabled(item.get()))
                output.accept(item.get());
        }
    }

    private static boolean matchesItemFilter(final Item item) {
        //Ignore testing / fake items
        return !BnbItems.ICON_LIGHTBULB.is(item) && !BnbItems.TEST_ROPE.is(item);
    }

}

