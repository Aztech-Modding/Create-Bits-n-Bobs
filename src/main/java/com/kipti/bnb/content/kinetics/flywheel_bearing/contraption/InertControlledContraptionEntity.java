package com.kipti.bnb.content.kinetics.flywheel_bearing.contraption;

import com.kipti.bnb.registry.content.BnbEntityTypes;
import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.ControlledContraptionEntity;
import com.simibubi.create.content.contraptions.IControlContraption;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;

public class InertControlledContraptionEntity extends ControlledContraptionEntity {

    public InertControlledContraptionEntity(final EntityType<?> type, final Level world) {
        super(type, world);
    }

    @Override
    protected boolean shouldActorTrigger(final MovementContext context, final StructureTemplate.StructureBlockInfo blockInfo, final MovementBehaviour actor, final Vec3 actorPosition, final BlockPos gridPosition) {
        return false;
    }

    public static InertControlledContraptionEntity create(final Level world, final IControlContraption controller,
                                                          final Contraption contraption) {
        final InertControlledContraptionEntity entity =
                new InertControlledContraptionEntity(BnbEntityTypes.INERT_CONTROLLED_CONTRAPTION.get(), world);
        entity.controllerPos = controller.getBlockPosition();
        entity.setContraption(contraption);
        return entity;
    }

}

