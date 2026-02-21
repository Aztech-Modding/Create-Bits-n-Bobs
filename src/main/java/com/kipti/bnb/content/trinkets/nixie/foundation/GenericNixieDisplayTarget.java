package com.kipti.bnb.content.trinkets.nixie.foundation;

import com.simibubi.create.api.behaviour.display.DisplayTarget;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class GenericNixieDisplayTarget extends DisplayTarget {
    public static void walkNixies(final Level level, final BlockPos pos, final TriConsumer<BlockPos, Integer, GenericNixieDisplayBlockEntity> consumer) {
        final BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof final GenericNixieDisplayBlockEntity displayBlockEntity)) {
            return;
        }
        final AtomicInteger consumedWidthOfCharacters = new AtomicInteger();
        displayBlockEntity.applyToEachElementOfThisStructure((nixieBE) -> {
            consumer.accept(nixieBE.getBlockPos(), consumedWidthOfCharacters.get(), nixieBE);
            consumedWidthOfCharacters.addAndGet(nixieBE.calculateDisplayedCharacterWidth());
        });
    }

    @Override
    public void acceptText(int line, final List<MutableComponent> text, final DisplayLinkContext context) {
        for (final MutableComponent lineComponent : text) {
            final String tagElement = Component.Serializer.toJson(lineComponent, context.level().registryAccess());
            final int finalLine = line;
            GenericNixieDisplayTarget.walkNixies(context.level(), context.getTargetPos(), (nixiePos, consumedWidth, nixieBE) -> {
                nixieBE.displayCustomText(tagElement, consumedWidth, finalLine);
            });
            line++;
        }
    }

    @Override
    public DisplayTargetStats provideStats(final DisplayLinkContext context) {
        final GenericNixieDisplayBlockEntity displayBlockEntity = (GenericNixieDisplayBlockEntity) context.getTargetBlockEntity();
        return new DisplayTargetStats(
                displayBlockEntity.currentDisplayOption == GenericNixieDisplayBlockEntity.ConfigurableDisplayOptions.DOUBLE_CHAR_DOUBLE_LINES
                        ? 2 : 1,
                displayBlockEntity.seekWidth(),
                this
        );
    }

    @Override
    public AABB getMultiblockBounds(final LevelAccessor level, final BlockPos pos) {
        final GenericNixieDisplayBlockEntity displayBlockEntity = (GenericNixieDisplayBlockEntity) level.getBlockEntity(pos);
        final AABB blockBounds = super.getMultiblockBounds(level, pos);
        final AABB[] resultBounds = {blockBounds};
        displayBlockEntity.applyToEachElementOfThisStructure((be) -> {
            final AABB elementBounds = blockBounds.move(be.getBlockPos().subtract(pos));
            resultBounds[0] = resultBounds[0].minmax(elementBounds);
        });
        return resultBounds[0];
    }
}

