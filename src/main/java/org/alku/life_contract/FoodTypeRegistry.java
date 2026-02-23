package org.alku.life_contract;

import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

public class FoodTypeRegistry {
    public enum FoodType {
        MEAT_RAW("meat_raw", "生肉类"),
        MEAT_COOKED("meat_cooked", "熟肉类"),
        FISH_RAW("fish_raw", "生鱼类"),
        FISH_COOKED("fish_cooked", "熟鱼类"),
        VEGETABLE("vegetable", "蔬菜类"),
        FRUIT("fruit", "水果类"),
        BAKERY("bakery", "烘焙食品"),
        SOUP("soup", "汤类"),
        SWEET("sweet", "甜食类"),
        DAIRY("dairy", "乳制品"),
        EGG("egg", "蛋类"),
        MUSHROOM("mushroom", "蘑菇类"),
        SPECIAL("special", "特殊食物"),
        OTHER("other", "其他食物");

        private final String id;
        private final String displayName;

        FoodType(String id, String displayName) {
            this.id = id;
            this.displayName = displayName;
        }

        public String getId() {
            return id;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private static final Map<String, FoodType> ITEM_FOOD_TYPE_MAP = new HashMap<>();
    private static final Set<String> REGISTERED_ITEMS = new HashSet<>();

    static {
        registerFoodTypes();
    }

    private static void registerFoodTypes() {
        registerMeatRaw();
        registerMeatCooked();
        registerFishRaw();
        registerFishCooked();
        registerVegetables();
        registerFruits();
        registerBakery();
        registerSoups();
        registerSweets();
        registerDairy();
        registerEggs();
        registerMushrooms();
        registerSpecial();
    }

    private static void registerMeatRaw() {
        String[] items = {
            "minecraft:beef", "minecraft:porkchop", "minecraft:chicken",
            "minecraft:mutton", "minecraft:rabbit", "minecraft:rotten_flesh"
        };
        for (String item : items) {
            ITEM_FOOD_TYPE_MAP.put(item, FoodType.MEAT_RAW);
        }
    }

    private static void registerMeatCooked() {
        String[] items = {
            "minecraft:cooked_beef", "minecraft:cooked_porkchop", "minecraft:cooked_chicken",
            "minecraft:cooked_mutton", "minecraft:cooked_rabbit"
        };
        for (String item : items) {
            ITEM_FOOD_TYPE_MAP.put(item, FoodType.MEAT_COOKED);
        }
    }

    private static void registerFishRaw() {
        String[] items = {
            "minecraft:cod", "minecraft:salmon", "minecraft:tropical_fish",
            "minecraft:pufferfish"
        };
        for (String item : items) {
            ITEM_FOOD_TYPE_MAP.put(item, FoodType.FISH_RAW);
        }
    }

    private static void registerFishCooked() {
        String[] items = {
            "minecraft:cooked_cod", "minecraft:cooked_salmon"
        };
        for (String item : items) {
            ITEM_FOOD_TYPE_MAP.put(item, FoodType.FISH_COOKED);
        }
    }

    private static void registerVegetables() {
        String[] items = {
            "minecraft:carrot", "minecraft:potato", "minecraft:baked_potato",
            "minecraft:poisonous_potato", "minecraft:beetroot", "minecraft:dried_kelp"
        };
        for (String item : items) {
            ITEM_FOOD_TYPE_MAP.put(item, FoodType.VEGETABLE);
        }
    }

    private static void registerFruits() {
        String[] items = {
            "minecraft:apple", "minecraft:golden_apple", "minecraft:enchanted_golden_apple",
            "minecraft:melon_slice", "minecraft:chorus_fruit", "minecraft:glow_berries",
            "minecraft:sweet_berries"
        };
        for (String item : items) {
            ITEM_FOOD_TYPE_MAP.put(item, FoodType.FRUIT);
        }
    }

    private static void registerBakery() {
        String[] items = {
            "minecraft:bread", "minecraft:cookie", "minecraft:cake"
        };
        for (String item : items) {
            ITEM_FOOD_TYPE_MAP.put(item, FoodType.BAKERY);
        }
    }

    private static void registerSoups() {
        String[] items = {
            "minecraft:mushroom_stew", "minecraft:beetroot_soup", "minecraft:rabbit_stew",
            "minecraft:suspicious_stew"
        };
        for (String item : items) {
            ITEM_FOOD_TYPE_MAP.put(item, FoodType.SOUP);
        }
    }

    private static void registerSweets() {
        String[] items = {
            "minecraft:honey_bottle"
        };
        for (String item : items) {
            ITEM_FOOD_TYPE_MAP.put(item, FoodType.SWEET);
        }
    }

    private static void registerDairy() {
        String[] items = {
            "minecraft:milk_bucket"
        };
        for (String item : items) {
            ITEM_FOOD_TYPE_MAP.put(item, FoodType.DAIRY);
        }
    }

    private static void registerEggs() {
        String[] items = {
            "minecraft:egg"
        };
        for (String item : items) {
            ITEM_FOOD_TYPE_MAP.put(item, FoodType.EGG);
        }
    }

    private static void registerMushrooms() {
        String[] items = {
            "minecraft:red_mushroom", "minecraft:brown_mushroom"
        };
        for (String item : items) {
            ITEM_FOOD_TYPE_MAP.put(item, FoodType.MUSHROOM);
        }
    }

    private static void registerSpecial() {
        String[] items = {
            "minecraft:golden_apple", "minecraft:enchanted_golden_apple",
            "minecraft:chorus_fruit"
        };
        for (String item : items) {
            ITEM_FOOD_TYPE_MAP.put(item, FoodType.SPECIAL);
        }
    }

    public static FoodType getFoodType(Item item) {
        if (item == null) {
            return FoodType.OTHER;
        }
        
        ResourceLocation resourceLocation = ForgeRegistries.ITEMS.getKey(item);
        if (resourceLocation == null) {
            return FoodType.OTHER;
        }
        
        String itemId = resourceLocation.toString();
        
        FoodType foodType = ITEM_FOOD_TYPE_MAP.get(itemId);
        if (foodType != null) {
            return foodType;
        }
        
        return FoodType.OTHER;
    }

    public static FoodType getFoodType(String itemId) {
        if (itemId == null || itemId.isEmpty()) {
            return FoodType.OTHER;
        }
        
        FoodType foodType = ITEM_FOOD_TYPE_MAP.get(itemId);
        if (foodType != null) {
            return foodType;
        }
        
        return FoodType.OTHER;
    }

    public static String getFoodTypeId(Item item) {
        return getFoodType(item).getId();
    }

    public static String getFoodTypeDisplayName(Item item) {
        return getFoodType(item).getDisplayName();
    }

    public static boolean isValidFood(Item item) {
        return item != null && item.isEdible();
    }

    public static int getTotalFoodTypeCount() {
        return FoodType.values().length;
    }

    public static List<FoodType> getAllFoodTypes() {
        return Arrays.asList(FoodType.values());
    }
}
