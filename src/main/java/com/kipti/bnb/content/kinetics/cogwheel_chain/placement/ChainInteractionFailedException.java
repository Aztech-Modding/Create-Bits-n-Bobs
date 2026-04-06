package com.kipti.bnb.content.kinetics.cogwheel_chain.placement;

import com.cake.azimuth.lang.IncludeLangDefaults;
import com.cake.azimuth.lang.LangDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

@IncludeLangDefaults({
        @LangDefault(key = "cannot_revisit_node", format = ChainInteractionFailedException.LANG_KEY_FORMAT, value = "You cannot self-intersect the chain!"),
        @LangDefault(key = "out_of_bounds", format = ChainInteractionFailedException.LANG_KEY_FORMAT, value = "Cogwheel exceeds maximum bounds!"),
        @LangDefault(key = "cogwheels_cannot_touch", format = ChainInteractionFailedException.LANG_KEY_FORMAT, value = "Cogwheels must not touch each other!"),
        @LangDefault(key = "not_valid_axis_change", format = ChainInteractionFailedException.LANG_KEY_FORMAT, value = "Large cogwheels must share a tangent to change axis!"),
        @LangDefault(key = "not_flat_connection", format = ChainInteractionFailedException.LANG_KEY_FORMAT, value = "Connections of the same direction must be flat!"),
        @LangDefault(key = "no_cogwheel_connection", format = ChainInteractionFailedException.LANG_KEY_FORMAT, value = "Connections with cogwheels must be at right angles!"),
        @LangDefault(key = "no_path_to_cogwheel", format = ChainInteractionFailedException.LANG_KEY_FORMAT, value = "No valid path to cogwheel!"),
        @LangDefault(key = "config_forbids", format = ChainInteractionFailedException.LANG_KEY_FORMAT, value = "Server has disabled chain drives!"),
        @LangDefault(key = "axis_change_forbidden_by_type", format = ChainInteractionFailedException.LANG_KEY_FORMAT, value = "This chain type does not allow axis changes!"),
        @LangDefault(key = "pathfinding_failed_at_node", format = ChainInteractionFailedException.LANG_KEY_FORMAT, value = "Couldn't find valid path between two nodes! (Try inserting more nodes?)"),
        @LangDefault(key = "pathfinding_failed", format = ChainInteractionFailedException.LANG_KEY_FORMAT, value = "Couldn't find valid path across chain! (Try inserting more nodes?)"),
})
public class ChainInteractionFailedException extends Exception {

    public static final String ABORTED_PLACEMENT_PREFIX = "message.bits_n_bobs.cogwheel_chain.chain_addition_aborted.";
    public static final String LANG_KEY_FORMAT = ABORTED_PLACEMENT_PREFIX + "%s";

    public ChainInteractionFailedException(final String message) {
        super(message);
    }

    public MutableComponent getTranslatedMessage() {
        return Component.translatable(ABORTED_PLACEMENT_PREFIX + this.getMessage());
    }

    public Component getComponent() {
        return this.getTranslatedMessage().withColor(0xFF_ff5d6c);
    }

}

