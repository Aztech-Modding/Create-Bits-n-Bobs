package com.kipti.bnb.registry.core;

import com.kipti.bnb.CreateBitsnBobs;
import com.kipti.bnb.registry.content.blocks.BnbKineticBlocks;
import com.kipti.bnb.registry.content.blocks.encased.BnbExtraEncasedBlocks;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTags;
import com.tterrag.registrate.providers.ProviderType;
import net.createmod.catnip.lang.Lang;
import net.minecraft.data.tags.TagsProvider.TagAppender;
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

        CHAIRS,
        SUPPRESSIBLE_COGWHEELS,
        SUPPRESSIBLE_DYED_LOGISTICS_COMPONENTS;

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
        DEDICATED_COGWHEEL_CHAIN_COMPONENT,
        FORBIDDEN_COGWHEEL_CHAIN_COMPONENT,

        FLANGED_COGWHEEL,
        LARGE_FLANGED_COGWHEEL,
        SMALL_FLANGED_COGWHEEL,

        ENCASED_FLANGED_COGWHEEL,
        ENCASED_LARGE_FLANGED_COGWHEEL,

        COGWHEEL_MATERIAL_CANDIDATES,
        COGWHEEL_MATERIAL_COGWHEEL_MODEL,
        COGWHEEL_MATERIAL_SHAFTLESS_COGWHEEL_MODEL,
        COGWHEEL_MATERIAL_SHAFTLESS_LARGE_COGWHEEL_MODEL,
        COGWHEEL_MATERIAL_FLANGED_COGWHEEL_MODEL,
        COGWHEEL_MATERIAL_LARGE_FLANGED_COGWHEEL_MODEL,
        COGWHEEL_MATERIAL_ENCASED_FLANGED_COGWHEEL_MODEL,
        COGWHEEL_MATERIAL_ENCASED_LARGE_FLANGED_COGWHEEL_MODEL,

        //For checking blocks that may be dyeable in both BnB OR BnD
        DYEABLE_FLUID_TANK,
        DYEABLE_ITEM_VAULT;

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

            prov.addTag(BnbBlockTags.FORBIDDEN_COGWHEEL_CHAIN_COMPONENT.tag)
                    .addOptional(ResourceLocation.fromNamespaceAndPath("gnkinetics", "chainable_cogwheel"));

            prov.addTag(BnbBlockTags.DYEABLE_FLUID_TANK.tag)
                    .add(AllBlocks.FLUID_TANK.getKey())
                    .addOptional(ResourceLocation.fromNamespaceAndPath("create_connected", "fluid_vessel"));

            prov.addTag(BnbBlockTags.DYEABLE_ITEM_VAULT.tag)
                    .add(AllBlocks.ITEM_VAULT.getKey())
                    .addOptional(ResourceLocation.fromNamespaceAndPath("create_connected", "item_silo"))
                    .addOptional(ResourceLocation.fromNamespaceAndPath("create_vibrant_vaults", "vertical_item_vault"));

            prov.addTag(BnbBlockTags.COGWHEEL_MATERIAL_CANDIDATES.tag)
                    .addTag(BnbBlockTags.COGWHEEL_MATERIAL_COGWHEEL_MODEL.tag)
                    .addTag(BnbBlockTags.COGWHEEL_MATERIAL_SHAFTLESS_COGWHEEL_MODEL.tag)
                    .addTag(BnbBlockTags.COGWHEEL_MATERIAL_SHAFTLESS_LARGE_COGWHEEL_MODEL.tag)
                    .addTag(BnbBlockTags.COGWHEEL_MATERIAL_FLANGED_COGWHEEL_MODEL.tag)
                    .addTag(BnbBlockTags.COGWHEEL_MATERIAL_LARGE_FLANGED_COGWHEEL_MODEL.tag)
                    .addTag(BnbBlockTags.COGWHEEL_MATERIAL_ENCASED_FLANGED_COGWHEEL_MODEL.tag)
                    .addTag(BnbBlockTags.COGWHEEL_MATERIAL_ENCASED_LARGE_FLANGED_COGWHEEL_MODEL.tag)
                    .addOptional(ResourceLocation.fromNamespaceAndPath("create_connected", "crank_wheel"))
                    .addOptional(ResourceLocation.fromNamespaceAndPath("create_connected", "large_crank_wheel"))
                    .addOptional(ResourceLocation.fromNamespaceAndPath("create_hypertube", "hypertube_entrance"))
                    .addOptional(ResourceLocation.fromNamespaceAndPath("create_hypertube", "hypertube_accelerator"));
            
            prov.addTag(BnbBlockTags.COGWHEEL_MATERIAL_COGWHEEL_MODEL.tag)
                    .add(AllBlocks.COGWHEEL.getKey());

            prov.addTag(BnbBlockTags.COGWHEEL_MATERIAL_SHAFTLESS_COGWHEEL_MODEL.tag)
                    .add(AllBlocks.MECHANICAL_MIXER.getKey())
                    .add(AllBlocks.DISPLAY_BOARD.getKey())
                    .add(AllBlocks.ANDESITE_ENCASED_COGWHEEL.getKey())
                    .add(AllBlocks.BRASS_ENCASED_COGWHEEL.getKey())
                    .add(BnbExtraEncasedBlocks.INDUSTRIAL_IRON_ENCASED_COGWHEEL.getKey())
                    .add(BnbExtraEncasedBlocks.WEATHERED_IRON_ENCASED_COGWHEEL.getKey())
                    .addOptional(ResourceLocation.fromNamespaceAndPath("create_connected", "encased_chain_cogwheel"))
                    .addOptional(ResourceLocation.fromNamespaceAndPath("sliceanddice", "slicer"))
                    .addOptional(ResourceLocation.fromNamespaceAndPath("createadditionallogistics", "package_accelerator"))
                    .addOptional(ResourceLocation.fromNamespaceAndPath("createadditionallogistics", "lazy_cogwheel"))
                    //Create Encased (createcasing): plain cogwheels/mixers/slicers render the standard shaftless model
                    .addOptional(ResourceLocation.fromNamespaceAndPath("createcasing", "creative_cogwheel"))
                    .addOptional(ResourceLocation.fromNamespaceAndPath("createcasing", "brass_mixer"))
                    .addOptional(ResourceLocation.fromNamespaceAndPath("createcasing", "copper_mixer"))
                    .addOptional(ResourceLocation.fromNamespaceAndPath("createcasing", "creative_mixer"))
                    .addOptional(ResourceLocation.fromNamespaceAndPath("createcasing", "industrial_iron_mixer"))
                    .addOptional(ResourceLocation.fromNamespaceAndPath("createcasing", "railway_mixer"))
                    .addOptional(ResourceLocation.fromNamespaceAndPath("createcasing", "refined_radiance_mixer"))
                    .addOptional(ResourceLocation.fromNamespaceAndPath("createcasing", "shadow_steel_mixer"))
                    .addOptional(ResourceLocation.fromNamespaceAndPath("createcasing", "weathered_iron_mixer"))
                    .addOptional(ResourceLocation.fromNamespaceAndPath("createcasing", "brass_slicer"))
                    .addOptional(ResourceLocation.fromNamespaceAndPath("createcasing", "copper_slicer"))
                    .addOptional(ResourceLocation.fromNamespaceAndPath("createcasing", "creative_slicer"))
                    .addOptional(ResourceLocation.fromNamespaceAndPath("createcasing", "industrial_iron_slicer"))
                    .addOptional(ResourceLocation.fromNamespaceAndPath("createcasing", "railway_slicer"))
                    .addOptional(ResourceLocation.fromNamespaceAndPath("createcasing", "refined_radiance_slicer"))
                    .addOptional(ResourceLocation.fromNamespaceAndPath("createcasing", "shadow_steel_slicer"))
                    .addOptional(ResourceLocation.fromNamespaceAndPath("createcasing", "weathered_iron_slicer"))
                    .addOptional(ResourceLocation.fromNamespaceAndPath("createcasing", "copper_encased_cogwheel"))
                    .addOptional(ResourceLocation.fromNamespaceAndPath("createcasing", "railway_encased_cogwheel"))
                    .addOptional(ResourceLocation.fromNamespaceAndPath("createcasing", "shadow_steel_encased_cogwheel"))
                    .addOptional(ResourceLocation.fromNamespaceAndPath("createcasing", "refined_radiance_encased_cogwheel"))
                    .addOptional(ResourceLocation.fromNamespaceAndPath("createcasing", "creative_encased_cogwheel"))
                    .addOptional(ResourceLocation.fromNamespaceAndPath("createcasing", "industrial_iron_encased_cogwheel"))
                    .addOptional(ResourceLocation.fromNamespaceAndPath("createcasing", "weathered_iron_encased_cogwheel"))
                    .addOptional(ResourceLocation.fromNamespaceAndPath("createcasing", "zinc_encased_cogwheel"));

            prov.addTag(BnbBlockTags.COGWHEEL_MATERIAL_SHAFTLESS_LARGE_COGWHEEL_MODEL.tag)
                    .add(AllBlocks.LARGE_COGWHEEL.getKey())
                    .add(AllBlocks.ANDESITE_ENCASED_LARGE_COGWHEEL.getKey())
                    .add(AllBlocks.BRASS_ENCASED_LARGE_COGWHEEL.getKey())
                    .add(BnbExtraEncasedBlocks.INDUSTRIAL_IRON_ENCASED_LARGE_COGWHEEL.getKey())
                    .add(BnbExtraEncasedBlocks.WEATHERED_IRON_ENCASED_LARGE_COGWHEEL.getKey())
                    .addOptional(ResourceLocation.fromNamespaceAndPath("createadditionallogistics", "lazy_large_cogwheel"))
                    .addOptional(ResourceLocation.fromNamespaceAndPath("createcasing", "copper_encased_large_cogwheel"))
                    .addOptional(ResourceLocation.fromNamespaceAndPath("createcasing", "railway_encased_large_cogwheel"))
                    .addOptional(ResourceLocation.fromNamespaceAndPath("createcasing", "shadow_steel_encased_large_cogwheel"))
                    .addOptional(ResourceLocation.fromNamespaceAndPath("createcasing", "refined_radiance_encased_large_cogwheel"))
                    .addOptional(ResourceLocation.fromNamespaceAndPath("createcasing", "creative_encased_large_cogwheel"))
                    .addOptional(ResourceLocation.fromNamespaceAndPath("createcasing", "industrial_iron_encased_large_cogwheel"))
                    .addOptional(ResourceLocation.fromNamespaceAndPath("createcasing", "weathered_iron_encased_large_cogwheel"))
                    .addOptional(ResourceLocation.fromNamespaceAndPath("createcasing", "zinc_encased_large_cogwheel"));

            prov.addTag(BnbBlockTags.COGWHEEL_MATERIAL_FLANGED_COGWHEEL_MODEL.tag)
                    .add(BnbKineticBlocks.SMALL_FLANGED_COGWHEEL.getKey());

            prov.addTag(BnbBlockTags.COGWHEEL_MATERIAL_LARGE_FLANGED_COGWHEEL_MODEL.tag)
                    .add(BnbKineticBlocks.LARGE_FLANGED_COGWHEEL.getKey());

            prov.addTag(BnbBlockTags.COGWHEEL_MATERIAL_ENCASED_FLANGED_COGWHEEL_MODEL.tag)
                    .addTag(BnbBlockTags.ENCASED_FLANGED_COGWHEEL.tag);

            prov.addTag(BnbBlockTags.COGWHEEL_MATERIAL_ENCASED_LARGE_FLANGED_COGWHEEL_MODEL.tag)
                    .addTag(BnbBlockTags.ENCASED_LARGE_FLANGED_COGWHEEL.tag);

            prov.addTag(BnbBlockTags.SMALL_FLANGED_COGWHEEL.tag)
                    .addTag(BnbBlockTags.ENCASED_FLANGED_COGWHEEL.tag)
                    .add(BnbKineticBlocks.SMALL_FLANGED_COGWHEEL.getKey());

            prov.addTag(BnbBlockTags.LARGE_FLANGED_COGWHEEL.tag)
                    .addTag(BnbBlockTags.ENCASED_LARGE_FLANGED_COGWHEEL.tag)
                    .add(BnbKineticBlocks.LARGE_FLANGED_COGWHEEL.getKey());

            prov.addTag(BnbBlockTags.FLANGED_COGWHEEL.tag)
                    .addTag(BnbBlockTags.SMALL_FLANGED_COGWHEEL.tag)
                    .addTag(BnbBlockTags.LARGE_FLANGED_COGWHEEL.tag);
        });

        CreateBitsnBobs.REGISTRATE.addDataGenerator(ProviderType.ITEM_TAGS, prov -> {
            final TagAppender<Item> cogwheelTag = prov.addTag(BnbItemTags.SUPPRESSIBLE_COGWHEELS.tag);
            //Create: Encased (createcasing) wooden cogwheels, mirroring TransmissionSets' wood list (spruce excluded: it reuses Create's own cogwheel)
            for (final String wood : new String[]{"acacia", "birch", "bamboo", "cherry", "crimson", "dark_oak", "oak", "jungle", "mangrove", "warped"}) {
                cogwheelTag.addOptional(ResourceLocation.fromNamespaceAndPath("createcasing", wood + "_cogwheel"));
                cogwheelTag.addOptional(ResourceLocation.fromNamespaceAndPath("createcasing", wood + "_large_cogwheel"));
            }

            final TagAppender<Item> dyedLogisticsTag = prov.addTag(BnbItemTags.SUPPRESSIBLE_DYED_LOGISTICS_COMPONENTS.tag);
            dyedLogisticsTag.addOptionalTag(ResourceLocation.fromNamespaceAndPath("create_vibrant_vaults", "colored_horizontal_item_vaults"));
            dyedLogisticsTag.addOptionalTag(ResourceLocation.fromNamespaceAndPath("create_vibrant_vaults", "colored_vertical_item_vaults"));
            dyedLogisticsTag.addOptionalTag(ResourceLocation.fromNamespaceAndPath("create_vibrant_vaults", "vibrant_frogports"));
            dyedLogisticsTag.addOptionalTag(ResourceLocation.fromNamespaceAndPath("create_vibrant_vaults", "vibrant_redstone_requesters"));
        });
    }

    public static void register() {
        BnbItemTags.register();
        BnbBlockTags.register();
    }

}

