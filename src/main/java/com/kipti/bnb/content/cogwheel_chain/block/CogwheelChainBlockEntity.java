package com.kipti.bnb.content.cogwheel_chain.block;

import com.kipti.bnb.content.cogwheel_chain.graph.CogwheelChain;
import com.kipti.bnb.content.cogwheel_chain.graph.PathedCogwheelNode;
import com.kipti.bnb.content.girder_strut.IBlockEntityRelighter;
import com.simibubi.create.api.contraption.transformable.TransformableBlockEntity;
import com.simibubi.create.api.schematic.requirement.SpecialBlockEntityItemRequirement;
import com.simibubi.create.content.contraptions.StructureTransform;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.SimpleKineticBlockEntity;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CogwheelChainBlockEntity extends SimpleKineticBlockEntity implements IBlockEntityRelighter, SpecialBlockEntityItemRequirement, TransformableBlockEntity {

    private boolean isController = false;
    @Nullable
    private CogwheelChain chain = null;
    @Nullable
    private Vec3i controllerOffset = null;
    private int chainsToRefund = 0;

    public CogwheelChainBlockEntity(final BlockEntityType<?> type, final BlockPos pos, final BlockState state) {
        super(type, pos, state);
        setLazyTickRate(5);
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (isController && chain != null) {
            if (!chain.checkIntegrity(level, worldPosition)) {
                destroyChain(true);
            }
        } else {
            if (controllerOffset != null && level != null) {
                final BlockPos controllerPos = worldPosition.offset(controllerOffset);
                final BlockEntity be = level.getBlockEntity(controllerPos);
                if (!(be instanceof CogwheelChainBlockEntity)) {
                    CogwheelChain.removeChainCogwheelFromLevelIfPresent(level, getBlockPos());
                }
            }
        }
    }

    @Override
    protected void read(final CompoundTag compound, final HolderLookup.Provider registries, final boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        isController = compound.getBoolean("IsController");
        if (compound.contains("ControllerOffsetX")) {
            controllerOffset = new Vec3i(
                    compound.getInt("ControllerOffsetX"),
                    compound.getInt("ControllerOffsetY"),
                    compound.getInt("ControllerOffsetZ")
            );
        } else {
            controllerOffset = null;
        }

        if (isController) {
            chainsToRefund = compound.getInt("ChainsToRefund");
            if (chain != null && compound.contains("Chain")) {
                chain.read(compound.getCompound("Chain"));
            } else {
                chain = new CogwheelChain(compound.getCompound("Chain"));
            }
        } else {
            chain = null;
        }
    }

    @Override
    protected void write(final CompoundTag compound, final HolderLookup.Provider registries, final boolean clientPacket) {
        super.write(compound, registries, clientPacket);
        writeConnectionInfo(compound);
    }

    @Override
    public void writeSafe(final CompoundTag tag, final HolderLookup.Provider registries) {
        super.writeSafe(tag, registries);
        writeConnectionInfo(tag);
    }

    private void writeConnectionInfo(final CompoundTag compound) {
        compound.putBoolean("IsController", isController);

        if (controllerOffset != null) {
            compound.putInt("ControllerOffsetX", controllerOffset.getX());
            compound.putInt("ControllerOffsetY", controllerOffset.getY());
            compound.putInt("ControllerOffsetZ", controllerOffset.getZ());
        }

        if (isController && chain != null) {
            final CompoundTag chainTag = new CompoundTag();
            chain.write(chainTag);
            compound.put("Chain", chainTag);
            compound.putInt("ChainsToRefund", chainsToRefund);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        destroyChain(true);
    }

    public ItemStack destroyChain(final boolean dropItemsInWorld) {
        //Try drop chains from the current block for convenience
        int chainsToReturn = chainsToRefund;
        if (!isController && controllerOffset != null && level != null) {
            final BlockPos controllerPos = worldPosition.offset(controllerOffset);
            final BlockEntity be = level.getBlockEntity(controllerPos);
            if (be instanceof final CogwheelChainBlockEntity controllerBE) {
                chainsToReturn = controllerBE.chainsToRefund;
                controllerBE.chainsToRefund = 0;
            }
        }
        final ItemStack drops = Items.CHAIN.getDefaultInstance().copyWithCount(chainsToReturn);
        if (dropItemsInWorld) {
            Block.popResource(level, worldPosition, drops);
        }
        this.chainsToRefund = 0; // Reset after dropping

        if (isController && chain != null) {
            chain.destroy(level, worldPosition);
        }
        if (!isController && controllerOffset != null && level != null) {
            final BlockPos controllerPos = worldPosition.offset(controllerOffset);
            final BlockEntity be = level.getBlockEntity(controllerPos);
            if (be instanceof final CogwheelChainBlockEntity controllerBE) {
                assert controllerBE.chain != null;
                controllerBE.chain.destroy(level, controllerPos);
            }
        }
        return drops;
    }

    public void setController(final Vec3i offset) {
        this.isController = false;
        this.controllerOffset = offset;
    }

    public void setAsController(final CogwheelChain cogwheelChain) {
        this.isController = true;
        this.chain = cogwheelChain;
    }

    @Override
    protected AABB createRenderBoundingBox() {
        return super.createRenderBoundingBox().inflate(64);
    }

    @Override
    public List<BlockPos> addPropagationLocations(final IRotate block, final BlockState state, final List<BlockPos> neighbours) {
        final List<BlockPos> toPropagate = new ArrayList<>(super.addPropagationLocations(block, state, neighbours));

        if (isController && chain != null) {
            addPropogationLocationsFromController(toPropagate, getBlockPos());
        } else {
            //Test putting child to child connections
            if (controllerOffset != null && level != null) {
                final BlockPos controllerPos = worldPosition.offset(controllerOffset);
                final BlockEntity be = level.getBlockEntity(controllerPos);
                if (be instanceof final CogwheelChainBlockEntity controllerBE) {
                    controllerBE.addPropogationLocationsFromController(toPropagate, getBlockPos());
                }
            }
        }

        return toPropagate;
    }

    @Override
    public float propagateRotationTo(final KineticBlockEntity target, final BlockState stateFrom, final BlockState stateTo, final BlockPos diff, final boolean connectedViaAxes, final boolean connectedViaCogs) {
        if (connectedViaAxes && Math.abs(diff.get(getBlockState().getValue(CogwheelChainBlock.AXIS))) == 1)
            return 0;

        //Else, check if this is the same chain structure.
        if (target instanceof final CogwheelChainBlockEntity chainTarget) {
            final boolean isControlledBySame = this.isController &&
                    chainTarget.controllerOffset != null &&
                    chainTarget.controllerOffset.equals(this.getBlockPos().subtract(target.getBlockPos())) ||

                    chainTarget.isController &&
                            this.controllerOffset != null &&
                            this.controllerOffset.equals(target.getBlockPos().subtract(this.getBlockPos())) ||

                    chainTarget.controllerOffset != null &&
                            this.controllerOffset != null &&
                            this.controllerOffset.offset(this.getBlockPos()).equals(chainTarget.controllerOffset.offset(target.getBlockPos()));

            if (isControlledBySame) {
                final float currentSide = this.getChainRotationFactor();
                final float otherSide = chainTarget.getChainRotationFactor();
                return currentSide / otherSide;
            }
        }
        return 0;
    }

    public float getChainRotationFactor() {
        if (isController) {
            if (chain == null) return 0;

            final PathedCogwheelNode controllerNode = chain.getNodeFromControllerOffset(new Vec3i(0, 0, 0));
            if (controllerNode == null) return 0;

            return controllerNode.sideFactor();
        }

        if (level == null || controllerOffset == null) return 0;

        final BlockPos controllerPos = worldPosition.offset(controllerOffset);
        final BlockEntity be = level.getBlockEntity(controllerPos);
        if (be instanceof final CogwheelChainBlockEntity controllerBE) {

            final CogwheelChain controllerChain = controllerBE.chain;
            if (controllerChain == null) return 0;

            final PathedCogwheelNode nodeInChain = controllerChain.getNodeFromControllerOffset(controllerOffset);
            return nodeInChain == null ? 0 : nodeInChain.sideFactor();
        }
        return 0;
    }

    private void addPropogationLocationsFromController(final List<BlockPos> toPropagate, final BlockPos exclude) {
        if (chain == null) return;
        for (final PathedCogwheelNode cogwheelNode : chain.getChainPathCogwheelNodes()) {
            final BlockPos cogwheelPos = worldPosition.offset(cogwheelNode.localPos());
            if (!toPropagate.contains(cogwheelPos) && !cogwheelPos.equals(exclude)) {
                toPropagate.add(cogwheelPos);
            }
        }
    }

    public boolean isController() {
        return isController;
    }

    public void setController(final boolean controller) {
        isController = controller;
    }

    public @Nullable CogwheelChain getChain() {
        return chain;
    }

    public void setChain(@Nullable final CogwheelChain chain) {
        this.chain = chain;
    }

    public @Nullable Vec3i getControllerOffset() {
        return controllerOffset;
    }

    public void setControllerOffset(@Nullable final Vec3i controllerOffset) {
        this.controllerOffset = controllerOffset;
    }

    public void setChainsUsed(final int chainsUsed) {
        this.chainsToRefund = chainsUsed;
    }

    public void clearStoredChains() {
        if (isController) {
            this.chainsToRefund = 0;
        } else {
            if (controllerOffset != null && level != null) {
                final BlockPos controllerPos = worldPosition.offset(controllerOffset);
                final BlockEntity be = level.getBlockEntity(controllerPos);
                if (be instanceof final CogwheelChainBlockEntity controllerBE) {
                    controllerBE.chainsToRefund = 0;
                }
            }
        }
    }

    @Override
    public ItemRequirement getRequiredItems(final BlockState state) {
        return isController ? new ItemRequirement(
                ItemRequirement.ItemUseType.CONSUME,
                Blocks.CHAIN.asItem().getDefaultInstance().copyWithCount(chain != null ? chain.getChainsRequired() : 0)
        ) : ItemRequirement.NONE;
    }

    @Override
    public void transform(final BlockEntity blockEntity, final StructureTransform transform) {
        if (chain != null)
            chain.transform(blockEntity, transform);
        if (controllerOffset != null) {
            final Vec3i transformedOffset = transform.applyWithoutOffset(new BlockPos(controllerOffset));
            setController(transformedOffset);
        }
    }
}
