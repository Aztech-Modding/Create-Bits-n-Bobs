package com.kipti.bnb.registry.content;

import com.cake.azimuth.advancement.AzimuthAdvancement;
import com.cake.azimuth.advancement.AzimuthAdvancementProvider;
import com.kipti.bnb.CreateBitsnBobs;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class BnbAdvancements {

    public static final AzimuthAdvancementProvider HELPER =
            new AzimuthAdvancementProvider(CreateBitsnBobs.MOD_ID, "Bits 'n' Bobs Advancements");

    public static final AzimuthAdvancement COOKIE_DOUGH = HELPER.create("cookie_dough", b -> b
            .icon(BnbItems.COOKIE_DOUGH)
            .title("But It Tastes So Good...")
            .description("Eat cookie dough")
            .after(() -> AllAdvancements.MIXER)
    );

    public static void register() {
        HELPER.register();
    }

    public static void provideLang(final BiConsumer<String, String> consumer) {
        HELPER.provideLang(consumer);
    }

    public static DataProvider dataProvider(final PackOutput output, final CompletableFuture<HolderLookup.Provider> registries) {
        return HELPER.dataProvider(output, registries);
    }

}
