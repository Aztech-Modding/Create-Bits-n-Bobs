package com.cake.azimuth.advancement;

import com.simibubi.create.foundation.advancement.CreateAdvancement;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.advancement.SimpleCreateTrigger;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.ItemUsedOnLocationTrigger;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class AzimuthAdvancement {

    private static final String SECRET_SUFFIX = "\n\u00A77(Hidden Advancement)";

    private final String modId;
    private final String id;
    private final Advancement.Builder mcBuilder = Advancement.Builder.advancement();

    private SimpleCreateTrigger builtinTrigger;
    private Supplier<ResourceLocation> parentSupplier;
    private final Builder azimuthBuilder = new Builder();

    private String title;
    private String description;

    public AzimuthAdvancement(String modId, String id, UnaryOperator<Builder> builder, Consumer<AzimuthAdvancement> entryCollector) {
        this.modId = modId;
        this.id = id;

        builder.apply(azimuthBuilder);

        if (!azimuthBuilder.externalTrigger) {
            builtinTrigger = AllTriggers.addSimple(modId + "_" + id + "_builtin");
            mcBuilder.addCriterion("0", builtinTrigger.createCriterion(builtinTrigger.instance()));
        }

        if (azimuthBuilder.type == TaskType.SECRET) {
            description += SECRET_SUFFIX;
        }

        entryCollector.accept(this);
    }

    private String titleKey() {
        return "advancement." + modId + "." + id;
    }

    private String descriptionKey() {
        return titleKey() + ".desc";
    }

    private ResourceLocation idAsResource() {
        return ResourceLocation.fromNamespaceAndPath(modId, id);
    }

    public boolean isAlreadyAwardedTo(Player player) {
        if (!(player instanceof ServerPlayer sp)) {
            return true;
        }
        AdvancementHolder advancement = sp.getServer().getAdvancements().get(idAsResource());
        if (advancement == null) {
            return true;
        }
        return sp.getAdvancements().getOrStartProgress(advancement).isDone();
    }

    public void awardTo(Player player) {
        if (!(player instanceof ServerPlayer sp)) {
            return;
        }
        if (builtinTrigger == null) {
            throw new UnsupportedOperationException("Advancement " + id + " uses external triggers and cannot be awarded directly");
        }
        builtinTrigger.trigger(sp);
    }

    public void save(Consumer<AdvancementHolder> consumer) {
        if (parentSupplier != null) {
            mcBuilder.parent(parentSupplier.get());
        }

        if (azimuthBuilder.iconSupplier != null) {
            azimuthBuilder.icon(azimuthBuilder.iconSupplier.get());
        }

        mcBuilder.display(
                azimuthBuilder.icon,
                Component.translatable(titleKey()),
                Component.translatable(descriptionKey()).withStyle(s -> s.withColor(0xDBA213)),
                id.equals("root") ? ResourceLocation.fromNamespaceAndPath(modId, "textures/gui/advancements.png") : null,
                azimuthBuilder.type.advancementType,
                azimuthBuilder.type.toast,
                azimuthBuilder.type.announce,
                azimuthBuilder.type.hide
        );

        mcBuilder.save(consumer, idAsResource().toString());
    }

    public void provideLang(BiConsumer<String, String> consumer) {
        consumer.accept(titleKey(), title);
        consumer.accept(descriptionKey(), description);
    }

    public enum TaskType {
        SILENT(AdvancementType.TASK, false, false, false),
        NORMAL(AdvancementType.TASK, true, false, false),
        NOISY(AdvancementType.TASK, true, true, false),
        EXPERT(AdvancementType.GOAL, true, true, false),
        SECRET(AdvancementType.GOAL, true, true, true);

        private final AdvancementType advancementType;
        private final boolean toast;
        private final boolean announce;
        private final boolean hide;

        TaskType(AdvancementType advancementType, boolean toast, boolean announce, boolean hide) {
            this.advancementType = advancementType;
            this.toast = toast;
            this.announce = announce;
            this.hide = hide;
        }
    }

    public class Builder {

        private TaskType type = TaskType.NORMAL;
        private boolean externalTrigger;
        private int keyIndex;
        private ItemStack icon;
        private Supplier<ItemStack> iconSupplier;

        public Builder special(TaskType type) {
            this.type = type;
            return this;
        }

        public Builder after(AzimuthAdvancement other) {
            AzimuthAdvancement.this.parentSupplier = other::idAsResource;
            return this;
        }

        public Builder after(CreateAdvancement other) {
            AzimuthAdvancement.this.parentSupplier = () -> CreateAdvancementIdAccessor.asId(other);
            return this;
        }

        public Builder after(Supplier<CreateAdvancement> other) {
            AzimuthAdvancement.this.parentSupplier = () -> CreateAdvancementIdAccessor.asId(other.get());
            return this;
        }

        public Builder after(ResourceLocation parentId) {
            AzimuthAdvancement.this.parentSupplier = () -> parentId;
            return this;
        }

        public Builder icon(ItemProviderEntry<?, ?> item) {
            this.iconSupplier = item::asStack;
            return this;
        }

        public Builder icon(ItemLike item) {
            return icon(new ItemStack(item));
        }

        public Builder icon(ItemStack stack) {
            this.icon = stack;
            return this;
        }

        public Builder title(String title) {
            AzimuthAdvancement.this.title = title;
            return this;
        }

        public Builder description(String description) {
            AzimuthAdvancement.this.description = description;
            return this;
        }

        public Builder whenBlockPlaced(Block block) {
            return externalTrigger(ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(block));
        }

        public Builder whenIconCollected() {
            return externalTrigger(InventoryChangeTrigger.TriggerInstance.hasItems(icon.getItem()));
        }

        public Builder whenItemCollected(ItemProviderEntry<?, ?> item) {
            return whenItemCollected(item.asStack().getItem());
        }

        public Builder whenItemCollected(ItemLike itemProvider) {
            return externalTrigger(InventoryChangeTrigger.TriggerInstance.hasItems(itemProvider));
        }

        public Builder whenItemCollected(TagKey<Item> tag) {
            return externalTrigger(InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(tag).build()));
        }

        public Builder awardedForFree() {
            return externalTrigger(InventoryChangeTrigger.TriggerInstance.hasItems(new ItemLike[]{}));
        }

        public Builder externalTrigger(Criterion<?> trigger) {
            mcBuilder.addCriterion(String.valueOf(keyIndex), trigger);
            externalTrigger = true;
            keyIndex++;
            return this;
        }
    }
}
