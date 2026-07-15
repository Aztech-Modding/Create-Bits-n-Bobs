package com.kipti.bnb.registry.core;

/**
 * ts is NOT ai twin this is my own cursed ass enum alternative that allows for type specific varargs
 *
 */
public class FeatureCategories {

    public static BlockFeatureCategory BLOCK = new BlockFeatureCategory();
    public static ItemFeatureCategory ITEM = new ItemFeatureCategory();
    public static BehaviourFeatureCategory BEHAVIOUR = new BehaviourFeatureCategory();

    public static class FeatureCategory {

        public String serialName() {
            return "unknown";
        }

    }

    public static class BlockFeatureCategory extends FeatureCategory {

        @Override
        public String serialName() {
            return "blocks";
        }

    }

    public static class ItemFeatureCategory extends FeatureCategory {

        @Override
        public String serialName() {
            return "items";
        }
    }

    public static class BehaviourFeatureCategory extends FeatureCategory {

        @Override
        public String serialName() {
            return "behaviours";
        }

    }

    public static FeatureCategory[] values() {
        return new FeatureCategory[]{BLOCK, ITEM, BEHAVIOUR};
    }


}
