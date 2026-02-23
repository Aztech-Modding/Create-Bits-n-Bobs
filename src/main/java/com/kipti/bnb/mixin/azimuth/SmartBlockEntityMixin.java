package com.kipti.bnb.mixin.azimuth;

import com.cake.azimuth.behaviour.*;
import com.cake.azimuth.behaviour.extensions.ItemRequirementBehaviourExtension;
import com.cake.azimuth.behaviour.extensions.RenderedBehaviourExtension;
import com.cake.azimuth.registration.BehaviourApplicators;
import com.simibubi.create.foundation.blockEntity.CachedRenderBBBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Mixin(SmartBlockEntity.class)
public abstract class SmartBlockEntityMixin extends CachedRenderBBBlockEntity implements AzimuthSmartBlockEntityExtension {

    @Shadow
    @Final
    private Map<BehaviourType<?>, BlockEntityBehaviour> behaviours;

    @Unique
    private final List<Runnable> azimuth$cacheClearListeners = new ArrayList<>();

    public SmartBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void azimuth$constructWithAdditionalBehaviours(BlockEntityType<?> type, BlockPos pos, BlockState state, CallbackInfo ci) {
        for (BlockEntityBehaviour b : BehaviourApplicators.getBehavioursFor((SmartBlockEntity) (Object) this)) {
            behaviours.put(b.getType(), b);
        }
    }

    @Override
    public void azimuth$updateBehaviourExtensionCache() {
        for (Runnable cacheClearListener : azimuth$cacheClearListeners) {
            cacheClearListener.run();
        }
    }

    @SuppressWarnings("unchecked")
    @Unique
    public <T> List<T> azimuth$searchExtensionBehaviours(Predicate<SuperBlockEntityBehaviour> filter) {
        return behaviours
                .values()
                .stream()
                .filter((beb) ->
                        beb instanceof SuperBlockEntityBehaviour sbeb &&
                                filter.test(sbeb))
                .map(sbeb -> (T) sbeb)
                .toList();
    }

    @Unique
    public SuperBlockEntityBehaviour[] azimuth$searchSuperBehaviours() {
        return behaviours
                .values()
                .stream()
                .filter((beb) ->
                        beb instanceof SuperBlockEntityBehaviour)
                .map(sbeb -> (SuperBlockEntityBehaviour) sbeb)
                .toArray(SuperBlockEntityBehaviour[]::new);
    }

    @Override
    public void azimuth$addCacheClearListener(Runnable cacheClearListener) {
        azimuth$cacheClearListeners.add(cacheClearListener);
    }

    @Unique
    private final CachedSuperBehaviourAccess azimuth$extensionCacheAccess =
            new CachedSuperBehaviourAccess(() -> this);

    //Non-integrated caches
    @Unique
    private final CachedBehaviourExtensionAccess<ItemRequirementBehaviourExtension> azimuth$itemRequirementExtension =
            new CachedBehaviourExtensionAccess<>(ItemRequirementBehaviourExtension.class, () -> this, (e) -> e instanceof ItemRequirementBehaviourExtension);

    @Unique
    private final CachedBehaviourExtensionAccess<RenderedBehaviourExtension> azimuth$renderedBehaviourCacheAccess =
            new CachedBehaviourExtensionAccess<>(RenderedBehaviourExtension.class, () -> this, (e) -> e instanceof RenderedBehaviourExtension);

    /**
     * A cache of specifically the super behaviours. Should be avoided in place of {@link BehaviourExtension}s
     */
    @Override
    public SuperBlockEntityBehaviour[] azimuth$getSuperBehaviours() {
        return azimuth$extensionCacheAccess.get();
    }

    @Override
    public ItemRequirementBehaviourExtension[] azimuth$getItemRequirementExtensionCache() {
        return azimuth$itemRequirementExtension.get();
    }

    @Override
    public RenderedBehaviourExtension[] azimuth$getRenderedExtensionCache() {
        return azimuth$renderedBehaviourCacheAccess.get();
    }
}
