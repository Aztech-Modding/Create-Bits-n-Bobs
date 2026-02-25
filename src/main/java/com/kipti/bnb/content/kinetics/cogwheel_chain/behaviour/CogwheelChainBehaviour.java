package com.kipti.bnb.content.kinetics.cogwheel_chain.behaviour;

import com.cake.azimuth.behaviour.SuperBlockEntityBehaviour;
import com.cake.azimuth.behaviour.extensions.ItemRequirementBehaviourExtension;
import com.cake.azimuth.behaviour.extensions.KineticBehaviourExtension;
import com.cake.azimuth.behaviour.extensions.RenderedBehaviourExtension;
import com.kipti.bnb.CreateBitsnBobs;
import com.kipti.bnb.content.kinetics.cogwheel_chain.block.CogwheelChainBlockEntity;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.CogwheelChain;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.PathedCogwheelNode;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.RenderedChainPathNode;
import com.kipti.bnb.content.kinetics.cogwheel_chain.shape.CogwheelChainInteractionHandler;
import com.kipti.bnb.content.kinetics.cogwheel_chain.shape.CogwheelChainShape;
import com.kipti.bnb.content.kinetics.cogwheel_chain.shape.CogwheelChainWholeShape;
import com.kipti.bnb.content.kinetics.cogwheel_chain.types.CogwheelChainType;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
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
        if (controlledChain != null) {
            if (!controlledChain.checkIntegrity(getLevel(), getPos())) {
                destroyChain(true);
            }
            updateChainShapes();
        } else {
            if (controllerOffset != null && getLevel() != null) {
                final BlockPos controllerPos = getPos().offset(controllerOffset);
                final BlockEntity be = getLevel().getBlockEntity(controllerPos);
                if (!(be instanceof CogwheelChainBlockEntity)) {
                    CogwheelChain.removeChainCogwheelFromLevelIfPresent(getLevel(), getPos());
                }
            }
        }
    }

    @Override
    public void writeSafe(final CompoundTag tag, final HolderLookup.Provider registries) {
        super.writeSafe(tag, registries);
        writeConnectionInfo(tag);
    }

    @Override
    public void destroy() {
        super.destroy();
        destroyChain(true);
    }

    @Override
    public ItemRequirement getRequiredItems(final BlockState state) {
        return isController() ? new ItemRequirement(
                ItemRequirement.ItemUseType.CONSUME,
                Blocks.CHAIN.asItem().getDefaultInstance().copyWithCount(controlledChain != null ? controlledChain.getChainsRequired() : 0)
        ) : ItemRequirement.NONE;
    }

    public void destroyChain(final boolean dropItemsInWorld) {
        destroyChain(dropItemsInWorld, false);
    }

    private void updateChainShapes() {
        if (controlledChain == null || getLevel() == null || !isClientLevel()) {
            return;
        }

        final List<RenderedChainPathNode> nodes = controlledChain.getChainPathNodes();
        if (nodes.size() < 2) {
            CogwheelChainInteractionHandler.invalidate(getLevel(), getPos());
            return;
        }

        final CogwheelChainType type = controlledChain.getChainType();
        final CogwheelChainType.ChainRenderInfo renderInfo = type.getRenderType();
        final double baseRadius = Math.max(renderInfo.getWidth(), renderInfo.getHeight()) / 32.0;

        final List<Vec3> path = new ArrayList<>();
        for (final RenderedChainPathNode node : nodes) {
            path.add(node.getPosition());
        }

        final List<CogwheelChainShape> shapes = List.of(new CogwheelChainWholeShape(path, baseRadius));
        CogwheelChainInteractionHandler.put(getLevel(), getPos(), shapes);
    }

    public ItemStack destroyChain(final boolean dropItemsInWorld, final boolean effects) {
        if (getLevel() == null) return ItemStack.EMPTY;
        invalidateClientChainShapeCache();

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
        this.chainsToRefund = 0; // Reset after dropping

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
        return drops;
    }

    private void invalidateClientChainShapeCache() {
        if (isClientLevel()) {
            return;
        }

        if (isController()) {
            CogwheelChainInteractionHandler.invalidate(getLevel(), getPos());
            return;
        }

        if (controllerOffset != null) {
            CogwheelChainInteractionHandler.invalidate(getLevel(), getPos().offset(controllerOffset));
        }
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
            if (controlledChain != null && isClientLevel()) {
                updateChainShapes();
            }
        } else {
            controlledChain = null;
            invalidateClientChainShapeCache();
        }
    }

    @Override
    public float propagateRotationTo(final KineticBlockEntity target, final BlockState stateFrom, final BlockState stateTo, final BlockPos diff, final boolean connectedViaAxes, final boolean connectedViaCogs) {
        if (connectedViaAxes && Math.abs(diff.get(getBlockState().getValue(BlockStateProperties.AXIS))) == 1)
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
        if (isClientLevel()) {
            updateChainShapes();
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
        this.controlledChain = null;
        this.controllerOffset = offset;
        invalidateClientChainShapeCache();
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
            controlledChain.transform(blockEntity, transform);
        if (controllerOffset != null) {
            final Vec3i transformedOffset = transform.applyWithoutOffset(new BlockPos(controllerOffset));
            setController(transformedOffset);
        }
    }

    public boolean isPartOfChain() {
        return this.controlledChain != null && this.controllerOffset != null;
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

    @Override
    public boolean shouldAlwaysActivateRenderer() {
        return false;
    }

}
