const fs = require("node:fs");
const path = require("node:path");

const root = path.resolve(__dirname, "..");
const clientDir = path.join(root, "src/main/java/org/alku/life_contract/client");

// 界面设计尺寸上限。GUI 缩放是按物理分辨率自动选的：1920x1080 下自动缩放为 4，
// 逻辑分辨率只有 480x270；1280x720 下是 426x240。设计尺寸超过这个范围就会跑出屏幕。
const MAX_DESIGN_WIDTH = 460;
const MAX_DESIGN_HEIGHT = 250;

// 两个消耗升华的界面（升华商店 / 阵营异变树）必须保持"小面板"：
// 它们是高频打开的操作界面，铺满半个屏幕会挡住游戏画面。
const SMALL_PANELS = {
  "SublimationShopUIHolder.java": { width: 240, height: 152 },
  "MutationUIHolder.java": { width: 232, height: 144 },
};

const holders = fs.readdirSync(clientDir).filter((name) => name.endsWith("UIHolder.java"));
if (holders.length === 0) {
  throw new Error("no UI holder found");
}

const constant = (source, name) => {
  const match = source.match(new RegExp("int " + name + "\\s*=\\s*(\\d+)"));
  return match ? Number(match[1]) : null;
};

for (const holder of holders) {
  const source = fs.readFileSync(path.join(clientDir, holder), "utf8");

  // 1) 每个界面都必须把尺寸夹进屏幕，否则高 GUI 缩放下会超出屏幕。
  if (!source.includes("UiLayout.fitToScreen")) {
    throw new Error(holder + " must clamp its size with UiLayout.fitToScreen");
  }

  // 2) 设计尺寸本身也不能过大。尺寸可以是字面量，也可以是 PANEL_WIDTH/PANEL_HEIGHT
  //    （PANEL_LIFT 是"把面板整体抬高"的外层余量，同样要算进去）。
  const declared = [];
  const width = constant(source, "PANEL_WIDTH");
  const height = constant(source, "PANEL_HEIGHT");
  if (width !== null && height !== null) {
    declared.push([width, height + (constant(source, "PANEL_LIFT") || 0)]);
  }
  for (const match of source.matchAll(/fitToScreen\((\d+),\s*(\d+)\)/g)) {
    declared.push([Number(match[1]), Number(match[2])]);
  }
  if (declared.length === 0) {
    throw new Error(holder + " declares no parsable design size");
  }
  for (const [w, h] of declared) {
    if (w > MAX_DESIGN_WIDTH || h > MAX_DESIGN_HEIGHT) {
      throw new Error(
        holder + " design size " + w + "x" + h +
        " exceeds the safe bound " + MAX_DESIGN_WIDTH + "x" + MAX_DESIGN_HEIGHT);
    }
  }
}

// 3) 升华商店与异变树：小面板 + 单列 + 常显滑块。
const shop = fs.readFileSync(path.join(clientDir, "SublimationShopUIHolder.java"), "utf8");
for (const needed of ["TaffyDisplay.NONE", "TaffyDisplay.FLEX"]) {
  if (!shop.includes(needed)) {
    throw new Error("sublimation shop should swap views via " + needed);
  }
}

for (const [holder, size] of Object.entries(SMALL_PANELS)) {
  const source = fs.readFileSync(path.join(clientDir, holder), "utf8");
  if (!source.includes("PANEL_WIDTH = " + size.width) ||
      !source.includes("PANEL_HEIGHT = " + size.height)) {
    throw new Error(holder + " should stay at the " + size.width + "x" + size.height + " design");
  }
  // 列表必须常显滑块（默认的 AUTO 只在滚动时才画出来，玩家看不出还能往下翻）。
  if (!source.includes("UiLayout.verticalScroller()")) {
    throw new Error(holder + " should scroll with UiLayout.verticalScroller()");
  }
  // 可视高度必须写死：只靠 flexGrow 会被内容顶开，表现为"面板被撑破 + 滑块拖不动"。
  if (!source.includes(".height(SCROLL_HEIGHT)")) {
    throw new Error(holder + " should pin its scroll view height with SCROLL_HEIGHT");
  }
}

// 4) 窄面板里的文字必须自动折行，否则超宽的行会被直接裁掉。
for (const file of ["SublimationShopUIHolder.java", "MutationUIHolder.java", "AccessoryDetailCard.java"]) {
  const source = fs.readFileSync(path.join(clientDir, file), "utf8");
  if (!source.includes("UiLayout.wrapText(")) {
    throw new Error(file + " should wrap its text with UiLayout.wrapText");
  }
}

// 5) 滚动区必须压住 flex 的最小高度，否则会被内容撑高：表现为"滚不动 + 内容溢出面板"。
const uiLayout = fs.readFileSync(path.join(clientDir, "UiLayout.java"), "utf8");
if (!uiLayout.includes("minHeight(0)")) {
  throw new Error("UiLayout.verticalScroller must set minHeight(0) or the list overflows the panel");
}

console.log("UI fit verification passed (" + holders.length + " holders checked).");
