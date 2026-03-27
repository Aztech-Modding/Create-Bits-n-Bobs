package com.kipti.bnb.content.kinetics.cogwheel_chain.behaviour;

import com.cake.azimuth.behaviour.SuperBlockEntityBehaviour;
import com.cake.azimuth.behaviour.extensions.ItemRequirementBehaviourExtension;
import com.cake.azimuth.behaviour.extensions.KineticBehaviourExtension;
import com.cake.azimuth.behaviour.extensions.RenderedBehaviourExtension;
import com.kipti.bnb.CreateBitsnBobs;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.CogwheelChain;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.CogwheelChainCandidate;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.PathedCogwheelNode;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.ResidualChainResult;
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
    @Nullable
    private BlockPos registeredControllerPos = null;

    public CogwheelChainBehaviour(final SmartBlockEntity be) {
        super(be);
        this.setLazyTickRate(5);
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (this.isClientLevel()) {
            return;
        }
        if (this.controlledChain != null) {
            if (!this.controlledChain.checkIntegrity(this.getLevel(), this.getPos())) {
                this.destroyForInvalidShape();
            }
        } else {
            if (this.controllerOffset != null && this.getLevel() != null) {
                final BlockPos controllerPos = this.getPos().offset(this.controllerOffset);
                if (!this.getLevel().isLoaded(controllerPos)) return;
                this.<CogwheelChainBehaviour>getSameBehaviourOptional(controllerPos)
                        .ifPresentOrElse(
                                controller -> {
                                    if (!controller.isInSameChain(this)) {
                                        this.destroyForInvalidShape();
                                    }
                                }, this::destroyForInvalidShape
                        );
            }
        }
    }

    @Override
    public void writeSafe(final CompoundTag tag, final HolderLookup.Provider registries) {
        super.writeSafe(tag, registries);
        this.writeConnectionInfo(tag);
    }

    @Override
    public boolean isSafeNBT() {
        return true;
    }

    @Override
    public void initialize() {
        super.initialize();
        this.syncControllerRegistration();
        if (this.isPartOfChain() && this.getBlockEntity() instanceof final KineticBlockEntity kbe) {
            kbe.updateSpeed = true;
        }
    }

    @Override
    public void unload() {
        this.unregisterController();
        super.unload();
    }

    @Override
    public void destroy() {
        this.unregisterController();
        super.destroy();
    }

    @Override
    public ItemRequirement getRequiredItems(final BlockState state) {
        return this.isController() ? new ItemRequirement(
                ItemRequirement.ItemUseType.CONSUME,
                Blocks.CHAIN.asItem().getDefaultInstance().copyWithCount(this.controlledChain != null ? this.controlledChain.getChainsRequired() : 0)
        ) : ItemRequirement.NONE;
    }

    public void destroyForInvalidShape() {
        this.destroyChain(true, false);
    }

    public void destroyChain(final boolean dropItemsInWorld) {
        this.destroyChain(dropItemsInWorld, false);
    }

    public ItemStack destroyChain(final boolean dropItemsInWorld, final boolean effects) {
        if (this.getLevel() == null || !this.isPartOfChain()) return ItemStack.EMPTY;

        this.detachKinetics();

        //Try drop chains from the current block for convenience
        int chainsToReturn = this.chainsToRefund;
        CogwheelChain chainSource = this.controlledChain;
        boolean hasChainData = chainSource != null;

        if (!this.isController() && this.controllerOffset != null && this.getLevel() != null) {
            final BlockPos controllerPos = this.getPos().offset(this.controllerOffset);

            if (this.getSameBehaviour(controllerPos) instanceof final CogwheelChainBehaviour controllerBE) {
                chainsToReturn = controllerBE.chainsToRefund;
                controllerBE.chainsToRefund = 0;
                hasChainData = true;
                chainSource = controllerBE.controlledChain;
            }
        }
        if (!hasChainData) {
            CreateBitsnBobs.LOGGER.warn("Failed to destroy chain with missing chain data at {}", this.getPos());
            this.disconnectFromChain();
            return ItemStack.EMPTY;
        }
        final ItemStack drops = chainSource == null ? ItemStack.EMPTY : chainSource.getReturnedItem().getDefaultInstance().copyWithCount(
                chainsToReturn);
        if (dropItemsInWorld) {
            Block.popResource(this.getLevel(), this.getPos(), drops);
        }

        if (this.isController()) {
            if (this.controlledChain == null) return ItemStack.EMPTY;
            if (effects) this.controlledChain.createDestroyEffects(this.getLevel(), this.getPos());
            this.controlledChain.destroy(this.getLevel(), this.getPos());
        }
        if (!this.isController() && this.controllerOffset != null && this.getLevel() != null) {
            final BlockPos controllerPos = this.getPos().offset(this.controllerOffset);
            this.<CogwheelChainBehaviour>getSameBehaviourOptional(controllerPos)
                    .ifPresent(controller -> {
                        if (controller.controlledChain == null) return;
                        if (effects) controller.controlledChain.createDestroyEffects(this.getLevel(), controllerPos);
                        controller.controlledChain.destroy(this.getLevel(), controllerPos);
                    });
        }
        return drops;
    }

    @Override
    public void write(final CompoundTag compound, final HolderLookup.Provider registries, final boolean clientPacket) {
        super.write(compound, registries, clientPacket);
        this.writeConnectionInfo(compound);
    }

    @Override
    public void read(final CompoundTag compound, final HolderLookup.Provider registries, final boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        if (compound.contains("ControllerOffsetX")) {
            this.controllerOffset = new Vec3i(
                    compound.getInt("ControllerOffsetX"),
                    compound.getInt("ControllerOffsetY"),
                    compound.getInt("ControllerOffsetZ")
            );
        } else {
            this.controllerOffset = null;
        }

        if (compound.contains("Chain") && compound.contains("ChainsToRefund")) {
            this.chainsToRefund = compound.getInt("ChainsToRefund");
            if (this.controlledChain != null && compound.contains("Chain")) {
                this.controlledChain.read(compound.getCompound("Chain"));
            } else {
                this.controlledChain = new CogwheelChain(compound.getCompound("Chain"));
            }
            this.controllerOffset = null;
        } else {
            this.controlledChain = null;
        }
        this.syncControllerRegistration();
    }

    @Override
    public float propagateRotationTo(final KineticBlockEntity target,
                                     final BlockState stateFrom,
                                     final BlockState stateTo,
                                     final BlockPos diff,
                                     final boolean connectedViaAxes,
                                     final boolean connectedViaCogs) {
        if (connectedViaAxes && Math.abs(diff.get(CogwheelChainCandidate.getAxis(this.getBlockState()))) == 1)
            return 0;

        //Else, check if this is the same chain structure.
        return this.<CogwheelChainBehaviour>getSameBehaviourOptional(target)
                .map((otherBehaviour) -> {
                    final boolean isControlledBySame =
                            this.isInSameChain(otherBehaviour);

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
                        this.controllerOffset.offset(this.getPos()).equals(otherBehaviour.controllerOffset.offset(
                                otherBehaviour.getPos())));
    }

    public float getChainRotationFactor() {
        if (this.controlledChain != null) {
            final PathedCogwheelNode controllerNode = this.controlledChain.getNodeFromControllerOffset(new Vec3i(
                    0,
                    0,
                    0
            ));
            if (controllerNode == null) return 0;

            return controllerNode.sideFactor();
        }

        if (this.getLevel() == null || this.controllerOffset == null) return 0;

        final BlockPos controllerPos = this.getPos().offset(this.controllerOffset);
        return this.<CogwheelChainBehaviour>getSameBehaviourOptional(controllerPos)
                .map(controller -> {
                    if (controller.controlledChain == null) return 0f;

                    final PathedCogwheelNode nodeInChain = controller.controlledChain.getNodeFromControllerOffset(this.controllerOffset);
                    return nodeInChain == null ? 0f : nodeInChain.sideFactor();
                }).orElse(0f);
    }

    private void writeConnectionInfo(final CompoundTag compound) {
        if (this.controllerOffset != null) {
            compound.putInt("ControllerOffsetX", this.controllerOffset.getX());
            compound.putInt("ControllerOffsetY", this.controllerOffset.getY());
            compound.putInt("ControllerOffsetZ", this.controllerOffset.getZ());
        }

        if (this.controlledChain != null) {
            final CompoundTag chainTag = new CompoundTag();
            this.controlledChain.write(chainTag);
            compound.put("Chain", chainTag);
            compound.putInt("ChainsToRefund", this.chainsToRefund);
        }
    }

    public void setAsController(final CogwheelChain cogwheelChain) {
        this.controlledChain = cogwheelChain;
        this.controllerOffset = null;
        this.syncControllerRegistration();
    }

    @Override
    public List<BlockPos> addExtraPropagationLocations(final IRotate block,
                                                       final BlockState state,
                                                       final List<BlockPos> neighbours) {
        final List<BlockPos> toPropagate = new ArrayList<>(KineticBehaviourExtension.super.addExtraPropagationLocations(
                block,
                state,
                neighbours
        ));
        if (this.controlledChain != null) {
            this.addPropagationLocationsFromControllerExcept(toPropagate, this.getPos());
        } else {
            //Test putting child to child connections
            if (this.controllerOffset != null && this.getLevel() != null) {
                final BlockPos controllerPos = this.getPos().offset(this.controllerOffset);
                this.<CogwheelChainBehaviour>getSameBehaviourOptional(controllerPos)
                        .ifPresent(controller -> controller.addPropagationLocationsFromControllerExcept(
                                toPropagate,
                                this.getPos()
                        ));
            }
        }

        return toPropagate;
    }

    private void addPropagationLocationsFromControllerExcept(final List<BlockPos> toPropagate, final BlockPos exclude) {
        if (this.controlledChain == null) return;
        for (final PathedCogwheelNode cogwheelNode : this.controlledChain.getChainPathCogwheelNodes()) {
            final BlockPos cogwheelPos = this.getPos().offset(cogwheelNode.localPos());
            if (!toPropagate.contains(cogwheelPos) && !cogwheelPos.equals(exclude)) {
                toPropagate.add(cogwheelPos);
            }
        }
    }

    public boolean isController() {
        return this.controlledChain != null;
    }

    public void setController(final Vec3i offset) {
        this.unregisterController();
        this.controlledChain = null;
        this.controllerOffset = offset;
    }

    public @Nullable CogwheelChain getControlledChain() {
        return this.controlledChain;
    }

    public @Nullable Vec3i getControllerOffset() {
        return this.controllerOffset;
    }

    public void setChainsUsed(final int chainsUsed) {
        this.chainsToRefund = chainsUsed;
    }

    public void clearStoredChains() {
        if (this.controlledChain != null) {
            this.chainsToRefund = 0;
        } else {
            if (this.controllerOffset != null && this.getLevel() != null) {
                final BlockPos controllerPos = this.getPos().offset(this.controllerOffset);
                this.<CogwheelChainBehaviour>getSameBehaviourOptional(controllerPos)
                        .ifPresent(controller -> controller.chainsToRefund = 0);
            }
        }
    }

    @Override
    public void transform(final BlockEntity blockEntity, final StructureTransform transform) {
        if (this.controlledChain != null)
            this.controlledChain.transform(transform);
        if (this.controllerOffset != null) {
            final Vec3i transformedOffset = transform.applyWithoutOffset(new BlockPos(this.controllerOffset));
            this.setController(transformedOffset);
        }
        this.syncControllerRegistration();
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
        final boolean isCreative = event.getPlayer().hasInfiniteMaterials();
        if (this.tryReplaceWithResidualChain(isCreative)) {
            return;
        }
        this.destroyChain(!isCreative, true);
    }

    private boolean tryReplaceWithResidualChain(final boolean isCreative) {
        if (this.getLevel() == null) return false;

        final BlockPos controllerWorldPos;
        final CogwheelChainBehaviour controllerBehaviour;

        if (this.isController()) {
            controllerWorldPos = this.getPos();
            controllerBehaviour = this;
        } else if (this.controllerOffset != null) {
            controllerWorldPos = this.getPos().offset(this.controllerOffset);
            controllerBehaviour = this.<CogwheelChainBehaviour>getSameBehaviourOptional(controllerWorldPos)
                    .filter(CogwheelChainBehaviour::isController)
                    .orElse(null);
            if (controllerBehaviour == null) return false;
        } else {
            return false;
        }

        final CogwheelChain existingChain = controllerBehaviour.controlledChain;
        if (existingChain == null) return false;

        final ResidualChainResult result = ResidualChainResult.tryBuildResidualChain(
                existingChain, controllerWorldPos, this.getPos());
        if (result == null) return false;

        final int oldCost = controllerBehaviour.chainsToRefund;
        final int newCost = result.placingChain().getChainsRequiredInLoop();
        final int costDifference = oldCost - newCost;

        controllerBehaviour.destroyChain(false, true);

        result.chain().placeInLevel(this.getLevel(), result.placingChain());

        if (!isCreative && costDifference > 0) {
            final ItemStack drops = existingChain.getReturnedItem().getDefaultInstance().copyWithCount(costDifference);
            Block.popResource(this.getLevel(), this.getPos(), drops);
        }

        return true;
    }

    /**
     * Since we have the visual to use, no forcing is necessary
     */
    @Override
    public boolean rendersWhenVisualizationAvailable() {
        return false;
    }

    public void disconnectFromChain() {//TODO: investage why flip = destroy
        this.unregisterController();
        this.controlledChain = null;
        this.controllerOffset = null;
        this.chainsToRefund = 0;
        this.detachKinetics();
        this.sendData();
    }

    private void syncControllerRegistration() {
        if (this.controlledChain == null) {
            this.unregisterController();
            return;
        }
        this.registerController();
    }

    private void registerController() {
        if (!this.hasLevel() || this.controlledChain == null) {
            return;
        }
        final BlockPos currentPos = this.getPos().immutable();
        final CogwheelChainWorld chainWorld = CogwheelChainWorld.get(this.getLevel());
        if (this.registeredControllerPos != null && !this.registeredControllerPos.equals(currentPos)) {
            chainWorld.remove(this.registeredControllerPos);
        }
        chainWorld.put(currentPos, this.controlledChain);
        this.registeredControllerPos = currentPos;
    }

    private void unregisterController() {
        if (!this.hasLevel()) {
            this.registeredControllerPos = null;
            return;
        }
        final CogwheelChainWorld chainWorld = CogwheelChainWorld.get(this.getLevel());
        if (this.registeredControllerPos != null) {
            chainWorld.remove(this.registeredControllerPos);
            this.registeredControllerPos = null;
            return;
        }
        chainWorld.remove(this.getPos());
    }

}
