package org.alku.airdrop.data;

import com.google.gson.*;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.item.ItemStack;
import org.alku.airdrop.Airdrop;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

/**
 * 空投池导入/导出工具类
 * 支持将空投池序列化为 JSON 文件或从 JSON 文件反序列化
 */
public class AirdropExportImport {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    /**
     * 获取导出目录 (config/airdrop/)
     */
    public static Path getExportDir() {
        Path dir = Path.of("config", "airdrop");
        if (!Files.exists(dir)) {
            try {
                Files.createDirectories(dir);
            } catch (IOException e) {
                Airdrop.LOGGER.error("Failed to create airdrop export directory", e);
            }
        }
        return dir;
    }

    /**
     * 获取所有可导入的文件名 (不含扩展名)
     */
    public static Set<String> getImportableFiles() {
        Path dir = getExportDir();
        Set<String> files = new java.util.LinkedHashSet<>();
        try {
            if (Files.exists(dir)) {
                try (var stream = Files.list(dir)) {
                    stream.filter(p -> p.toString().endsWith(".json"))
                            .forEach(p -> {
                                String name = p.getFileName().toString();
                                files.add(name.substring(0, name.length() - 5)); // 去掉 .json
                            });
                }
            }
        } catch (IOException e) {
            Airdrop.LOGGER.error("Failed to list airdrop export files", e);
        }
        return files;
    }

    // ===========================
    // 单个池导出/导入
    // ===========================

    /**
     * 导出单个空投池到 JSON 文件
     *
     * @param poolName 池名称
     * @param items    池内物品列表
     * @return 成功返回文件路径，失败返回 null
     */
    public static Path exportPool(String poolName, NonNullList<ItemStack> items) {
        try {
            JsonObject root = new JsonObject();
            root.addProperty("type", "single_pool");
            root.addProperty("pool_name", poolName);
            root.add("items", serializeItemList(items));

            Path file = getExportDir().resolve(poolName + ".json");
            try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                    Files.newOutputStream(file), StandardCharsets.UTF_8))) {
                GSON.toJson(root, writer);
            }

            Airdrop.LOGGER.info("Exported pool '{}' to {}", poolName, file);
            return file;
        } catch (Exception e) {
            Airdrop.LOGGER.error("Failed to export pool '{}'", poolName, e);
            return null;
        }
    }

    /**
     * 从 JSON 文件导入单个空投池
     *
     * @param fileName 文件名 (不含 .json 后缀)
     * @param data     要导入到的 AirdropSavedData
     * @return 导入的池名，失败返回 null
     */
    public static String importPool(String fileName, AirdropSavedData data) {
        try {
            Path file = getExportDir().resolve(fileName + ".json");
            if (!Files.exists(file)) {
                Airdrop.LOGGER.error("Import file not found: {}", file);
                return null;
            }

            String json;
            try (Reader reader = new BufferedReader(new InputStreamReader(
                    Files.newInputStream(file), StandardCharsets.UTF_8))) {
                json = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
            }

            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            String type = root.has("type") ? root.get("type").getAsString() : "single_pool";

            if ("all_pools".equals(type)) {
                // 这是全部池的文件，按 importAll 处理
                return importAllFromJson(root, data) > 0 ? "(all)" : null;
            }

            String poolName = root.get("pool_name").getAsString();
            JsonArray itemsArray = root.getAsJsonArray("items");
            NonNullList<ItemStack> items = deserializeItemList(itemsArray);

            data.savePool(poolName, items);
            Airdrop.LOGGER.info("Imported pool '{}' from {}", poolName, file);
            return poolName;
        } catch (Exception e) {
            Airdrop.LOGGER.error("Failed to import from '{}'", fileName, e);
            return null;
        }
    }

    // ===========================
    // 全部池导出/导入
    // ===========================

    /**
     * 导出所有空投池到单个 JSON 文件
     *
     * @param data     AirdropSavedData
     * @param fileName 文件名 (不含 .json 后缀)
     * @return 成功返回文件路径，失败返回 null
     */
    public static Path exportAll(AirdropSavedData data, String fileName) {
        try {
            JsonObject root = new JsonObject();
            root.addProperty("type", "all_pools");

            JsonObject poolsObj = new JsonObject();
            for (String poolName : data.getPoolNames()) {
                NonNullList<ItemStack> items = data.getPool(poolName);
                poolsObj.add(poolName, serializeItemList(items));
            }
            root.add("pools", poolsObj);

            Path file = getExportDir().resolve(fileName + ".json");
            try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                    Files.newOutputStream(file), StandardCharsets.UTF_8))) {
                GSON.toJson(root, writer);
            }

            Airdrop.LOGGER.info("Exported all pools to {}", file);
            return file;
        } catch (Exception e) {
            Airdrop.LOGGER.error("Failed to export all pools", e);
            return null;
        }
    }

    /**
     * 从 JSON 文件导入所有空投池
     *
     * @param fileName 文件名 (不含 .json 后缀)
     * @param data     要导入到的 AirdropSavedData
     * @return 导入的池数量，失败返回 -1
     */
    public static int importAll(String fileName, AirdropSavedData data) {
        try {
            Path file = getExportDir().resolve(fileName + ".json");
            if (!Files.exists(file)) {
                Airdrop.LOGGER.error("Import file not found: {}", file);
                return -1;
            }

            String json = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();

            return importAllFromJson(root, data);
        } catch (Exception e) {
            Airdrop.LOGGER.error("Failed to import all from '{}'", fileName, e);
            return -1;
        }
    }

    private static int importAllFromJson(JsonObject root, AirdropSavedData data) {
        String type = root.has("type") ? root.get("type").getAsString() : "";

        if ("single_pool".equals(type)) {
            // 单个池文件
            String poolName = root.get("pool_name").getAsString();
            JsonArray itemsArray = root.getAsJsonArray("items");
            NonNullList<ItemStack> items = deserializeItemList(itemsArray);
            data.savePool(poolName, items);
            return 1;
        }

        // all_pools 格式
        JsonObject poolsObj = root.getAsJsonObject("pools");
        int count = 0;
        for (Map.Entry<String, JsonElement> entry : poolsObj.entrySet()) {
            String poolName = entry.getKey();
            JsonArray itemsArray = entry.getValue().getAsJsonArray();
            NonNullList<ItemStack> items = deserializeItemList(itemsArray);
            data.savePool(poolName, items);
            count++;
        }

        Airdrop.LOGGER.info("Imported {} pools", count);
        return count;
    }

    // ===========================
    // 序列化/反序列化工具方法
    // ===========================

    /**
     * 将物品列表序列化为 JsonArray
     * 使用 Minecraft 原生 NBT 序列化，确保所有物品数据完整保留
     */
    private static JsonArray serializeItemList(NonNullList<ItemStack> items) {
        JsonArray array = new JsonArray();
        for (int i = 0; i < items.size(); i++) {
            ItemStack stack = items.get(i);
            if (!stack.isEmpty()) {
                JsonObject itemObj = new JsonObject();
                itemObj.addProperty("slot", i);

                // 使用 NBT 序列化完整的物品数据
                CompoundTag tag = new CompoundTag();
                stack.save(tag);
                itemObj.addProperty("nbt", tag.toString());

                array.add(itemObj);
            }
        }
        return array;
    }

    /**
     * 从 JsonArray 反序列化物品列表
     */
    private static NonNullList<ItemStack> deserializeItemList(JsonArray array) {
        NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
        for (JsonElement element : array) {
            try {
                JsonObject itemObj = element.getAsJsonObject();
                int slot = itemObj.get("slot").getAsInt();
                String nbtString = itemObj.get("nbt").getAsString();

                CompoundTag tag = TagParser.parseTag(nbtString);
                ItemStack stack = ItemStack.of(tag);

                if (slot >= 0 && slot < 27 && !stack.isEmpty()) {
                    items.set(slot, stack);
                }
            } catch (Exception e) {
                Airdrop.LOGGER.error("Failed to deserialize item", e);
            }
        }
        return items;
    }
}
