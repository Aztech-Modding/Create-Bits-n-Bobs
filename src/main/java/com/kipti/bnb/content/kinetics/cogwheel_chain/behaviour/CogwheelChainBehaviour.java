package com.kipti.bnb.content.kinetics.cogwheel_chain.behaviour;

import com.cake.azimuth.behaviour.SuperBlockEntityBehaviour;
import com.cake.azimuth.behaviour.extensions.ItemRequirementBehaviourExtension;
import com.cake.azimuth.behaviour.extensions.KineticBehaviourExtension;
import com.cake.azimuth.behaviour.extensions.RenderedBehaviourExtension;
import com.kipti.bnb.CreateBitsnBobs;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.CogwheelChain;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.CogwheelChainCandidate;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.PathedCogwheelNode;
import com.kipti.bnb.content.kinetics.cogwheel_chain.world.CogwheelChainWorld;
import com.kipti.bnb.registry.client.BnbBlockEntityBehaviourRenderers;
import com.simibubi.create.content.contraptions.StructureTransform;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CogwheelChainBehaviour extends SuperBlockEntityBehaviour implements RenderedBehaviourExtension, KineticBehaviourExtension, ItemRequirementBehaviourExtension {

    public static final BehaviourType<CogwheelChainBehaviour> TYPE = new BehaviourType<>();

    /**
     * The controller is responsible for storing the chain data and doing integrity checks. It will also handle rendering the chain shapes.
     */
    @Nullable
    private CogwheelChain controlledChain = null;

    /**
     * Non-controller components will just store an offset to their controller and do a dumb integrity check to make sure they are still connected to a valid controller.
     */
    @Nullable
    private Vec3i controllerOffset = null;

    private int chainsToRefund = 0;

    public CogwheelChainBehaviour(final SmartBlockEntity be) {
        super(be);
        setLazyTickRate(5);
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (isClientLevel()) {
            return;
        }
        if (controlledChain != null) {
            if (!controlledChain.checkIntegrity(getLevel(), getPos())) {
                destroyForInvalidShape();
            }
        } else {
            if (controllerOffset != null && getLevel() != null) {
                final BlockPos controllerPos = getPos().offset(controllerOffset);
                if (!getLevel().isLoaded(controllerPos)) return;
                this.<CogwheelChainBehaviour>getSameBehaviourOptional(controllerPos)
                        .ifPresentOrElse(controller -> {
                            if (!controller.isInSameChain(this)) {
                                destroyForInvalidShape();
                            }
                        }, this::destroyForInvalidShape);
            }
        }
    }

    @Override
    public void writeSafe(final CompoundTag tag, final HolderLookup.Provider registries) {
        super.writeSafe(tag, registries);
        writeConnectionInfo(tag);
    }

    @Override
    public boolean isSafeNBT() {
        return true;
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    @Override
    public ItemRequirement getRequiredItems(final BlockState state) {
        return isController() ? new ItemRequirement(
                ItemRequirement.ItemUseType.CONSUME,
                Blocks.CHAIN.asItem().getDefaultInstance().copyWithCount(controlledChain != null ? controlledChain.getChainsRequired() : 0)
        ) : ItemRequirement.NONE;
    }

    public void destroyForInvalidShape() {
        destroyChain(true, false);
    }

    public void destroyChain(final boolean dropItemsInWorld) {
        destroyChain(dropItemsInWorld, false);
    }

    public ItemStack destroyChain(final boolean dropItemsInWorld, final boolean effects) {
        if (getLevel() == null || !isPartOfChain()) return ItemStack.EMPTY;
        repropagateKinetics();

        //Try drop chains from the current block for convenience
        int chainsToReturn = chainsToRefund;
        CogwheelChain chainSource = this.controlledChain;
        boolean hasChainData = chainSource != null;

        if (!isController() && controllerOffset != null && getLevel() != null) {
            final BlockPos controllerPos = getPos().offset(controllerOffset);

            if (this.getSameBehaviour(controllerPos) instanceof final CogwheelChainBehaviour controllerBE) {
                chainsToReturn = controllerBE.chainsToRefund;
                controllerBE.chainsToRefund = 0;
                hasChainData = true;
                chainSource = controllerBE.controlledChain;
            }
        }
        if (!hasChainData) {
            CreateBitsnBobs.LOGGER.warn("Failed to destroy chain with missing chain data at {}", getPos());
            return ItemStack.EMPTY;
        }
        final ItemStack drops = chainSource == null ? ItemStack.EMPTY : chainSource.getReturnedItem().getDefaultInstance().copyWithCount(chainsToReturn);
        if (dropItemsInWorld) {
            Block.popResource(getLevel(), getPos(), drops);
        }

        if (isController()) {
            if (this.controlledChain == null) return ItemStack.EMPTY;
            if (effects) this.controlledChain.createDestroyEffects(getLevel(), getPos());
            this.controlledChain.destroy(getLevel(), getPos());
        }
        if (!isController() && controllerOffset != null && getLevel() != null) {
            final BlockPos controllerPos = getPos().offset(controllerOffset);
            this.<CogwheelChainBehaviour>getSameBehaviourOptional(controllerPos)
                    .ifPresent(controller -> {
                        if (controller.controlledChain == null) return;
                        if (effects) controller.controlledChain.createDestroyEffects(getLevel(), controllerPos);
                        controller.controlledChain.destroy(getLevel(), controllerPos);
                    });
        }
        detachKinetics();
        return drops;
    }

    @Override
    public void write(final CompoundTag compound, final HolderLookup.Provider registries, final boolean clientPacket) {
        super.write(compound, registries, clientPacket);
        writeConnectionInfo(compound);
    }

    @Override
    public void read(final CompoundTag compound, final HolderLookup.Provider registries, final boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        if (compound.contains("ControllerOffsetX")) {
            controllerOffset = new Vec3i(
                    compound.getInt("ControllerOffsetX"),
                    compound.getInt("ControllerOffsetY"),
                    compound.getInt("ControllerOffsetZ")
            );
        } else {
            controllerOffset = null;
        }

        if (compound.contains("Chain") && compound.contains("ChainsToRefund")) {
            chainsToRefund = compound.getInt("ChainsToRefund");
            if (controlledChain != null && compound.contains("Chain")) {
                controlledChain.read(compound.getCompound("Chain"));
            } else {
                controlledChain = new CogwheelChain(compound.getCompound("Chain"));
            }
            if (controlledChain != null && hasLevel()) {
                CogwheelChainWorld.get(getLevel()).put(getPos(), controlledChain);
            }
        } else {
            if (controlledChain != null && hasLevel()) {
                CogwheelChainWorld.get(getLevel()).remove(getPos());
            }
            controlledChain = null;
        }
    }

    @Override
    public float propagateRotationTo(final KineticBlockEntity target, final BlockState stateFrom, final BlockState stateTo, final BlockPos diff, final boolean connectedViaAxes, final boolean connectedViaCogs) {
        if (connectedViaAxes && Math.abs(diff.get(CogwheelChainCandidate.getAxis(getBlockState()))) == 1)
            return 0;

        //Else, check if this is the same chain structure.
        return this.<CogwheelChainBehaviour>getSameBehaviourOptional(target)
                .map((otherBehaviour) -> {
                    final boolean isControlledBySame =
                            isInSameChain(otherBehaviour);

                    if (isControlledBySame) {
                        final float currentSide = this.getChainRotationFactor();
                        final float otherSide = otherBehaviour.getChainRotationFactor();
                        return currentSide / otherSide;
                    }
                    return 0f;
                }).orElse(0f);
    }

    private boolean isInSameChain(final CogwheelChainBehaviour otherBehaviour) {
        return (this.controlledChain != null &&
                otherBehaviour.controllerOffset != null &&
                otherBehaviour.controllerOffset.equals(this.getPos().subtract(otherBehaviour.getPos()))) ||

                (otherBehaviour.controlledChain != null &&
                        this.controllerOffset != null &&
                        this.controllerOffset.equals(otherBehaviour.getPos().subtract(this.getPos()))) ||

                (otherBehaviour.controllerOffset != null &&
                        this.controllerOffset != null &&
                        this.controllerOffset.offset(this.getPos()).equals(otherBehaviour.controllerOffset.offset(otherBehaviour.getPos())));
    }

    public float getChainRotationFactor() {
        if (controlledChain != null) {
            final PathedCogwheelNode controllerNode = controlledChain.getNodeFromControllerOffset(new Vec3i(0, 0, 0));
            if (controllerNode == null) return 0;

            return controllerNode.sideFactor();
        }

        if (getLevel() == null || controllerOffset == null) return 0;

        final BlockPos controllerPos = getPos().offset(controllerOffset);
        return this.<CogwheelChainBehaviour>getSameBehaviourOptional(controllerPos)
                .map(controller -> {
                    if (controller.controlledChain == null) return 0f;

                    final PathedCogwheelNode nodeInChain = controller.controlledChain.getNodeFromControllerOffset(controllerOffset);
                    return nodeInChain == null ? 0f : nodeInChain.sideFactor();
                }).orElse(0f);
    }

    private void writeConnectionInfo(final CompoundTag compound) {
        if (controllerOffset != null) {
            compound.putInt("ControllerOffsetX", controllerOffset.getX());
            compound.putInt("ControllerOffsetY", controllerOffset.getY());
            compound.putInt("ControllerOffsetZ", controllerOffset.getZ());
        }

        if (controlledChain != null) {
            final CompoundTag chainTag = new CompoundTag();
            controlledChain.write(chainTag);
            compound.put("Chain", chainTag);
            compound.putInt("ChainsToRefund", chainsToRefund);
        }
    }

    public void setAsController(final CogwheelChain cogwheelChain) {
        this.controlledChain = cogwheelChain;
        if (hasLevel()) {
            CogwheelChainWorld.get(getLevel()).put(getPos(), cogwheelChain);
        }
    }

    @Override
    public List<BlockPos> addExtraPropagationLocations(final IRotate block, final BlockState state, final List<BlockPos> neighbours) {
        final List<BlockPos> toPropagate = new ArrayList<>(KineticBehaviourExtension.super.addExtraPropagationLocations(block, state, neighbours));
        if (controlledChain != null) {
            addPropagationLocationsFromControllerExcept(toPropagate, getPos());
        } else {
            //Test putting child to child connections
            if (controllerOffset != null && getLevel() != null) {
                final BlockPos controllerPos = getPos().offset(controllerOffset);
                this.<CogwheelChainBehaviour>getSameBehaviourOptional(controllerPos)
                        .ifPresent(controller -> controller.addPropagationLocationsFromControllerExcept(toPropagate, getPos()));
            }
        }

        return toPropagate;
    }

    private void addPropagationLocationsFromControllerExcept(final List<BlockPos> toPropagate, final BlockPos exclude) {
        if (controlledChain == null) return;
        for (final PathedCogwheelNode cogwheelNode : controlledChain.getChainPathCogwheelNodes()) {
            final BlockPos cogwheelPos = getPos().offset(cogwheelNode.localPos());
            if (!toPropagate.contains(cogwheelPos) && !cogwheelPos.equals(exclude)) {
                toPropagate.add(cogwheelPos);
            }
        }
    }

    public boolean isController() {
        return controlledChain != null;
    }

    public void setController(final Vec3i offset) {
        if (this.controlledChain != null && hasLevel()) {
            CogwheelChainWorld.get(getLevel()).remove(getPos());
        }
        this.controlledChain = null;
        this.controllerOffset = offset;
    }

    public @Nullable CogwheelChain getControlledChain() {
        return controlledChain;
    }

    public @Nullable Vec3i getControllerOffset() {
        return controllerOffset;
    }

    public void setChainsUsed(final int chainsUsed) {
        this.chainsToRefund = chainsUsed;
    }

    public void clearStoredChains() {
        if (controlledChain != null) {
            this.chainsToRefund = 0;
        } else {
            if (controllerOffset != null && getLevel() != null) {
                final BlockPos controllerPos = getPos().offset(controllerOffset);
                this.<CogwheelChainBehaviour>getSameBehaviourOptional(controllerPos)
                        .ifPresent(controller -> controller.chainsToRefund = 0);
            }
        }
    }

    @Override
    public void transform(final BlockEntity blockEntity, final StructureTransform transform) {
        if (controlledChain != null)
            controlledChain.transform(transform);
        if (controllerOffset != null) {
            final Vec3i transformedOffset = transform.applyWithoutOffset(new BlockPos(controllerOffset));
            setController(transformedOffset);
        }
    }

    public boolean isPartOfChain() {
        return this.controlledChain != null || this.controllerOffset != null;
    }

    @Override
    public BehaviourType<CogwheelChainBehaviour> getType() {
        return TYPE;
    }

    @Override
    public BehaviourRenderSupplier getRenderer() {
        return BnbBlockEntityBehaviourRenderers.COGWHEEL_CHAIN;
    }

    @Override
    public BehaviourVisualFactory getVisualFactory() {
        return (context, behaviour, blockEntity, parentVisual, partialTick) -> {
            if (!(blockEntity instanceof final KineticBlockEntity kineticBlockEntity) || behaviour != this) {
                return null;
            }
            return new CogwheelChainBehaviourVisual(context, kineticBlockEntity, this, parentVisual);
        };
    }

    @Override
    public void onBlockBroken(final BlockEvent.BreakEvent event) {
        super.onBlockBroken(event);
        destroyChain(!event.getPlayer().hasInfiniteMaterials(), true);
    }

    /**
     * Since we have the visual to use, no forcing is necessary
     */
    @Override
    public boolean rendersWhenVisualizationAvailable() {
        return false;
    }

    public void disconnectFromChain() {
        if (this.controlledChain != null && hasLevel()) {
            CogwheelChainWorld.get(getLevel()).remove(getPos());
        }
        this.controlledChain = null;
        this.controllerOffset = null;
        this.chainsToRefund = 0;
        sendData();
    }
}
