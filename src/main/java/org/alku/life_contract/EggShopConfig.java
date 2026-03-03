package org.alku.life_contract;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.BufferedReader;
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
    private static File simpleConfigFile;

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
                    ItemStack stack = new ItemStack(eggItem, count);
                    stack.getOrCreateTag().putBoolean("EggShopFollower", true);
                    return stack;
                }
            }
            return ItemStack.EMPTY;
        }
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
        simpleConfigFile = new File(configDir, "life_contract_egg_shop_prices.txt");
        
        if (simpleConfigFile.exists()) {
            loadSimpleConfig();
        } else if (configFile.exists()) {
            load();
        } else {
            createDefaultSimpleConfig();
            loadSimpleConfig();
        }
    }

    private static void createDefaultSimpleConfig() {
        try (FileWriter writer = new FileWriter(simpleConfigFile)) {
            writer.write("# 生物蛋商店配置文件\n");
            writer.write("# 格式: entity_id price\n");
            writer.write("# entity_id: 生物的注册ID (如 minecraft:zombie)\n");
            writer.write("# price: 价格 (金币数量)\n");
            writer.write("# 以 # 开头的行为注释，会被忽略\n");
            writer.write("# 示例:\n");
            writer.write("# minecraft:zombie 5\n");
            writer.write("# minecraft:skeleton 5\n");
            writer.write("#\n");
            writer.write("# === the_flesh_that_hates ===\n");
            writer.write("the_flesh_that_hates:pig 6\n");
            writer.write("the_flesh_that_hates:cow 6\n");
            writer.write("the_flesh_that_hates:sheep 6\n");
            writer.write("the_flesh_that_hates:piece 6\n");
            writer.write("the_flesh_that_hates:servant 6\n");
            writer.write("the_flesh_that_hates:human 8\n");
            writer.write("the_flesh_that_hates:pillager 8\n");
            writer.write("the_flesh_that_hates:plaquecontaminator 10\n");
            writer.write("the_flesh_that_hates:villager 10\n");
            writer.write("the_flesh_that_hates:boomer 12\n");
            writer.write("the_flesh_that_hates:dog 12\n");
            writer.write("the_flesh_that_hates:vindicator 12\n");
            writer.write("the_flesh_that_hates:flesh_howler 14\n");
            writer.write("the_flesh_that_hates:plaquecreatureone 20\n");
            writer.write("the_flesh_that_hates:bruteplaquecreatureone 20\n");
            writer.write("the_flesh_that_hates:flesh_hunter_two 30\n");
            writer.write("the_flesh_that_hates:plaquecrerturetwo 30\n");
            writer.write("the_flesh_that_hates:plaquethreelegcreature 30\n");
            writer.write("the_flesh_that_hates:flesh_suffer 50\n");
            writer.write("the_flesh_that_hates:flesh_hunter 60\n");
            writer.write("the_flesh_that_hates:flesh_community 150\n");
            writer.write("# === spore ===\n");
            writer.write("spore:infected 6\n");
            writer.write("spore:inf_pillager 7\n");
            writer.write("spore:inf_husk 8\n");
            writer.write("spore:inf_drowned 8\n");
            writer.write("spore:inf_villager 8\n");
            writer.write("spore:inf_diseased_villager 8\n");
            writer.write("spore:inf_witch 10\n");
            writer.write("spore:inf_wanderer 10\n");
            writer.write("spore:inf_player 12\n");
            writer.write("spore:inf_vind 25\n");
            writer.write("spore:inf_evo 20\n");
            writer.write("spore:inf_hazmat 10\n");
            writer.write("spore:scent 25\n");
            writer.write("spore:knight 35\n");
            writer.write("spore:griefer 35\n");
            writer.write("spore:braio 35\n");
            writer.write("spore:slasher 33\n");
            writer.write("spore:leaper 35\n");
            writer.write("spore:jagd 40\n");
            writer.write("spore:busser 40\n");
            writer.write("spore:spitter 30\n");
            writer.write("spore:stalker 40\n");
            writer.write("spore:howler 45\n");
            writer.write("spore:prot 45\n");
            writer.write("spore:scavenger 45\n");
            writer.write("spore:brute 50\n");
            writer.write("spore:inebriater 35\n");
            writer.write("spore:volatile 50\n");
            writer.write("spore:meph 50\n");
            writer.write("spore:nuclea 55\n");
            writer.write("spore:gastgaber 60\n");
            writer.write("spore:specter 70\n");
            writer.write("spore:wendigo 90\n");
            writer.write("spore:inquisitor 90\n");
            writer.write("spore:brotkatze 90\n");
            writer.write("spore:ogre 95\n");
            writer.write("spore:hvindicator 95\n");
            writer.write("spore:hevoker 100\n");
            writer.write("spore:vanguard 100\n");
            writer.write("spore:brain_remnants 100\n");
            writer.write("# === phayriosisreborn ===\n");
            writer.write("phayriosisreborn:phayrector 12\n");
            writer.write("phayriosisreborn:converted_human 12\n");
            writer.write("phayriosisreborn:convertedvillager 12\n");
            writer.write("phayriosisreborn:converted_chicken 12\n");
            writer.write("phayriosisreborn:converted_sheep 12\n");
            writer.write("phayriosisreborn:converted_pillager 20\n");
            writer.write("phayriosisreborn:converted_husk 20\n");
            writer.write("phayriosisreborn:converted_creeper 25\n");
            writer.write("phayriosisreborn:converted_wandering_trader 30\n");
            writer.write("phayriosisreborn:converted_skeleton 20\n");
            writer.write("phayriosisreborn:converted_sniffer 35\n");
            writer.write("phayriosisreborn:converted_enderman 70\n");
            writer.write("phayriosisreborn:converted_revager 70\n");
            writer.write("phayriosisreborn:converted_golem 80\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadSimpleConfig() {
        ENTRIES.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(simpleConfigFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] parts = line.split("\\s+");
                if (parts.length >= 2) {
                    String entityId = parts[0];
                    try {
                        int price = Integer.parseInt(parts[1]);
                        String displayName = generateDisplayName(entityId);
                        ENTRIES.add(new EggShopEntry(entityId, displayName, new ItemStack(Life_contract.GOLD_COIN.get()), price, 1));
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid price for entity: " + entityId);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String generateDisplayName(String entityId) {
        String[] parts = entityId.split(":");
        String name = parts.length > 1 ? parts[1] : parts[0];
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (i == 0) {
                result.append(Character.toUpperCase(c));
            } else if (c == '_') {
                result.append(' ');
                if (i + 1 < name.length()) {
                    result.append(Character.toUpperCase(name.charAt(++i)));
                }
            } else {
                result.append(c);
            }
        }
        return result.toString() + " Spawn Egg";
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
                    ItemStack currency = currencyItem != null ? new ItemStack(currencyItem) : new ItemStack(Life_contract.GOLD_COIN.get());
                    
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
                entryObj.addProperty("currency", currencyLoc != null ? currencyLoc.toString() : Life_contract.MODID + ":gold_coin");
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
