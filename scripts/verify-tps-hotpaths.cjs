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

function forbidText(source, text, label) {
  if (source.includes(text)) {
    throw new Error(`${label}: should no longer contain ${text}`);
  }
}

const navigator = read("src/main/java/org/alku/life_contract/airdrop/event/AirdropNavigator.java");
const airdropEvents = read("src/main/java/org/alku/life_contract/airdrop/event/CommonEvents.java");
const contractEvents = read("src/main/java/org/alku/life_contract/ContractEvents.java");
const followers = read("src/main/java/org/alku/life_contract/follower/FollowerEvents.java");
const mutation = read("src/main/java/org/alku/life_contract/mutation/MutationCombatEvents.java");
const smelter = read("src/main/java/org/alku/life_contract/TeamSmelter.java");
const events = read("src/main/java/org/alku/life_contract/events/WorldEventManager.java");
const mixinConfig = read("src/main/resources/life_contract.mixins.json");

// 1) 空投导航不再按世界边界做实体扫描
forbidText(navigator, "getEntitiesOfClass(AirdropEntity.class", "airdrop scan");
forbidText(navigator, "level.getWorldBorder().getMinX()", "world-sized search box");
requireText(navigator, "TRACKED_AIRDROPS", "airdrop registry");
requireText(navigator, "getActiveAirdrops(ServerLevel level)", "registry lookup");
requireText(navigator, "public static void trackAirdrop(Level level, Entity entity)", "registry add");
requireText(navigator, "public static void untrackAirdrop(Level level, Entity entity)", "registry remove");
requireText(airdropEvents, "AirdropNavigator.trackAirdrop(event.getLevel(), event.getEntity())", "join hook");
requireText(airdropEvents, "AirdropNavigator.untrackAirdrop(event.getLevel(), event.getEntity())", "leave hook");
requireText(navigator, "level.getGameTime() % 10L == 0L", "action bar cadence");

// 2) 契约模组 -> 玩家 的查找改为按 tick 缓存
requireText(contractEvents, "public static ServerPlayer findPlayerForContractMod(", "cached lookup");
requireText(contractEvents, "if (tick != contractModOwnersTick)", "per-tick cache rebuild");
requireText(followers, "ContractEvents.findPlayerForContractMod(", "follower join uses cache");
// 刷怪路径里不能再出现“逐个玩家解析契约模组”的循环
const followerJoin = followers.slice(followers.indexOf("onEntityJoinLevel"), followers.indexOf("onEntityJoinLevel") + 1600);
forbidText(followerJoin, "getEffectiveContractMod(player)", "per-mob player loop");
requireText(mutation, "ContractEvents.findPlayerForContractMod(", "mutation resolve uses cache");

// 3) 队伍熔炼降频，且不再为不存在的队伍凭空创建背包
requireText(smelter, "if (tickCount % 40 != 0)", "smelter throttled");
forbidText(smelter, "inventory = TeamInventory.getOrCreate(player);", "smelter must not create inventories");

// 4) 事件系统热路径降频
requireText(events, "if (currentTick % 10L == 0L) {\n            syncToClients();", "event sync cadence");
requireText(events, "if (currentTick % 10L == 0L) {\n                AABB bounds = bubble.getBounds();", "bubble query cadence");
requireText(events, "if (currentTick % 10L == 0L) {\n            spawnSporeRainParticles();", "particle cadence");

// 5) 高度与深层矿物补丁不应回归
forbidText(mixinConfig, "DimensionTypeHeightMixin", "height mixin");
assertNoFile("src/main/java/org/alku/life_contract/world/DeepOreGenerationHandler.java");

function assertNoFile(relativePath) {
  if (fs.existsSync(path.join(root, relativePath))) {
    throw new Error(`${relativePath} should stay removed`);
  }
}

console.log("TPS hot path verification passed.");
