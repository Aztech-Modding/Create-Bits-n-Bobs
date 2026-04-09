package com.kipti.bnb.content.kinetics.cogwheel_chain.migration;

import com.kipti.bnb.CreateBitsnBobs;
import com.kipti.bnb.registry.content.blocks.BnbKineticBlocks;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.simpleRelays.SimpleKineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class MigratingSimpleKineticBlockEntity extends SimpleKineticBlockEntity {

    private boolean checkedMigration = false;

    public MigratingSimpleKineticBlockEntity(final BlockEntityType<?> type, final BlockPos pos, final BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void tick() {
        super.tick();

        final Level levelSafe = this.level;
        if (!checkedMigration && levelSafe != null && !levelSafe.isClientSide()) {
            checkedMigration = true;

            final BlockState state = getBlockState();
            final boolean isCreateCogwheel = AllBlocks.COGWHEEL.has(state) ||
                    AllBlocks.LARGE_COGWHEEL.has(state);
            final boolean isBnbCogwheel = BnbKineticBlocks.SMALL_FLANGED_COGWHEEL.has(state) ||
                    BnbKineticBlocks.LARGE_FLANGED_COGWHEEL.has(state);

            if (isCreateCogwheel || isBnbCogwheel) {
                final CompoundTag tag = saveWithFullMetadata(levelSafe.registryAccess());

                final String targetId = isCreateCogwheel ? "create:simple_kinetic" : "bits_n_bobs:simple_kinetic";
                tag.putString("id", targetId);

                final BlockEntity newBe = BlockEntity.loadStatic(worldPosition, state, tag, levelSafe.registryAccess());
                if (newBe != null && levelSafe.getServer() != null) {
                    levelSafe.getServer().execute(() -> {
                        if (levelSafe.getBlockEntity(worldPosition) == this) {
                            levelSafe.removeBlockEntity(worldPosition);
                            levelSafe.setBlockEntity(newBe);
                            levelSafe.sendBlockUpdated(worldPosition, state, state, 3);
                            CreateBitsnBobs.LOGGER.info("Migrated old form chain block entity at {} to {}", worldPosition, targetId);
                        }
                    });
                }
            }
        }
    }
}
