package com.kipti.bnb.mixin.dyeable.fluid_tank;

import com.simibubi.create.content.fluids.tank.FluidTankItem;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FluidTankItem.class)
public class FluidTankItemMixin {

    //Agent: basically anything the normal dyed pipe item does thats relevant needs to be done here, which is predominantly fixing the offhand dye delay, since its incredibly frustrating
    //Also, this includes the placeMulti method needing to be overriden to handle changing dye of full set
    //Delete the placement handling in the behaviour its an innapropriate place that stuff needs to happen here

}
