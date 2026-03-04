package com.kipti.bnb.content.decoration.strut;

import com.cake.struts.content.CableStrutInfo;
import com.cake.struts.content.StrutModelType;
import com.cake.struts.content.block.StrutBlock;
import com.cake.struts.content.block.StrutBlockEntity;
import com.kipti.bnb.registry.content.BnbBlockEntities;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.Nullable;

public class BnbStrutBlock extends StrutBlock {

    public BnbStrutBlock(final Properties properties, final StrutModelType modelType) {
        super(properties, modelType);
    }

    public BnbStrutBlock(final Properties properties, final StrutModelType modelType, final @Nullable CableStrutInfo cableRenderInfo) {
        super(properties, modelType, cableRenderInfo);
    }

    @Override
    protected BlockEntityType<? extends StrutBlockEntity> getStrutBlockEntityType() {
        return BnbBlockEntities.GIRDER_STRUT.get();
    }
}
