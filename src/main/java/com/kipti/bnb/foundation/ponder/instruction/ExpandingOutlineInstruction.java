package com.kipti.bnb.foundation.ponder.instruction;

import com.kipti.bnb.foundation.client.ExpandingLineOutline;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.instruction.TickingInstruction;
import net.minecraft.world.phys.Vec3;

public class ExpandingOutlineInstruction extends TickingInstruction {

    private final PonderPalette color;

    private final ExpandingLineOutline expandingOutlineInstruction = new ExpandingLineOutline();

    public ExpandingOutlineInstruction(PonderPalette color, Vec3 start, Vec3 end, int ticks, int growingTicks) {
        super(false, ticks);
        this.color = color;
        this.expandingOutlineInstruction
                .setGrowingTicks(growingTicks)
                .set(start, end);
    }

    @Override
    protected void firstTick(final PonderScene scene) {
        super.firstTick(scene);
        expandingOutlineInstruction.setGrowingTicksElapsed(0);
    }

    @Override
    public void tick(PonderScene scene) {
        super.tick(scene);
        expandingOutlineInstruction.tickGrowingTicksElapsed();
        scene.getOutliner()
                .showOutline(this, expandingOutlineInstruction)
                .lineWidth(1 / 16f)
                .colored(color.getColor());
    }

}

