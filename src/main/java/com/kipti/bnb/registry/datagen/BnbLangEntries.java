package com.kipti.bnb.registry.datagen;

import com.cake.azimuth.goggle.component.GoggleLangRegistry;
import com.cake.azimuth.lang.LangDefaultCollector;
import com.kipti.bnb.CreateBitsnBobs;

public class BnbLangEntries {

    public static void register() {
        LangDefaultCollector.collectAll();
        GoggleLangRegistry.provideLang(CreateBitsnBobs.MOD_ID, CreateBitsnBobs.REGISTRATE::addRawLang);
    }

}

