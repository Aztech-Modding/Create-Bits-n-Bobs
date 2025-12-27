package com.kipti.bnb;

import com.kipti.bnb.network.BnbPackets;
import com.kipti.bnb.registry.*;
import com.mojang.logging.LogUtils;
import com.simibubi.create.Create;
import com.simibubi.create.api.behaviour.display.DisplayTarget;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;
import net.createmod.catnip.lang.FontHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(CreateBitsnBobs.MOD_ID)
public class CreateBitsnBobs {

    public static final String MOD_ID = "bits_n_bobs";
    public static final String NAME = "Create: Bits 'n' Bobs";
    public static final String TAB_NAME = "Bits 'n' Bobs";
    public static final String DECO_NAME = "Bits 'n' Bobs' Building Blocks";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MOD_ID)
            .defaultCreativeTab((ResourceKey<CreativeModeTab>) null)
            .setTooltipModifierFactory(item ->
                    new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)
                            .andThen(TooltipModifier.mapNull(KineticStats.create(item)))
            );

    public CreateBitsnBobs(final FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        modEventBus.addListener(CreateBitsnBobsData::gatherData);

        REGISTRATE.registerEventListeners(modEventBus);

        REGISTRATE.setCreativeTab(BnbCreativeTabs.BASE_CREATIVE_TAB);

        BnbItems.register();
        BnbBlocks.register();
        BnbEntityTypes.register();
        BnbCreativeTabs.register(modEventBus);
        BnbBlockEntities.register();
        BnbTags.register();
        BnbPackets.registerPackets();
        BnbDecoBlocks.register();

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> CreateBitsnBobsClient.setup(context));
        BnbCreateStresses.register();

        BnbLangEntries.register();
        BnbTags.registerDataGenerators();

        modEventBus.addListener(BnbDataConditions::registerSerializers);
        modEventBus.addListener(BnbRegisterEvent::onRegisterEvent);

        BnbConfigs.register(context, context.getContainer());
    }

    public static ResourceLocation asResource(final String s) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, s);
    }

}


