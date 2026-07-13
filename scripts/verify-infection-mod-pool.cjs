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

const config = read("src/main/java/org/alku/life_contract/ModPoolConfig.java");
const screen = read("src/main/java/org/alku/life_contract/client/LifeContractConfigScreen.java");
const commands = read("src/main/java/org/alku/life_contract/ContractCommands.java");

requireText(config, 'LEGACY_PHAYRIOSIS_ID = "phayriosis"', "legacy mod id");
requireText(config, "isAssignableInfectionMod", "central pool eligibility rule");
requireText(config, "data.modPool.removeIf", "loaded config cleanup");
requireText(config, "filter(ModPoolConfig::isAssignableInfectionMod)", "pool read filtering");
requireText(screen, "ModPoolConfig.isAssignableInfectionMod(info.getModId())", "Cloth Config filtering");
requireText(commands, "ModPoolConfig.isAssignableInfectionMod(modId)", "pool command validation");
requireText(config, 'PHAYRIOSIS_REBORN_ID = "phayriosisreborn"', "reborn mod remains assignable");

console.log("Infection mod pool verification passed.");
