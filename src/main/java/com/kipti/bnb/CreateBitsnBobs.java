package com.kipti.bnb;

import com.cake.azimuth.lang.IncludeLangDefaults;
import com.cake.azimuth.lang.LangDefault;
import com.cake.azimuth.registration.BehaviourApplicators;
import com.cake.azimuth.registration.VisualWrapperInterest;
import com.kipti.bnb.content.kinetics.cogwheel_chain.types.BnbCogwheelChainTypes;
import com.kipti.bnb.network.BnbPackets;
import com.kipti.bnb.registry.azimuth.BnbBehaviourApplicators;
import com.kipti.bnb.registry.compat.BnbCreateStresses;
import com.kipti.bnb.registry.content.*;
import com.kipti.bnb.registry.core.BnbConfigs;
import com.kipti.bnb.registry.core.BnbDataComponents;
import com.kipti.bnb.registry.core.BnbTags;
import com.kipti.bnb.registry.datagen.BnbCreativeTabs;
import com.kipti.bnb.registry.datagen.BnbDataConditions;
import com.kipti.bnb.registry.datagen.BnbLangEntries;
import com.mojang.logging.LogUtils;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;
import net.createmod.catnip.lang.FontHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(CreateBitsnBobs.MOD_ID)
@IncludeLangDefaults({
        @LangDefault(key = "tab.bits_n_bobs.base", value = CreateBitsnBobs.TAB_NAME),
        @LangDefault(key = "tab.bits_n_bobs.deco", value = CreateBitsnBobs.DECO_NAME),
})
public class CreateBitsnBobs {

    public static final String MOD_ID = "bits_n_bobs";
    public static final String NAME = "Create: Bits 'n' Bobs";
    public static final String TAB_NAME = "Bits 'n' Bobs";
    public static final String DECO_NAME = "Bits 'n' Bobs' Block Palettes";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MOD_ID)
            .defaultCreativeTab((ResourceKey<CreativeModeTab>) null)
            .setTooltipModifierFactory(item ->
                                               new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)
                                                       .andThen(TooltipModifier.mapNull(KineticStats.create(item)))
            );

    public CreateBitsnBobs(final IEventBus modEventBus, final ModContainer modContainer) {
        modEventBus.addListener(CreateBitsnBobsData::gatherData);
        final ModLoadingContext modLoadingContext = ModLoadingContext.get();

        REGISTRATE.registerEventListeners(modEventBus);

        REGISTRATE.setCreativeTab(BnbCreativeTabs.BASE_CREATIVE_TAB);

        BnbCreativeTabs.register(modEventBus);
        BnbDataComponents.register(modEventBus);
        BnbCogwheelChainTypes.register(modEventBus);
        BnbDataConditions.register(modEventBus);

        BnbItems.register();
        BnbAdvancements.register();
        BnbBlocksBootstrap.register();
        BnbEntityTypes.register();
        BnbBlockEntities.register();
        BnbTags.register();
        BnbPackets.register();

        BnbCreateStresses.registerRedirects();

        BnbLangEntries.register();
        BnbTags.registerDataGenerators();

        modEventBus.addListener(CreateBitsnBobs::commonSetup);

        BnbConfigs.register(modLoadingContext, modContainer);

        BnbBehaviourApplicators.register();
        modEventBus.addListener(CreateBitsnBobs::onRegister);
    }

    private static void onRegister(final RegisterEvent event) {
        BnbContraptionTypes.register();
    }

    private static void commonSetup(final FMLCommonSetupEvent event) {
        BehaviourApplicators.resolveRegisteredTypes();//TODO: CORRECT FUCKING LCOATION
        VisualWrapperInterest.resolve();
    }

    public static ResourceLocation asResource(final String s) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, s);
    }

}


