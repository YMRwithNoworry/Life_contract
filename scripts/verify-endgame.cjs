const fs = require("node:fs");
const path = require("node:path");

const root = path.resolve(__dirname, "..");
const endgame = "src/main/java/org/alku/life_contract/endgame/StrongholdEndgameManager.java";
const source = fs.readFileSync(path.join(root, endgame), "utf8");

function require_(text, label) {
  if (!source.includes(text)) {
    throw new Error(label + ": expected " + endgame + " to contain " + JSON.stringify(text));
  }
}

function forbid(text, label) {
  if (source.includes(text)) {
    throw new Error(label + ": " + endgame + " must not contain " + JSON.stringify(text));
  }
}

// 1) 朽翼魔的注册名是 spore:verfall（游戏内 /summon spore:verfall），不是 spore:verfalldrache。
require_('fromNamespaceAndPath("spore", "verfall")', "spore boss id");

// 2) ENTITY_TYPE 是 DefaultedRegistry（默认值是 minecraft:pig），未注册的 id 会返回猪而不是 null。
//    所以判定"实体是否存在"必须比对注册名，不能只判 != null，否则会生成一只猪当终局 Boss。
forbid("BuiltInRegistries.ENTITY_TYPE.get(entityId) != null", "defaulted registry misuse");
require_("BuiltInRegistries.ENTITY_TYPE.getKey(entityType)", "registry key comparison");

// 3) 终局 Boss 必须有 Boss 血条，且要防止被距离剔除。
require_("ServerBossEvent", "boss bar");
require_("startEndBossBar", "boss bar start");
require_("updateEndBossBar", "boss bar update");
require_("setPersistenceRequired", "boss persistence");

// 4) 原版会把主世界边界的中心与尺寸同步到其它维度，末地边界必须每个 tick 纠正回来。
require_("enforceEndBorder", "end border enforcement");
require_("DelegateBorderChangeListener", "end border rationale");

// 5) 胜利文案不能再按旧的 path 判断。
const events = fs.readFileSync(path.join(root, "src/main/java/org/alku/life_contract/events/GameEventManager.java"), "utf8");
if (events.includes('"verfalldrache".equals(bossId.getPath())')) {
  throw new Error("declareDragonWinner still matches the wrong boss path");
}

console.log("Endgame verification passed.");