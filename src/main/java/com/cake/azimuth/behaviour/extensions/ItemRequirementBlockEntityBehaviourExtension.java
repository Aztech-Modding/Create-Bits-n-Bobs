package com.cake.azimuth.behaviour.extensions;

import com.cake.azimuth.behaviour.IBehaviourExtension;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Allows behaviours to add additional item requirements to a kinetic block entity.
 */
public interface ItemRequirementBlockEntityBehaviourExtension extends IBehaviourExtension {


    ItemRequirement getRequiredItems(BlockState state);
}
