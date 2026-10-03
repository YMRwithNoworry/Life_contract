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
const followerCommands = read("src/main/java/org/alku/life_contract/ContractCommands.java");
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

// 5) 契约阵营的“自然刷怪”不再被设为永不消失（否则生物只增不减）
const allyBlock = followers.slice(followers.indexOf("private static void registerContractModAlly"),
        followers.indexOf("public static boolean isContractAlly"));
forbidText(allyBlock, "mob.setPersistenceRequired();", "contract ally must despawn naturally");
requireText(followers, "public static void registerFollower(Mob mob, UUID ownerUUID)", "manual follower path");

// 6) 空投追踪线：没有目标时不再每 5 tick 重复发空包
requireText(navigator, "trackingActive", "tracking state set");
requireText(navigator, "trackingActive.remove(player.getUUID())", "clear only when tracked");
requireText(navigator, "public static void forgetPlayer(UUID playerId)", "logout prune");
requireText(airdropEvents, "AirdropNavigator.forgetPlayer(event.getEntity().getUUID())", "logout hook");

// 7) 追随者 AI 目标安装必须幂等，否则区块反复加载会让每 tick AI 评估次数无限增长
requireText(followers, "private static final Map<UUID, List<Goal>> INSTALLED_AI_GOALS", "goal tracking map");
requireText(followers, "removeInstalledGoals(mob);", "idempotent install");
requireText(followers, "private static void removeInstalledGoals(Mob mob)", "goal removal helper");
requireText(followers, "INSTALLED_AI_GOALS.remove(mobUUID);", "goal tracking pruned with follower");

// 8) 守卫缓存要在实体离开世界时清理
const golem = read("src/main/java/org/alku/life_contract/TeamIronGolemSystem.java");
requireText(golem, "removeGolemFromCache(golem.getUUID())", "golem cache prune");

// 8) 「标记」集火扫描按 tick 节流（枪械高频命中不再每一发都扫 64³ 范围）
requireText(mutation, "MARK_AGGRO_INTERVAL_TICKS", "mark aggro throttle constant");
requireText(mutation, "MARK_AGGRO_LAST_TICK.getOrDefault(p.getUUID(), -1000L)", "mark aggro throttle check");
requireText(mutation, "MARK_AGGRO_LAST_TICK.remove(e.getEntity().getUUID())", "mark aggro state pruned");

// 9) 登录时的全量实体扫描跳过已登记友军
requireText(followers, "ownerUUID.equals(data.getUUID(TAG_CONTRACT_OWNER_UUID))", "skip already-registered allies");

// 10) 客户端每帧热路径：命数查询走索引、守卫查询降频
const client = read("src/main/java/org/alku/life_contract/ClientDataStorage.java");
const nameplate = read("src/main/java/org/alku/life_contract/client/LifePointNameplateRenderer.java");
const highlight = read("src/main/java/org/alku/life_contract/TeamHighlightRenderer.java");
requireText(client, "public static int getLifePointsFor(UUID playerId)", "life point index");
requireText(client, "playerLifePointMap.put(data.uuid(), data.lifePoints())", "life point index build");
requireText(nameplate, "ClientDataStorage.getLifePointsFor(player.getUUID())", "nameplate uses index");
forbidText(nameplate, "for (PacketSyncLifePoints.PlayerLifePoints", "nameplate must not scan the list");
requireText(highlight, "GOLEM_QUERY_INTERVAL_TICKS", "golem query interval");
requireText(highlight, "cachedTeamGolems", "golem query cache");
requireText(nameplate, "LP_SUFFIX_CACHE", "nameplate suffix cache");
const contractHud = read("src/main/java/org/alku/life_contract/ContractHUD.java");
requireText(contractHud, "CONTENT_REFRESH_TICKS", "contract HUD content cache");
requireText(contractHud, "cachedLines", "contract HUD cached lines");
requireText(contractHud, "rebuildContent(player)", "contract HUD rebuild path");
const eventHud = read("src/main/java/org/alku/life_contract/client/EventHUD.java");
requireText(eventHud, "cachedStatusLines", "event HUD cached lines");
requireText(eventHud, "buildStatusLines(minecraft)", "event HUD rebuild path");
forbidText(contractEvents, "onServerTick", "dead per-tick handler");
// 名牌渲染每帧都会触发 NameFormat，队伍颜色必须按 tick 缓存
requireText(contractEvents, "private record TeamColorStamp(int tick, int color)", "team colour stamp");
requireText(contractEvents, "private static int getCachedTeamColor(Player player)", "cached team colour");
requireText(contractEvents, "int teamColor = getCachedTeamColor(player);", "NameFormat uses cache");
requireText(contractEvents, "TEAM_COLOR_CACHE.remove(playerId);", "team colour cache pruned");
requireText(contractEvents, "LAST_ATTACKER_MOD.remove(playerId)", "attack map pruned on logout");
requireText(contractEvents, "LAST_ATTACK_TIME.remove(playerId)", "attack time map pruned on logout");

// 12) 伤害热路径不得每次事件都分配数组；等级为 0 时直接跳过
requireText(mutation, "private static final float[] BLADE_BONUS", "blade bonus constant array");
forbidText(mutation, "float[] bonus={", "per-event array allocation");
requireText(mutation, "if (lv <= 0 || lv >= BLADE_BONUS.length) return;", "skip zero-level damage adjust");

// 13) 雷达屏蔽敌方玩家时，本地队伍 ID 按 tick 缓存
const radarMixin = read("src/main/java/org/alku/life_contract/mixin/XaeroRadarStateUpdaterMixin.java");
requireText(radarMixin, "lifeContract$localTeamTick", "radar local team cache tick");
requireText(radarMixin, "lifeContract$localTeamId", "radar local team cache value");
requireText(radarMixin, "localPlayer.tickCount != lifeContract$localTeamTick", "radar cache refresh check");

// 14) 导航在“没有目标也没有追踪线”时直接返回，不遍历玩家
requireText(navigator, "activeAirdrops.isEmpty() && activeDecoys.isEmpty() && trackingActive.isEmpty()",
        "navigator no-op early return");

// 15) 分段计时：默认关闭、关闭时零开销，且四个每 tick 处理器都已接入
const profiler = read("src/main/java/org/alku/life_contract/PerfProfiler.java");
requireText(profiler, "public static long begin()", "profiler begin");
requireText(profiler, "public static void end(String name, long startNanos)", "profiler end");
requireText(profiler, "return enabled ? System.nanoTime() : 0L;", "profiler disabled fast path");
requireText(profiler, "if (startNanos == 0L)", "profiler end fast path");
requireText(profiler, "public static List<String> report()", "profiler report");
for (const [file, section] of [
  ["src/main/java/org/alku/life_contract/border/BorderManager.java", "BorderManager"],
  ["src/main/java/org/alku/life_contract/events/GameEventManager.java", "GameEventManager"],
  ["src/main/java/org/alku/life_contract/events/WorldEventManager.java", "WorldEventManager"],
  ["src/main/java/org/alku/life_contract/TeamSmelter.java", "TeamSmelter"],
  ["src/main/java/org/alku/life_contract/airdrop/event/CommonEvents.java", "Airdrop.levelTick"],
  ["src/main/java/org/alku/life_contract/endgame/StrongholdEndgameManager.java", "EndBoss.levelTick"],
]) {
  const source = read(file);
  requireText(source, "long perfStart = PerfProfiler.begin();", section + " timing start");
  requireText(source, 'PerfProfiler.end("' + section + '"', section + " timing end");
}

// 16) 性能诊断指令：按需采样，不占每 tick 开销
const diagnostics = read("src/main/java/org/alku/life_contract/PerfDiagnostics.java");
requireText(diagnostics, "public static void report(CommandSourceStack source)", "diagnostics entry point");
requireText(diagnostics, "server.getTickCount()", "tick sampling on demand");
requireText(diagnostics, "level.getAllEntities()", "entity census");
requireText(diagnostics, "FollowerEvents.debugSummary()", "module state report");
requireText(followerCommands, 'Commands.literal("perf")', "perf command registration");
requireText(followerCommands, "PerfDiagnostics.report(context.getSource())", "perf command wiring");
requireText(followers, "public static String debugSummary()", "follower debug summary");
requireText(diagnostics, "EntityJoinLevelEvent", "entity join churn counter");
requireText(diagnostics, "EntityLeaveLevelEvent", "entity leave churn counter");
requireText(diagnostics, "实体变化", "entity churn report line");
requireText(diagnostics, "reportProfiler(source)", "profiler report section");
requireText(followerCommands, 'Commands.literal("on")', "perf on subcommand");
requireText(followerCommands, "PerfProfiler.setEnabled(true)", "perf enable wiring");
requireText(followerCommands, "PerfProfiler.setEnabled(false)", "perf disable wiring");

const sublimation = read("src/main/java/org/alku/life_contract/items/SublimationItem.java");
requireText(sublimation, "COLOR_REFRESH_MILLIS", "name colour cache");
requireText(sublimation, "cachedColorMillis", "name colour cache state");

// 11) 高度与深层矿物补丁不应回归
forbidText(mixinConfig, "DimensionTypeHeightMixin", "height mixin");
assertNoFile("src/main/java/org/alku/life_contract/world/DeepOreGenerationHandler.java");

function assertNoFile(relativePath) {
  if (fs.existsSync(path.join(root, relativePath))) {
    throw new Error(`${relativePath} should stay removed`);
  }
}

console.log("TPS hot path verification passed.");
