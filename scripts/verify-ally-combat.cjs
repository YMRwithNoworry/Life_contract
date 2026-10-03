const fs = require("node:fs");
const path = require("node:path");

const root = path.resolve(__dirname, "..");
const followers = fs.readFileSync(
  path.join(root, "src/main/java/org/alku/life_contract/follower/FollowerEvents.java"),
  "utf8",
);

function requireText(text, label) {
  if (!followers.includes(text)) {
    throw new Error(label + ": missing " + text);
  }
}

// 玩家 -> 生物：只有自己/队友的召唤物、队伍守卫还拦；同盟阵营生物放行
requireText("public static boolean isProtectedSummon(Player player, Mob mob)", "protected summon predicate");
requireText("isProtectedSummon(player, mob)", "player attack uses the summon-only guard");
if (followers.includes("if (target instanceof Mob mob && isAlliedWithPlayer(player, mob)) {")) {
  throw new Error("players must be able to kill contract-allied mobs (the blanket ally guard is back)");
}
requireText("无法攻击自己的召唤物！", "feedback message");

// 生物 -> 玩家：同盟生物依旧打不到玩家（契约免疫不变）
requireText("if (attacker instanceof Mob mob && isAlliedWithPlayer(player, mob)) {", "mob to player immunity");
// 生物 -> 生物：同盟生物之间依旧不互相攻击
requireText("areMobsAllied(attackerMob, targetMob)", "mob to mob ally guard");

console.log("Ally combat verification passed (players may kill contract allies, summons stay protected).");
