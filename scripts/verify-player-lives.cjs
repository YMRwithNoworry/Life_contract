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

const lives = read("src/main/java/org/alku/life_contract/PlayerLivesSystem.java");
const game = read("src/main/java/org/alku/life_contract/events/GameEventManager.java");
const revive = read("src/main/java/org/alku/life_contract/revive/ReviveTeammateSystem.java");

// 1) 默认 5 条命，开局重置
requireText(lives, "public static final int DEFAULT_LIVES = 5;", "default lives");
requireText(lives, 'private static final String TAG_LIVES = "LifeContractLives";', "lives storage tag");
requireText(lives, "public static void resetLives(ServerPlayer player)", "lives reset");
requireText(game, "PlayerLivesSystem.resetLives(player);", "reset on participant registration");

// 2) 每次死亡 -1
requireText(lives, "int remaining = current - 1;", "death decrement");
requireText(lives, "setLives(player, remaining);", "decrement applied");
requireText(lives, "public static void onPlayerDeath(LivingDeathEvent event)", "death listener");
requireText(lives, "GameEventManager.isPlayerPartOfGame(player.getUUID())", "participant gate");

// 3) 命数只剩 1 再死亡 -> 旁观
requireText(lives, "if (remaining <= 0) {", "elimination branch");
requireText(lives, "player.setGameMode(GameType.SPECTATOR);", "spectator on elimination");
requireText(lives, "public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event)", "respawn guard");
requireText(lives, "public static void onPlayerClone(PlayerEvent.Clone event)", "lives carried over on respawn");

// 名牌显示与复活联动
requireText(lives, "public static int getLivesForDisplay(ServerPlayer player)", "display value");
requireText(game, "PlayerLivesSystem.getLivesForDisplay(player)", "nameplate sync source");
requireText(revive, "PlayerLivesSystem.reviveAsSurvivor(teammate);", "revive integration");
requireText(game, "PlayerLivesSystem.clearLives(player);", "lives cleared when the match stops");

// caerula_arbor 只是可选的：命数不能只存在它的字段里
requireText(lives, "CaerulaArborCompat.setLifePoints(player, Math.max(1, value));", "optional mirror into caerula_arbor");
if (lives.includes("getLifePoints(player);") === false) {
  throw new Error("display fallback to caerula_arbor missing");
}

console.log("Player lives verification passed.");
