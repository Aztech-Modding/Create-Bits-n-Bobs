package com.kipti.bnb.registry.core;

import com.kipti.bnb.foundation.config.conditions.BnbFeatureEnabledCondition;
import com.kipti.bnb.registry.content.blocks.BnbBlocksBootstrap;
import com.kipti.bnb.registry.content.blocks.deco.BnbDecorativeBlocks;
import com.kipti.bnb.registry.worldgen.BnbPaletteStoneTypes;
import com.simibubi.create.foundation.block.DyedBlockList;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.util.Lazy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public enum BnbFeatureFlag {
    COGWHEEL_CHAIN_DRIVES("Ability for the player to create chain drives using create's cogwheels specifically."),
    FLANGED_CHAIN_DRIVES("Ability for the player to create chain drives using the flanged gear cogwheels.", BnbBlocksBootstrap.LARGE_EMPTY_FLANGED_COGWHEEL::get, BnbBlocksBootstrap.SMALL_EMPTY_FLANGED_COGWHEEL::get),

    CHAIN_PULLEY("Availability of the Chain Pulley block.", BnbBlocksBootstrap.CHAIN_PULLEY::get),

    EXPERIMENTAL_FLYWHEEL_BEARING("Availability of the Flywheel Bearing block. (In development)", false, BnbBlocksBootstrap.FLYWHEEL_BEARING::get),
    EXPERIMENTAL_WOODEN_STRUT("Availability of the Wooden Strut block. (In development)", false, BnbBlocksBootstrap.WOODEN_GIRDER_STRUT::get),

    WEATHERED_GIRDER("Availability of the weathered girder block.", BnbDecorativeBlocks.WEATHERED_METAL_GIRDER::get, BnbDecorativeBlocks.WEATHERED_GIRDER_STRUT::get),
    GIRDER_STRUT("Availability of the girder strut blocks.", BnbDecorativeBlocks.GIRDER_STRUT::get, BnbDecorativeBlocks.WEATHERED_GIRDER_STRUT::get),

    NIXIE_BOARD("Availability of Nixie Board block.", BnbBlocksBootstrap.NIXIE_BOARD::get),
    LARGE_NIXIE_TUBE("Availability of Large Nixie Tube block.", BnbBlocksBootstrap.LARGE_NIXIE_TUBE::get),

    LIGHTBULB("Availability of the Lightbulb block.", BnbBlocksBootstrap.LIGHTBULB::get),
    BRASS_LAMP("Availability of the Brass Lamp block.", BnbBlocksBootstrap.BRASS_LAMP::get),
    HEADLAMP("Availability of the Headlamp block.", BnbBlocksBootstrap.HEADLAMP::get),
    CHAIRS("Availability of the Chair blocks.", createSupplierSet(BnbDecorativeBlocks.CHAIRS)),

    TILES("Availability of the tile decoration blocks.", createDecoBlockSupplierSet(BnbPaletteStoneTypes.values())),

    ;

    @SuppressWarnings("unchecked")
    private static Lazy<Supplier<Block>[]> createDecoBlockSupplierSet(final BnbPaletteStoneTypes[] values) {
        return Lazy.of(() -> {
            final List<Supplier<Block>> blocks = new ArrayList<>();
            for (final BnbPaletteStoneTypes type : values) {
                blocks.addAll(type.getVariants()
                        .registeredBlocks.stream()
                        .map(e -> (Supplier<Block>) e::get)
                        .toList());
                blocks.addAll(type.getVariants()
                        .registeredPartials.stream()
                        .map(e -> (Supplier<Block>) e::get)
                        .toList());
            }
            return blocks.toArray(Supplier[]::new);
        });
    }

    @SuppressWarnings("unchecked")
    private static Supplier<Block>[] createSupplierSet(final DyedBlockList<? extends Block> dyedBlockList) {
        return (Supplier<Block>[]) Arrays.stream(dyedBlockList.toArray())
                .map(chairEntry -> ((Supplier<Block>) chairEntry::get))
                .toArray(Supplier[]::new);
    }

    private final String description;
    private final Lazy<Supplier<Block>[]> associatedBlocks;
    private final boolean defaultState;

    @SafeVarargs
    BnbFeatureFlag(final String description, final Supplier<Block>... associatedBlocks) {
        this.description = description;
        this.associatedBlocks = Lazy.of(() -> associatedBlocks);
        this.defaultState = true;
    }

    @SafeVarargs
    BnbFeatureFlag(final String description, final boolean defaultState, final Supplier<Block>... associatedBlocks) {
        this.description = description;
        this.associatedBlocks = Lazy.of(() -> associatedBlocks);
        this.defaultState = defaultState;
    }

    BnbFeatureFlag(final String description, final Lazy<Supplier<Block>[]> associatedBlocks) {
        this.description = description;
        this.associatedBlocks = associatedBlocks;
        this.defaultState = true;
    }

    public static boolean isEnabled(final Item item) {
        if (!(item instanceof final BlockItem blockItem)) {
            return true;
        }
        return isEnabled(blockItem);
    }

    /**
     * Returns whether the block is disabled by ANY related feature flag.
     */
    public static boolean isEnabled(final BlockItem tabItem) {
        if (tabItem == null) {
            return false;
        }

        for (final BnbFeatureFlag featureFlag : BnbFeatureFlag.values()) {
            for (final Supplier<Block> blockSupplier : featureFlag.getAssociatedBlocks()) {
                if (blockSupplier.get() == tabItem.getBlock() && !featureFlag.get()) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isEnabled(final String featureFlagKey) {
        if (featureFlagKey == null || featureFlagKey.isEmpty()) {
            return false;
        }

        final BnbFeatureFlag flag;

        try {
            flag = BnbFeatureFlag.valueOf(featureFlagKey.toUpperCase());
        } catch (final IllegalArgumentException e) {
            return false;
        }

        return BnbConfigs.common().getFeatureFlagState(flag);
    }

    public String getDescription() {
        return description;
    }

    public boolean getDefaultState() {
        return defaultState;
    }

    public Supplier<Block>[] getAssociatedBlocks() {
        return associatedBlocks.get();
    }

    public boolean get() {
        return BnbConfigs.common().getFeatureFlagState(this);
    }

    public BnbFeatureEnabledCondition getDataCondition() {
        return new BnbFeatureEnabledCondition(this.name().toLowerCase());
    }

}

