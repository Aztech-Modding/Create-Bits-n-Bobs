package com.kipti.bnb.mixin;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Map;
import java.util.UUID;

/**
 * Backports Create's seat-mapping snapshot fix so contraption passenger updates
 * cannot encode against a live map that is still being mutated.
 */
@Mixin(AbstractContraptionEntity.class)
public class AbstractContraptionEntityMixin {

    @ModifyArg(
            method = "addSittingPassenger",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/contraptions/sync/ContraptionSeatMappingPacket;<init>(ILjava/util/Map;)V"
            ),
            index = 1
    )
    private Map<UUID, Integer> copySeatMappingForMountPacket(final Map<UUID, Integer> mapping) {
        return Map.copyOf(mapping);
    }

    @ModifyArg(
            method = "removePassenger",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/contraptions/sync/ContraptionSeatMappingPacket;<init>(ILjava/util/Map;I)V"
            ),
            index = 1
    )
    private Map<UUID, Integer> copySeatMappingForDismountPacket(final Map<UUID, Integer> mapping) {
        return Map.copyOf(mapping);
    }

}
