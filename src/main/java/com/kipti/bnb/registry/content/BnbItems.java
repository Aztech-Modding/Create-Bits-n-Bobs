package com.kipti.bnb.registry.content;

import com.kipti.bnb.CreateBitsnBobs;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.Tags;

import static com.kipti.bnb.CreateBitsnBobs.REGISTRATE;

public class BnbItems {

    //Stuff to be moved into big industry mod later
//    public static final ItemEntry<Item> CRUSHED_DEEPSLATE = REGISTRATE.item("crushed_deepslate", Item::new)
//            .register();
//
//    public static final ItemEntry<Item> CLINKER_SHARD = REGISTRATE.item("clinker_shard", Item::new)
//            .register();

    public static final ItemEntry<Item> ICON_LIGHTBULB = REGISTRATE.item("icon_lightbulb", Item::new)
            .model((c, p) -> p.withExistingParent(c.getName(), CreateBitsnBobs.asResource("block/lightbulb/lightbulb_on")))
            .register();
    public static final ItemEntry<Item> TEST_ROPE = REGISTRATE.item("test_rope", Item::new)
            .tag(Tags.Items.ROPES)
            .register();

    public static final ItemEntry<Item> COOKIE_DOUGH = REGISTRATE.item("cookie_dough", Item::new)
            .properties(p -> p.food(new FoodProperties.Builder()
                    .nutrition(1)
                    .saturationModifier(0.3F)
                    .effect(() -> new MobEffectInstance(MobEffects.POISON, 120, 1), 0.3F)
                    .build()))
            .register();


    public static void register() {
    }

}

