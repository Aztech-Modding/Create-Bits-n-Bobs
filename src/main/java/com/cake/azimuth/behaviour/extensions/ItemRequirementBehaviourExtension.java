package com.cake.azimuth.behaviour.extensions;

import com.cake.azimuth.behaviour.BehaviourExtension;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Allows behaviours to add additional item requirements to a kinetic block entity.
 */
public interface ItemRequirementBehaviourExtension extends BehaviourExtension {


    ItemRequirement getRequiredItems(BlockState state);
}
