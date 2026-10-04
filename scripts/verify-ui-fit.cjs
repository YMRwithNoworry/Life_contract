// 界面尺寸自适应校验。
// 规则：每个界面都必须按屏幕尺寸决定大小（fitToScreen 夹住 / fillScreen 铺满），
// 铺满的界面内部必须靠百分比 + flexGrow 伸展，列表不能写死高度（否则内容会被下边缘裁掉）。
const fs = require("node:fs");
const path = require("node:path");

const root = path.resolve(__dirname, "..");
const clientDir = path.join(root, "src/main/java/org/alku/life_contract/client");

// 小面板界面的设计尺寸上限。GUI 缩放按物理分辨率自动选：1920x1080 -> 缩放 4，
// 逻辑分辨率只有 480x270；1280x720 -> 426x240。超过这个范围就会跑出屏幕。
const MAX_DESIGN_WIDTH = 460;
const MAX_DESIGN_HEIGHT = 250;

// 铺满整个屏幕的界面：没有固定尺寸，内部全部按百分比 / flexGrow 伸展。
const MAXIMIZED_PANELS = ["SublimationShopUIHolder.java", "MutationUIHolder.java"];

const holders = fs.readdirSync(clientDir).filter((name) => name.endsWith("UIHolder.java"));
if (holders.length === 0) {
  throw new Error("no UI holder found");
}

const readHolder = (name) => fs.readFileSync(path.join(clientDir, name), "utf8");
const constant = (source, name) => {
  const match = source.match(new RegExp("int " + name + "\\s*=\\s*(\\d+)"));
  return match ? Number(match[1]) : null;
};

for (const holder of holders) {
  const source = readHolder(holder);

  // 1) 每个界面都必须按屏幕尺寸决定大小，否则高 GUI 缩放下会超出屏幕。
  const clamps = source.includes("UiLayout.fitToScreen");
  const fills = source.includes("UiLayout.fillScreen");
  if (!clamps && !fills) {
    throw new Error(holder + " must size itself against the screen (fitToScreen or fillScreen)");
  }

  // 2) 写死的设计尺寸（fitToScreen 的字面量）不能超过安全上限。
  const declared = [];
  for (const match of source.matchAll(/fitToScreen\((\d+),\s*(\d+)\)/g)) {
    declared.push([Number(match[1]), Number(match[2])]);
  }
  const width = constant(source, "PANEL_WIDTH");
  const height = constant(source, "PANEL_HEIGHT");
  if (width !== null && height !== null) {
    declared.push([width, height + (constant(source, "PANEL_LIFT") || 0)]);
  }
  for (const [w, h] of declared) {
    if (w > MAX_DESIGN_WIDTH || h > MAX_DESIGN_HEIGHT) {
      throw new Error(holder + " design size " + w + "x" + h +
        " exceeds the safe bound " + MAX_DESIGN_WIDTH + "x" + MAX_DESIGN_HEIGHT);
    }
  }
}

// 3) 铺满屏幕的界面：不许有固定尺寸，必须靠百分比 + flexGrow 伸展，列表不许写死高度。
for (const holder of MAXIMIZED_PANELS) {
  const source = readHolder(holder);
  if (!source.includes("UiLayout.fillScreen()")) {
    throw new Error(holder + " must fill the screen with UiLayout.fillScreen()");
  }
  if (constant(source, "PANEL_WIDTH") !== null || constant(source, "PANEL_HEIGHT") !== null) {
    throw new Error(holder + " must not pin a fixed panel size when it is maximized");
  }
  if (!source.includes("widthPercent(100).heightPercent(100)")) {
    throw new Error(holder + " must stretch its panel to the full screen");
  }
  if (!source.includes(".flexGrow(1)")) {
    throw new Error(holder + " must let its list take the remaining height with flexGrow");
  }
  if (/\.height\(SCROLL_HEIGHT\)/.test(source) || source.includes("SCROLL_HEIGHT =")) {
    throw new Error(holder + " must not pin its scroll height - the list has to grow with the window");
  }
  // 列表必须常显滑块（默认的 AUTO 只在滚动时才画出来，玩家看不出还能往下翻）。
  if (!source.includes("UiLayout.verticalScroller()")) {
    throw new Error(holder + " should scroll with UiLayout.verticalScroller()");
  }
}

// 4) 升华商店：标题右侧余额必须给固定宽度。
//    flex 行里没设宽度的文本元素会被压成极窄的一条，文字会竖着折行画到面板外。
const shop = readHolder("SublimationShopUIHolder.java");
for (const needed of ["TaffyDisplay.NONE", "TaffyDisplay.FLEX"]) {
  if (!shop.includes(needed)) {
    throw new Error("sublimation shop should swap views via " + needed);
  }
}
if (!shop.includes("balanceLabel.getLayout().width(BALANCE_WIDTH)")) {
  throw new Error("sublimation shop balance needs an explicit width or it wraps into a vertical strip");
}

// 5) 窄面板里的文字必须自动折行，否则超宽的行会被直接裁掉。
for (const file of ["SublimationShopUIHolder.java", "MutationUIHolder.java", "AccessoryDetailCard.java"]) {
  if (!readHolder(file).includes("UiLayout.wrapText(")) {
    throw new Error(file + " should wrap its text with UiLayout.wrapText");
  }
}

// 6) 滚动区必须压住 flex 的最小高度，否则会被内容撑高：表现为"滚不动 + 内容溢出面板"。
const uiLayout = fs.readFileSync(path.join(clientDir, "UiLayout.java"), "utf8");
if (!uiLayout.includes("minHeight(0)")) {
  throw new Error("UiLayout.verticalScroller must set minHeight(0) or the list overflows the panel");
}
if (!uiLayout.includes("public static UI.DynamicSizeProvider fillScreen()")) {
  throw new Error("UiLayout must expose fillScreen() for maximized panels");
}
// fillScreen 不能像 fitToScreen 那样收缩，否则"铺满屏幕"就是假的。
const fillBody = uiLayout.slice(uiLayout.indexOf("fillScreen()"));
if (fillBody.slice(0, 400).includes("clamp(")) {
  throw new Error("UiLayout.fillScreen must not shrink the available size");
}

console.log("UI fit verification passed (" + holders.length + " holders checked, " +
  MAXIMIZED_PANELS.length + " maximized).");
