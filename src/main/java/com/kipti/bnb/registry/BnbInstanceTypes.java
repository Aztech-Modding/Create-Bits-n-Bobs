package com.kipti.bnb.registry;

import com.kipti.bnb.CreateBitsnBobs;
import com.kipti.bnb.content.decoration.light.headlamp.rendering.pipeline.visual.ShiftTransformedInstance;
import dev.engine_room.flywheel.api.instance.InstanceType;
import dev.engine_room.flywheel.api.layout.FloatRepr;
import dev.engine_room.flywheel.api.layout.IntegerRepr;
import dev.engine_room.flywheel.api.layout.LayoutBuilder;
import dev.engine_room.flywheel.lib.instance.SimpleInstanceType;
import dev.engine_room.flywheel.lib.util.ExtraMemoryOps;
import org.lwjgl.system.MemoryUtil;

public class BnbInstanceTypes {

    public static final InstanceType<ShiftTransformedInstance> SHIFT_TRANSFORMED = SimpleInstanceType.builder(ShiftTransformedInstance::new)
            .cullShader(CreateBitsnBobs.asResource("instance/cull/shift_transformed.glsl"))
            .vertexShader(CreateBitsnBobs.asResource("instance/shift_transformed.vert"))
            .layout(LayoutBuilder.create()
                    .matrix("pose", FloatRepr.FLOAT, 4)
                    .vector("color", FloatRepr.NORMALIZED_UNSIGNED_BYTE, 4)
                    .vector("light", IntegerRepr.SHORT, 2)
                    .vector("overlay", IntegerRepr.SHORT, 2)
                    .vector("diff", FloatRepr.FLOAT, 2)
                    .build())
            .writer((ptr, instance) -> {
                ExtraMemoryOps.putMatrix4f(ptr, instance.pose);
                MemoryUtil.memPutByte(ptr + 64, instance.red);
                MemoryUtil.memPutByte(ptr + 65, instance.green);
                MemoryUtil.memPutByte(ptr + 66, instance.blue);
                MemoryUtil.memPutByte(ptr + 67, instance.alpha);
                ExtraMemoryOps.put2x16(ptr + 68, instance.light);
                ExtraMemoryOps.put2x16(ptr + 72, instance.overlay);
                MemoryUtil.memPutFloat(ptr + 76, instance.diffU);
                MemoryUtil.memPutFloat(ptr + 80, instance.diffV);
            })
            .build();

    public static void init() {
    }

}
