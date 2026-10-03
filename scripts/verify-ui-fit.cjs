const fs = require("node:fs");
const path = require("node:path");

const root = path.resolve(__dirname, "..");
const clientDir = path.join(root, "src/main/java/org/alku/life_contract/client");

// 界面设计尺寸上限。GUI 缩放是按物理分辨率自动选的：1920x1080 下自动缩放为 4，
// 逻辑分辨率只有 480x270；1280x720 下是 426x240。设计尺寸超过这个范围就会跑出屏幕。
const MAX_DESIGN_WIDTH = 460;
const MAX_DESIGN_HEIGHT = 250;

const holders = fs.readdirSync(clientDir).filter((name) => name.endsWith("UIHolder.java"));
if (holders.length === 0) {
  throw new Error("no UI holder found");
}

for (const holder of holders) {
  const source = fs.readFileSync(path.join(clientDir, holder), "utf8");

  // 1) 每个界面都必须把尺寸夹进屏幕，否则高 GUI 缩放下会超出屏幕。
  if (!source.includes("UiLayout.fitToScreen")) {
    throw new Error(holder + " must clamp its size with UiLayout.fitToScreen");
  }

  // 2) 设计尺寸本身也不能过大。
  for (const match of source.matchAll(/fitToScreen\((\d+),\s*(\d+)\)/g)) {
    const width = Number(match[1]);
    const height = Number(match[2]);
    if (width > MAX_DESIGN_WIDTH || height > MAX_DESIGN_HEIGHT) {
      throw new Error(
        holder + " design size " + width + "x" + height +
        " exceeds the safe bound " + MAX_DESIGN_WIDTH + "x" + MAX_DESIGN_HEIGHT);
    }
  }
}

// 3) 升华商店与异变树（都用升华）必须是单列布局，不能再左右分栏把界面撑宽。
const shop = fs.readFileSync(path.join(clientDir, "SublimationShopUIHolder.java"), "utf8");
if (!shop.includes("PANEL_WIDTH = 320")) {
  throw new Error("sublimation shop should stay at the 320-wide design");
}
for (const needed of ["TaffyDisplay.NONE", "TaffyDisplay.FLEX"]) {
  if (!shop.includes(needed)) {
    throw new Error("sublimation shop should swap views via " + needed);
  }
}

console.log("UI fit verification passed (" + holders.length + " holders checked).");