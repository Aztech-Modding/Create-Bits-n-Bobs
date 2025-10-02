package com.kipti.bnb.content.girder_strut.cap;

import net.createmod.catnip.outliner.Outliner;
import net.createmod.catnip.theme.Color;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class GirderCapDebugOutlines {

    private GirderCapDebugOutlines() {
    }

    private static final Queue<Line> QUEUE = new ConcurrentLinkedQueue<>();

    static void queueLine(String key, Vector3f from, Vector3f to, Color color, float width) {
        if (key == null || from == null || to == null || color == null) {
            return;
        }
        QUEUE.add(new Line(key, toVec3(from), toVec3(to), color, width));
    }

    public static void flush() {
        if (QUEUE.isEmpty()) {
            return;
        }
        Outliner outliner = Outliner.getInstance();
        Line line;
        while ((line = QUEUE.poll()) != null) {
            outliner
                .showLine(line.key(), line.start(), line.end())
                .lineWidth(line.width())
                .colored(line.color());
        }
    }

    private static Vec3 toVec3(Vector3f vector) {
        return new Vec3(vector.x, vector.y, vector.z);
    }

    private record Line(String key, Vec3 start, Vec3 end, Color color, float width) {

        private Line {
            Objects.requireNonNull(key, "key");
            Objects.requireNonNull(start, "start");
            Objects.requireNonNull(end, "end");
            Objects.requireNonNull(color, "color");
        }
    }
}
