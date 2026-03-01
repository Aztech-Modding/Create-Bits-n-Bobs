package com.kipti.bnb.content.decoration.girder_strut;

import com.kipti.bnb.CreateBitsnBobs;
import com.kipti.bnb.registry.client.BnbPartialModels;

public class BnbStrutModels {
    public static final StrutModelType WEATHERED = new StrutModelType(BnbPartialModels.WEATHERED_GIRDER_STRUT_SEGMENT, CreateBitsnBobs.asResource("block/weathered_iron_block"));
    public static final StrutModelType NORMAL = new StrutModelType(BnbPartialModels.GIRDER_STRUT_SEGMENT, CreateBitsnBobs.asResource("block/industrial_iron_block"));
    public static final StrutModelType WOODEN = new StrutModelType(BnbPartialModels.WOODEN_GIRDER_STRUT_SEGMENT, CreateBitsnBobs.asResource("block/oak_planks"), 8, 8);
    public static final StrutModelType CABLE = new StrutModelType(BnbPartialModels.CABLE_STRUT_SEGMENT, CreateBitsnBobs.asResource("block/industrial_iron_block"), 2, 2);
}
