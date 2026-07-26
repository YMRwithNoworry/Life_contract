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
const gameEvents = read("src/main/java/org/alku/life_contract/events/GameEventManager.java");
const mixinConfig = read("src/main/resources/life_contract.mixins.json");
const blockBreakMixin = read("src/main/java/org/alku/life_contract/mixin/ServerPlayerGameModeMixin.java");
const blockUseMixin = read("src/main/java/org/alku/life_contract/mixin/ServerGamePacketListenerImplMixin.java");
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

requireText(endgame, "END_BORDER_SIZE = 500.0D", "fixed End border size");
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
requireText(endgame, "CONVERTED_DRAGON_MAX_Y = 105.0D", "fixed converted dragon ceiling");
requireText(endgame, "CONVERTED_DRAGON_RESET_Y = 103.0D", "converted dragon reset height");
forbidText(endgame, "findHighestEndSpike", "converted dragon ceiling must not depend on End spikes");

const levelTick = methodBody(endgame, "public static void onLevelTick");
requireText(levelTick, "TickEvent.Phase.END", "post-movement flight ceiling enforcement");
requireText(levelTick, "capConvertedDragonFlight", "converted dragon flight ceiling");

const flightCap = methodBody(endgame, "private static void capConvertedDragonFlight");
requireText(flightCap, "CONVERTED_DRAGON_MAX_Y", "fixed flight ceiling");
requireText(flightCap, "CONVERTED_DRAGON_RESET_Y", "reset below flight ceiling");
requireText(flightCap, "dragon.setPos", "dragon position clamp");
requireText(flightCap, "dragon.setDeltaMovement", "upward motion clamp");

requireText(mixinConfig, "ServerPlayerGameModeMixin", "outside-border block breaking mixin registration");
requireText(mixinConfig, "ServerGamePacketListenerImplMixin", "outside-border block placement mixin registration");
requireText(blockBreakMixin, "BorderInteractionHelper.mayInteractIgnoringWorldBorder", "outside-border block breaking allowance");
requireText(blockUseMixin, "BorderInteractionHelper.mayInteractIgnoringWorldBorder", "outside-border block placement allowance");

const relocateParticipants = methodBody(gameEvents, "private static void relocateParticipantsInsideBorder");
requireText(relocateParticipants, "buildSpawnPlatform", "spawn platform creation");
const spawnPlatform = methodBody(gameEvents, "private static void buildSpawnPlatform");
requireText(spawnPlatform, "offsetX = -2; offsetX <= 2", "five-block platform width");
requireText(spawnPlatform, "offsetZ = -2; offsetZ <= 2", "five-block platform depth");
requireText(spawnPlatform, "Blocks.COBBLESTONE", "cobblestone spawn platform");

requireText(gameEvents, "INITIAL_DEBUFF_PROTECTION_SECONDS = 120L", "two-minute debuff protection");
const debuffProtection = methodBody(gameEvents, "public static boolean hasInitialDebuffProtection");
requireText(debuffProtection, "gameStartPlayerIds.contains", "debuff protection participant guard");
const initialDebuffApplicable = methodBody(contractEvents, "public static void onInitialDebuffApplicable");
requireText(initialDebuffApplicable, "MobEffectCategory.HARMFUL", "harmful effect detection");
requireText(initialDebuffApplicable, "Event.Result.DENY", "harmful effect rejection");

console.log("End encounter verification passed.");
