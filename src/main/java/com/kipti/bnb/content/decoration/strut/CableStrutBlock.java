package com.kipti.bnb.content.decoration.strut;

import com.cake.struts.content.CableStrutInfo;
import com.cake.struts.content.StrutModelType;
import com.kipti.bnb.registry.client.BnbShapes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CableStrutBlock extends BnbStrutBlock {
    public CableStrutBlock(Properties properties, StrutModelType modelType) {
        super(properties, modelType);
    }

    public CableStrutBlock(Properties properties, StrutModelType modelType, @Nullable CableStrutInfo cableRenderInfo) {
        super(properties, modelType, cableRenderInfo);
    }

    @Override
    public @NotNull VoxelShape getShape(final BlockState state, @NotNull final BlockGetter level, @NotNull final BlockPos pos, @NotNull final CollisionContext context) {
        return BnbShapes.CABLE_STRUT.get(state.getValue(FACING));
    }
}
