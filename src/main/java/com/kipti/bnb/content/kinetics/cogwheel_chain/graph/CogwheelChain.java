package com.kipti.bnb.content.kinetics.cogwheel_chain.graph;

import com.cake.azimuth.behaviour.SuperBlockEntityBehaviour;
import com.kipti.bnb.content.kinetics.cogwheel_chain.behaviour.CogwheelChainBehaviour;
import com.kipti.bnb.content.kinetics.cogwheel_chain.segment.CogwheelChainSegment;
import com.kipti.bnb.content.kinetics.cogwheel_chain.segment.CogwheelChainSegmentBuilder;
import com.kipti.bnb.content.kinetics.cogwheel_chain.types.BnbCogwheelChainTypes;
import com.kipti.bnb.content.kinetics.cogwheel_chain.types.CogwheelChainType;
import com.kipti.bnb.registry.core.BnbRegistries;
import com.simibubi.create.content.contraptions.StructureTransform;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CogwheelChain {

    private List<PathedCogwheelNode> cogwheelNodes;
    private List<RenderedChainPathNode> renderedNodes;
    private List<CogwheelChainSegment> cachedSegments;
    private CogwheelChainType type;
    private Item returnedItem;
    private boolean flipInsideOutside;

    public CogwheelChain(final CompoundTag tag) {
        this.renderedNodes = new ArrayList<>();
        this.cogwheelNodes = new ArrayList<>();
        this.type = BnbCogwheelChainTypes.CHAIN.get();
        this.returnedItem = Items.CHAIN;
        this.read(tag);
    }

    public void read(final CompoundTag tag) {
        this.cogwheelNodes.clear();
        final int cogWheelPosCount = tag.getInt("cogwheel_pos_count");
        for (int i = 0; i < cogWheelPosCount; i++) {
            final CompoundTag posTag = tag.getCompound("cogwheel_pos_" + i);
            final PathedCogwheelNode pos = PathedCogwheelNode.read(posTag);
            this.cogwheelNodes.add(pos);
        }
        this.renderedNodes.clear();
        this.renderedNodes.addAll(CogwheelChainGeometryBuilder.buildFullChainFromPathNodes(this.cogwheelNodes));
        this.cachedSegments = null;
        if (tag.contains("chain_type")) {
            final ResourceLocation typeId = ResourceLocation.parse(tag.getString("chain_type"));
            final CogwheelChainType foundType = BnbRegistries.COGWHEEL_CHAIN_TYPES.get(typeId);
            if (foundType != null) {
                this.type = foundType;
            }
        }
        if (tag.contains("returned_item")) {
            final ResourceLocation itemId = ResourceLocation.parse(tag.getString("returned_item"));
            final Item foundItem = BuiltInRegistries.ITEM.get(itemId);
            if (foundItem != Items.AIR) {
                this.returnedItem = foundItem;
            }
        }
        this.updateInsideOutsideFlip();
    }

    private void updateInsideOutsideFlip() {
        if (!this.type.getRenderType().usesConsistentInsideOutside()) {
            this.flipInsideOutside = false;
            return;
        }

        int sideSum = 0;
        for (final PathedCogwheelNode node : this.cogwheelNodes) {
            sideSum += node.side();
        }

        this.flipInsideOutside = sideSum < 0;
    }

    public CogwheelChain(final List<PathedCogwheelNode> path, final CogwheelChainType type, final Item returnedItem) {
        this.cogwheelNodes = path;
        this.renderedNodes = CogwheelChainGeometryBuilder.buildFullChainFromPathNodes(path);
        this.cachedSegments = null;
        this.type = type;
        this.returnedItem = returnedItem;
        this.updateInsideOutsideFlip();
    }

    public @Nullable PathedCogwheelNode getNodeFromControllerOffset(final Vec3i controllerOffset) {
        final Vec3i offsetFromStart = controllerOffset.multiply(-1);

        for (final PathedCogwheelNode cogwheelNode : this.cogwheelNodes) {
            if (cogwheelNode.localPos().equals(offsetFromStart)) {
                return cogwheelNode;
            }
        }
        return null;
    }

    public boolean checkIntegrity(final Level level, final BlockPos origin) {
        for (final PathedCogwheelNode node : this.cogwheelNodes) {
            final BlockPos pos = node.localPos().offset(origin);
            if (!level.isLoaded(pos)) continue; //Skip checks if unloaded
            final BlockState state = level.getBlockState(pos);
            final CogwheelChainCandidate candidate = CogwheelChainCandidate.getForBlock(state);
            if (candidate == null || !candidate.isConsistentWithNode(node)) {
                return false;
            }
        }
        return true;
    }

    public int getChainsRequired() {
        double length = 0;
        for (int i = 0; i < this.cogwheelNodes.size(); i++) {
            final PathedCogwheelNode startNode = this.cogwheelNodes.get(i);
            final PathedCogwheelNode endNode = this.cogwheelNodes.get((i + 1) % this.cogwheelNodes.size());
            length += startNode.dist(endNode);
        }
        return PlacingCogwheelChain.getChainsRequiredForLength(length, this.type);
    }

    public void write(final CompoundTag tag) {
        tag.putInt("cogwheel_pos_count", this.cogwheelNodes.size());
        for (int i = 0; i < this.cogwheelNodes.size(); i++) {
            final CompoundTag posTag = new CompoundTag();
            this.cogwheelNodes.get(i).write(posTag);
            tag.put("cogwheel_pos_" + i, posTag);
        }
        tag.putString("chain_type", this.type.getKey().toString());
        tag.putString("returned_item", BuiltInRegistries.ITEM.getKey(this.returnedItem).toString());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.renderedNodes, this.type, this.returnedItem);
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || this.getClass() != o.getClass()) return false;
        final CogwheelChain that = (CogwheelChain) o;
        return Objects.equals(this.renderedNodes, that.renderedNodes)
                && Objects.equals(this.type, that.type)
                && Objects.equals(this.returnedItem, that.returnedItem);
    }

    public void placeInLevel(final Level level, final PlacingCogwheelChain source) {
        final BlockPos controllerPos = source.getFirstNode().pos();
        final int chainsUsed = source.getChainsRequiredInLoop(this.type);

        for (final PlacingCogwheelNode node : source.getVisitedNodes()) {
            if (level.getBlockEntity(node.pos()) instanceof final KineticBlockEntity kbe) {
                kbe.detachKinetics();
            }
        }

        boolean isController = true;
        for (final PlacingCogwheelNode node : source.getVisitedNodes()) {
            this.placeChainCogwheelInLevel(level, node, isController, chainsUsed, controllerPos);
            isController = false;
        }

        final BlockEntity be = level.getBlockEntity(controllerPos);
        if (be instanceof final KineticBlockEntity kbe) {
            kbe.updateSpeed = true;
        } else {
            throw new IllegalStateException(
                    "Expected a kinetic block entity at the controller position when placing a cogwheel chain, but found none! Position: " + controllerPos);
        }
    }

    private void placeChainCogwheelInLevel(final Level level,
                                           final PlacingCogwheelNode node,
                                           final boolean isController,
                                           final int chainsUsed,
                                           final BlockPos controllerPos) {
        final CogwheelChainBehaviour behaviour = SuperBlockEntityBehaviour.getOrThrow(
                level,
                node.pos(),
                CogwheelChainBehaviour.TYPE
        );

        if (isController) {
            behaviour.setAsController(this);
            behaviour.setChainsUsed(chainsUsed);
        } else {
            behaviour.setController(controllerPos.subtract(node.pos()));
        }

        behaviour.sendData();
    }

    /**
     * Destroys this chain by clearing all member data and properly detaching kinetics.
     * <p>
     * Uses a three-phase approach to avoid race conditions with Create's kinetic BFS propagator:
     * <ol>
     *     <li>Clear chain data on all members so chain connections become invisible to the propagator</li>
     *     <li>Detach kinetics on all members (only axis neighbours are visible, preventing re-propagation through dead chain links)</li>
     *     <li>Force-clear any residual kinetic state and mark members for speed re-evaluation from non-chain neighbours</li>
     * </ol>
     */
    public void destroy(final Level level, final BlockPos worldPosition) {
        final List<CogwheelChainBehaviour> behaviours = new ArrayList<>();
        for (final PathedCogwheelNode cogwheel : this.cogwheelNodes) {
            final BlockPos pos = worldPosition.offset(cogwheel.localPos());
            SuperBlockEntityBehaviour.getOptional(level, pos, CogwheelChainBehaviour.TYPE)
                    .ifPresent(behaviours::add);
        }

        for (final CogwheelChainBehaviour behaviour : behaviours) {
            behaviour.clearChainData();
        }

        this.detachAndResetKinetics(behaviours);
    }

    private void detachAndResetKinetics(final List<CogwheelChainBehaviour> behaviours) {
        for (final CogwheelChainBehaviour behaviour : behaviours) {
            if (behaviour.getBlockEntity() instanceof final KineticBlockEntity kbe
                    && kbe.getTheoreticalSpeed() != 0) {
                kbe.detachKinetics();
            }
        }

        for (final CogwheelChainBehaviour behaviour : behaviours) {
            if (behaviour.getBlockEntity() instanceof final KineticBlockEntity kbe) {
                if (kbe.getTheoreticalSpeed() != 0) {
                    kbe.removeSource();
                }
                kbe.updateSpeed = true;
            }
            behaviour.sendData();
        }
    }

    public void createDestroyEffects(final Level level, final BlockPos worldPosition) {
        if (!(level instanceof final ServerLevel serverLevel)) {
            return;
        }

        if (this.renderedNodes.size() < 2) {
            return;
        }

        final BlockState particleState = this.type.getBreakEffectsBlock().defaultBlockState();
        final BlockParticleOption particle = new BlockParticleOption(ParticleTypes.BLOCK, particleState);
        final Vec3 origin = Vec3.atLowerCornerOf(worldPosition);

        final SoundType soundType = particleState.getSoundType();
        serverLevel.playSound(
                null,
                worldPosition,
                soundType.getBreakSound(),
                SoundSource.BLOCKS,
                (soundType.getVolume() + 1.0F) / (this.cogwheelNodes.size() * 3f),
                soundType.getPitch() * 0.8F
        );

        final int size = this.renderedNodes.size();
        for (int i = 0; i < size; i++) {
            final Vec3 a = this.renderedNodes.get(i).getPosition().add(origin);
            final Vec3 b = this.renderedNodes.get((i + 1) % size).getPosition().add(origin);
            final Vec3 segment = b.subtract(a);
            final double distance = segment.length();
            if (distance < 1e-6) {
                continue;
            }

            final int samples = Math.max(2, (int) Math.ceil(distance * 1.5));
            for (int sample = 0; sample <= samples; sample++) {
                final double t = sample / (double) samples;
                final Vec3 p = a.lerp(b, t);

                serverLevel.sendParticles(
                        particle,
                        p.x,
                        p.y,
                        p.z,
                        1,
                        0.03,
                        0.03,
                        0.03,
                        0.01
                );
            }
        }
    }

    /**
     * Returns a lazily computed and cached list of {@link CogwheelChainSegment} instances
     * representing the typed, distance-annotated geometry of this chain.
     */
    public List<CogwheelChainSegment> getSegments() {
        if (this.cachedSegments == null) {
            this.cachedSegments = CogwheelChainSegmentBuilder.buildSegments(this.renderedNodes);
        }
        return this.cachedSegments;
    }

    /**
     * All nodes in the chain, there are typically multiple, as the path wraps around cogwheels
     */
    public List<RenderedChainPathNode> getChainPathNodes() {
        return this.renderedNodes;
    }

    /**
     * Each cogwheel in the chain
     */
    public List<PathedCogwheelNode> getChainPathCogwheelNodes() {
        return this.cogwheelNodes;
    }

    /**
     * See {@link BnbCogwheelChainTypes} for available types (in this mod).
     */
    public CogwheelChainType getChainType() {
        return this.type;
    }

    public Item getReturnedItem() {
        return this.returnedItem;
    }

    public boolean shouldFlipInsideOutside() {
        return this.flipInsideOutside;
    }

    public void transform(final StructureTransform transform) {
        final List<PathedCogwheelNode> newNodes = new ArrayList<>();
        for (final PathedCogwheelNode node : this.cogwheelNodes) {
            newNodes.add(node.transform(transform));
        }
        this.cogwheelNodes = newNodes;
        this.renderedNodes = CogwheelChainGeometryBuilder.buildFullChainFromPathNodes(this.cogwheelNodes);
        this.cachedSegments = null;
    }
}

