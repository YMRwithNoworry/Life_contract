// 升华商店内容校验：TaCZ 弹药 + 羊毛 + 分区标题 + 语言文件
const fs = require("fs");
const path = require("path");

const root = path.resolve(__dirname, "..");
const read = (rel) => fs.readFileSync(path.join(root, rel), "utf8");

function requireText(haystack, needle, label) {
    if (!haystack.includes(needle)) {
        throw new Error(label + ": missing " + JSON.stringify(needle));
    }
}

function forbidText(haystack, needle, label) {
    if (haystack.includes(needle)) {
        throw new Error(label + ": must not contain " + JSON.stringify(needle));
    }
}

// 1) 商店服务：TaCZ 弹药与羊毛的价格/数量
const service = read("src/main/java/org/alku/life_contract/market/BulletShopService.java");
requireText(service, "TACZ_AMMO_PRICE", "tacz ammo price");
requireText(service, "TACZ_AMMO_QUANTITY", "tacz ammo quantity");
requireText(service, "WOOL_PRICE = 2", "wool price");
requireText(service, "WOOL_QUANTITY = 16", "wool quantity");
requireText(service, "ATTACHMENT_PRICE = 50", "attachment price");
requireText(service, "ATTACHMENT_QUANTITY = 1", "attachment quantity");
requireText(service, "public static List<ItemStack> findTaczAttachmentStacks()", "tacz attachment listing");
requireText(service, "public static List<ItemStack> findTaczAmmoStacks()", "tacz ammo listing");
requireText(service, "public static List<ItemStack> findWoolStacks()", "wool listing");
requireText(service, "public static Component purchase(ServerPlayer player, ItemStack template)",
        "template based purchase");

// 2) TaCZ 必须走反射：没有编译期依赖，未安装时也能加载
requireText(service, 'Class.forName(TACZ_AMMO_ITEM_CLASS)', "reflective tacz lookup");
requireText(service, 'getMethod("fillItemCategory").invoke(null)', "reflective creative tab fill");
requireText(service, "catch (Throwable", "reflection failure fallback");
requireText(service, "attachmentTypeClass.getEnumConstants()", "attachment type enum walk");
requireText(service, 'getMethod("fillItemCategory", attachmentTypeClass)', "typed attachment listing");
requireText(service, "isTaczAttachment(item)", "attachment purchase branch");
requireText(service, 'TACZ_ATTACHMENT_INTERFACE = "com.tacz.guns.api.item.IAttachment"',
        "attachment interface name constant");
requireText(service, 'private static final String TACZ_AMMO_ITEM_CLASS = "com.tacz.guns.item.AmmoItem"',
        "tacz ammo class name constant");

// 3) 羊毛走原版标签
requireText(service, "BuiltInRegistries.ITEM.getTagOrEmpty(ItemTags.WOOL)", "wool tag lookup");
requireText(service, "template.is(ItemTags.WOOL)", "wool purchase check");

// 4) 兑换必须保留物品组件（TaCZ 弹种存在组件里）
requireText(service, "template.copyWithCount(", "component preserving reward");

// 5) 全仓不得出现编译期 TaCZ 依赖
const javaFiles = [];
(function walk(dir) {
    for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
        const full = path.join(dir, entry.name);
        if (entry.isDirectory()) walk(full);
        else if (entry.name.endsWith(".java")) javaFiles.push(full);
    }
})(path.join(root, "src/main/java"));
for (const file of javaFiles) {
    const text = fs.readFileSync(file, "utf8");
    if (/^\s*import\s+com\.tacz\./m.test(text)) {
        throw new Error(path.relative(root, file) + ": must not import TaCZ classes directly");
    }
}

// 6) 商店界面：四个分区与模板传递
const ui = read("src/main/java/org/alku/life_contract/client/SublimationShopUIHolder.java");
requireText(ui, "BulletShopService.findTaczAmmoStacks()", "ui tacz section");
requireText(ui, "BulletShopService.findWoolStacks()", "ui wool section");
requireText(ui, "gui.life_contract.shop.section.tacz_ammo", "ui tacz section title");
requireText(ui, "gui.life_contract.shop.section.wool", "ui wool section title");
requireText(ui, "gui.life_contract.shop.section.contract_ammo", "ui contract section title");
requireText(ui, "BulletShopService.findTaczAttachmentStacks()", "ui attachment section");
requireText(ui, "gui.life_contract.shop.section.attachment", "ui attachment section title");
requireText(ui, "addProductRow(UIElement rows, ItemStack template, int price)", "ui template row");
requireText(ui, "BulletShopService.purchase(serverPlayer, template)", "ui purchase call");

// 7) 语言文件：两种语言都要有分区键，且必须是合法 JSON
for (const lang of ["zh_cn", "en_us"]) {
    const file = "src/main/resources/assets/life_contract/lang/" + lang + ".json";
    const raw = read(file);
    let parsed;
    try {
        parsed = JSON.parse(raw);
    } catch (error) {
        throw new Error(file + ": invalid JSON - " + error.message);
    }
    for (const key of [
        "gui.life_contract.shop.section.supply",
        "gui.life_contract.shop.section.tacz_ammo",
        "gui.life_contract.shop.section.contract_ammo",
        "gui.life_contract.shop.section.attachment",
        "gui.life_contract.shop.section.wool",
    ]) {
        if (typeof parsed[key] !== "string" || parsed[key].length === 0) {
            throw new Error(file + ": missing translation " + key);
        }
    }
}

console.log("Shop content verification passed.");
