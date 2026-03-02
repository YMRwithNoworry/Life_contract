package org.alku.life_contract;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EggShopConfig {
    private static final List<EggShopEntry> ENTRIES = new ArrayList<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static File configFile;

    public static class EggShopEntry {
        private final String entityId;
        private final String displayName;
        private final ItemStack currency;
        private final int price;
        private final int count;

        public EggShopEntry(String entityId, String displayName, ItemStack currency, int price, int count) {
            this.entityId = entityId;
            this.displayName = displayName;
            this.currency = currency;
            this.price = price;
            this.count = count;
        }

        public String getEntityId() {
            return entityId;
        }

        public String getDisplayName() {
            return displayName;
        }

        public ItemStack getCurrency() {
            return currency.copy();
        }

        public int getPrice() {
            return price;
        }

        public int getCount() {
            return count;
        }

        public ItemStack createEggStack() {
            ResourceLocation entityLoc = new ResourceLocation(entityId);
            if (ForgeRegistries.ENTITY_TYPES.containsKey(entityLoc)) {
                net.minecraft.world.entity.EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(entityLoc);
                SpawnEggItem eggItem = SpawnEggItem.byId(entityType);
                if (eggItem != null) {
                    return new ItemStack(eggItem, count);
                }
            }
            return ItemStack.EMPTY;
        }
    }

    static {
        addDefaultEntries();
    }

    private static void addDefaultEntries() {
        addEntry(new EggShopEntry("minecraft:zombie", "僵尸刷怪蛋", new ItemStack(Items.GOLD_INGOT), 5, 1));
        addEntry(new EggShopEntry("minecraft:skeleton", "骷髅刷怪蛋", new ItemStack(Items.GOLD_INGOT), 5, 1));
        addEntry(new EggShopEntry("minecraft:spider", "蜘蛛刷怪蛋", new ItemStack(Items.GOLD_INGOT), 5, 1));
        addEntry(new EggShopEntry("minecraft:creeper", "苦力怕刷怪蛋", new ItemStack(Items.GOLD_INGOT), 8, 1));
        addEntry(new EggShopEntry("minecraft:enderman", "末影人刷怪蛋", new ItemStack(Items.GOLD_INGOT), 15, 1));
        addEntry(new EggShopEntry("minecraft:wolf", "狼刷怪蛋", new ItemStack(Items.GOLD_INGOT), 10, 1));
        addEntry(new EggShopEntry("minecraft:cow", "牛刷怪蛋", new ItemStack(Items.GOLD_INGOT), 3, 1));
        addEntry(new EggShopEntry("minecraft:pig", "猪刷怪蛋", new ItemStack(Items.GOLD_INGOT), 3, 1));
        addEntry(new EggShopEntry("minecraft:sheep", "羊刷怪蛋", new ItemStack(Items.GOLD_INGOT), 3, 1));
        addEntry(new EggShopEntry("minecraft:chicken", "鸡刷怪蛋", new ItemStack(Items.GOLD_INGOT), 2, 1));
        addEntry(new EggShopEntry("minecraft:iron_golem", "铁傀儡刷怪蛋", new ItemStack(Items.DIAMOND), 3, 1));
        addEntry(new EggShopEntry("minecraft:snow_golem", "雪傀儡刷怪蛋", new ItemStack(Items.GOLD_INGOT), 10, 1));
        addEntry(new EggShopEntry("minecraft:blaze", "烈焰人刷怪蛋", new ItemStack(Items.GOLD_INGOT), 20, 1));
        addEntry(new EggShopEntry("minecraft:ghast", "恶魂刷怪蛋", new ItemStack(Items.GOLD_INGOT), 25, 1));
        addEntry(new EggShopEntry("minecraft:witch", "女巫刷怪蛋", new ItemStack(Items.GOLD_INGOT), 15, 1));
        addEntry(new EggShopEntry("minecraft:villager", "村民刷怪蛋", new ItemStack(Items.EMERALD), 5, 1));
        addEntry(new EggShopEntry("minecraft:ravager", "劫掠兽刷怪蛋", new ItemStack(Items.DIAMOND), 10, 1));
        addEntry(new EggShopEntry("minecraft:phantom", "幻翼刷怪蛋", new ItemStack(Items.GOLD_INGOT), 12, 1));
    }

    public static void addEntry(EggShopEntry entry) {
        ENTRIES.add(entry);
    }

    public static void setEntries(List<EggShopEntry> entries) {
        ENTRIES.clear();
        ENTRIES.addAll(entries);
    }

    public static EggShopEntry getEntry(int index) {
        if (index >= 0 && index < ENTRIES.size()) {
            return ENTRIES.get(index);
        }
        return null;
    }

    public static List<EggShopEntry> getAllEntries() {
        return new ArrayList<>(ENTRIES);
    }

    public static int getEntryCount() {
        return ENTRIES.size();
    }

    public static void init(File configDir) {
        configFile = new File(configDir, "life_contract_egg_shop.json");
        if (configFile.exists()) {
            load();
        } else {
            save();
        }
    }

    public static void load() {
        if (configFile == null || !configFile.exists()) {
            return;
        }
        try (FileReader reader = new FileReader(configFile)) {
            JsonObject json = GSON.fromJson(reader, JsonObject.class);
            if (json.has("entries")) {
                JsonArray entriesArray = json.getAsJsonArray("entries");
                List<EggShopEntry> loadedEntries = new ArrayList<>();
                for (int i = 0; i < entriesArray.size(); i++) {
                    JsonObject entryObj = entriesArray.get(i).getAsJsonObject();
                    String entityId = entryObj.get("entityId").getAsString();
                    String displayName = entryObj.get("displayName").getAsString();
                    String currencyItemId = entryObj.get("currency").getAsString();
                    int price = entryObj.get("price").getAsInt();
                    int count = entryObj.has("count") ? entryObj.get("count").getAsInt() : 1;
                    
                    ResourceLocation currencyLoc = new ResourceLocation(currencyItemId);
                    net.minecraft.world.item.Item currencyItem = ForgeRegistries.ITEMS.getValue(currencyLoc);
                    ItemStack currency = currencyItem != null ? new ItemStack(currencyItem) : new ItemStack(Items.GOLD_INGOT);
                    
                    loadedEntries.add(new EggShopEntry(entityId, displayName, currency, price, count));
                }
                if (!loadedEntries.isEmpty()) {
                    ENTRIES.clear();
                    ENTRIES.addAll(loadedEntries);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        if (configFile == null) {
            return;
        }
        try (FileWriter writer = new FileWriter(configFile)) {
            JsonObject json = new JsonObject();
            JsonArray entriesArray = new JsonArray();
            for (EggShopEntry entry : ENTRIES) {
                JsonObject entryObj = new JsonObject();
                entryObj.addProperty("entityId", entry.getEntityId());
                entryObj.addProperty("displayName", entry.getDisplayName());
                ResourceLocation currencyLoc = ForgeRegistries.ITEMS.getKey(entry.getCurrency().getItem());
                entryObj.addProperty("currency", currencyLoc != null ? currencyLoc.toString() : "minecraft:gold_ingot");
                entryObj.addProperty("price", entry.getPrice());
                entryObj.addProperty("count", entry.getCount());
                entriesArray.add(entryObj);
            }
            json.add("entries", entriesArray);
            GSON.toJson(json, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
