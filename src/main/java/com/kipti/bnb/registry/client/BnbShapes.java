package com.kipti.bnb.registry.client;

import com.simibubi.create.AllShapes;
import net.createmod.catnip.math.VoxelShaper;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BnbShapes {

    public static final VoxelShaper HEADLAMP_INTERACTION = shape(0, 0, 0, 16, 7, 16)
            .forDirectional();

    public static final VoxelShaper GIRDER_STRUT = shape(3, 0, 3, 13, 10, 13)
            .forDirectional();

    public static final VoxelShaper SIX_VOXEL_POLE = shape(5, 0, 5, 11, 16, 11).forAxis(),
            SMALL_GEAR = shape(cuboid(2, 4, 2, 14, 12, 14)).add(SIX_VOXEL_POLE.get(Direction.Axis.Y))
                    .forAxis(),
            LARGE_GEAR = shape(cuboid(0, 4, 0, 16, 12, 16)).add(SIX_VOXEL_POLE.get(Direction.Axis.Y))
                    .forAxis();
    public static final VoxelShaper LIGHTBULB_SHAPE = shape(6, 0, 6, 10, 2, 10)
            .add(cuboid(5, 2, 5, 11, 13, 11))
            .forDirectional();

    public static final VoxelShaper LIGHTBULB_CAGED_SHAPE = shape(6, 0, 6, 10, 2, 10)
            .add(cuboid(5, 2, 5, 11, 5, 11))
            .add(cuboid(4, 5, 4, 12, 14, 12))
            .forDirectional();

    public static final VoxelShaper BRASS_LAMP_SHAPE = shape(1, 0, 1, 15, 3, 15)
            .add(cuboid(2, 3, 2, 14, 10, 14))
            .add(cuboid(3, 10, 3, 13, 13, 13))
            .add(cuboid(5, 13, 5, 11, 15, 11))
            .forDirectional();

    public static final VoxelShaper NIXIE_BOARD_SIDE = shape(7, 0, 0, 9, 19, 16)
            .forDirectional();

    public static final VoxelShaper NIXIE_BOARD_FRONT = shape(0, 0, 7, 16, 19, 9)
            .forDirectional();

    public static final VoxelShaper LARGE_NIXIE_TUBE_SIDE = shape(6, 0, 0, 10, 3, 16)
            .add(cuboid(2, 0, 2, 14, 3, 14))
            .add(cuboid(3, 3, 3, 13, 16, 13))
            .forDirectional();

    public static final VoxelShaper LARGE_NIXIE_TUBE_FRONT = shape(0, 0, 6, 16, 3, 10)
            .add(cuboid(2, 0, 2, 14, 3, 14))
            .add(cuboid(3, 3, 3, 13, 16, 13))
            .forDirectional();

    public static AllShapes.Builder shape(VoxelShape shape) {
        return new AllShapes.Builder(shape);
    }

    public static AllShapes.Builder shape(double x1, double y1, double z1, double x2, double y2, double z2) {
        return shape(cuboid(x1, y1, z1, x2, y2, z2));
    }

    public static VoxelShape cuboid(double x1, double y1, double z1, double x2, double y2, double z2) {
        return Block.box(x1, y1, z1, x2, y2, z2);
    }

}

