// 合并两份饰品设计数据并做严格校验
const fs = require("fs");
const path = require("path");
const root = path.resolve(__dirname, "..");

const VANILLA_ITEMS_PLACEHOLDER = new Set(`bone flint glass leather string red_dye white_dye black_dye blue_dye
obsidian redstone amethyst_shard glowstone_dust pointed_dripstone lapis_lazuli paper ink_sac oak_leaves
wheat_seeds blaze_powder gold_nugget quartz charcoal diamond gold_ingot iron_ingot copper_ingot
ender_pearl snowball ice oak_log white_wool coal emerald netherite_scrap stick feather gunpowder
slime_ball clay_ball brick nether_brick blaze_rod ghast_tear phantom_membrane rabbit_hide
honeycomb prismarine_shard nautilus_shell heart_of_the_sea echo_shard sculk bone_meal
cyan_dye gray_dye green_dye brown_dye pink_dye purple_dye magenta_dye orange_dye yellow_dye lime_dye light_blue_dye
cobblestone stone deepslate granite diorite andesite tuff calcite basalt blackstone
crimson_stem warped_stem netherrack soul_sand magma_cream`.split(/\s+/).filter(Boolean));

// ---- 原版物品清单改为从官方 1.21.1 客户端 jar 的语言文件里取，避免手写清单漏项 ----
function loadVanillaItems() {
  const candidates = [
    "C:/Users/Administrator/.gradle/caches/neoformruntime/artifacts/minecraft_1.21.1_client.jar",
    "D:/MC/.minecraft/versions/1.21.1优化/1.21.1优化.jar",
  ];
  const { execFileSync } = require("child_process");
  for (const jar of candidates) {
    if (!fs.existsSync(jar)) continue;
    try {
      const text = execFileSync("tar", ["-xOf", jar, "assets/minecraft/lang/en_us.json"],
        { maxBuffer: 64 * 1024 * 1024 }).toString("utf8");
      const json = JSON.parse(text);
      const items = new Set();
      for (const key of Object.keys(json)) {
        if (key.startsWith("item.minecraft.")) items.add(key.slice("item.minecraft.".length));
      }
      // 语言文件并不包含全部物品（方块类物品常常没有条目），再用物品模型清单补齐
      const listing = execFileSync("tar", ["-tf", jar], { maxBuffer: 256 * 1024 * 1024 }).toString("utf8");
      for (const line of listing.split("\n")) {
        const match = /^assets\/minecraft\/models\/item\/([a-z0-9_]+)\.json$/.exec(line.trim());
        if (match) items.add(match[1]);
      }
      if (items.size > 100) {
        console.log("原版物品清单来自:", jar, "共", items.size, "项");
        return items;
      }
    } catch (error) {
      // 换下一个候选
    }
  }
  throw new Error("无法读取原版物品清单");
}

// ---- 数据修正：设计稿里两处引用了不存在/不合规的物品 ----
const INGREDIENT_REPLACEMENTS = { "minecraft:silver_ingot": "minecraft:copper_ingot" };
const PER_ENTRY_REPLACEMENTS = {
  cursed_tome_charm: { violet_eye_amulet: "ancient_scroll" },
};
function applyFixes(entries) {
  let fixed = 0;
  for (const entry of entries) {
    const recipe = entry.recipe;
    if (!recipe || !Array.isArray(recipe.ingredients)) continue;
    const perEntry = PER_ENTRY_REPLACEMENTS[entry.id] || {};
    recipe.ingredients = recipe.ingredients.map((ingredient) => {
      const replacement = perEntry[ingredient] || INGREDIENT_REPLACEMENTS[ingredient];
      if (replacement) {
        fixed++;
        console.log("修正配方:", entry.id, ingredient, "->", replacement);
      }
      return replacement || ingredient;
    });
  }
  return fixed;
}

const TIER_PRICE = { 1: 60, 2: 120, 3: 200, 4: 320, 5: 500 };
const CONSUMABLE_PRICE = { 1: 20, 2: 35, 3: 50, 4: 70, 5: 90 };
const MATERIAL_PRICE = { 1: 8, 2: 12, 3: 18, 4: 25, 5: 35 };
const CATEGORIES = new Set(["pendant", "ring", "charm", "crown", "amulet", "consumable", "material"]);
const EFFECT_VALUES = {
  MAX_HEALTH: [2, 4, 6, 8, 12], ARMOR: [1, 2, 3, 4, 6], ARMOR_TOUGHNESS: [1, 2, 3, 4, 6],
  ATTACK_DAMAGE: [0.5, 1, 1.5, 2, 3], ATTACK_SPEED: [0.05, 0.1, 0.15, 0.2, 0.3],
  MOVEMENT_SPEED_PERCENT: [2, 4, 6, 8, 12], KNOCKBACK_RESISTANCE: [0.05, 0.1, 0.15, 0.2, 0.3],
  LUCK: [1, 2, 3, 4, 6], SUBLIMATION_PERCENT: [5, 10, 15, 20, 30],
  LIFESTEAL_PERCENT: [2, 4, 6, 8, 10], DAMAGE_REDUCTION_PERCENT: [3, 5, 8, 12, 15],
  NIGHT_VISION: [1], FIRE_RESISTANCE: [1], WATER_BREATHING: [1], REGENERATION: [1, 2],
  SATURATION: [1], JUMP_BOOST: [1, 2], HASTE: [1, 2], RESISTANCE: [1, 2],
};
const CONSUME_TYPES = new Set(["HEAL", "STRENGTH", "SPEED", "RESISTANCE", "NIGHT_VISION", "FIRE_RESISTANCE", "XP", "FEED"]);

const errors = [];
const warnings = [];
const a = JSON.parse(fs.readFileSync(path.join(root, "scripts/accessory-design/a.json"), "utf8"));
const b = JSON.parse(fs.readFileSync(path.join(root, "scripts/accessory-design/b.json"), "utf8"));
const entries = [...a, ...b];
const VANILLA_ITEMS = loadVanillaItems();
applyFixes(entries);

const textureDir = path.join(root, "src/main/resources/assets/life_contract/textures/item/accessory");
const textures = new Set(fs.readdirSync(textureDir).filter((f) => f.endsWith(".png")).map((f) => f.replace(/\.png$/, "")));
const materialIds = new Set(entries.filter((e) => e.category === "material").map((e) => e.id));
const seenIds = new Set();

for (const entry of entries) {
  const where = entry.id || entry.texture;
  if (!entry.id) { errors.push(`缺 id: ${JSON.stringify(entry)}`); continue; }
  if (seenIds.has(entry.id)) errors.push(`重复 id: ${entry.id}`);
  seenIds.add(entry.id);
  if (!textures.has(entry.texture)) errors.push(`${where}: 贴图不存在 ${entry.texture}.png`);
  if (!CATEGORIES.has(entry.category)) errors.push(`${where}: 未知类别 ${entry.category}`);
  if (!(entry.tier >= 1 && entry.tier <= 5)) errors.push(`${where}: tier 非法 ${entry.tier}`);

  const expected = entry.category === "consumable" ? CONSUMABLE_PRICE[entry.tier]
      : entry.category === "material" ? MATERIAL_PRICE[entry.tier] : TIER_PRICE[entry.tier];
  if (entry.price !== expected) errors.push(`${where}: 价格 ${entry.price} != 表值 ${expected}`);

  for (const effect of entry.effects || []) {
    const allowed = EFFECT_VALUES[effect.type];
    if (!allowed) { errors.push(`${where}: 未知效果 ${effect.type}`); continue; }
    if (!allowed.includes(effect.value)) errors.push(`${where}: ${effect.type} 数值 ${effect.value} 不在 ${allowed}`);
  }
  if (entry.category === "consumable") {
    if ((entry.effects || []).length) errors.push(`${where}: 消耗品不应有 effects`);
    if (!entry.consume || !CONSUME_TYPES.has(entry.consume.type)) errors.push(`${where}: consume 非法`);
  } else if (entry.consume) {
    errors.push(`${where}: 非消耗品不应有 consume`);
  }
  if (entry.category === "material" && (entry.effects || []).length) errors.push(`${where}: 材料不应有效果`);

  for (const ingredient of (entry.recipe && entry.recipe.ingredients) || []) {
    if (ingredient.startsWith("minecraft:")) {
      const name = ingredient.slice("minecraft:".length);
      if (!VANILLA_ITEMS.has(name)) errors.push(`${where}: 原版物品不存在 -> ${ingredient}`);
    } else if (!materialIds.has(ingredient)) {
      errors.push(`${where}: 引用了非材料 id -> ${ingredient}`);
    }
  }
}

const missing = [...textures].filter((t) => !entries.some((e) => e.texture === t));
if (missing.length) errors.push(`有贴图没有对应设计: ${missing.join(", ")}`);

console.log("元素:", entries.length, "| 贴图:", textures.size);
console.log("类别:", JSON.stringify(entries.reduce((acc, e) => (acc[e.category] = (acc[e.category] || 0) + 1, acc), {})));
console.log("品阶:", JSON.stringify(entries.reduce((acc, e) => (acc[e.tier] = (acc[e.tier] || 0) + 1, acc), {})));
console.log("错误:", errors.length);
for (const error of errors.slice(0, 25)) console.log("  ! " + error);
if (errors.length === 0) {
  fs.writeFileSync(path.join(root, "build/accessory-merged.json"), JSON.stringify(entries, null, 2) + "\n");
  console.log("已写出 build/accessory-merged.json");
}

// ==================== 生成资源文件 ====================

const SLOT_BY_CATEGORY = { pendant: "necklace", amulet: "necklace", ring: "ring", charm: "charm", crown: "head" };

function writeJson(file, value) {
  fs.mkdirSync(path.dirname(file), { recursive: true });
  fs.writeFileSync(file, JSON.stringify(value, null, 2) + "\n");
}

function dominantEol(text) {
  const crlf = (text.match(/\r\n/g) || []).length;
  const lf = (text.match(/(?<!\r)\n/g) || []).length;
  return crlf > lf ? "\r\n" : "\n";
}

/** 幂等地把生成出来的语言条目插进现有 lang 文件（保留原有格式与换行风格）。 */
function mergeLang(file, entries) {
  const raw = fs.readFileSync(file, "utf8");
  const eol = dominantEol(raw);
  const keys = new Set(entries.map((entry) => entry.key));
  const lines = raw.split(/\r?\n/);
  const kept = lines.filter((line) => {
    const match = /^\s*"([^"]+)"\s*:/.exec(line);
    return !(match && keys.has(match[1]));
  });

  let closing = kept.length - 1;
  while (closing > 0 && kept[closing].trim() === "") closing--;
  if (kept[closing].trim() !== "}") throw new Error(file + ": 找不到结尾的 }");

  if (!kept[closing - 1].trimEnd().endsWith(",")) {
    kept[closing - 1] = kept[closing - 1].trimEnd() + ",";
  }

  const inserted = entries.map((entry, index) => {
    const comma = index === entries.length - 1 ? "" : ",";
    return "  " + JSON.stringify(entry.key) + ": " + JSON.stringify(entry.value) + comma;
  });

  const result = [...kept.slice(0, closing), ...inserted, ...kept.slice(closing)].join(eol);
  fs.writeFileSync(file, result);
}

const GENERIC_LANG = {
  "tooltip.life_contract.accessory.tier": ["品阶 %s", "Tier %s"],
  "tooltip.life_contract.accessory.equip_rule": ["同类饰品只生效一件，自动取品阶最高者",
    "Only the highest tier accessory of each type takes effect"],
  "tooltip.life_contract.accessory.use_hint": ["右键使用", "Right-click to use"],
  "tooltip.life_contract.accessory.price": ["商店价格：%s 升华", "Shop price: %s Sublimation"],
  "gui.life_contract.shop.section.pendant": ["吊坠（%s 升华起）", "Pendants (from %s Sublimation)"],
  "gui.life_contract.shop.section.ring": ["戒指（%s 升华起）", "Rings (from %s Sublimation)"],
  "gui.life_contract.shop.section.charm": ["护符（%s 升华起）", "Charms (from %s Sublimation)"],
  "gui.life_contract.shop.section.crown": ["王冠（%s 升华起）", "Crowns (from %s Sublimation)"],
  "gui.life_contract.shop.section.amulet": ["护身符（%s 升华起）", "Amulets (from %s Sublimation)"],
  "gui.life_contract.shop.section.consumable": ["消耗品（%s 升华起）", "Consumables (from %s Sublimation)"],
  "gui.life_contract.shop.section.material": ["材料（%s 升华起）", "Materials (from %s Sublimation)"],
};

function generate() {
  const dataDir = path.join(root, "src/main/resources/data/life_contract");
  writeJson(path.join(dataDir, "accessory_catalog.json"), entries);

  // 配方
  const recipeDir = path.join(dataDir, "recipe");
  let recipeCount = 0;
  let cappedCount = 0;
  for (const entry of entries) {
    const recipe = entry.recipe || {};
    const materials = (recipe.ingredients || []).slice();
    const requested = recipe.sublimation || 0;
    const allowed = Math.max(0, 9 - materials.length);
    const sublimation = Math.min(requested, allowed);
    if (sublimation < requested) cappedCount++;

    const ingredients = materials.map((id) => ({
      item: id.includes(":") ? id : "life_contract:" + id,
    }));
    for (let i = 0; i < sublimation; i++) {
      ingredients.push({ item: "life_contract:sublimation" });
    }
    if (ingredients.length === 0) continue;

    writeJson(path.join(recipeDir, "accessory_" + entry.id + ".json"), {
      type: "minecraft:crafting_shapeless",
      category: "misc",
      ingredients,
      result: { id: "life_contract:" + entry.id, count: 1 },
    });
    recipeCount++;
  }

  // Curios 槽位标签（Curios 未安装时这些文件无副作用）
  const slotItems = {};
  for (const entry of entries) {
    const slot = SLOT_BY_CATEGORY[entry.category];
    if (!slot) continue;
    (slotItems[slot] = slotItems[slot] || []).push("life_contract:" + entry.id);
  }
  let slotCount = 0;
  for (const [slot, values] of Object.entries(slotItems)) {
    writeJson(path.join(root, "src/main/resources/data/curios/tags/item", slot + ".json"),
      { replace: false, values: values.sort() });
    slotCount++;
  }

  // 语言
  const zh = [];
  const en = [];
  for (const [key, pair] of Object.entries(GENERIC_LANG)) {
    zh.push({ key, value: pair[0] });
    en.push({ key, value: pair[1] });
  }
  for (const entry of entries) {
    zh.push({ key: "item.life_contract." + entry.id, value: entry.name_zh });
    en.push({ key: "item.life_contract." + entry.id, value: entry.name_en });
    zh.push({ key: "tooltip.life_contract." + entry.id + ".effect", value: entry.effect_zh });
    en.push({ key: "tooltip.life_contract." + entry.id + ".effect", value: entry.effect_en });
  }
  mergeLang(path.join(root, "src/main/resources/assets/life_contract/lang/zh_cn.json"), zh);
  mergeLang(path.join(root, "src/main/resources/assets/life_contract/lang/en_us.json"), en);

  console.log("已生成: accessory_catalog.json,", recipeCount, "个配方（", cappedCount, "个因 3x3 格子不足而削减升华）,",
      slotCount, "个 Curios 槽位标签,", zh.length, "条语言条目 x2");
}

generate();
