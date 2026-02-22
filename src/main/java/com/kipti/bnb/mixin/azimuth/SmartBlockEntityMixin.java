package com.kipti.bnb.mixin.azimuth;

import com.cake.azimuth.behaviour.AzimuthSmartBlockEntityExtension;
import com.cake.azimuth.behaviour.CachedBehaviourExtensionAccess;
import com.cake.azimuth.behaviour.SuperBlockEntityBehaviour;
import com.cake.azimuth.behaviour.extensions.ItemRequirementBlockEntityBehaviourExtension;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Mixin(SmartBlockEntity.class)
public class SmartBlockEntityMixin extends CachedRenderBBBlockEntity implements AzimuthSmartBlockEntityExtension {

    @Shadow
    @Final
    private Map<BehaviourType<?>, BlockEntityBehaviour> behaviours;

    @Unique
    private final List<Runnable> azimuth$cacheClearListeners = new ArrayList<>();

    /**
     * A cache but it's just everything lmao
     */
    @Unique
    private final CachedBehaviourExtensionAccess<SuperBlockEntityBehaviour> azimuth$extensionCacheAccess =
            new CachedBehaviourExtensionAccess<>(() -> this, (e) -> true);

    //Non-integrated caches
    @Unique
    private final CachedBehaviourExtensionAccess<ItemRequirementBlockEntityBehaviourExtension> azimuth$itemRequirementExtension =
            new CachedBehaviourExtensionAccess<>(() -> this, (e) -> e instanceof ItemRequirementBlockEntityBehaviourExtension);

    public SmartBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void azimuth$updateBehaviourExtensionCache() {
        for (Runnable cacheClearListener : azimuth$cacheClearListeners) {
            cacheClearListener.run();
        }
    }

    @Override
    public SuperBlockEntityBehaviour[] azimuth$getSuperBlockEntityBehaviours() {
        return azimuth$extensionCacheAccess.get();
    }

    @SuppressWarnings("unchecked")
    @Unique
    public <T> List<T> azimuth$getExtensionBehavioursCache(Predicate<SuperBlockEntityBehaviour> filter) {
        return behaviours
                .values()
                .stream()
                .filter(
                        (beb) ->
                                beb instanceof SuperBlockEntityBehaviour sbeb &&
                                        filter.test(sbeb))
                .map(sbeb -> (T) sbeb).toList();
    }

    @Override
    public void azimuth$addCacheClearListener(Runnable cacheClearListener) {
        azimuth$cacheClearListeners.add(cacheClearListener);
    }

    @Override
    public CachedBehaviourExtensionAccess<ItemRequirementBlockEntityBehaviourExtension> azimuth$getItemRequirementExtensionCache() {
        return azimuth$itemRequirementExtension;
    }
}
