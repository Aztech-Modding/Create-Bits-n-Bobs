package com.kipti.bnb.registry.content.blocks.deco;

import com.cake.struts.content.CableStrutInfo;
import com.cake.struts.content.StrutModelType;
import com.kipti.bnb.CreateBitsnBobs;

public class BnbStrutDefinitions {
    public static final StrutModelType WEATHERED_MODEL = new StrutModelType(CreateBitsnBobs.asResource("block/girder_strut/weathered_girder"), CreateBitsnBobs.asResource("block/weathered_iron_block"));
    public static final StrutModelType NORMAL_MODEL = new StrutModelType(CreateBitsnBobs.asResource("block/girder_strut/girder"), CreateBitsnBobs.asResource("block/industrial_iron_block"));
    public static final StrutModelType WOODEN_MODEL = new StrutModelType(CreateBitsnBobs.asResource("block/girder_strut/wooden_girder"), CreateBitsnBobs.asResource("block/oak_planks"), 8, 8);
    public static final StrutModelType CABLE_MODEL = new StrutModelType(CreateBitsnBobs.asResource("block/girder_strut/cable"), CreateBitsnBobs.asResource("block/industrial_iron_block"), 2, 2);

    public static final CableStrutInfo CABLE_INFO = new CableStrutInfo(0.05f);

}
