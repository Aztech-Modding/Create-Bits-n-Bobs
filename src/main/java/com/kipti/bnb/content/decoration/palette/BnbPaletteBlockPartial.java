package com.kipti.bnb.content.decoration.palette;

import com.google.common.collect.ImmutableMap;
import com.kipti.bnb.CreateBitsnBobs;
import com.kipti.bnb.registry.worldgen.BnbPaletteStoneTypes;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import com.tterrag.registrate.util.DataIngredient;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonnullType;
import net.createmod.catnip.lang.Lang;
import net.minecraft.core.Direction;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.*;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.MultiPartBlockStateBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;

public abstract class BnbPaletteBlockPartial<B extends Block> {

    public static final BnbPaletteBlockPartial<StairBlock> STAIR = new BnbPaletteBlockPartial.Stairs();
    public static final BnbPaletteBlockPartial<SlabBlock> SLAB = new BnbPaletteBlockPartial.Slab(false);
    public static final BnbPaletteBlockPartial<SlabBlock> UNIQUE_SLAB = new BnbPaletteBlockPartial.Slab(true);
    public static final BnbPaletteBlockPartial<WallBlock> WALL = new BnbPaletteBlockPartial.Wall();
    public static final BnbPaletteBlockPartial<StairBlock> TILE_STAIR = new BnbPaletteBlockPartial.TileStairs();
    public static final BnbPaletteBlockPartial<SlabBlock> TILE_SLAB = new BnbPaletteBlockPartial.TileSlab();
    public static final BnbPaletteBlockPartial<WallBlock> TILE_WALL = new BnbPaletteBlockPartial.TileWall();

    public static final BnbPaletteBlockPartial<?>[] ALL_PARTIALS = {STAIR, SLAB, WALL};
    public static final BnbPaletteBlockPartial<?>[] ALL_PARTIALS_TILE = {TILE_STAIR, TILE_SLAB, TILE_WALL};
    public static final BnbPaletteBlockPartial<?>[] FOR_POLISHED = {STAIR, UNIQUE_SLAB, WALL};

    private final String name;

    private BnbPaletteBlockPartial(final String name) {
        this.name = name;
    }

    public @NonnullType BlockBuilder<B, CreateRegistrate> create(final String variantName, final BnbPaletteBlockPattern pattern,
                                                                 final BlockEntry<? extends Block> block, final BnbPaletteStoneTypes variant) {
        final String patternName = Lang.nonPluralId(pattern.createName(variantName));
        final String blockName = patternName + "_" + this.name;

        final BlockBuilder<B, CreateRegistrate> blockBuilder = CreateBitsnBobs.REGISTRATE
                .block(blockName, p -> createBlock(block))
                .blockstate((c, p) -> generateBlockState(c, p, variantName, pattern, block))
                .recipe((c, p) -> createRecipes(variant, block, c, p))
                .transform(b -> transformBlock(b, variantName, pattern));

        final ItemBuilder<BlockItem, BlockBuilder<B, CreateRegistrate>> itemBuilder = blockBuilder.item()
                .transform(b -> transformItem(b, variantName, pattern));

        if (canRecycle())
            itemBuilder.tag(variant.materialTag);

        return itemBuilder.build();
    }

    protected ResourceLocation getTexture(final String variantName, final BnbPaletteBlockPattern pattern, final int index) {
        return BnbPaletteBlockPattern.toLocation(variantName, pattern.getTexture(index));
    }

    protected ResourceLocation getFlippedTexture(final String variantName, final BnbPaletteBlockPattern pattern, final int index) {
        return BnbPaletteBlockPattern.toLocation(variantName, pattern.getTexture(index) + "_flipped");
    }

    protected BlockBuilder<B, CreateRegistrate> transformBlock(final BlockBuilder<B, CreateRegistrate> builder,
                                                               final String variantName, final BnbPaletteBlockPattern pattern) {
        getBlockTags().forEach(builder::tag);
        return builder.transform(pickaxeOnly());
    }

    protected ItemBuilder<BlockItem, BlockBuilder<B, CreateRegistrate>> transformItem(
            final ItemBuilder<BlockItem, BlockBuilder<B, CreateRegistrate>> builder, final String variantName,
            final BnbPaletteBlockPattern pattern) {
        getItemTags().forEach(builder::tag);
        return builder;
    }

    protected boolean canRecycle() {
        return true;
    }

    protected abstract Iterable<TagKey<Block>> getBlockTags();

    protected abstract Iterable<TagKey<Item>> getItemTags();

    protected abstract B createBlock(Supplier<? extends Block> block);

    protected abstract void createRecipes(BnbPaletteStoneTypes type, BlockEntry<? extends Block> patternBlock,
                                          DataGenContext<Block, ? extends Block> c, RegistrateRecipeProvider p);

    protected abstract void generateBlockState(DataGenContext<Block, B> ctx, RegistrateBlockstateProvider prov,
                                               String variantName, BnbPaletteBlockPattern pattern, Supplier<? extends Block> block);

    private static class Stairs extends BnbPaletteBlockPartial<StairBlock> {

        public Stairs() {
            super("stairs");
        }

        @Override
        protected StairBlock createBlock(final Supplier<? extends Block> block) {
            return new StairBlock(block.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(block.get()));
        }

        @Override
        protected void generateBlockState(final DataGenContext<Block, StairBlock> ctx, final RegistrateBlockstateProvider prov,
                                          final String variantName, final BnbPaletteBlockPattern pattern, final Supplier<? extends Block> block) {
            prov.stairsBlock(ctx.get(), getTexture(variantName, pattern, 0));
        }

        @Override
        protected Iterable<TagKey<Block>> getBlockTags() {
            return List.of(BlockTags.STAIRS);
        }

        @Override
        protected Iterable<TagKey<Item>> getItemTags() {
            return List.of(ItemTags.STAIRS);
        }

        @Override
        protected void createRecipes(final BnbPaletteStoneTypes type, final BlockEntry<? extends Block> patternBlock,
                                     final DataGenContext<Block, ? extends Block> c, final RegistrateRecipeProvider p) {
            final RecipeCategory category = RecipeCategory.BUILDING_BLOCKS;
            p.stairs(DataIngredient.items(patternBlock.get()), category, c, c.getName(), false);
            p.stonecutting(DataIngredient.tag(type.materialTag), category, c, 1);
        }

    }

    private static class Slab extends BnbPaletteBlockPartial<SlabBlock> {

        private final boolean customSide;

        public Slab(final boolean customSide) {
            super("slab");
            this.customSide = customSide;
        }

        @Override
        protected SlabBlock createBlock(final Supplier<? extends Block> block) {
            return new SlabBlock(BlockBehaviour.Properties.ofFullCopy(block.get()));
        }

        @Override
        protected boolean canRecycle() {
            return false;
        }

        @Override
        protected void generateBlockState(final DataGenContext<Block, SlabBlock> ctx, final RegistrateBlockstateProvider prov,
                                          final String variantName, final BnbPaletteBlockPattern pattern, final Supplier<? extends Block> block) {
            final String name = ctx.getName();
            final ResourceLocation mainTexture = getTexture(variantName, pattern, 0);
            final ResourceLocation sideTexture = customSide ? getTexture(variantName, pattern, 1) : mainTexture;

            final ModelFile bottom = prov.models()
                    .slab(name, sideTexture, mainTexture, mainTexture);
            final ModelFile top = prov.models()
                    .slabTop(name + "_top", sideTexture, mainTexture, mainTexture);
            final ModelFile doubleSlab;

            if (customSide) {
                doubleSlab = prov.models()
                        .cubeColumn(name + "_double", sideTexture, mainTexture);
            } else {
                doubleSlab = prov.models()
                        .getExistingFile(prov.modLoc(pattern.createName(variantName)));
            }

            prov.slabBlock(ctx.get(), bottom, top, doubleSlab);
        }

        @Override
        protected Iterable<TagKey<Block>> getBlockTags() {
            return List.of(BlockTags.SLABS);
        }

        @Override
        protected Iterable<TagKey<Item>> getItemTags() {
            return List.of(ItemTags.SLABS);
        }

        @Override
        protected void createRecipes(final BnbPaletteStoneTypes type, final BlockEntry<? extends Block> patternBlock,
                                     final DataGenContext<Block, ? extends Block> c, final RegistrateRecipeProvider p) {
            final RecipeCategory category = RecipeCategory.BUILDING_BLOCKS;
            p.slab(DataIngredient.items(patternBlock.get()), category, c, c.getName(), false);
            p.stonecutting(DataIngredient.tag(type.materialTag), category, c, 2);
            final DataIngredient ingredient = DataIngredient.items(c.get());
            ShapelessRecipeBuilder.shapeless(category, patternBlock.get())
                    .requires(ingredient.toVanilla())
                    .requires(ingredient.toVanilla())
                    .unlockedBy("has_" + c.getName(), ingredient.getCriterion(p))
                    .save(p, Create.ID + ":" + c.getName() + "_recycling");
        }

        @Override
        protected BlockBuilder<SlabBlock, CreateRegistrate> transformBlock(
                final BlockBuilder<SlabBlock, CreateRegistrate> builder, final String variantName, final BnbPaletteBlockPattern pattern) {
            builder.loot((lt, block) -> lt.add(block, lt.createSlabItemTable(block)));
            return super.transformBlock(builder, variantName, pattern);
        }

    }

    //Are these the right ways to do tile stairs/slabs/walls? No.

    private static class TileStairs extends BnbPaletteBlockPartial<StairBlock> {

        public TileStairs() {
            super("stairs");
        }

        @Override
        protected StairBlock createBlock(final Supplier<? extends Block> block) {
            return new StairBlock(block.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(block.get()));
        }

        @Override
        protected void generateBlockState(final DataGenContext<Block, StairBlock> ctx, final RegistrateBlockstateProvider prov,
                                          final String variantName, final BnbPaletteBlockPattern pattern, final Supplier<? extends Block> block) {
            final String name = ctx.getName();
            final ResourceLocation mainTexture = getTexture(variantName, pattern, 0);
            final ResourceLocation flippedTexture = getFlippedTexture(variantName, pattern, 0);

            final ModelFile stairs = getStairModel(prov, name, "block/tile_stairs/stairs", mainTexture, flippedTexture);
            final ModelFile stairsAlternate = getStairModel(prov, name + "_alternate", "block/tile_stairs/stairs_alternate", mainTexture, flippedTexture);

            final ModelFile stairsInner = getStairModel(prov, name + "_inner", "block/tile_stairs/inner_stairs", mainTexture, flippedTexture);
            final ModelFile stairsInnerAlternate = getStairModel(prov, name + "_inner_alternate", "block/tile_stairs/inner_stairs_alternate", mainTexture, flippedTexture);

            final ModelFile stairsOuter = getStairModel(prov, name + "_outer", "block/tile_stairs/outer_stairs", mainTexture, flippedTexture);
            final ModelFile stairsOuterAlternate = getStairModel(prov, name + "_outer_alternate", "block/tile_stairs/outer_stairs_alternate", mainTexture, flippedTexture);

            prov.getVariantBuilder(ctx.get())
                    .forAllStatesExcept(state -> {
                        final Direction facing = state.getValue(StairBlock.FACING);
                        final Half half = state.getValue(StairBlock.HALF);
                        final StairsShape shape = state.getValue(StairBlock.SHAPE);
                        int yRot = (int) facing.getClockWise().toYRot(); // Stairs model is rotated 90 degrees clockwise for some reason
                        if (shape == StairsShape.INNER_LEFT || shape == StairsShape.OUTER_LEFT) {
                            yRot += 270; // Left facing stairs are rotated 90 degrees clockwise
                        }
                        if (shape != StairsShape.STRAIGHT && half == Half.TOP) {
                            yRot += 90; // Top stairs are rotated 90 degrees clockwise
                        }
                        yRot %= 360;

                        final boolean alternate = yRot % 180 != 0;

                        final boolean uvlock = yRot != 0 || half == Half.TOP; // Don't set uvlock for states that have no rotation
                        return ConfiguredModel.builder()
                                .modelFile(shape == StairsShape.STRAIGHT ?
                                        (alternate ? stairsAlternate : stairs) :
                                        shape == StairsShape.INNER_LEFT || shape == StairsShape.INNER_RIGHT ?
                                                (alternate ? stairsInnerAlternate : stairsInner) : (alternate ? stairsOuterAlternate : stairsOuter))
                                .rotationX(half == Half.BOTTOM ? 0 : 180)
                                .rotationY(yRot)
                                .uvLock(uvlock)
                                .build();
                    }, StairBlock.WATERLOGGED);
        }

        private static @NotNull BlockModelBuilder getStairModel(final RegistrateBlockstateProvider prov, final String name, final String s, final ResourceLocation mainTexture, final ResourceLocation flippedTexture) {
            return prov.models()
                    .withExistingParent(name, CreateBitsnBobs.asResource(s))
                    .texture("side", mainTexture)
                    .texture("side_flipped", flippedTexture)
                    .texture("bottom", mainTexture)
                    .texture("top", mainTexture);
        }

        @Override
        protected Iterable<TagKey<Block>> getBlockTags() {
            return List.of(BlockTags.STAIRS);
        }

        @Override
        protected Iterable<TagKey<Item>> getItemTags() {
            return List.of(ItemTags.STAIRS);
        }

        @Override
        protected void createRecipes(final BnbPaletteStoneTypes type, final BlockEntry<? extends Block> patternBlock,
                                     final DataGenContext<Block, ? extends Block> c, final RegistrateRecipeProvider p) {
            final RecipeCategory category = RecipeCategory.BUILDING_BLOCKS;
            p.stairs(DataIngredient.items(patternBlock.get()), category, c, c.getName(), false);
            p.stonecutting(DataIngredient.tag(type.materialTag), category, c, 1);
        }

    }

    private static class TileSlab extends BnbPaletteBlockPartial<SlabBlock> {

        public TileSlab() {
            super("slab");
        }

        @Override
        protected SlabBlock createBlock(final Supplier<? extends Block> block) {
            return new SlabBlock(BlockBehaviour.Properties.ofFullCopy(block.get()));
        }

        @Override
        protected boolean canRecycle() {
            return false;
        }

        @Override
        protected void generateBlockState(final DataGenContext<Block, SlabBlock> ctx, final RegistrateBlockstateProvider prov,
                                          final String variantName, final BnbPaletteBlockPattern pattern, final Supplier<? extends Block> block) {
            final String name = ctx.getName();
            final ResourceLocation mainTexture = getTexture(variantName, pattern, 0);
            final ResourceLocation flippedTexture = getFlippedTexture(variantName, pattern, 0);

            final ModelFile bottom = prov.models()
                    .withExistingParent(name, CreateBitsnBobs.asResource("block/tile_slab/slab"))
                    .texture("side", mainTexture)
                    .texture("side_flipped", flippedTexture)
                    .texture("bottom", mainTexture)
                    .texture("top", mainTexture);
            final ModelFile top = prov.models()
                    .withExistingParent(name + "_top", CreateBitsnBobs.asResource("block/tile_slab/slab_top"))
                    .texture("side", mainTexture)
                    .texture("side_flipped", flippedTexture)
                    .texture("bottom", mainTexture)
                    .texture("top", mainTexture);
            final ModelFile doubleSlab;

            doubleSlab = prov.models()
                    .getExistingFile(prov.modLoc(pattern.createName(variantName)));

            prov.slabBlock(ctx.get(), bottom, top, doubleSlab);
        }

        @Override
        protected Iterable<TagKey<Block>> getBlockTags() {
            return List.of(BlockTags.SLABS);
        }

        @Override
        protected Iterable<TagKey<Item>> getItemTags() {
            return List.of(ItemTags.SLABS);
        }

        @Override
        protected void createRecipes(final BnbPaletteStoneTypes type, final BlockEntry<? extends Block> patternBlock,
                                     final DataGenContext<Block, ? extends Block> c, final RegistrateRecipeProvider p) {
            final RecipeCategory category = RecipeCategory.BUILDING_BLOCKS;
            p.slab(DataIngredient.items(patternBlock.get()), category, c, c.getName(), false);
            p.stonecutting(DataIngredient.tag(type.materialTag), category, c, 2);
            final DataIngredient ingredient = DataIngredient.items(c.get());
            ShapelessRecipeBuilder.shapeless(category, patternBlock.get())
                    .requires(ingredient.toVanilla())
                    .requires(ingredient.toVanilla())
                    .unlockedBy("has_" + c.getName(), ingredient.getCriterion(p))
                    .save(p, Create.ID + ":" + c.getName() + "_recycling");
        }

        @Override
        protected BlockBuilder<SlabBlock, CreateRegistrate> transformBlock(
                final BlockBuilder<SlabBlock, CreateRegistrate> builder, final String variantName, final BnbPaletteBlockPattern pattern) {
            builder.loot((lt, block) -> lt.add(block, lt.createSlabItemTable(block)));
            return super.transformBlock(builder, variantName, pattern);
        }

    }

    private static class Wall extends BnbPaletteBlockPartial<WallBlock> {

        public Wall() {
            super("wall");
        }

        @Override
        protected WallBlock createBlock(final Supplier<? extends Block> block) {
            return new WallBlock(BlockBehaviour.Properties.ofFullCopy(block.get()).forceSolidOn());
        }

        @Override
        protected ItemBuilder<BlockItem, BlockBuilder<WallBlock, CreateRegistrate>> transformItem(
                final ItemBuilder<BlockItem, BlockBuilder<WallBlock, CreateRegistrate>> builder, final String variantName,
                final BnbPaletteBlockPattern pattern) {
            builder.model((c, p) -> p.wallInventory(c.getName(), getTexture(variantName, pattern, 0)));
            return super.transformItem(builder, variantName, pattern);
        }

        @Override
        protected void generateBlockState(final DataGenContext<Block, WallBlock> ctx, final RegistrateBlockstateProvider prov,
                                          final String variantName, final BnbPaletteBlockPattern pattern, final Supplier<? extends Block> block) {
            prov.wallBlock(ctx.get(), pattern.createName(variantName), getTexture(variantName, pattern, 0));
        }

        @Override
        protected Iterable<TagKey<Block>> getBlockTags() {
            return List.of(BlockTags.WALLS);
        }

        @Override
        protected Iterable<TagKey<Item>> getItemTags() {
            return List.of(ItemTags.WALLS);
        }

        @Override
        protected void createRecipes(final BnbPaletteStoneTypes type, final BlockEntry<? extends Block> patternBlock,
                                     final DataGenContext<Block, ? extends Block> c, final RegistrateRecipeProvider p) {
            final RecipeCategory category = RecipeCategory.BUILDING_BLOCKS;
            p.stonecutting(DataIngredient.tag(type.materialTag), category, c, 1);
            final DataIngredient ingredient = DataIngredient.items(patternBlock.get());
            ShapedRecipeBuilder.shaped(category, c.get(), 6)
                    .pattern("XXX")
                    .pattern("XXX")
                    .define('X', ingredient.toVanilla())
                    .unlockedBy("has_" + p.safeName(ingredient), ingredient.getCriterion(p))
                    .save(p, p.safeId(c.get()));
        }

    }

    private static class TileWall extends BnbPaletteBlockPartial<WallBlock> {

        public TileWall() {
            super("wall");
        }

        @Override
        protected WallBlock createBlock(final Supplier<? extends Block> block) {
            return new WallBlock(BlockBehaviour.Properties.ofFullCopy(block.get()).forceSolidOn());
        }

        @Override
        protected ItemBuilder<BlockItem, BlockBuilder<WallBlock, CreateRegistrate>> transformItem(
                final ItemBuilder<BlockItem, BlockBuilder<WallBlock, CreateRegistrate>> builder, final String variantName,
                final BnbPaletteBlockPattern pattern) {
            final ResourceLocation wallTexture = getTexture(variantName, pattern, 0);
            final ResourceLocation wallTextureFlipped = getFlippedTexture(variantName, pattern, 0);

            builder.model((c, p) -> p.singleTexture(c.getName(), CreateBitsnBobs.asResource("block/tile_wall/wall_inventory"), "wall", wallTexture)
                    .texture("wall_flipped", wallTextureFlipped));

            return super.transformItem(builder, variantName, pattern);
        }


        public BlockModelBuilder tileWallPost(final RegistrateBlockstateProvider p, final String name, final ResourceLocation wall, final ResourceLocation wallFlipped) {
            return p.models().singleTexture(name, CreateBitsnBobs.asResource("block/tile_wall/template_wall_post"), "wall", wall)
                    .texture("wall_flipped", wallFlipped);
        }

        public BlockModelBuilder tileWallSide(final RegistrateBlockstateProvider p, final String name, final ResourceLocation wall, final ResourceLocation wallFlipped) {
            return p.models().singleTexture(name, CreateBitsnBobs.asResource("block/tile_wall/template_wall_side"), "wall", wall)
                    .texture("wall_flipped", wallFlipped);
        }

        public BlockModelBuilder tileWallSideTall(final RegistrateBlockstateProvider p, final String name, final ResourceLocation wall, final ResourceLocation wallFlipped) {
            return p.models().singleTexture(name, CreateBitsnBobs.asResource("block/tile_wall/template_wall_side_tall"), "wall", wall)
                    .texture("wall_flipped", wallFlipped);
        }

        public BlockModelBuilder tileWallSideAlternate(final RegistrateBlockstateProvider p, final String name, final ResourceLocation wall, final ResourceLocation wallFlipped) {
            return p.models().singleTexture(name, CreateBitsnBobs.asResource("block/tile_wall/template_wall_side_alternate"), "wall", wall)
                    .texture("wall_flipped", wallFlipped);
        }

        public BlockModelBuilder tileWallSideTallAlternate(final RegistrateBlockstateProvider p, final String name, final ResourceLocation wall, final ResourceLocation wallFlipped) {
            return p.models().singleTexture(name, CreateBitsnBobs.asResource("block/tile_wall/template_wall_side_tall_alternate"), "wall", wall)
                    .texture("wall_flipped", wallFlipped);
        }

        private void tileWallBlockInternal(final RegistrateBlockstateProvider p, final WallBlock block, final String baseName, final ResourceLocation texture, final ResourceLocation flippedTexture) {
            tileWallBlock(p, block, tileWallPost(p, baseName + "_post", texture, flippedTexture),
                    tileWallSide(p, baseName + "_side", texture, flippedTexture),
                    tileWallSideAlternate(p, baseName + "_side_alternate", texture, flippedTexture),
                    tileWallSideTall(p, baseName + "_side_tall", texture, flippedTexture),
                    tileWallSideTallAlternate(p, baseName + "_side_tall_alternate", texture, flippedTexture));
        }

        public static final ImmutableMap<Direction, Property<WallSide>> WALL_PROPS = ImmutableMap.<Direction, Property<WallSide>>builder()
                .put(Direction.EAST, BlockStateProperties.EAST_WALL)
                .put(Direction.NORTH, BlockStateProperties.NORTH_WALL)
                .put(Direction.SOUTH, BlockStateProperties.SOUTH_WALL)
                .put(Direction.WEST, BlockStateProperties.WEST_WALL)
                .build();

        public void tileWallBlock(final RegistrateBlockstateProvider p, final WallBlock block, final ModelFile post, final ModelFile side, final ModelFile sideAlternate, final ModelFile sideTall, final ModelFile sideTallAlternate) {
            final MultiPartBlockStateBuilder builder = p.getMultipartBuilder(block)
                    .part().modelFile(post).addModel()
                    .condition(WallBlock.UP, true).end();
            WALL_PROPS.entrySet().stream()
                    .filter(e -> e.getKey().getAxis().isHorizontal())
                    .forEach(e -> {
                        tileWallSidePart(builder, side, sideAlternate, e, WallSide.LOW);
                        tileWallSidePart(builder, sideTall, sideTallAlternate, e, WallSide.TALL);
                    });
        }

        private void tileWallSidePart(final MultiPartBlockStateBuilder builder, final ModelFile model, final ModelFile modelAlternate, final Map.Entry<Direction, Property<WallSide>> entry, final WallSide height) {
            final int yRot = (((int) entry.getKey().toYRot()) + 180) % 360;
            builder.part()
                    .modelFile((yRot == 0 || yRot == 180) ? model : modelAlternate)
                    .rotationY(yRot)
                    .uvLock(true)
                    .addModel()
                    .condition(entry.getValue(), height);
        }

        public void tileWallBlock(final RegistrateBlockstateProvider p, final WallBlock block, final String name, final ResourceLocation texture, final ResourceLocation flippedTexture) {
            tileWallBlockInternal(p, block, name + "_wall", texture, flippedTexture);
        }

        @Override
        protected void generateBlockState(final DataGenContext<Block, WallBlock> ctx, final RegistrateBlockstateProvider prov,
                                          final String variantName, final BnbPaletteBlockPattern pattern, final Supplier<? extends Block> block) {
            tileWallBlock(prov, ctx.get(), pattern.createName(variantName), getTexture(variantName, pattern, 0), getFlippedTexture(variantName, pattern, 0));
        }

        @Override
        protected Iterable<TagKey<Block>> getBlockTags() {
            return List.of(BlockTags.WALLS);
        }

        @Override
        protected Iterable<TagKey<Item>> getItemTags() {
            return List.of(ItemTags.WALLS);
        }

        @Override
        protected void createRecipes(final BnbPaletteStoneTypes type, final BlockEntry<? extends Block> patternBlock,
                                     final DataGenContext<Block, ? extends Block> c, final RegistrateRecipeProvider p) {
            final RecipeCategory category = RecipeCategory.BUILDING_BLOCKS;
            p.stonecutting(DataIngredient.tag(type.materialTag), category, c, 1);
            final DataIngredient ingredient = DataIngredient.items(patternBlock.get());
            ShapedRecipeBuilder.shaped(category, c.get(), 6)
                    .pattern("XXX")
                    .pattern("XXX")
                    .define('X', ingredient.toVanilla())
                    .unlockedBy("has_" + p.safeName(ingredient), ingredient.getCriterion(p))
                    .save(p, p.safeId(c.get()));
        }
    }
}


