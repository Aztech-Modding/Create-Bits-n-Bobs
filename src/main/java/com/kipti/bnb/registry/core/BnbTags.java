package com.kipti.bnb.registry.core;

import com.kipti.bnb.CreateBitsnBobs;
import com.kipti.bnb.registry.content.blocks.BnbKineticBlocks;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTags;
import com.tterrag.registrate.providers.ProviderType;
import net.createmod.catnip.lang.Lang;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.Tags;

public class BnbTags {

    public enum BnbItemTags {

        CHAIRS;

        public final TagKey<Item> tag;

        BnbItemTags() {
            final ResourceLocation id = ResourceLocation.fromNamespaceAndPath(CreateBitsnBobs.MOD_ID, Lang.asId(this.name()));
            this.tag = ItemTags.create(id);
        }

        @SuppressWarnings("deprecation")
        public boolean matches(final Item item) {
            return item.builtInRegistryHolder()
                    .is(this.tag);
        }

        public boolean matches(final ItemStack stack) {
            return stack.is(this.tag);
        }

        private static void register() {
        }

    }

    public enum BnbBlockTags {

        //TODO: if sable installed, use that
        LIGHT,
        HEAVY,
        SUPER_HEAVY,

        COGWHEEL_CHAIN_NO_SMALL_OFFSET,

        NIXIE_BOARDS,
        NIXIE_TUBES,

        CHAIRS,

        EXTRA_COGWHEEL_CHAIN_CANDIDATES,
        DEDICATED_COGWHEEL_CHAIN_COMPONENT;

        public final TagKey<Block> tag;

        BnbBlockTags() {
            final ResourceLocation id = ResourceLocation.fromNamespaceAndPath(CreateBitsnBobs.MOD_ID, Lang.asId(this.name()));
            this.tag = BlockTags.create(id);
        }

        @SuppressWarnings("deprecation")
        public boolean matches(final Block item) {
            return item.builtInRegistryHolder()
                    .is(this.tag);
        }

        public boolean matches(final BlockState stack) {
            return stack.is(this.tag);
        }

        private static void register() {
        }

    }

    public static void registerDataGenerators() {
        CreateBitsnBobs.REGISTRATE.addDataGenerator(ProviderType.BLOCK_TAGS, prov -> {
            prov.addTag(BnbTags.BnbBlockTags.SUPER_HEAVY.tag)
                    .addTag(Tags.Blocks.STORAGE_BLOCKS)
                    .add(AllBlocks.INDUSTRIAL_IRON_BLOCK.getKey());

            prov.addTag(BnbBlockTags.HEAVY.tag)
                    .addTag(Tags.Blocks.STONES);

            prov.addTag(BnbBlockTags.LIGHT.tag)
                    .addOptionalTag(AllTags.AllBlockTags.BRITTLE.tag)
                    .addOptionalTag(AllTags.AllBlockTags.WRENCH_PICKUP.tag)
                    .addTag(BlockTags.LOGS_THAT_BURN)
                    .addTag(BlockTags.PLANKS)
                    .addTag(BlockTags.WOODEN_BUTTONS)
                    .addTag(BlockTags.WOODEN_DOORS)
                    .addTag(BlockTags.WOODEN_FENCES)
                    .addTag(BlockTags.WOODEN_SLABS)
                    .addTag(BlockTags.WOODEN_STAIRS)
                    .remove(AllBlocks.INDUSTRIAL_IRON_BLOCK.getKey());

            prov.addTag(BnbBlockTags.DEDICATED_COGWHEEL_CHAIN_COMPONENT.tag)
                    .add(BnbKineticBlocks.LARGE_FLANGED_COGWHEEL.getKey())
                    .add(BnbKineticBlocks.SMALL_FLANGED_COGWHEEL.getKey());
        });
    }

    public static void register() {
        BnbItemTags.register();
        BnbBlockTags.register();
    }
}

