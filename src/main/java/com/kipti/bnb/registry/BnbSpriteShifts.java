package com.kipti.bnb.registry;

import com.kipti.bnb.CreateBitsnBobs;
import com.simibubi.create.foundation.block.connected.AllCTTypes;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.CTSpriteShifter;
import com.simibubi.create.foundation.block.connected.CTType;
import net.createmod.catnip.render.SpriteShiftEntry;
import net.createmod.catnip.render.SpriteShifter;
import net.minecraft.resources.ResourceLocation;

public class BnbSpriteShifts {

    public static final SpriteShiftEntry
            CHAIN_PULLEY_COIL = get("block/chain_pulley_coil", "block/chain_pulley_coil_scroll"),
            CHAIN_ROPE = SpriteShifter.get(ResourceLocation.withDefaultNamespace("block/chain"), CreateBitsnBobs.asResource("block/chain_scroll"));

    public static final CTSpriteShiftEntry
            WEATHERED_GIRDER_POLE = vertical("weathered_girder_pole_side");

    public static final CTSpriteShiftEntry
            INDUSTRIAL_GRATING = omni("industrial_grating");

    private static CTSpriteShiftEntry omni(String name) {
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
