package com.kipti.bnb.content.girder_strut;

import com.kipti.bnb.CreateBitsnBobs;
import com.kipti.bnb.registry.BnbPartialModels;
import com.simibubi.create.Create;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.resources.ResourceLocation;

public enum StrutModelType {
    WEATHERED(BnbPartialModels.WEATHERED_GIRDER_STRUT_SEGMENT, CreateBitsnBobs.asResource("block/weathered_industrial_iron_block")),
    NORMAL(BnbPartialModels.GIRDER_STRUT_SEGMENT, Create.asResource("block/industrial_iron_block"));

    private final PartialModel segmentPartial;
    private final ResourceLocation capTexture;

    StrutModelType(PartialModel segmentPartial, ResourceLocation capTexture) {
        this.segmentPartial = segmentPartial;
        this.capTexture = capTexture;
    }

    public PartialModel getPartialModel() {
        return segmentPartial;
    }

    public ResourceLocation getCapTexture() {
        return capTexture;
    }

}
