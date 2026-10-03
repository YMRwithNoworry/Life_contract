// 饰品系统校验：数据文件、模型、配方、Curios 槽位标签、语言、以及 Java 侧接线
const fs = require("fs");
const path = require("path");
const root = path.resolve(__dirname, "..");
const read = (rel) => fs.readFileSync(path.join(root, rel), "utf8");
const exists = (rel) => fs.existsSync(path.join(root, rel));

function requireText(haystack, needle, label) {
  if (!haystack.includes(needle)) throw new Error(label + ": missing " + JSON.stringify(needle));
}
function forbidText(haystack, needle, label) {
  if (haystack.includes(needle)) throw new Error(label + ": must not contain " + JSON.stringify(needle));
}

// 1) 目录数据
const catalogPath = "src/main/resources/data/life_contract/accessory_catalog.json";
if (!exists(catalogPath)) throw new Error("缺少 " + catalogPath);
const catalog = JSON.parse(read(catalogPath));
if (catalog.length !== 70) throw new Error("饰品定义应为 70 条，实际 " + catalog.length);

const CATEGORIES = new Set(["pendant", "ring", "charm", "crown", "amulet", "consumable", "material"]);
const SLOTS = { pendant: "necklace", amulet: "necklace", ring: "ring", charm: "charm", crown: "head" };
const ids = new Set();
const byCategory = {};
for (const entry of catalog) {
  if (ids.has(entry.id)) throw new Error("重复 id " + entry.id);
  ids.add(entry.id);
  if (!CATEGORIES.has(entry.category)) throw new Error(entry.id + ": 类别非法");
  if (!(entry.tier >= 1 && entry.tier <= 5)) throw new Error(entry.id + ": tier 非法");
  if (!(entry.price > 0)) throw new Error(entry.id + ": 价格非法");
  if (!entry.name_zh || !entry.name_en) throw new Error(entry.id + ": 缺少名称");
  if (!entry.effect_zh && entry.category !== "material") throw new Error(entry.id + ": 缺少效果说明");
  byCategory[entry.category] = (byCategory[entry.category] || 0) + 1;

  // 模型 + 贴图
  const model = "src/main/resources/assets/life_contract/models/item/" + entry.id + ".json";
  if (!exists(model)) throw new Error(entry.id + ": 缺少物品模型");
  const modelJson = JSON.parse(read(model));
  if (modelJson.textures.layer0 !== "life_contract:item/accessory/" + entry.texture) {
    throw new Error(entry.id + ": 模型贴图路径不对");
  }
  const texture = "src/main/resources/assets/life_contract/textures/item/accessory/" + entry.texture + ".png";
  if (!exists(texture)) throw new Error(entry.id + ": 贴图不存在");

  // 配方
  const recipe = "src/main/resources/data/life_contract/recipe/accessory_" + entry.id + ".json";
  if (!exists(recipe)) throw new Error(entry.id + ": 缺少配方");
  const recipeJson = JSON.parse(read(recipe));
  if (recipeJson.type !== "minecraft:crafting_shapeless") throw new Error(entry.id + ": 配方类型不对");
  if (recipeJson.result.id !== "life_contract:" + entry.id) throw new Error(entry.id + ": 配方产物不对");
  if (recipeJson.ingredients.length < 2 || recipeJson.ingredients.length > 9) {
    throw new Error(entry.id + ": 配方材料数量 " + recipeJson.ingredients.length + " 超出 3x3 上限");
  }
}

// 2) 每类都要有内容，且佩戴类别只能映射到已声明的槽位
for (const category of CATEGORIES) {
  if (!byCategory[category]) throw new Error("类别 " + category + " 没有任何物品");
}

// 3) Curios 槽位标签
for (const [category, slot] of Object.entries(SLOTS)) {
  const file = "src/main/resources/data/curios/tags/item/" + slot + ".json";
  if (!exists(file)) throw new Error("缺少 Curios 槽位标签 " + file);
  const tag = JSON.parse(read(file));
  const expected = catalog.filter((entry) => entry.category === category)
      .map((entry) => "life_contract:" + entry.id);
  for (const id of expected) {
    if (!tag.values.includes(id)) throw new Error(slot + " 标签缺少 " + id);
  }
}

// 4) 语言：中英都要有名称与效果
for (const lang of ["zh_cn", "en_us"]) {
  const json = JSON.parse(read("src/main/resources/assets/life_contract/lang/" + lang + ".json"));
  for (const entry of catalog) {
    const nameKey = "item.life_contract." + entry.id;
    if (typeof json[nameKey] !== "string") throw new Error(lang + " 缺少 " + nameKey);
    if (entry.category !== "material") {
      const effectKey = "tooltip.life_contract." + entry.id + ".effect";
      if (typeof json[effectKey] !== "string") throw new Error(lang + " 缺少 " + effectKey);
    }
  }
  for (const key of ["tooltip.life_contract.accessory.tier", "tooltip.life_contract.accessory.equip_rule",
      "tooltip.life_contract.accessory.use_hint", "tooltip.life_contract.accessory.price",
      "gui.life_contract.accessory.title", "gui.life_contract.accessory.equip", "gui.life_contract.accessory.unequip",
      "gui.life_contract.upgrade_hub.accessories"]) {
    if (typeof json[key] !== "string") throw new Error(lang + " 缺少 " + key);
  }
  for (const category of CATEGORIES) {
    const key = "gui.life_contract.shop.section." + category;
    if (typeof json[key] !== "string") throw new Error(lang + " 缺少 " + key);
  }
}

// 5) Java 侧：注册由数据驱动、Curios 走反射、同类只生效一件
const lifeContract = read("src/main/java/org/alku/life_contract/Life_contract.java");
requireText(lifeContract, "AccessoryCatalog.load()", "目录加载");
requireText(lifeContract, "ACCESSORY_ITEMS.put(definition.id()", "按目录注册物品");
requireText(lifeContract, "for (DeferredHolder<Item, Item> accessory : ACCESSORY_ITEMS.values())",
    "创造模式标签页包含饰品");

const effects = read("src/main/java/org/alku/life_contract/accessory/AccessoryEffects.java");
requireText(effects, "definition.tier() > current.tier()", "同类取最高品阶");
requireText(effects, "CuriosCompat.equippedStacks(player)", "读取 Curios 槽位");
requireText(effects, "AccessorySlots.all(player).values()", "读取本模组饰品栏");
// 规则：必须装进饰品栏才生效，因此不得再扫描主物品栏
if (effects.includes("player.getInventory().items")) {
  throw new Error("AccessoryEffects: 不得按背包内容生效，必须只认饰品栏");
}
const slots = read("src/main/java/org/alku/life_contract/accessory/AccessorySlots.java");
requireText(slots, 'TAG_SLOTS = "LifeContractAccessorySlots"', "饰品栏存档键");
requireText(slots, "public static ItemStack equip(Player player, ItemStack stack)", "装备接口");
requireText(slots, "public static ItemStack unequip(Player player, AccessoryCategory category)", "卸下接口");
requireText(slots, "public static void onPlayerClone(PlayerEvent.Clone event)", "死亡后保留饰品栏");
const ui = read("src/main/java/org/alku/life_contract/client/AccessoryUIHolder.java");
requireText(ui, "AccessorySlots.get(owner, category)", "界面显示已装备");
requireText(ui, "AccessorySlots.equip(player, best)", "界面装备逻辑");
const commands = read("src/main/java/org/alku/life_contract/ContractCommands.java");
requireText(commands, 'Commands.literal("accessory")', "饰品栏指令");
requireText(commands, ".equip(player, held)", "指令装备");
requireText(commands, ".unequip(player,", "指令卸下");
const hub = read("src/main/java/org/alku/life_contract/client/UpgradeHubUIHolder.java");
requireText(hub, "AccessoryUIHolder.UI_ID", "升级中枢入口");
const lifeContractUi = read("src/main/java/org/alku/life_contract/Life_contract.java");
requireText(lifeContractUi, "AccessoryUIHolder::new", "饰品栏界面注册");
requireText(effects, "addOrUpdateTransientModifier", "属性加成");
requireText(effects, "instance.removeModifier(modifierId(definition.category()))", "换装时移除旧加成");
requireText(effects, "public static double damageReduction(Player player)", "受伤减免查询");
requireText(effects, "public static double lifesteal(Player player)", "吸血查询");
requireText(effects, "public static double sublimationBonus(Player player)", "升华加成查询");

const curios = read("src/main/java/org/alku/life_contract/accessory/CuriosCompat.java");
requireText(curios, 'Class.forName(API_CLASS)', "反射 Curios API");
requireText(curios, 'ModList.get().isLoaded("curios")', "Curios 存在性判断");
requireText(curios, "getEquippedCurios", "读取已佩戴饰品");

// 6) 全仓不得出现 Curios 编译期依赖（未装 Curios 时也能加载）
const javaFiles = [];
(function walk(dir) {
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    const full = path.join(dir, entry.name);
    if (entry.isDirectory()) walk(full);
    else if (entry.name.endsWith(".java")) javaFiles.push(full);
  }
})(path.join(root, "src/main/java"));
for (const file of javaFiles) {
  const text = fs.readFileSync(file, "utf8");
  if (/^\s*import\s+top\.theillusivec4\.curios\./m.test(text)) {
    throw new Error(path.relative(root, file) + ": 不得直接 import Curios 类");
  }
}

// 7) 掉落与战斗钩子
const drops = read("src/main/java/org/alku/life_contract/items/SublimationDropHandler.java");
requireText(drops, "AccessoryEffects.sublimationBonus(killer)", "升华掉落加成");
const combat = read("src/main/java/org/alku/life_contract/accessory/AccessoryCombatEvents.java");
requireText(combat, "AccessoryEffects.damageReduction(player)", "受伤减免钩子");
requireText(combat, "AccessoryEffects.lifesteal(player)", "吸血钩子");

// 8) 商店接线
const shopService = read("src/main/java/org/alku/life_contract/market/BulletShopService.java");
requireText(shopService, "AccessoryCatalog.of(template)", "商店识别饰品");
const shopUi = read("src/main/java/org/alku/life_contract/client/SublimationShopUIHolder.java");
requireText(shopUi, "AccessoryCatalog.byCategory(category)", "商店按类别列出饰品");
requireText(shopUi, '"gui.life_contract.shop.section." + category.id()', "商店分区标题");

// 9) 可选依赖声明
const toml = read("src/main/resources/META-INF/neoforge.mods.toml");
requireText(toml, 'modId="curios"', "声明 curios 可选依赖");

console.log("Accessory verification passed. 类别分布:", JSON.stringify(byCategory));