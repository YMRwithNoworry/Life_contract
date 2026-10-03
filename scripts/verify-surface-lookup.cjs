const fs = require("node:fs");
const path = require("node:path");

const root = path.resolve(__dirname, "..");
const javaRoot = path.join(root, "src/main/java");
const finder = "src/main/java/org/alku/life_contract/world/SurfaceFinder.java";

function read(relativePath) {
  return fs.readFileSync(path.join(root, relativePath), "utf8");
}

function walk(dir) {
  const out = [];
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    const full = path.join(dir, entry.name);
    if (entry.isDirectory()) {
      out.push(...walk(full));
    } else if (entry.name.endsWith(".java")) {
      out.push(full);
    }
  }
  return out;
}

function relative(full) {
  return path.relative(root, full).split(path.sep).join("/");
}

// 1) 除了 SurfaceFinder 自己，任何地方都不允许再直接信任高度图。
//    原因见 SurfaceFinder 的类注释：区块未加载时高度图会退化成 minBuildHeight，
//    而高度图的原始数据是相对 minBuildHeight 存的，世界高度上下限变过之后
//    旧存档会被整体读偏，直接把平台/气泡放到天上或地底。
const offenders = [];
for (const file of walk(javaRoot)) {
  const rel = relative(file);
  if (rel === finder) {
    continue;
  }
  const source = read(rel);
  if (source.includes("getHeightmapPos(") || source.includes("Heightmap.Types")) {
    offenders.push(rel);
  }
}
if (offenders.length > 0) {
  throw new Error("surface lookup must go through SurfaceFinder, found raw heightmap usage in:\n  " + offenders.join("\n  "));
}

// 2) SurfaceFinder 必须同时做"强制加载区块"和"用真实方块校验"两件事。
const finderSource = read(finder);
for (const required of ["level.getChunk(", "isSurfaceAir", "getMinBuildHeight", "getMaxBuildHeight"]) {
  if (!finderSource.includes(required)) {
    throw new Error("SurfaceFinder is missing " + required);
  }
}

// 3) 曾经出过 bug 的调用点必须走 SurfaceFinder。
const callSites = [
  ["src/main/java/org/alku/life_contract/events/GameEventManager.java", "SurfaceFinder.findSurfaceY", "game start spawn platform"],
  ["src/main/java/org/alku/life_contract/events/WorldEventManager.java", "SurfaceFinder.findSurfaceY", "safe bubble / elite spawn"],
  ["src/main/java/org/alku/life_contract/border/EliminationHandler.java", "SurfaceFinder.findSurfacePos", "elimination mob spawn"],
  ["src/main/java/org/alku/life_contract/border/BorderRespawnHandler.java", "SurfaceFinder.findSurfaceY", "border respawn"],
  ["src/main/java/org/alku/life_contract/endgame/StrongholdEndgameManager.java", "SurfaceFinder.findSurfacePos", "end encounter spawn"],
];
for (const [file, needle, label] of callSites) {
  if (!read(file).includes(needle)) {
    throw new Error(label + ": " + file + " should call " + needle);
  }
}

console.log("Surface lookup verification passed.");