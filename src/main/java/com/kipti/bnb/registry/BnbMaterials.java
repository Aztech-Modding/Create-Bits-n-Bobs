package com.kipti.bnb.registry;

import dev.engine_room.flywheel.api.material.DepthTest;
import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.api.material.Transparency;
import dev.engine_room.flywheel.lib.material.SimpleMaterial;

public class BnbMaterials {

    public static final Material HEADLAMP_MATERIAL = SimpleMaterial.builder()
            .transparency(Transparency.TRANSLUCENT)
            .mipmap(false)
            .blur(false)
            .depthTest(DepthTest.LEQUAL)
            .build();

}
