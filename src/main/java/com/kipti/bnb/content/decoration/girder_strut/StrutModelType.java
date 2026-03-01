package com.kipti.bnb.content.decoration.girder_strut;

import com.kipti.bnb.CreateBitsnBobs;
import com.kipti.bnb.registry.client.BnbPartialModels;
import com.simibubi.create.Create;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.resources.ResourceLocation;

public record StrutModelType(PartialModel segmentPartial, ResourceLocation capTexture, int shapeSizeXPixels, int shapeSizeYPixels) {

    public StrutModelType(final PartialModel segmentPartial, final ResourceLocation capTexture) {
        this(segmentPartial, capTexture, 8, 12);
    }

    public PartialModel getPartialModel() {
        return segmentPartial;
    }

    public ResourceLocation getCapTexture() {
        return capTexture;
    }
}

