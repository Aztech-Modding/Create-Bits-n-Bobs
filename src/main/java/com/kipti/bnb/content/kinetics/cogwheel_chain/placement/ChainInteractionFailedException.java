package com.kipti.bnb.content.kinetics.cogwheel_chain.placement;

import com.cake.azimuth.lang.IncludeLangDefaults;
import com.cake.azimuth.lang.LangDefault;
import com.cake.azimuth.lang.LangKeyFormat;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

@LangKeyFormat(ChainInteractionFailedException.LANG_KEY_FORMAT)
@IncludeLangDefaults({
        @LangDefault(key = "cannot_revisit_node", source = ChainInteractionFailedException.class, value = "You cannot self-intersect the chain!"),
        @LangDefault(key = "out_of_bounds", source = ChainInteractionFailedException.class, value = "Cogwheel exceeds maximum bounds!"),
        @LangDefault(key = "cogwheels_cannot_touch", source = ChainInteractionFailedException.class, value = "Cogwheels must not touch each other!"),
        @LangDefault(key = "not_valid_axis_change", source = ChainInteractionFailedException.class, value = "Large cogwheels must share a tangent to change axis!"),
        @LangDefault(key = "not_flat_connection", source = ChainInteractionFailedException.class, value = "Connections of the same direction must be flat!"),
        @LangDefault(key = "no_cogwheel_connection", source = ChainInteractionFailedException.class, value = "Connections with cogwheels must be at right angles!"),
        @LangDefault(key = "no_path_to_cogwheel", source = ChainInteractionFailedException.class, value = "No valid path to cogwheel!"),
        @LangDefault(key = "config_forbids", source = ChainInteractionFailedException.class, value = "Server has disabled chain drives!"),
        @LangDefault(key = "axis_change_forbidden_by_type", source = ChainInteractionFailedException.class, value = "This chain type does not allow axis changes!"),
        @LangDefault(key = "pathfinding_failed_at_node", source = ChainInteractionFailedException.class, value = "Couldn't find valid path between two nodes! (Try inserting more nodes?)"),
        @LangDefault(key = "pathfinding_failed", source = ChainInteractionFailedException.class, value = "Couldn't find valid path across chain! (Try inserting more nodes?)"),
})
public class ChainInteractionFailedException extends Exception {

    public static final String ABORTED_PLACEMENT_PREFIX = "message.bits_n_bobs.cogwheel_chain.chain_addition_aborted.";
    public static final String LANG_KEY_FORMAT = ABORTED_PLACEMENT_PREFIX + "%s";

    public ChainInteractionFailedException(final String message) {
        super(message);
    }

    public MutableComponent getTranslatedMessage() {
        return Component.translatable("message.bits_n_bobs.cogwheel_chain.chain_addition_aborted." + getMessage());
    }

    public Component getComponent() {
        return getTranslatedMessage().withColor(0xFF_ff5d6c);
    }

}

