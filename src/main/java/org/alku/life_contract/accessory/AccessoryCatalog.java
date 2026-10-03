package org.alku.life_contract.accessory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.world.item.ItemStack;
import org.alku.life_contract.Life_contract;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 饰品目录：启动时从 {@code data/life_contract/accessory_catalog.json} 读取全部定义。
 * <p>
 * 物品注册、效果结算、商店价格、创造模式标签页都由这一份数据驱动，
 * 因此调整数值只需要改 JSON，不需要改代码。
 */
public final class AccessoryCatalog {

    private static final String RESOURCE = "/data/life_contract/accessory_catalog.json";

    private static final List<AccessoryDefinition> ALL = new ArrayList<>();
    private static final Map<String, AccessoryDefinition> BY_ID = new LinkedHashMap<>();
    private static boolean loaded;

    private AccessoryCatalog() {
    }

    public static synchronized void load() {
        if (loaded) {
            return;
        }
        loaded = true;

        try (InputStream stream = AccessoryCatalog.class.getResourceAsStream(RESOURCE)) {
            if (stream == null) {
                Life_contract.LOGGER.error("[饰品] 找不到 {}，本次不会注册任何饰品", RESOURCE);
                return;
            }

            JsonElement root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
            if (!root.isJsonArray()) {
                Life_contract.LOGGER.error("[饰品] {} 的根节点不是数组，已跳过", RESOURCE);
                return;
            }

            for (JsonElement element : root.getAsJsonArray()) {
                if (!element.isJsonObject()) {
                    continue;
                }
                AccessoryDefinition definition = parse(element.getAsJsonObject());
                if (definition != null && !BY_ID.containsKey(definition.id())) {
                    BY_ID.put(definition.id(), definition);
                    ALL.add(definition);
                }
            }
            Life_contract.LOGGER.info("[饰品] 已加载 {} 条饰品/材料定义", ALL.size());
        } catch (Exception exception) {
            Life_contract.LOGGER.error("[饰品] 读取饰品目录失败，本次不会注册任何饰品", exception);
        }
    }

    private static AccessoryDefinition parse(JsonObject object) {
        try {
            String id = object.get("id").getAsString();
            AccessoryCategory category = AccessoryCategory.byId(object.get("category").getAsString());
            if (category == null) {
                Life_contract.LOGGER.warn("[饰品] {} 的类别无法识别，已跳过", id);
                return null;
            }

            int tier = Math.max(1, Math.min(5, object.has("tier") ? object.get("tier").getAsInt() : 1));
            String texture = object.has("texture") ? object.get("texture").getAsString() : id;
            String nameZh = stringOr(object, "name_zh", id);
            String nameEn = stringOr(object, "name_en", id);
            String effectZh = stringOr(object, "effect_zh", "");
            String effectEn = stringOr(object, "effect_en", "");
            int price = object.has("price") ? Math.max(0, object.get("price").getAsInt()) : 0;

            List<AccessoryEffect> effects = new ArrayList<>();
            if (object.has("effects") && object.get("effects").isJsonArray()) {
                for (JsonElement element : object.getAsJsonArray("effects")) {
                    if (!element.isJsonObject()) {
                        continue;
                    }
                    JsonObject effectObject = element.getAsJsonObject();
                    AccessoryEffectType type = AccessoryEffectType.byId(effectObject.get("type").getAsString());
                    if (type != null) {
                        effects.add(new AccessoryEffect(type, effectObject.get("value").getAsDouble()));
                    }
                }
            }

            AccessoryEffect consume = null;
            if (object.has("consume") && object.get("consume").isJsonObject()) {
                JsonObject consumeObject = object.getAsJsonObject("consume");
                AccessoryEffectType type = AccessoryEffectType.byId(consumeObject.get("type").getAsString());
                if (type != null) {
                    consume = new AccessoryEffect(type, consumeObject.get("value").getAsDouble());
                }
            }

            List<String> ingredients = new ArrayList<>();
            int sublimation = 0;
            if (object.has("recipe") && object.get("recipe").isJsonObject()) {
                JsonObject recipe = object.getAsJsonObject("recipe");
                if (recipe.has("ingredients") && recipe.get("ingredients").isJsonArray()) {
                    for (JsonElement element : recipe.getAsJsonArray("ingredients")) {
                        ingredients.add(element.getAsString());
                    }
                }
                if (recipe.has("sublimation")) {
                    sublimation = Math.max(0, recipe.get("sublimation").getAsInt());
                }
            }

            return new AccessoryDefinition(id, texture, category, tier, nameZh, nameEn, effectZh, effectEn,
                    List.copyOf(effects), consume, price, List.copyOf(ingredients), sublimation);
        } catch (Exception exception) {
            Life_contract.LOGGER.warn("[饰品] 解析一条定义失败，已跳过", exception);
            return null;
        }
    }

    private static String stringOr(JsonObject object, String key, String fallback) {
        return object.has(key) && object.get(key).isJsonPrimitive() ? object.get(key).getAsString() : fallback;
    }

    public static List<AccessoryDefinition> all() {
        return Collections.unmodifiableList(ALL);
    }

    public static AccessoryDefinition byId(String id) {
        return BY_ID.get(id);
    }

    /** 某一类别下的全部定义，按品阶从低到高排序。 */
    public static List<AccessoryDefinition> byCategory(AccessoryCategory category) {
        List<AccessoryDefinition> result = new ArrayList<>();
        for (AccessoryDefinition definition : ALL) {
            if (definition.category() == category) {
                result.add(definition);
            }
        }
        result.sort((left, right) -> {
            int byTier = Integer.compare(left.tier(), right.tier());
            return byTier != 0 ? byTier : left.id().compareTo(right.id());
        });
        return result;
    }

    /** 物品堆叠对应的定义；不是饰品则返回 null。 */
    public static AccessoryDefinition of(ItemStack stack) {
        if (stack != null && stack.getItem() instanceof AccessoryItem item) {
            return item.definition();
        }
        return null;
    }
}