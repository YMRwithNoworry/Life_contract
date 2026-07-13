const fs = require("node:fs");
const path = require("node:path");

const root = path.resolve(__dirname, "..");

function read(relativePath) {
  return fs.readFileSync(path.join(root, relativePath), "utf8");
}

function methodBody(source, signature) {
  const start = source.indexOf(signature);
  if (start < 0) {
    throw new Error(`Missing method: ${signature}`);
  }
  const open = source.indexOf("{", start);
  let depth = 0;
  for (let index = open; index < source.length; index++) {
    if (source[index] === "{") depth++;
    if (source[index] === "}") depth--;
    if (depth === 0) return source.slice(open, index + 1);
  }
  throw new Error(`Unclosed method: ${signature}`);
}

function requireText(source, text, label) {
  if (!source.includes(text)) {
    throw new Error(`${label}: missing ${text}`);
  }
}

function forbidText(source, text, label) {
  if (source.includes(text)) {
    throw new Error(`${label}: forbidden ${text}`);
  }
}

const contractEvents = read("src/main/java/org/alku/life_contract/ContractEvents.java");
const respawnHandler = read("src/main/java/org/alku/life_contract/border/BorderRespawnHandler.java");
const endgame = read("src/main/java/org/alku/life_contract/endgame/StrongholdEndgameManager.java");
const teammateRevive = read("src/main/java/org/alku/life_contract/revive/ReviveTeammateSystem.java");

const respawnEvent = methodBody(contractEvents, "public static void onPlayerRespawn");
requireText(respawnEvent, "event.isEndConquered()", "death respawn guard");
requireText(respawnEvent, "BorderRespawnHandler.ensureInsideBorder", "death respawn relocation");

requireText(respawnHandler, "border.isWithinBounds", "inside-border fast path");
requireText(respawnHandler, "border.getMinX()", "border X clamp");
requireText(respawnHandler, "border.getMaxZ()", "border Z clamp");
requireText(respawnHandler, "Heightmap.Types.MOTION_BLOCKING_NO_LEAVES", "safe surface lookup");
requireText(respawnHandler, "player.teleportTo", "respawn teleport");

const performRevive = methodBody(teammateRevive, "private static void performRevive");
requireText(performRevive, "BorderRespawnHandler.ensureInsideBorder(teammate)",
  "teammate revive relocation");

requireText(endgame, "END_ISLAND_RADIUS = 100.0D", "main island radius");
requireText(endgame, "END_BORDER_PADDING = 50.0D", "end border padding");
requireText(endgame, "endBorder.setCenter(0.0D, 0.0D)", "end border center");
requireText(endgame, "endBorder.setSize(END_BORDER_SIZE)", "end border size");
requireText(endgame, "new ClientboundInitializeBorderPacket(endBorder)", "end border client sync");

const dimensionChange = methodBody(endgame, "public static void onPlayerChangedDimension");
forbidText(dimensionChange, "GameEventManager.isGameActive()", "end entry must work outside active games");
requireText(dimensionChange, "configureEndBorder", "end border initialization");
requireText(dimensionChange, "initializeEndEncounter", "end encounter initialization");

const entityJoin = methodBody(endgame, "public static void onEntityJoinLevel");
forbidText(entityJoin, "GameEventManager.isGameActive()", "vanilla dragon rejection must always apply");
requireText(entityJoin, "entity instanceof EnderDragon", "vanilla dragon rejection");

requireText(endgame, 'ResourceLocation.fromNamespaceAndPath("phayriosis", "converted_dragon")',
  "converted dragon entity id");
requireText(endgame, "dragonFight.removePlayer", "vanilla dragon boss bar cleanup");
requireText(endgame, "endLevel.setDragonFight(null)", "vanilla dragon fight shutdown");
requireText(endgame, "getEntitiesOfClass(EnderDragon.class", "vanilla dragon entity cleanup");
requireText(endgame, "SpikeFeature.getSpikesForLevel", "seed-aware End spike lookup");
requireText(endgame, "SpikeFeature.EndSpike::getHeight", "tallest End spike height");

const levelTick = methodBody(endgame, "public static void onLevelTick");
requireText(levelTick, "TickEvent.Phase.END", "post-movement flight ceiling enforcement");
requireText(levelTick, "capConvertedDragonFlight", "converted dragon flight ceiling");

const flightCap = methodBody(endgame, "private static void capConvertedDragonFlight");
requireText(flightCap, "convertedDragonFlightCeiling", "calculated flight ceiling");
requireText(flightCap, "dragon.setPos", "dragon position clamp");
requireText(flightCap, "dragon.setDeltaMovement", "upward motion clamp");

console.log("End encounter verification passed.");
