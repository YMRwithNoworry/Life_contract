const fs = require("node:fs");
const path = require("node:path");

const root = path.resolve(__dirname, "..");

function read(relativePath) {
  return fs.readFileSync(path.join(root, relativePath), "utf8");
}

function requireText(source, text, label) {
  if (!source.includes(text)) {
    throw new Error(`${label}: missing ${text}`);
  }
}

const manager = read("src/main/java/org/alku/life_contract/events/WorldEventManager.java");
const payload = read("src/main/java/org/alku/life_contract/events/EventSyncPayload.java");
const hud = read("src/main/java/org/alku/life_contract/client/EventHUD.java");
const bubbles = read("src/main/java/org/alku/life_contract/client/SafeBubbleRenderer.java");
const events = read("src/main/java/org/alku/life_contract/events/GameEventManager.java");
const network = read("src/main/java/org/alku/life_contract/NetworkHandler.java");
const client = read("src/main/java/org/alku/life_contract/ClientDataStorage.java");
const commands = read("src/main/java/org/alku/life_contract/ContractCommands.java");

// 事件节奏：孢子潮第 5 分钟、净化裂隙第 9 分钟、每 2 次淘汰发悬赏、剩 3 人进入终局过载
requireText(manager, "SPORE_SURGE_MINUTE = 5", "spore surge timing");
requireText(manager, "PURIFICATION_RIFT_MINUTE = 9", "purification rift timing");
requireText(manager, "ENDGAME_PLAYER_COUNT = 3", "endgame overload player threshold");
requireText(manager, "ELIMINATION_THRESHOLD = 2", "bounty elimination threshold");
requireText(manager, "SAFE_BUBBLE_COUNT = 3", "safe bubble count");
requireText(manager, "SAFE_BUBBLE_RADIUS = 15.0D", "safe bubble radius");
requireText(manager, "ENDGAME_BORDER_DAMAGE_MULTIPLIER = 2.0D", "border damage multiplier");

// 固定时间点事件每局只触发一次
requireText(manager, "private static boolean sporeSurgeScheduled;", "spore surge one-shot flag");
requireText(manager, "private static boolean purificationRiftScheduled;", "rift one-shot flag");

// 终局过载重写世界边界伤害
requireText(manager, "event.getSource().is(DamageTypes.OUTSIDE_BORDER)", "border damage source check");
requireText(manager, "event.setAmount(event.getAmount() * (float) ENDGAME_BORDER_DAMAGE_MULTIPLIER)",
  "border damage scaling");

// 孢潮推进：随机精英 + spore 缺失时的备用精英
requireText(manager, 'ModList.get().isLoaded("spore")', "spore mod detection");
requireText(manager, "BuiltInRegistries.ENTITY_TYPE.get(id)", "elite entity lookup");
requireText(manager, "fallbackEntityType()", "fallback elite");

// 清道夫悬赏：全图发光标记 + 击杀奖励写进生命上限
requireText(manager, "MobEffects.GLOWING", "bounty glow marker");
requireText(manager, "AttributeModifier.Operation.ADD_VALUE", "bounty max health reward");

// 净化裂隙：泡内持续给予生命回复与饱和
requireText(manager, "refreshBubbleEffect(player, MobEffects.REGENERATION, 1)", "bubble regeneration");
requireText(manager, "refreshBubbleEffect(player, MobEffects.SATURATION, 2)", "bubble saturation");

// 孢子雨事件与「缓慢感染」效果已经移除，别被加回来
for (const removed of ["sporeRain", "SPORE_RAIN", "孢子雨", "SLOW_INFECTION", "slow_infection"]) {
  for (const [name, source] of [["WorldEventManager", manager], ["EventSyncPayload", payload],
                                ["EventHUD", hud], ["ClientDataStorage", client]]) {
    if (source.includes(removed)) {
      throw new Error(name + " still references the removed feature: " + removed);
    }
  }
}

// 同步与 UI 接线
requireText(payload, 'NetworkHandler.type("event_status")', "event payload id");
requireText(network, "EventSyncPayload.STREAM_CODEC", "event payload registration");
requireText(hud, "public static void update(EventSyncPayload payload)", "client event state update");
requireText(network, "EventHUD.update(payload)", "event payload handler wiring");
requireText(client, "setEventData(", "client event data setter");
requireText(client, "getBubblePositions()", "client bubble positions");
requireText(bubbles, "RenderLevelStageEvent", "safe bubble world rendering");
requireText(bubbles, "ClientDataStorage.isPurificationRiftActive()", "bubble render gate");

// 对局生命周期挂接
requireText(events, "WorldEventManager.startGame(gameLevel", "event manager start hook");
requireText(events, "WorldEventManager.reset()", "event manager reset hook");

// 管理指令
requireText(commands, 'Commands.literal("event")', "event command root");
requireText(commands, "WorldEventManager.forceTriggerSporeSurge(level)", "force trigger command");
requireText(commands, "WorldEventManager.stopPurificationRift()", "stop event command");

console.log("World event verification passed.");
