package com.kipti.bnb.content.decoration.shroud;

import com.cake.azimuth.behaviour.SuperBlockEntityBehaviour;
import com.simibubi.create.content.contraptions.StructureTransform;
import com.simibubi.create.content.decoration.bracket.BracketBlock;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class ShroudedBehaviour extends SuperBlockEntityBehaviour {

    public static final BehaviourType<ShroudedBehaviour> TYPE = new BehaviourType<>();

    private BlockState shroud;
    private boolean reRender;

    private final Predicate<BlockState> pred;

    public ShroudedBehaviour(final SmartBlockEntity be) {
        this(be, state -> true);
    }

    public ShroudedBehaviour(final SmartBlockEntity be, final Predicate<BlockState> pred) {
        super(be);
        this.pred = pred;
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

    public void applyBracket(final BlockState state) {
        this.shroud = state;
        this.reRender = true;
        this.blockEntity.notifyUpdate();
        final Level world = this.getWorld();
        if (world.isClientSide)
            return;
        this.blockEntity.getBlockState()
                .updateNeighbourShapes(world, this.getPos(), 3);
    }

    @Override
    public void transform(final BlockEntity be, final StructureTransform transform) {
        super.transform(be, transform);
        if (this.isBracketPresent()) {
            final BlockState transformedBracket = transform.apply(this.shroud);
            this.applyBracket(transformedBracket);
        }
    }

    @Nullable
    public BlockState removeBracket(final boolean inOnReplacedContext) {
        if (this.shroud == null) {
            return null;
        }

        final BlockState removed = this.shroud;
        final Level world = this.getWorld();
        if (!world.isClientSide)
            world.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, this.getPos(), Block.getId(this.shroud));
        this.shroud = null;
        this.reRender = true;
        if (inOnReplacedContext) {
            this.blockEntity.sendData();
            return removed;
        }
        this.blockEntity.notifyUpdate();
        if (world.isClientSide)
            return removed;
        this.blockEntity.getBlockState()
                .updateNeighbourShapes(world, this.getPos(), 3);
        return removed;
    }

    public boolean isBracketPresent() {
        return this.shroud != null;
    }

    public boolean isBracketValid(final BlockState bracketState) {
        return bracketState.getBlock() instanceof BracketBlock;
    }

    @Nullable
    public BlockState getBracket() {
        return this.shroud;
    }

    public boolean canHaveBracket() {
        return this.pred.test(this.blockEntity.getBlockState());
    }

    @Override
    public ItemRequirement getRequiredItems() {
        if (!this.isBracketPresent()) {
            return ItemRequirement.NONE;
        }
        return ItemRequirement.of(this.shroud, null);
    }

    @Override
    public boolean isSafeNBT() {
        return true;
    }

    @Override
    public void write(final CompoundTag nbt, final HolderLookup.Provider registries, final boolean clientPacket) {
        if (this.isBracketPresent() && this.isBracketValid(this.shroud)) {
            nbt.put("Bracket", NbtUtils.writeBlockState(this.shroud));
        }
        if (clientPacket && this.reRender) {
            NBTHelper.putMarker(nbt, "Redraw");
            this.reRender = false;
        }
        super.write(nbt, registries, clientPacket);
    }

    @Override
    public void read(final CompoundTag nbt, final HolderLookup.Provider registries, final boolean clientPacket) {
        if (nbt.contains("Bracket")) {
            this.shroud = null;
            final BlockState readBlockState = NbtUtils.readBlockState(
                    this.blockEntity.blockHolderGetter(),
                    nbt.getCompound("Bracket")
            );
            if (this.isBracketValid(readBlockState))
                this.shroud = readBlockState;
        }
        if (clientPacket && nbt.contains("Redraw"))
            this.getWorld().sendBlockUpdated(
                    this.getPos(),
                    this.blockEntity.getBlockState(),
                    this.blockEntity.getBlockState(),
                    16
            );
        super.read(nbt, registries, clientPacket);
    }

}
