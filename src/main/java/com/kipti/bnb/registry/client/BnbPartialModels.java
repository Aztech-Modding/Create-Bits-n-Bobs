package com.kipti.bnb.registry.client;

import com.kipti.bnb.CreateBitsnBobs;
import com.simibubi.create.Create;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.lang.Lang;
import net.minecraft.core.Direction;

import java.util.EnumMap;
import java.util.Map;

public class BnbPartialModels {


//    public static final PartialModel CHAIR_LEFT_ARM = block("chair/chair_left_armrest");
//    public static final PartialModel CHAIR_RIGHT_ARM = block("chair/chair_right_armrest");

    public static final PartialModel
            HEADLAMP_OFF = block("headlamp/headlight"),
            HEADLAMP_ON = block("headlamp/headlight_on"),
            HEADLAMP_INSTANCE_BASE = block("headlamp/instance/headlight_base"),
            HEADLAMP_INSTANCE_OFF = block("headlamp/instance/headlight_off"),
            HEADLAMP_INSTANCE_ON = block("headlamp/instance/headlight_on"),

    SMALL_SPROCKET_COGWHEEL_BLOCK = block("sprocket/small_cogwheel"),
            LARGE_SPROCKET_COGWHEEL_BLOCK = block("sprocket/large_cogwheel"),
            SMALL_FLANGED_COGWHEEL_BLOCK = block("flanged_gear/small_cogwheel"),
            LARGE_FLANGED_COGWHEEL_BLOCK = block("flanged_gear/large_cogwheel"),
            ENCASED_SPROCKET_COGWHEEL_BLOCK = block("sprocket/encased_cogwheel"),
            ENCASED_LARGE_SPROCKET_COGWHEEL_BLOCK = block("sprocket/encased_large_cogwheel"),
            ENCASED_FLANGED_COGWHEEL_BLOCK = block("flanged_gear/encased_cogwheel"),
            ENCASED_LARGE_FLANGED_COGWHEEL_BLOCK = block("flanged_gear/encased_large_cogwheel"),


    CHAIN_ROPE_COIL = block("chain_pulley/chain_coil"), CHAIN_ROPE_HALF = block("chain_pulley/chain_rope_half"),
            CHAIN_ROPE_HALF_MAGNET = block("chain_pulley/chain_rope_half_magnet"),
            CHAIN_ROPE = block("chain_pulley/chain_rope"),
            CHAIN_PULLEY_MAGNET_NO_CHAIN = block("chain_pulley/chain_pulley_magnet_no_chain"),
            CHAIN_PULLEY_MAGNET_CHAIN = block("chain_pulley/chain_pulley_magnet_chain"),
            CHAIN_PULLEY_MAGNET_CHAIN_HALF = block("chain_pulley/chain_pulley_magnet_chain_half"),

    ROPE_PULLEY_JEI = createBlock("rope_pulley/item"),

    LARGE_STONE_COG_SHAFTLESS = block("large_stone_cog_shaftless"),

    THROTTLE_LEVER_HANDLE = block("throttle_lever/handle"),

    COGWHEEL_CHAIN_CARRIAGE_SHOE_ARM = block("cogwheel_chain_carriage/shoe_arm"),
    COGWHEEL_CHAIN_CARRIAGE_SHOE = block("cogwheel_chain_carriage/shoe");


    public static final Map<Direction, PartialModel> WEATHERED_METAL_GIRDER_BRACKETS = new EnumMap<>(Direction.class);

    static {
        for (final Direction d : Iterate.horizontalDirections) {
            WEATHERED_METAL_GIRDER_BRACKETS.put(d, block("weathered_metal_girder/bracket_" + Lang.asId(d.name())));
        }
    }

    private static PartialModel block(final String path) {
        return PartialModel.of(CreateBitsnBobs.asResource("block/" + path));
    }

    private static PartialModel createBlock(final String path) {
        return PartialModel.of(Create.asResource("block/" + path));
    }

    public static void register() {
    }
}

