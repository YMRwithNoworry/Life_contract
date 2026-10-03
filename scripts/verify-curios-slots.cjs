const fs = require("node:fs");
const path = require("node:path");

const root = path.resolve(__dirname, "..");
const entityFile = "src/main/resources/data/life_contract/curios/entities/player.json";
const tagDir = path.join(root, "src/main/resources/data/curios/tags/item");

// Curios 自带的全部标准槽位（见 curios 的 data/curios/curios/slots/*.json）。
const ALL_CURIOS_SLOTS = [
  "head", "necklace", "ring", "charm", "belt",
  "hands", "back", "body", "bracelet", "curio",
];

function read(relativePath) {
  return fs.readFileSync(path.join(root, relativePath), "utf8");
}

const entityData = JSON.parse(read(entityFile));
const granted = new Set(entityData.slots || []);
if (!Array.isArray(entityData.entities) || !entityData.entities.includes("minecraft:player")) {
  throw new Error(entityFile + " should target minecraft:player");
}

// 1) 玩家必须拿到全部标准饰品栏。
//    之前只写了 ["head"]，结果只有王冠能戴，项链/戒指/护符全都装不上。
for (const slot of ALL_CURIOS_SLOTS) {
  if (!granted.has(slot)) {
    throw new Error(entityFile + " is missing the \"" + slot + "\" slot");
  }
}

// 2) 模组饰品实际会用到的槽位（见 AccessoryItem.slotFor），每个都必须
//    既有物品标签（curios 的 tag 校验器靠它决定能不能放进去），又被授予玩家。
const itemSource = read("src/main/java/org/alku/life_contract/accessory/AccessoryItem.java");
const used = new Set(
  [...itemSource.matchAll(/case\s+[A-Z_,\s]+->\s*"([a-z_]+)"/g)].map((m) => m[1]),
);
if (used.size === 0) {
  throw new Error("could not read any slot id out of AccessoryItem.slotFor");
}
for (const slot of used) {
  if (!granted.has(slot)) {
    throw new Error("AccessoryItem maps to slot \"" + slot + "\" but " + entityFile + " does not grant it");
  }
  if (!fs.existsSync(path.join(tagDir, slot + ".json"))) {
    throw new Error("missing item tag data/curios/tags/item/" + slot + ".json for slot \"" + slot + "\"");
  }
}

console.log("Curios slot verification passed (" + used.size + " used, " + granted.size + " granted).");