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

const inventoryMixinPath = path.join(
  root,
  "src/main/java/org/alku/life_contract/mixin/InventoryStackSizeMixin.java",
);
if (!fs.existsSync(inventoryMixinPath)) {
  throw new Error("player inventory still uses the vanilla 64 item container limit");
}

const item = read("src/main/java/org/alku/life_contract/items/SublimationItem.java");
const inventoryMixin = fs.readFileSync(inventoryMixinPath, "utf8");
const teamInventory = read("src/main/java/org/alku/life_contract/TeamInventory.java");
const teamMenu = read("src/main/java/org/alku/life_contract/TeamInventoryMenu.java");
const itemStackMixin = read("src/main/java/org/alku/life_contract/mixin/ItemStackCountMixin.java");
const bufferMixin = read("src/main/java/org/alku/life_contract/mixin/FriendlyByteBufItemCountMixin.java");
const mixinConfig = read("src/main/resources/life_contract.mixins.json");

requireText(item, "public static final int MAX_STACK_SIZE = 1024", "shared stack limit");
requireText(inventoryMixin, "@Mixin(Inventory.class)", "player inventory mixin target");
requireText(inventoryMixin, "return SublimationItem.MAX_STACK_SIZE", "player inventory stack limit");
requireText(mixinConfig, '"InventoryStackSizeMixin"', "player inventory mixin registration");
requireText(teamInventory, "return SublimationItem.MAX_STACK_SIZE", "team inventory server stack limit");
requireText(teamMenu, "createClientContainer", "team inventory client container");
requireText(teamMenu, "return SublimationItem.MAX_STACK_SIZE", "team inventory client stack limit");
requireText(itemStackMixin, 'tag.putInt("Count"', "persistent extended count");
requireText(bufferMixin, "buffer.writeVarInt(stack.getCount())", "network extended count write");
requireText(bufferMixin, "buffer.readVarInt()", "network extended count read");

console.log("Sublimation stack verification passed.");
