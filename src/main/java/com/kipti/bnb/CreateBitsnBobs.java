package com.kipti.bnb;

import com.kipti.bnb.compat.computercraft.peripherals.HeadlampPeripheral;
import com.kipti.bnb.content.decoration.light.headlamp.HeadlampBlockEntity;
import com.kipti.bnb.content.kinetics.cogwheel_chain.types.BnbCogwheelChainTypes;
import com.kipti.bnb.network.BnbPackets;
import com.kipti.bnb.registry.*;
import com.mojang.logging.LogUtils;
import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.PeripheralCapability;
import net.createmod.catnip.config.ui.BaseConfigScreen;
import net.createmod.catnip.lang.FontHelper;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.slf4j.Logger;

import java.util.function.Supplier;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
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
        BnbBlocks.register();
        BnbEntityTypes.register();
        BnbBlockEntities.register();
        BnbTags.register();
        BnbPackets.register();
        ;
        BnbDecoBlocks.register();

        BnbCreateStresses.registerRedirects();

        BnbLangEntries.register();
        BnbTags.registerDataGenerators();

        modEventBus.addListener(CreateBitsnBobs::commonSetup);
        modEventBus.addListener(this::registerCapabilities);

        BnbConfigs.register(modLoadingContext, modContainer);
    }

    private static void commonSetup(final FMLCommonSetupEvent event) {
    }

    public static ResourceLocation asResource(final String s) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, s);
    }


    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        addPeripheral(event, BnbBlockEntities.HEADLAMP.get(), (be, dir) -> new HeadlampPeripheral(be));
    }

    private static <T extends BlockEntity> void addPeripheral(RegisterCapabilitiesEvent event, BlockEntityType<T> type, ICapabilityProvider<T, Direction, IPeripheral> factory) {
        event.registerBlockEntity(PeripheralCapability.get(), type, factory);
    }


}


