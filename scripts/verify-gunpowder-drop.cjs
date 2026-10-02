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

const handler = read("src/main/java/org/alku/life_contract/items/GunpowderDropHandler.java");

requireText(handler, "@EventBusSubscriber(modid = Life_contract.MODID)", "event subscriber");
requireText(handler, "public static final float GUNPOWDER_DROP_CHANCE = 0.5F;", "50 percent chance");
requireText(handler, "public static final int GUNPOWDER_AMOUNT = 1;", "one gunpowder");
requireText(handler, "public static void onLivingDrops(LivingDropsEvent event)", "drops listener");
requireText(handler, "event.getEntity().getRandom().nextFloat() >= GUNPOWDER_DROP_CHANCE", "chance roll");
requireText(handler, "new ItemStack(Items.GUNPOWDER, GUNPOWDER_AMOUNT)", "gunpowder stack");
requireText(handler, "event.getDrops().add(", "adds to drops");
requireText(handler, "if (event.getEntity() instanceof Player) return;", "players excluded");

console.log("Gunpowder drop verification passed.");
