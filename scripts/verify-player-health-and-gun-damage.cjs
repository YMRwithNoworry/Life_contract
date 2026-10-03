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

const modClass = read("src/main/java/org/alku/life_contract/Life_contract.java");
const damage = read("src/main/java/org/alku/life_contract/PlayerDamageReduction.java");

// 玩家生命上限 40
requireText(modClass, "public static final double PLAYER_MAX_HEALTH = 40.0D;", "player max health constant");
requireText(modClass, "EntityAttributeModificationEvent event", "attribute modification listener");
requireText(modClass, "event.add(EntityType.PLAYER,", "player attribute override");
requireText(modClass, "Attributes.MAX_HEALTH,", "max health attribute target");

// 玩家挖掘速度 2.5 倍（原版 Player#getDigSpeed 末尾乘 BLOCK_BREAK_SPEED）
requireText(modClass, "public static final double PLAYER_BLOCK_BREAK_SPEED = 2.5D;",
  "player block break speed constant");
requireText(modClass, "Attributes.BLOCK_BREAK_SPEED,", "block break speed attribute target");
requireText(modClass, "PLAYER_BLOCK_BREAK_SPEED);", "block break speed attribute value");
requireText(modClass, "modEventBus.addListener(ModEvents::onEntityAttributeModification)",
  "attribute modification listener registration");

// 枪械子弹对玩家伤害 20%
requireText(damage, "public static final float BULLET_DAMAGE_MULTIPLIER = 0.2F;", "bullet damage multiplier");
requireText(damage, "public static final float NON_PLAYER_DAMAGE_MULTIPLIER = 0.4F;",
  "non-player damage multiplier");
requireText(damage, 'private static final String TACZ_MOD_ID = "tacz";', "TaCZ mod id");
requireText(damage, 'private static final String TACZ_PACKAGE_PREFIX = "com.tacz.";', "TaCZ package prefix");
requireText(damage, "source.getDirectEntity()", "projectile entity inspection");
requireText(damage, "className.contains(\"Bullet\")", "bullet entity class check");
requireText(damage, "source.typeHolder().unwrapKey()", "damage type inspection");
requireText(damage, "id.getPath().contains(\"bullet\")", "bullet damage type check");
requireText(damage, "event.setNewDamage(event.getNewDamage() * BULLET_DAMAGE_MULTIPLIER);", "bullet damage scaling");
requireText(damage, "event.setNewDamage(event.getNewDamage() * NON_PLAYER_DAMAGE_MULTIPLIER);",
  "non-player damage scaling");

// 子弹规则必须排在“玩家来源直接返回”之前，否则玩家开枪打人不会生效
const bulletIndex = damage.indexOf("isBulletDamage(source)");
const playerSourceIndex = damage.indexOf("source.getEntity() instanceof Player) {");
if (bulletIndex < 0 || playerSourceIndex < 0 || bulletIndex > playerSourceIndex) {
  throw new Error("bullet rule must be evaluated before the player-source early return");
}

console.log("Player health and gun damage verification passed.");
