const fs = require("node:fs");
const path = require("node:path");

const root = path.resolve(__dirname, "..");
const borderManager = fs.readFileSync(
  path.join(root, "src/main/java/org/alku/life_contract/border/BorderManager.java"),
  "utf8",
);

function requireText(text, label) {
  if (!borderManager.includes(text)) {
    throw new Error(label + ": missing " + text);
  }
}

// 缩圈节奏：每 3 分钟一次、每次 10%
requireText("GAME_BORDER_SHRINK_INTERVAL_SECONDS = 3 * 60", "shrink interval");
requireText("GAME_BORDER_SHRINK_PERCENTAGE = 10.0D", "shrink percentage");

// 过渡时长必须是 30 秒，而且是毫秒单位。
// 原版 WorldBorder.lerpSizeBetween(from, to, duration) 的第三个参数虽然叫 ticks，
// 但 MovingBorderExtent 内部用 Util.getMillis() 算进度，单位其实是毫秒 ——
// 传 600 就只有 0.6 秒，看起来是"瞬间缩圈"。
requireText("GAME_BORDER_SHRINK_DURATION_MILLIS = 30_000L", "30 second transition (milliseconds)");
if (borderManager.includes("GAME_BORDER_SHRINK_DURATION_TICKS")) {
  throw new Error("transition duration must be expressed in milliseconds, not ticks");
}

// 必须是平滑过渡（lerpSizeBetween），不能是瞬间 setSize
requireText(
  "border.transitionSize(newSize, GAME_BORDER_SHRINK_DURATION_MILLIS)",
  "shrink transition usage",
);
requireText("worldBorder.lerpSizeBetween(worldBorder.getSize(), boundedTargetSize", "smooth lerp");

console.log("Border timing verification passed (30s smooth shrink, 10% per 3 minutes).");
