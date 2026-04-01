package com.kipti.bnb.registry.core;

import com.kipti.bnb.foundation.config.conditions.BnbFeatureEnabledCondition;
import com.kipti.bnb.registry.content.BnbItems;
import com.kipti.bnb.registry.content.blocks.BnbBracketBlocks;
import com.kipti.bnb.registry.content.blocks.BnbKineticBlocks;
import com.kipti.bnb.registry.content.blocks.BnbTrinketBlocks;
import com.kipti.bnb.registry.content.blocks.deco.BnbDecorativeBlocks;
import com.kipti.bnb.registry.content.blocks.encased.BnbExtraEncasedBlocks;
import com.kipti.bnb.registry.content.blocks.encased.BnbSpecialEncasedBlocks;
import com.kipti.bnb.registry.worldgen.BnbPaletteStoneTypes;
import com.simibubi.create.foundation.block.DyedBlockList;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.util.Lazy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public enum BnbFeatureFlag {
    COGWHEEL_CHAIN_DRIVES(
            FeatureCategories.BEHAVIOUR,
            "Ability for the player to create chain drives using create's cogwheels specifically."
    ),
    FLANGED_CHAIN_DRIVES(
            FeatureCategories.BLOCK, "Ability for the player to create chain drives using the flanged gear cogwheels.",
            BnbKineticBlocks.LARGE_FLANGED_COGWHEEL::get, BnbKineticBlocks.SMALL_FLANGED_COGWHEEL::get
    ),

    CHAIN_PULLEY(
            FeatureCategories.BLOCK,
            "Availability of the Chain Pulley block.",
            BnbKineticBlocks.CHAIN_PULLEY::get,
            BnbKineticBlocks.CHAIN_ROPE::get,
            BnbKineticBlocks.CHAIN_PULLEY_MAGNET::get
    ),

    FLYWHEEL_BEARING(
            FeatureCategories.BLOCK, "Availability of the Flywheel Bearing block.",
            BnbKineticBlocks.FLYWHEEL_BEARING::get
    ),
    WOODEN_STRUT(
            FeatureCategories.BLOCK, "Availability of the Wooden Strut block.", true
//            BnbDecorativeBlocks.WOODEN_GIRDER_STRUT::get
    ),

    WEATHERED_GIRDER(
            FeatureCategories.BLOCK,
            "Availability of the weathered girder block.",
            BnbDecorativeBlocks.WEATHERED_METAL_GIRDER::get,
            BnbDecorativeBlocks.WEATHERED_GIRDER_STRUT::get,
            BnbDecorativeBlocks.WEATHERED_METAL_GIRDER_ENCASED_SHAFT::get
    ),
    GIRDER_STRUT(
            FeatureCategories.BLOCK, "Availability of the girder strut blocks.",
            BnbDecorativeBlocks.GIRDER_STRUT::get, BnbDecorativeBlocks.WEATHERED_GIRDER_STRUT::get
    ),

    NIXIE_BOARD(
            FeatureCategories.BLOCK, "Availability of Nixie Board block.",
            createBlockAndDyedSupplierSet(BnbTrinketBlocks.NIXIE_BOARD, BnbTrinketBlocks.DYED_NIXIE_BOARD)
    ),
    LARGE_NIXIE_TUBE(
            FeatureCategories.BLOCK, "Availability of Large Nixie Tube block.",
            createBlockAndDyedSupplierSet(BnbTrinketBlocks.LARGE_NIXIE_TUBE, BnbTrinketBlocks.DYED_LARGE_NIXIE_TUBE)
    ),

    LIGHTBULB(
            FeatureCategories.BLOCK, "Availability of the Lightbulb block.",
            BnbTrinketBlocks.LIGHTBULB::get
    ),
    BRASS_LAMP(
            FeatureCategories.BLOCK, "Availability of the Brass Lamp block.",
            BnbTrinketBlocks.BRASS_LAMP::get
    ),
    HEADLAMP(
            FeatureCategories.BLOCK, "Availability of the Headlamp block.",
            BnbTrinketBlocks.HEADLAMP::get
    ),
    CHAIRS(
            FeatureCategories.BLOCK, "Availability of the Chair blocks.",
            createDyedSupplierSet(BnbTrinketBlocks.CHAIRS)
    ),

    TILES(
            FeatureCategories.BLOCK, "Availability of the tile decoration blocks.",
            createDecoBlockSupplierSet(BnbPaletteStoneTypes.values())
    ),

    INDUSTRIAL_GRATING(
            FeatureCategories.BLOCK,
            "Availability of the industrial grating blocks.",
            true,
            BnbDecorativeBlocks.INDUSTRIAL_GRATING::get,
            BnbDecorativeBlocks.INDUSTRIAL_GRATING_PANEL::get,
            BnbSpecialEncasedBlocks.INDUSTRIAL_GRATING_PANEL::get
    ),
    INDUSTRIAL_TRUSS(
            FeatureCategories.BLOCK, "Availability of the industrial truss blocks.", true,
            BnbDecorativeBlocks.INDUSTRIAL_TRUSS::get, BnbDecorativeBlocks.INDUSTRIAL_TRUSS_ENCASED_SHAFT::get
    ),
    COGWHEEL_CHAIN_CARRIAGE(
            FeatureCategories.BLOCK, "Availability of the Cogwheel Chain Carriage block.", true,
            BnbKineticBlocks.COGWHEEL_CHAIN_CARRIAGE::get
    ),
    THROTTLE_LEVER(
            FeatureCategories.BLOCK,
            "Availability of the Throttle Lever block.",
            true,
            BnbTrinketBlocks.THROTTLE_LEVER::get
    ),

    CABLE_GIRDER_STRUT(
            FeatureCategories.BLOCK, "Availability of the Cable Girder Strut block.",
            BnbDecorativeBlocks.CABLE_GIRDER_STRUT::get
    ),
    WEATHERED_METAL_BRACKET(
            FeatureCategories.BLOCK, "Availability of the Weathered Metal Bracket block.",
            BnbBracketBlocks.WEATHERED_METAL_BRACKET::get
    ),

    INDUSTRIAL_IRON_ENCASING(
            FeatureCategories.BLOCK,
            "Availability of the Industrial Iron encased block variants.",
            BnbExtraEncasedBlocks.INDUSTRIAL_IRON_ENCASED_SHAFT::get,
            BnbExtraEncasedBlocks.INDUSTRIAL_IRON_ENCASED_COGWHEEL::get,
            BnbExtraEncasedBlocks.INDUSTRIAL_IRON_ENCASED_LARGE_COGWHEEL::get
    ),
    WEATHERED_IRON_ENCASING(
            FeatureCategories.BLOCK,
            "Availability of the Weathered Iron encased block variants.",
            BnbExtraEncasedBlocks.WEATHERED_IRON_ENCASED_SHAFT::get,
            BnbExtraEncasedBlocks.WEATHERED_IRON_ENCASED_COGWHEEL::get,
            BnbExtraEncasedBlocks.WEATHERED_IRON_ENCASED_LARGE_COGWHEEL::get
    ),

    COOKIE_DOUGH(
            FeatureCategories.ITEM, "Availability of the Cookie Dough item.", BnbItems.COOKIE_DOUGH
    ),

    DYEABLE_PIPES(FeatureCategories.BEHAVIOUR, "Ability to dye fluid pipes."),
    DYEABLE_TANKS(FeatureCategories.BEHAVIOUR, "Ability to dye fluid tanks."),

    ;

    private static Lazy<List<Supplier<Block>>> createDecoBlockSupplierSet(final BnbPaletteStoneTypes[] values) {
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
            return blocks;
        });
    }

    @SuppressWarnings("unchecked")
    private static Supplier<Block>[] createDyedSupplierSet(final DyedBlockList<? extends Block> dyedBlockList) {
        return (Supplier<Block>[]) Arrays.stream(dyedBlockList.toArray())
                .map(entry -> ((Supplier<Block>) entry::get))
                .toArray(Supplier[]::new);
    }

    private static Lazy<List<Supplier<Block>>> createBlockAndDyedSupplierSet(final BlockEntry<? extends Block> baseBlock,
                                                                             final DyedBlockList<? extends Block> dyedBlockList) {
        return Lazy.of(() -> {
            final List<Supplier<Block>> blocks = new ArrayList<>();
            blocks.add(baseBlock::get);
            blocks.addAll(Arrays.stream(dyedBlockList.toArray())
                                  .map(dyedEntry -> (Supplier<Block>) dyedEntry::get)
                                  .toList());
            return blocks.stream().toList();
        });
    }

    private final FeatureCategories.FeatureCategory category;
    private final String description;
    private final Lazy<List<Supplier<Block>>> associatedBlocks;
    private final Lazy<List<Supplier<Item>>> associatedItems;
    private final boolean defaultState;
    private final boolean releaseLocked;

    @SafeVarargs
    BnbFeatureFlag(final FeatureCategories.ItemFeatureCategory blocks,
                   final String description,
                   final Supplier<Item>... associatedItems) {
        this(
                blocks,
                description,
                false,
                true,
                Lazy.of(List::of),
                Lazy.of(() -> Arrays.asList(associatedItems))
        );
    }

    @SafeVarargs
    BnbFeatureFlag(final FeatureCategories.ItemFeatureCategory blocks,
                   final String description,
                   final boolean experimental,
                   final Supplier<Item>... associatedItems) {
        this(
                blocks,
                description,
                experimental,
                !experimental,
                Lazy.of(List::of),
                Lazy.of(() -> Arrays.asList(associatedItems))
        );
    }

    @SafeVarargs
    BnbFeatureFlag(final FeatureCategories.BlockFeatureCategory blocks,
                   final String description,
                   final Supplier<Block>... associatedBlocks) {
        this(blocks, description, false, true, Lazy.of(() -> Arrays.asList(associatedBlocks)), Lazy.of(List::of));
    }

    @SafeVarargs
    BnbFeatureFlag(final FeatureCategories.BlockFeatureCategory blocks,
                   final String description,
                   final boolean releaseLocked,
                   final Supplier<Block>... associatedBlocks) {
        this(
                blocks,
                description,
                releaseLocked,
                !releaseLocked,
                Lazy.of(() -> Arrays.asList(associatedBlocks)),
                Lazy.of(List::of)
        );
    }

    @SafeVarargs
    BnbFeatureFlag(final FeatureCategories.BlockFeatureCategory blocks,
                   final String description,
                   final boolean releaseLocked,
                   final boolean defaultState,
                   final Supplier<Block>... associatedBlocks) {
        this(
                blocks,
                description,
                releaseLocked,
                defaultState,
                Lazy.of(() -> Arrays.asList(associatedBlocks)),
                Lazy.of(List::of)
        );
    }

    BnbFeatureFlag(final FeatureCategories.BehaviourFeatureCategory blocks,
                   final String description) {
        this(blocks, description, false, true, Lazy.of(List::of), Lazy.of(List::of));
    }

    BnbFeatureFlag(final FeatureCategories.BlockFeatureCategory block,
                   final String description,
                   final Lazy<List<Supplier<Block>>> associatedBlocks) {
        this(block, description, false, true, associatedBlocks, Lazy.of(List::of));
    }

    BnbFeatureFlag(final FeatureCategories.FeatureCategory category,
                   final String description,
                   final boolean releaseLocked,
                   final boolean defaultState,
                   final Lazy<List<Supplier<Block>>> associatedBlocks,
                   final Lazy<List<Supplier<Item>>> associatedItems) {
        this.category = category;
        this.description = description;
        this.associatedBlocks = associatedBlocks;
        this.associatedItems = associatedItems;
        this.defaultState = defaultState;
        this.releaseLocked = releaseLocked;
    }

    public static boolean isDevEnvironment() {
        return !FMLLoader.isProduction();
    }

    private static boolean checkFlags(final Predicate<BnbFeatureFlag> flagPredicate,
                                      final Predicate<Item> itemPredicate,
                                      final Predicate<Block> blockPredicate) {
        for (final BnbFeatureFlag flag : BnbFeatureFlag.values()) {
            if (!flagPredicate.test(flag)) continue;

            for (final Supplier<Item> item : flag.getAssociatedItems()) {
                if (itemPredicate.test(item.get())) {
                    return true;
                }
            }

            for (final Supplier<Block> block : flag.getAssociatedBlocks()) {
                if (blockPredicate.test(block.get())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isReleaseLocked(final Item item) {
        return checkFlags(
                BnbFeatureFlag::isReleaseLocked,
                flagItem -> flagItem == item,
                block -> item instanceof final BlockItem blockItem && blockItem.getBlock() == block
        );
    }

    public static boolean isExperimental(final Item item) {
        return checkFlags(
                BnbFeatureFlag::isExperimental,
                flagItem -> flagItem == item,
                block -> item instanceof final BlockItem blockItem && blockItem.getBlock() == block
        );
    }

    public static boolean isEnabled(final Item item) {
        return !checkFlags(
                flag -> !flag.isEnabled(),
                flagItem -> flagItem == item,
                block -> item instanceof final BlockItem blockItem && blockItem.getBlock() == block
        );
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

        return flag.isEnabled();
    }

    private boolean isExperimental() {
        return !this.defaultState;
    }

    public FeatureCategories.FeatureCategory getCategory() {
        return this.category;
    }

    public String getDescription() {
        return this.description;
    }

    public boolean getDefaultState() {
        return this.defaultState;
    }

    public boolean isReleaseLocked() {
        return this.releaseLocked;
    }

    public List<Supplier<Block>> getAssociatedBlocks() {
        return this.associatedBlocks.get();
    }

    public List<Supplier<Item>> getAssociatedItems() {
        return this.associatedItems.get();
    }

    public boolean isEnabled() {
        if (isDevEnvironment()) {
            return true;
        }
        return BnbConfigs.common().getFeatureFlagState(this);
    }

    public BnbFeatureEnabledCondition getDataCondition() {
        return new BnbFeatureEnabledCondition(this.name().toLowerCase());
    }

}

