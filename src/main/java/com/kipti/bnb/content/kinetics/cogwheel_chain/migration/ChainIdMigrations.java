package com.kipti.bnb.content.kinetics.cogwheel_chain.migration;

import com.kipti.bnb.CreateBitsnBobs;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public class ChainIdMigrations {

    public static final ResourceLocation BNB_SIMPLE_KINETIC =
            CreateBitsnBobs.asResource("simple_kinetic");
    public static final ResourceLocation MIGRATING_SIMPLE_KINETIC =
            CreateBitsnBobs.asResource("migrating_simple_kinetic");

    /**
     * Old block registry ID → new block registry ID.
     * Used by both {@code addAlias} for world saves and the schematic-load mixin
     * for palette remapping.
     */
    public static final Map<ResourceLocation, ResourceLocation> BLOCK_RENAMES = Map.of(
            CreateBitsnBobs.asResource("small_cogwheel_chain"), ResourceLocation.fromNamespaceAndPath("create", "cogwheel"),
            CreateBitsnBobs.asResource("large_cogwheel_chain"), ResourceLocation.fromNamespaceAndPath("create", "large_cogwheel"),
            CreateBitsnBobs.asResource("small_flanged_cogwheel_chain"), CreateBitsnBobs.asResource("small_flanged_cogwheel"),
            CreateBitsnBobs.asResource("large_flanged_cogwheel_chain"), CreateBitsnBobs.asResource("large_flanged_cogwheel")
    );

    /**
     * The old shared block entity type id used by every cogwheel-chain block.
     * Its migration target depends on the block it sits on
     */
    public static final ResourceLocation COGWHEEL_CHAIN_BE = CreateBitsnBobs.asResource("cogwheel_chain");

    /**
     * Unambiguous block entity renames (target independent of which block they sit on, later differentiation may occur).
     */
    public static final Map<ResourceLocation, ResourceLocation> BLOCK_ENTITY_RENAMES = Map.of(
            CreateBitsnBobs.asResource("empty_flanged_cogwheel"), BNB_SIMPLE_KINETIC,
            COGWHEEL_CHAIN_BE, MIGRATING_SIMPLE_KINETIC
    );

}
