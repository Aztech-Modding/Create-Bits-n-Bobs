package com.kipti.bnb.registry.core;

import dev.engine_room.flywheel.api.material.DepthTest;
import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.api.material.Transparency;
import dev.engine_room.flywheel.lib.material.SimpleMaterial;

public class BnbMaterials {

    public static final Material HEADLAMP_MATERIAL = SimpleMaterial.builder()
            .transparency(Transparency.TRANSLUCENT)
            .mipmap(false)
            .blur(false)
            .polygonOffset(true) // Ensure it renders on top of the block
            .depthTest(DepthTest.LEQUAL)
            .build();
    public static final Material HEADLAMP_NO_DIFFUSE_MATERIAL = SimpleMaterial.builder()
            .transparency(Transparency.TRANSLUCENT)
            .mipmap(false)
            .blur(false)
            .polygonOffset(true) // Ensure it renders on top of the block
            .diffuse(false)
            .depthTest(DepthTest.LEQUAL)
            .build();

}

