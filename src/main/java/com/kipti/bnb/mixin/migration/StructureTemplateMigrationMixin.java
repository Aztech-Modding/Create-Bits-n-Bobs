package com.kipti.bnb.mixin.migration;

import com.kipti.bnb.content.kinetics.cogwheel_chain.migration.ChainIdMigrations;
import net.minecraft.core.HolderGetter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Patches schematic (.nbt) files on load to migrate legacy chain-drive block and block
 * entity IDs before StructureTemplate deserialises them.
 * <p>
 * Two things are remapped:
 * <ol>
 *   <li><b>Palette entries</b> — old block Names are replaced with their new
 *       equivalents via {@link ChainIdMigrations#BLOCK_RENAMES}.</li>
 *   <li><b>Block entity {@code id} fields</b> — the original palette Name (before
 *       remapping) is used as context to resolve the correct target for the shared
 *       {@code bits_n_bobs:cogwheel_chain} BE id, since non-flanged chain blocks
 *       migrate to {@code create:simple_kinetic} while flanged ones migrate to
 *       {@code bits_n_bobs:simple_kinetic}.</li>
 * </ol>
 * All remapping tables live in {@link ChainIdMigrations} — the single source of truth
 * shared with the registry alias event.
 */
@Mixin(StructureTemplate.class)
public class StructureTemplateMigrationMixin {

    @Inject(method = "load", at = @At("HEAD"))
    private void bnb$migrateChainIds(
            final HolderGetter<Block> blockGetter, final CompoundTag nbt, final CallbackInfo ci
    ) {
        if (!nbt.contains("palette", Tag.TAG_LIST) || !nbt.contains("blocks", Tag.TAG_LIST)) return;

        // Phase 1 — remap block entity IDs.
        // Must run BEFORE palette remapping so we can still read the original block Name
        // as context for the ambiguous bits_n_bobs:cogwheel_chain BE id.
        final ListTag palette = nbt.getList("palette", Tag.TAG_COMPOUND);
        final ListTag blocks  = nbt.getList("blocks",  Tag.TAG_COMPOUND);

        for (int i = 0; i < blocks.size(); i++) {
            final CompoundTag blockEntry = blocks.getCompound(i);
            if (!blockEntry.contains("nbt", Tag.TAG_COMPOUND)) continue;

            final CompoundTag beNbt = blockEntry.getCompound("nbt");
            final ResourceLocation oldBeId = ResourceLocation.tryParse(beNbt.getString("id"));
            if (oldBeId == null) continue;

            if (oldBeId.equals(ChainIdMigrations.COGWHEEL_CHAIN_BE)) {
                // Context-aware: check which block this BE sits on (original palette name)
                // before we remap the palette in Phase 2.
                final int stateIndex = blockEntry.getInt("state");
                final ResourceLocation blockName = stateIndex < palette.size()
                        ? ResourceLocation.tryParse(palette.getCompound(stateIndex).getString("Name"))
                        : null;
                final ResourceLocation target = blockName != null
                        && ChainIdMigrations.FLANGED_CHAIN_BLOCK_NAMES.contains(blockName)
                        ? ChainIdMigrations.BNB_SIMPLE_KINETIC
                        : ChainIdMigrations.CREATE_SIMPLE_KINETIC;
                beNbt.putString("id", target.toString());
            } else {
                final ResourceLocation newBeId = ChainIdMigrations.BLOCK_ENTITY_RENAMES.get(oldBeId);
                if (newBeId != null) beNbt.putString("id", newBeId.toString());
            }
        }

        // Phase 2 — remap palette block Names.
        for (int i = 0; i < palette.size(); i++) {
            final CompoundTag entry = palette.getCompound(i);
            final ResourceLocation blockName = ResourceLocation.tryParse(entry.getString("Name"));
            if (blockName == null) continue;
            final ResourceLocation newName = ChainIdMigrations.BLOCK_RENAMES.get(blockName);
            if (newName != null) entry.putString("Name", newName.toString());
        }
    }
}
