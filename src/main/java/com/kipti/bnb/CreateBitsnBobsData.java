package com.kipti.bnb;

import com.kipti.bnb.foundation.ponder.BnbPonderPlugin;
import com.tterrag.registrate.providers.ProviderType;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class CreateBitsnBobsData {

    public static void gatherData(final GatherDataEvent event) {
        final DataGenerator generator = event.getGenerator();
        final PackOutput output = generator.getPackOutput();
        final CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        CreateBitsnBobs.REGISTRATE.addDataGenerator(ProviderType.LANG, provider -> {
            final BiConsumer<String, String> langConsumer = provider::add;
            // Register this since FMLClientSetupEvent does not run during datagen
            PonderIndex.addPlugin(new BnbPonderPlugin());
            PonderIndex.getLangAccess().provideLang(CreateBitsnBobs.MOD_ID, langConsumer);
        });
    }

}

