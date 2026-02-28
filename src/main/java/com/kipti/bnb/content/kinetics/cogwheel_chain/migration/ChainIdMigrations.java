package com.kipti.bnb.content.kinetics.cogwheel_chain.migration;

import com.kipti.bnb.CreateBitsnBobs;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.Set;

public class ChainIdMigrations {

    public static final ResourceLocation CREATE_SIMPLE_KINETIC =
            ResourceLocation.fromNamespaceAndPath("create", "simple_kinetic");
    public static final ResourceLocation BNB_SIMPLE_KINETIC =
            CreateBitsnBobs.asResource("simple_kinetic");

    /**
     * Old block registry ID → new block registry ID.
     * Used by both {@code addAlias} for world saves and the schematic-load mixin
     * for palette remapping.
     *
     * <ul>
     *   <li>Non-flanged chain blocks collapse back into the vanilla Create cogwheel blocks
     *       they were always paired with.</li>
     *   <li>Flanged chain blocks rename to their non-chain equivalents.</li>
     * </ul>
     */
    public static final Map<ResourceLocation, ResourceLocation> BLOCK_RENAMES = Map.of(
            CreateBitsnBobs.asResource("small_cogwheel_chain"), ResourceLocation.fromNamespaceAndPath("create", "cogwheel"),
            CreateBitsnBobs.asResource("large_cogwheel_chain"), ResourceLocation.fromNamespaceAndPath("create", "large_cogwheel"),
            CreateBitsnBobs.asResource("small_flanged_cogwheel_chain"), CreateBitsnBobs.asResource("small_flanged_cogwheel"),
            CreateBitsnBobs.asResource("large_flanged_cogwheel_chain"), CreateBitsnBobs.asResource("large_flanged_cogwheel")
    );

    /**
     * The old shared block entity type id used by every cogwheel-chain block.
     * Its migration target depends on the block it sits on — see
     * {@link #FLANGED_CHAIN_BLOCK_NAMES} — so it is handled separately from the
     * unambiguous {@link #BLOCK_ENTITY_RENAMES} map.
     */
    public static final ResourceLocation COGWHEEL_CHAIN_BE = CreateBitsnBobs.asResource("cogwheel_chain");

    /**
     * Cogwheel-chain block names whose {@link #COGWHEEL_CHAIN_BE} block entity
     * should migrate to {@link #BNB_SIMPLE_KINETIC}.
     * All other cogwheel-chain blocks migrate to {@link #CREATE_SIMPLE_KINETIC}.
     */
    public static final Set<ResourceLocation> FLANGED_CHAIN_BLOCK_NAMES = Set.of(
            CreateBitsnBobs.asResource("small_flanged_cogwheel_chain"),
            CreateBitsnBobs.asResource("large_flanged_cogwheel_chain")
    );

    /**
     * Unambiguous block entity renames (target independent of which block they sit on).
     * {@link #COGWHEEL_CHAIN_BE} is intentionally absent — see above.
     */
    public static final Map<ResourceLocation, ResourceLocation> BLOCK_ENTITY_RENAMES = Map.of(
            CreateBitsnBobs.asResource("empty_flanged_cogwheel"), BNB_SIMPLE_KINETIC
    );

    private ChainIdMigrations() {
    }
}
