const fs = require("node:fs");
const path = require("node:path");

const root = path.resolve(__dirname, "..");
const borderManager = fs.readFileSync(
  path.join(root, "src/main/java/org/alku/life_contract/border/BorderManager.java"),
  "utf8",
);

function requireText(text, label) {
  if (!borderManager.includes(text)) {
    throw new Error(`${label}: missing ${text}`);
  }
}

requireText("BASE_BORDER_TRANSITION_TICKS = 60L", "original transition duration");
requireText("BORDER_TRANSITION_DURATION_PERCENT = 130L", "130 percent duration");
requireText(
  "BASE_BORDER_TRANSITION_TICKS * BORDER_TRANSITION_DURATION_PERCENT / 100L",
  "scaled transition duration",
);
requireText("border.transitionSize(newSize, BORDER_TRANSITION_TICKS)", "shrink transition usage");
requireText("durationTicks * 50L", "tick to millisecond conversion");

console.log("Border timing verification passed.");
