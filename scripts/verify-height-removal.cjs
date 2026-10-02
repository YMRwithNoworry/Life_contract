const fs = require("node:fs");
const path = require("node:path");

const root = path.resolve(__dirname, "..");

function read(relativePath) {
  return fs.readFileSync(path.join(root, relativePath), "utf8");
}

function assertMissing(relativePath, label) {
  if (fs.existsSync(path.join(root, relativePath))) {
    throw new Error(`${label}: ${relativePath} should have been removed`);
  }
}

function assertNoText(source, text, label) {
  if (source.includes(text)) {
    throw new Error(`${label}: unexpected ${text}`);
  }
}

// 地形高度修改相关的 mixin 与深层矿物补丁必须彻底移除
assertMissing("src/main/java/org/alku/life_contract/mixin/DimensionTypeHeightMixin.java", "overworld height mixin");
assertMissing("src/main/java/org/alku/life_contract/mixin/NoiseGeneratorSettingsMixin.java", "noise height mixin");
assertMissing("src/main/java/org/alku/life_contract/mixin/StrongholdStructureMixin.java", "deep stronghold mixin");
assertMissing("src/main/java/org/alku/life_contract/world/DeepOreGenerationHandler.java", "deep ore handler");

const mixinConfig = read("src/main/resources/life_contract.mixins.json");
assertNoText(mixinConfig, "DimensionTypeHeightMixin", "mixin config");
assertNoText(mixinConfig, "NoiseGeneratorSettingsMixin", "mixin config");
assertNoText(mixinConfig, "StrongholdStructureMixin", "mixin config");

console.log("Terrain height removal verification passed.");
