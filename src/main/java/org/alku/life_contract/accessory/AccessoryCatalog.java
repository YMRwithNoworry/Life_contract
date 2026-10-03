package org.alku.life_contract.accessory;

import com.google.gson.JsonArray;
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
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 饰品目录：启动时从 {@code data/life_contract/accessory_catalog.json} 与
 * {@code data/life_contract/accessory_resonance.json} 读取全部定义。
 * <p>
 * 物品注册、效果结算、商店价格、创造模式标签页都由这一份数据驱动，
 * 因此调整数值与机制只需要改 JSON，不需要改代码。
 */
public final class AccessoryCatalog {

    private static final String RESOURCE = "/data/life_contract/accessory_catalog.json";
    private static final String RESONANCE_RESOURCE = "/data/life_contract/accessory_resonance.json";

    private static final List<AccessoryDefinition> ALL = new ArrayList<>();
    private static final Map<String, AccessoryDefinition> BY_ID = new LinkedHashMap<>();
    private static final Map<AccessoryFaction, AccessoryResonance> RESONANCES =
            new EnumMap<>(AccessoryFaction.class);
    private static boolean loaded;

    private AccessoryCatalog() {
    }

    public static synchronized void load() {
        if (loaded) {
            return;
        }
        loaded = true;
        loadDefinitions();
        loadResonances();
    }

    private static void loadDefinitions() {
        JsonElement root = readJson(RESOURCE);
        if (root == null) {
            return;
        }
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
    }

    private static void loadResonances() {
        JsonElement root = readJson(RESONANCE_RESOURCE);
        if (root == null) {
            return;
        }
        if (!root.isJsonArray()) {
            Life_contract.LOGGER.error("[饰品] {} 的根节点不是数组，已跳过", RESONANCE_RESOURCE);
            return;
        }

        for (JsonElement element : root.getAsJsonArray()) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject object = element.getAsJsonObject();
            AccessoryFaction faction = AccessoryFaction.byId(stringOr(object, "faction", "none"));
            if (faction == AccessoryFaction.NONE) {
                continue;
            }
            RESONANCES.put(faction, new AccessoryResonance(
                    faction,
                    stringOr(object, "name_zh", faction.displayZh() + "共鸣"),
                    stringOr(object, "name_en", faction.displayEn() + " Resonance"),
                    stringOr(object, "desc2_zh", ""),
                    stringOr(object, "desc2_en", ""),
                    readEffects(object.get("effects2")),
                    stringOr(object, "desc3_zh", ""),
                    stringOr(object, "desc3_en", ""),
                    readEffects(object.get("effects3"))));
        }
        Life_contract.LOGGER.info("[饰品] 已加载 {} 组派系共鸣", RESONANCES.size());
    }

    private static JsonElement readJson(String resource) {
        try (InputStream stream = AccessoryCatalog.class.getResourceAsStream(resource)) {
            if (stream == null) {
                Life_contract.LOGGER.error("[饰品] 找不到 {}，相关功能不会生效", resource);
                return null;
            }
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
        } catch (Exception exception) {
            Life_contract.LOGGER.error("[饰品] 读取 {} 失败", resource, exception);
            return null;
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
            AccessoryFaction faction = AccessoryFaction.byId(stringOr(object, "faction", "none"));

            List<AccessoryEffect> effects = readEffects(object.get("effects"));
            AccessoryCharge charge = readCharge(object.get("charge"));
            AccessoryActive active = readActive(object.get("active"));

            List<AccessoryEffect> consume = new ArrayList<>();
            JsonElement consumeElement = object.get("consume");
            if (consumeElement != null && consumeElement.isJsonArray()) {
                consume.addAll(readEffects(consumeElement));
            } else if (consumeElement != null && consumeElement.isJsonObject()) {
                AccessoryEffect single = readEffect(consumeElement.getAsJsonObject());
                if (single != null) {
                    consume.add(single);
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

            return new AccessoryDefinition(id, texture, category, faction, tier, nameZh, nameEn, effectZh, effectEn,
                    List.copyOf(effects), charge, active, List.copyOf(consume), price,
                    List.copyOf(ingredients), sublimation);
        } catch (Exception exception) {
            Life_contract.LOGGER.warn("[饰品] 解析一条定义失败，已跳过", exception);
            return null;
        }
    }

    private static List<AccessoryEffect> readEffects(JsonElement element) {
        List<AccessoryEffect> effects = new ArrayList<>();
        if (element == null || !element.isJsonArray()) {
            return effects;
        }
        for (JsonElement child : element.getAsJsonArray()) {
            if (!child.isJsonObject()) {
                continue;
            }
            AccessoryEffect effect = readEffect(child.getAsJsonObject());
            if (effect != null) {
                effects.add(effect);
            }
        }
        return effects;
    }

    private static AccessoryEffect readEffect(JsonObject object) {
        if (object == null || !object.has("type")) {
            return null;
        }
        AccessoryEffectType type = AccessoryEffectType.byId(object.get("type").getAsString());
        if (type == null) {
            return null;
        }
        double value = object.has("value") ? object.get("value").getAsDouble() : 0.0D;
        AccessoryCondition when = AccessoryCondition.byId(stringOr(object, "when", "always"));
        return new AccessoryEffect(type, value, when);
    }

    private static AccessoryCharge readCharge(JsonElement element) {
        if (element == null || !element.isJsonObject()) {
            return AccessoryCharge.NONE;
        }
        JsonObject object = element.getAsJsonObject();
        int max = Math.max(0, object.has("max") ? object.get("max").getAsInt() : 0);
        List<AccessoryEffect> perStack = readEffects(object.get("per_stack"));
        int decay = Math.max(0, object.has("decay_ticks") ? object.get("decay_ticks").getAsInt() : 0);
        int loseOnHurt = Math.max(0, object.has("lose_on_hurt") ? object.get("lose_on_hurt").getAsInt() : 0);
        AccessoryCharge charge = new AccessoryCharge(max, List.copyOf(perStack), decay, loseOnHurt);
        return charge.isEmpty() ? AccessoryCharge.NONE : charge;
    }

    private static AccessoryActive readActive(JsonElement element) {
        if (element == null || !element.isJsonObject()) {
            return AccessoryActive.NONE;
        }
        JsonObject object = element.getAsJsonObject();
        AccessoryActiveKind kind = AccessoryActiveKind.byId(stringOr(object, "kind", ""));
        if (kind == null) {
            return AccessoryActive.NONE;
        }
        double value = object.has("value") ? object.get("value").getAsDouble() : 0.0D;
        int duration = Math.max(0, object.has("duration") ? object.get("duration").getAsInt() : 0);
        int cooldown = Math.max(1, object.has("cooldown") ? object.get("cooldown").getAsInt() : 1200);
        return new AccessoryActive(kind, value, duration, cooldown);
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

    /** 派系共鸣定义；未定义时返回 null。 */
    public static AccessoryResonance resonance(AccessoryFaction faction) {
        return RESONANCES.get(faction);
    }

    /** 物品堆叠对应的定义；不是饰品则返回 null。 */
    public static AccessoryDefinition of(ItemStack stack) {
        if (stack != null && stack.getItem() instanceof AccessoryItem item) {
            return item.definition();
        }
        return null;
    }
}
