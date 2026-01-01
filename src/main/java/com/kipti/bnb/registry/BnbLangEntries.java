package com.kipti.bnb.registry;

import com.kipti.bnb.CreateBitsnBobs;
import com.kipti.bnb.content.cogwheel_chain.graph.ChainInteractionFailedException;

public class BnbLangEntries {

    public static void register() {
        CreateBitsnBobs.REGISTRATE.addRawLang("tab." + CreateBitsnBobs.MOD_ID + ".base", CreateBitsnBobs.TAB_NAME);
        CreateBitsnBobs.REGISTRATE.addRawLang("tab." + CreateBitsnBobs.MOD_ID + ".deco", CreateBitsnBobs.DECO_NAME);
        CreateBitsnBobs.REGISTRATE.addRawLang("message.bits_n_bobs.girder_strut.missing_anchors", "You need %s more Girder Struts");

        ChainInteractionFailedException.addTranslationLangs(CreateBitsnBobs.REGISTRATE,
                "cannot_revisit_node", "You cannot self-intersect the chain!",
                "out_of_bounds", "Cogwheel exceeds maximum bounds!",
                "cogwheels_cannot_touch", "Cogwheels must not touch each other!",
                "not_valid_axis_change", "Large cogwheels must share a tangent to change axis!",
                "not_flat_connection", "Connections of the same direction must be flat!",
                "no_cogwheel_connection", "Connections with cogwheels must be at right angles!",
                "no_path_to_cogwheel", "No valid path to cogwheel!",
                "config_forbids", "Server has disabled chain drives!",

                //Pathfinding errors, should not happen, since the validation in chain placement, but in case
                "pathfinding_failed_at_node", "Couldn't find valid path between two nodes! (Try inserting more nodes?)",
                "pathfinding_failed", "Couldn't find valid path across chain! (Try inserting more nodes?)"
        );

        //Reused tooltips that cant be shared by changing keys
        final String commonRightClickWithEmptyHand = "When R-Clicked with empty hand";
        final String commonRightClickTurnOnBehaviour = "Toggles if the lightbulb should be _always on_, irregardless of redstone power.";

        final String[] entries = {
                "generator.bits_n_bobs.ponderous_planes", "Ponderflat",
                "tooltip.bits_n_bobs.chain_drive_placing_hint", "Placing chain drive, create a complete loop to finish.",
                //Tooltips
                "block.bits_n_bobs.headlamp.tooltip.summary", "Can be dyed and placed _multiple times in same block_. Useful for trains or fancy signage too!",
                "block.bits_n_bobs.headlamp.tooltip.condition1", commonRightClickWithEmptyHand,
                "block.bits_n_bobs.headlamp.tooltip.behaviour1", commonRightClickTurnOnBehaviour,

                "block.bits_n_bobs.brass_lamp.tooltip.summary", "_It's not just a lightbulb_, this one's got a fancy brass casing.",
                "block.bits_n_bobs.brass_lamp.tooltip.condition1", commonRightClickWithEmptyHand,
                "block.bits_n_bobs.brass_lamp.tooltip.behaviour1", commonRightClickTurnOnBehaviour,

                "block.bits_n_bobs.lightbulb.tooltip.summary", "_It's just a lightbulb_, what do you expect.",
                "block.bits_n_bobs.lightbulb.tooltip.condition1", "When R-Clicked with Wrench",
                "block.bits_n_bobs.lightbulb.tooltip.behaviour1", "Toggles the lightbulb _cage variant_.",
                "block.bits_n_bobs.lightbulb.tooltip.condition2", commonRightClickWithEmptyHand,
                "block.bits_n_bobs.lightbulb.tooltip.behaviour2", commonRightClickTurnOnBehaviour,

                "block.bits_n_bobs.girder_strut.tooltip.summary", "A type of girder used to span a distance _between two anchor points_.",

                "block.bits_n_bobs.chair.tooltip.summary", "Sit yourself down and enjoy the ride! Will anchor a player onto a moving _contraption_. Even _fancier than a seat_ for static furniture too! Comes in a variety of colours. Will form _corners_ and _flat backs_ when placed against other chairs and blocks accordingly",
                "block.bits_n_bobs.chair.tooltip.condition1", "Right click on Chair",
                "block.bits_n_bobs.chair.tooltip.behaviour1", "Sits the player on the _Chair_. Press L-shift to leave the _Chair_.",

                //Flywheel Bearing Tooltip
                "tooltip.bits_n_bobs.flywheel_bearing.flywheel_stats", "Flywheel Stats:",
                "tooltip.bits_n_bobs.flywheel_bearing.angular_mass", "Angular Mass:",
                "tooltip.bits_n_bobs.flywheel_bearing.stored_stress", "Stored Stress:",
                "tooltip.bits_n_bobs.flywheel_bearing.kinetic_transfer", "Kinetic Transfer:",

                "tooltip.bits_n_bobs.flywheel_bearing.angular_mass.none", "(none)",
                "tooltip.bits_n_bobs.flywheel_bearing.angular_mass.super_light", "(super light)",
                "tooltip.bits_n_bobs.flywheel_bearing.angular_mass.light", "(light)",
                "tooltip.bits_n_bobs.flywheel_bearing.angular_mass.medium", "(medium)",
                "tooltip.bits_n_bobs.flywheel_bearing.angular_mass.heavy", "(heavy)",
                "tooltip.bits_n_bobs.flywheel_bearing.angular_mass.super_heavy", "(super heavy)",
                "tooltip.bits_n_bobs.flywheel_bearing.angular_mass.absurdly_heavy", "(absurdly heavy)",

                "tooltip.bits_n_bobs.flywheel_bearing.empty", "(empty)",
                "tooltip.bits_n_bobs.flywheel_bearing.full", "(full)",
        };

        for (int i = 0; i < entries.length; i += 2) {
            CreateBitsnBobs.REGISTRATE.addRawLang(entries[i], entries[i + 1]);
        }
    }

}
