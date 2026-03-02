package com.kipti.bnb.registry.content.blocks.deco;

import com.cake.struts.girder_strut.StrutModelType;
import com.kipti.bnb.CreateBitsnBobs;

public class BnbStrutModels {
    public static final StrutModelType WEATHERED = new StrutModelType(CreateBitsnBobs.asResource("block/girder_strut/weathered_girder"), CreateBitsnBobs.asResource("block/weathered_iron_block"));
    public static final StrutModelType NORMAL = new StrutModelType(CreateBitsnBobs.asResource("block/girder_strut/girder"), CreateBitsnBobs.asResource("block/industrial_iron_block"));
    public static final StrutModelType WOODEN = new StrutModelType(CreateBitsnBobs.asResource("block/girder_strut/wooden_girder"), CreateBitsnBobs.asResource("block/oak_planks"), 8, 8);
    public static final StrutModelType CABLE = new StrutModelType(CreateBitsnBobs.asResource("block/girder_strut/cable"), CreateBitsnBobs.asResource("block/industrial_iron_block"), 2, 2);

    private BnbStrutModels() {
    }
}
