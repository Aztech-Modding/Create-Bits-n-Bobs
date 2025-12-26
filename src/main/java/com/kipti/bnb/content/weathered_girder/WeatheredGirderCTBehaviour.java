package com.kipti.bnb.content.weathered_girder;

import com.kipti.bnb.registry.BnbSpriteShifts;
import com.simibubi.create.content.decoration.girder.GirderBlock;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class WeatheredGirderCTBehaviour extends ConnectedTextureBehaviour.Base {

    @Override
    public CTSpriteShiftEntry getShift(BlockState state, Direction direction, @Nullable TextureAtlasSprite sprite) {
        if (!state.hasProperty(GirderBlock.X))
            return null;
        return !state.getValue(GirderBlock.X) && !state.getValue(GirderBlock.Z) && direction.getAxis() != Axis.Y
                ? BnbSpriteShifts.WEATHERED_GIRDER_POLE
                : null;
    }

    @Override
    public boolean connectsTo(BlockState state, BlockState other, BlockAndTintGetter reader, BlockPos pos,
                              BlockPos otherPos, Direction face) {
        if (other.getBlock() != state.getBlock() && !(other.getBlock() instanceof GirderBlock))
            return false;
        return !other.getValue(GirderBlock.X) && !other.getValue(GirderBlock.Z);
    }

}
