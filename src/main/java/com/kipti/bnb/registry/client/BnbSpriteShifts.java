package com.kipti.bnb.registry.client;

import com.kipti.bnb.CreateBitsnBobs;
import com.simibubi.create.foundation.block.connected.AllCTTypes;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.CTSpriteShifter;
import com.simibubi.create.foundation.block.connected.CTType;
import net.createmod.catnip.render.SpriteShiftEntry;
import net.createmod.catnip.render.SpriteShifter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;

import java.util.Map;

public class BnbSpriteShifts {

    public static final SpriteShiftEntry
            CHAIN_PULLEY_COIL = get("block/chain_pulley_coil", "block/chain_pulley_coil_scroll"),
            CHAIN_ROPE = SpriteShifter.get(ResourceLocation.withDefaultNamespace("block/chain"), CreateBitsnBobs.asResource("block/chain_scroll"));

    public static final CTSpriteShiftEntry
            WEATHERED_GIRDER_POLE = vertical("weathered_girder_pole_side");

    public static final CTSpriteShiftEntry
            INDUSTRIAL_GRATING = omni("industrial_grating");

    private static Map<DyeColor, SpriteShiftEntry> getHeadlampSpriteShifts(final boolean off) {
        final Map<DyeColor, SpriteShiftEntry> map = new java.util.EnumMap<>(DyeColor.class);
        for (final DyeColor color : DyeColor.values()) {
            map.put(color, get(
                    "block/headlight/headlight_off",
                    "block/headlight/headlight" + (off ? "_off" : "_on") + "_" + color.getName()
            ));
        }
        return java.util.Collections.unmodifiableMap(map);
    }

    /**
     * Sprite shift from the off (undyed) headlamp texture to the on (undyed) headlamp texture.
     */
    public static final SpriteShiftEntry HEADLAMP_ON_UNDYED_SPRITE_SHIFT = get(
            "block/headlight/headlight_off",
            "block/headlight/headlight_on"
    );

    /**
     * Sprite shifts from the off (undyed) base texture to the on (dyed) texture for each dye color.
     */
    public static final Map<DyeColor, SpriteShiftEntry> HEADLAMP_ON_SPRITE_SHIFTS = getHeadlampSpriteShifts(false);
    /**
     * Sprite shifts from the off (undyed) base texture to the off (dyed) texture for each dye color.
     */
    public static final Map<DyeColor, SpriteShiftEntry> HEADLAMP_OFF_SPRITE_SHIFTS = getHeadlampSpriteShifts(true);

    private static CTSpriteShiftEntry omni(final String name) {
        return getCT(AllCTTypes.OMNIDIRECTIONAL, name);
    }

    private static CTSpriteShiftEntry vertical(final String name) {
        return getCT(AllCTTypes.VERTICAL, name);
    }

    private static CTSpriteShiftEntry getCT(final CTType type, final String blockTextureName, final String connectedTextureName) {
        return CTSpriteShifter.getCT(type, CreateBitsnBobs.asResource("block/" + blockTextureName),
                CreateBitsnBobs.asResource("block/" + connectedTextureName + "_connected"));
    }

    private static CTSpriteShiftEntry getCT(final CTType type, final String blockTextureName) {
        return getCT(type, blockTextureName, blockTextureName);
    }

    private static SpriteShiftEntry get(final String originalLocation, final String targetLocation) {
        return SpriteShifter.get(CreateBitsnBobs.asResource(originalLocation), CreateBitsnBobs.asResource(targetLocation));
    }

    public static void register() {
    }

}

