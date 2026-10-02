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

const payload = read("src/main/java/org/alku/life_contract/PacketSyncWaypoints.java");
const sync = read("src/main/java/org/alku/life_contract/WaypointSync.java");
const hud = read("src/main/java/org/alku/life_contract/ContractHUD.java");
const client = read("src/main/java/org/alku/life_contract/ClientDataStorage.java");
const network = read("src/main/java/org/alku/life_contract/NetworkHandler.java");
const events = read("src/main/java/org/alku/life_contract/events/GameEventManager.java");
const endgame = read("src/main/java/org/alku/life_contract/endgame/StrongholdEndgameManager.java");

// 同步包
requireText(payload, 'NetworkHandler.type("sync_waypoints")', "waypoint payload id");
requireText(payload, "public record PacketSyncWaypoints", "waypoint payload type");
requireText(network, "PacketSyncWaypoints.STREAM_CODEC", "waypoint payload registration");
requireText(network, "EventHUD.updateWaypoints(payload)", "client waypoint handler");

// 服务端广播
requireText(sync, "StrongholdEndgameManager.getPortalCenter()", "portal coordinates");
requireText(sync, "BorderManager.getCurrentBorder()", "border centre coordinates");
requireText(sync, "StrongholdEndgameManager.isPortalActivated()", "portal state");
requireText(sync, "public static void broadcast()", "broadcast helper");
requireText(sync, "public static void clear()", "clear helper");
requireText(sync, "PlayerEvent.PlayerLoggedInEvent", "login resync");
requireText(events, "WaypointSync.broadcast();", "broadcast on game start");
requireText(events, "WaypointSync.clear();", "clear on game stop");
requireText(endgame, "WaypointSync.broadcast();", "broadcast when the portal opens");

// 客户端存储与 HUD 渲染
requireText(client, "public static void setWaypoints(", "client setter");
requireText(client, "public static boolean hasWaypoints()", "client gate");
requireText(client, "public static int getPortalX()", "portal x");
requireText(client, "public static int getBorderCenterX()", "border centre x");
requireText(hud, "ClientDataStorage.hasWaypoints()", "HUD gate");
requireText(hud, "末地传送门: ", "portal line");
requireText(hud, "边界中心: ", "border centre line");
requireText(hud, "已开启", "portal opened state");
requireText(hud, "Level.OVERWORLD.equals(player.level().dimension())", "cross-dimension distance guard");

console.log("Left HUD waypoints verification passed.");
