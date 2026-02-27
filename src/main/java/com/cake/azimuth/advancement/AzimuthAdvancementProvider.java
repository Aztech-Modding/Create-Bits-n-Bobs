package com.cake.azimuth.advancement;

import com.google.common.collect.Sets;
import com.simibubi.create.foundation.advancement.AdvancementBehaviour;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.PackOutput.PathProvider;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

/**
 * Minimal helper for Create-compatible addon advancements.
 * <p>
 * Intended usage pattern in your addon class:
 * <pre>
 * public class MyAdvancements {
 *     public static final AzimuthAdvancementProvider HELPER =
 *         new AzimuthAdvancementProvider(MOD_ID, "My Mod Advancements");
 *
 *     public static final AzimuthAdvancement MY_ADVANCEMENT = HELPER.create("my_advancement", b -> b
 *         .icon(MyItems.MY_ITEM)
 *         .title("My Title")
 *         .description("My Description")
 *     );
 *
 *     public static void register() {
 *         HELPER.register();
 *     }
 *
 *     public static void provideLang(BiConsumer<String, String> consumer) {
 *         HELPER.provideLang(consumer);
 *     }
 *
 *     public static DataProvider dataProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
 *         return HELPER.dataProvider(output, registries);
 *     }
 * }
 * </pre>
 * <p>
 * Required lifecycle calls:
 * <ul>
 *     <li>Common init: call {@link #register()} once (usually from your mod constructor) to make intent explicit.</li>
 *     <li>Datagen lang: call {@link #provideLang(BiConsumer)}.</li>
 *     <li>Datagen advancements: add provider from {@link #dataProvider(PackOutput, CompletableFuture)}.</li>
 * </ul>
 * For usage, see also {@link AzimuthAdvancementBehaviour}, the compliment of {@link AdvancementBehaviour}.
 */
public class AzimuthAdvancementProvider {

    private final String modId;
    private final String name;
    private final List<AzimuthAdvancement> entries = new ArrayList<>();

    public AzimuthAdvancementProvider(final String modId, final String name) {
        this.modId = modId;
        this.name = name;
    }

    public final AzimuthAdvancement create(final String id, final UnaryOperator<AzimuthAdvancement.Builder> builder) {
        return new AzimuthAdvancement(modId, id, builder, entries::add);
    }

    /**
     * Symmetry lifecycle call for addon bootstrapping.
     * <p>
     * No explicit runtime action is required here because triggers are queued when advancements are created,
     * but calling this in your mod init keeps addon registration flow obvious and consistent.
     */
    public final void register() {
    }

    public final void provideLang(final BiConsumer<String, String> consumer) {
        for (final AzimuthAdvancement advancement : entries) {
            advancement.provideLang(consumer);
        }
    }

    /**
     * Datagen provider for advancement jsons.
     *
     * @param output     datagen output
     * @param registries lookup provider from GatherDataEvent
     */
    public final DataProvider dataProvider(final PackOutput output, final CompletableFuture<HolderLookup.Provider> registries) {
        return new Provider(output, registries);
    }

    private class Provider implements DataProvider {

        private final PackOutput output;
        private final CompletableFuture<HolderLookup.Provider> registries;

        private Provider(final PackOutput output, final CompletableFuture<HolderLookup.Provider> registries) {
            this.output = output;
            this.registries = registries;
        }

        @Override
        public CompletableFuture<?> run(final CachedOutput cache) {
            return registries.thenCompose(provider -> {
                final PathProvider pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "advancement");
                final List<CompletableFuture<?>> futures = new ArrayList<>();

                final Set<ResourceLocation> set = Sets.newHashSet();
                final Consumer<AdvancementHolder> consumer = advancement -> {
                    final ResourceLocation id = advancement.id();
                    if (!set.add(id)) {
                        throw new IllegalStateException("Duplicate advancement " + id);
                    }
                    final Path path = pathProvider.json(id);
                    futures.add(DataProvider.saveStable(cache, provider, Advancement.CODEC, advancement.value(), path));
                };

                for (final AzimuthAdvancement advancement : entries) {
                    advancement.save(consumer);
                }

                return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
            });
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
