package com.kipti.bnb.registry.datagen;

import com.cake.azimuth.foundation.lang.AzimuthGeneratedLangEntry;
import com.cake.azimuth.lang.LangDefaultCollector;
import com.kipti.bnb.CreateBitsnBobs;

public class BnbLangEntries {

    public static void register() {
        LangDefaultCollector.collectAll();
        AzimuthGeneratedLangEntry.provideLang(CreateBitsnBobs.MOD_ID, CreateBitsnBobs.REGISTRATE::addRawLang);
    }

}

