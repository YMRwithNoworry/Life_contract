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

const endgame = read("src/main/java/org/alku/life_contract/endgame/StrongholdEndgameManager.java");
const events = read("src/main/java/org/alku/life_contract/events/GameEventManager.java");

// 末影龙被 spore 的朽翼魔（Verfalldrache）取代
requireText(endgame, 'ResourceLocation.fromNamespaceAndPath("spore", "verfalldrache")', "spore end boss id");
requireText(endgame, "private static final ResourceLocation END_BOSS_ID", "end boss constant");
requireText(endgame, 'ResourceLocation.fromNamespaceAndPath("phayriosis", "converted_dragon")', "legacy fallback boss");
requireText(endgame, "private static ResourceLocation resolveEndBossId()", "boss resolution");
requireText(endgame, "hasEntityType(END_BOSS_ID)", "spore availability check");
requireText(endgame, "朽翼魔 Verfalldrache", "boss display name");
requireText(endgame, "spawnEncounterMob(endLevel, bossId, dragonPos, true)", "boss spawn");

// 原版末影龙与血条依然要被移除
requireText(endgame, "suppressVanillaDragonFight(endLevel)", "vanilla dragon fight suppression");
requireText(endgame, "getEntitiesOfClass(EnderDragon.class, islandArea).forEach(Entity::discard)",
  "vanilla dragon removal");
requireText(endgame, "endBossUuid = dragon.getUUID();", "boss tracked");

// 击杀归属 -> 队伍胜利
requireText(endgame, "public static void onDragonDeath(LivingDeathEvent event)", "boss death listener");
requireText(endgame, "isEndBossEntity(event.getEntity())", "boss tag detection");
requireText(endgame, "GameEventManager.declareDragonWinner(killer,", "kill credit declaration");
requireText(events, "public static boolean declareDragonWinner(ServerPlayer winner, net.minecraft.resources.ResourceLocation bossId)",
  "winner declaration signature");
requireText(events, '"verfalldrache".equals(bossId.getPath())', "boss name mapping");

// 最后存活队伍胜利
requireText(events, "private static void checkLastTeamStanding()", "last team standing check");
requireText(events, "if (activeTeams.size() == 1)", "single team detection");
requireText(events, "declareLastStandingTeam(activeTeams.iterator().next())", "last team win declaration");
requireText(events, "checkLastTeamStanding();", "last team check invocation");

// Boss 必须能被普通攻击伤害到（不能是龙类无敌判定）
requireText(endgame, "putBoolean(END_BOSS_TAG, true)", "boss tag set on spawn");

console.log("Endgame boss verification passed.");
