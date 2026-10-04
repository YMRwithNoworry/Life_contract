// 升华商店与升华经济的整体校验：
// 商品表 / 兑换链路 / 界面结构 / 语言文案 / 升华获取途径 都在这里锁一遍。
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

const service = read("src/main/java/org/alku/life_contract/market/BulletShopService.java");
const catalog = read("src/main/java/org/alku/life_contract/market/ShopCatalog.java");
const product = read("src/main/java/org/alku/life_contract/market/ShopProduct.java");
const category = read("src/main/java/org/alku/life_contract/market/ShopCategory.java");
const purchase = read("src/main/java/org/alku/life_contract/market/ShopPurchasePayload.java");
const feedback = read("src/main/java/org/alku/life_contract/market/ShopFeedbackPayload.java");
const rewards = read("src/main/java/org/alku/life_contract/market/SublimationRewards.java");
const ui = read("src/main/java/org/alku/life_contract/client/SublimationShopUIHolder.java");
const network = read("src/main/java/org/alku/life_contract/NetworkHandler.java");
const drops = read("src/main/java/org/alku/life_contract/items/SublimationDropHandler.java");
const events = read("src/main/java/org/alku/life_contract/events/GameEventManager.java");
const worldEvents = read("src/main/java/org/alku/life_contract/events/WorldEventManager.java");
const border = read("src/main/java/org/alku/life_contract/border/BorderManager.java");

// 1) 兑换只信商品 id：价格、数量、发放内容全部服务端查表
requireText(service, "public static PurchaseResult purchase(ServerPlayer player, String productId, int bundles)",
        "id based purchase");
requireText(service, "ShopCatalog.byId(player, productId)", "server side catalog lookup");
forbidText(service, "public static Component purchase(ServerPlayer player, ItemStack template)",
        "template based purchase must be gone");
requireText(service, "public static final int MAX_BUNDLES = 8", "bundle cap");
requireText(service, "Mth.clamp(bundles, 1, MAX_BUNDLES)", "bundle clamping");
requireText(service, "freeSpaceFor(player, template) < totalQuantity", "inventory space precheck");
requireText(service, "if (balance < totalPrice)", "balance precheck");
requireText(service, "player.containerMenu.broadcastChanges()", "inventory resync after purchase");

// 2) 请求/反馈两条链路都注册了
requireText(purchase, "record ShopPurchasePayload(String productId, int bundles)", "purchase request payload");
requireText(feedback, "record ShopFeedbackPayload(boolean success, String text)", "purchase feedback payload");
requireText(network, "ShopPurchasePayload.STREAM_CODEC", "purchase payload registration");
requireText(network, "ShopFeedbackPayload.STREAM_CODEC", "feedback payload registration");
requireText(network, "SublimationShopUIHolder.showFeedback(payload)", "feedback client handler");

// 3) 商品模型：分类 + 二级分组 + 说明键
for (const id of ["survival", "gear", "combat", "ammo", "firearm", "accessory", "special"]) {
    requireText(category, '("' + id + '")', "shop category " + id);
}
requireText(product, "public record ShopProduct(String id, ShopCategory category, String sectionKey, ItemStack template,",
        "product record");
requireText(catalog, "public static List<ShopProduct> productsFor(Player player)", "catalog listing");
requireText(catalog, "public static ShopProduct byId(Player player, String id)", "catalog lookup by id");

// 4) 关键商品与价格（改价必须同步改这里，避免手滑把经济改崩）
const priceChecks = [
    ['new Entry("supply.torch"', "16), 4, 16", "torch"],
    ['new Entry("supply.cooked_beef"', "1), 10, 1", "cooked beef"],
    ['new Entry("gear.iron_pickaxe"', "1), 35, 1", "iron pickaxe"],
    ['new Entry("gear.shield"', "1), 25, 1", "shield"],
    ['new Entry("combat.golden_apple"', "1), 80, 1", "golden apple"],
    ['new Entry("combat.healing_potion"', "), 30, 1,", "healing potion"],
    ['new Entry("combat.ender_pearl"', "2), 40, 2", "ender pearl"],
    ['new Entry("special.flare_gun"', "100, 1", "flare gun"],
];
const entryStarts = [...catalog.matchAll(/new Entry\(/g)].map((m) => m.index);
for (const [needle, priceFragment, label] of priceChecks) {
    const index = catalog.indexOf(needle);
    if (index < 0) {
        throw new Error("catalog: missing product " + label);
    }
    const nextStart = entryStarts.find((start) => start > index);
    const entry = catalog.slice(index, nextStart === undefined ? catalog.length : nextStart);
    if (!entry.includes(priceFragment)) {
        throw new Error("catalog: " + label + " price/quantity changed - " + entry.replace(/\s+/g, " "));
    }
}

// 5) TaCZ 仍然只能走反射（没有编译期依赖），羊毛走原版标签
requireText(service, 'Class.forName(TACZ_AMMO_ITEM_CLASS)', "reflective tacz lookup");
requireText(service, 'getMethod("fillItemCategory").invoke(null)', "reflective creative tab fill");
requireText(service, "catch (Throwable", "reflection failure fallback");
requireText(service, "attachmentTypeClass.getEnumConstants()", "attachment type enum walk");
requireText(service, "BuiltInRegistries.ITEM.getTagOrEmpty(ItemTags.WOOL)", "wool tag lookup");
requireText(catalog, "BulletShopService.findWoolStacks()", "wool products");
requireText(catalog, "BulletShopService.findTaczPistolStacks()", "pistol products");
requireText(catalog, "BulletShopService.findTaczAttachmentStacks()", "attachment products");
requireText(catalog, "BulletShopService.findContractAmmoStacks(player)", "contract ammo products");
requireText(catalog, "AccessoryCatalog.byCategory(category)", "accessory products");

const javaFiles = [];
(function walk(dir) {
    for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
        const full = path.join(dir, entry.name);
        if (entry.isDirectory()) walk(full);
        else if (entry.name.endsWith(".java")) javaFiles.push(full);
    }
})(path.join(root, "src/main/java"));
for (const file of javaFiles) {
    const rel = path.relative(root, file).replace(/\\/g, "/");
    if (/^\s*import\s+com\.tacz\./m.test(read(rel))) {
        throw new Error(rel + ": must not import TaCZ classes directly");
    }
}

// 6) 界面：余额 + 分类标签 + 图标 + 买不起的灰显 + 右键快速购买
requireText(ui, "gui.life_contract.shop.balance", "balance label");
requireText(ui, "balanceLabel.setValue(balanceText(balance))", "balance refresh");
requireText(ui, "for (ShopCategory category : ShopCategory.values())", "category tabs");
requireText(ui, "updateTabLabels()", "tab highlight");
requireText(ui, "new ShopIcon(product.template())", "item icon");
requireText(ui, "class ShopIcon extends ItemSlot", "icon element");
requireText(ui, "protected void onMouseDown(UIEvent event)", "icon must not be draggable");
requireText(ui, "event.button == 1 ? QUICK_BUNDLES : 1", "right click quick buy");
requireText(ui, "countSublimation(player)", "client balance");
requireText(ui, "gui.life_contract.shop.need_more", "insufficient funds feedback in tooltip");
requireText(service, "gui.life_contract.shop.not_enough_detail", "insufficient funds feedback in chat");
requireText(ui, "ShopCatalog.descriptionKey(product.descriptionKey())", "description tooltip");
requireText(ui, "TaffyDisplay.NONE", "view swapping");

// 7) 语言文件：分类 / 分组 / 说明 全都要有，中英都不能缺
const langs = {};
for (const lang of ["zh_cn", "en_us"]) {
    const file = "src/main/resources/assets/life_contract/lang/" + lang + ".json";
    const raw = read(file);
    try {
        langs[lang] = JSON.parse(raw);
    } catch (error) {
        throw new Error(file + ": invalid JSON - " + error.message);
    }
}

const categoryKeys = ["survival", "gear", "combat", "ammo", "firearm", "accessory", "special"]
        .map((id) => "gui.life_contract.shop.category." + id);
const groupKeys = [
    "gui.life_contract.shop.group.supply",
    "gui.life_contract.shop.group.gear",
    "gui.life_contract.shop.group.combat",
    "gui.life_contract.shop.group.special",
    "gui.life_contract.shop.group.wool",
    "gui.life_contract.shop.group.contract_ammo",
    "gui.life_contract.shop.group.tacz_ammo",
    "gui.life_contract.shop.group.pistol",
    "gui.life_contract.shop.group.attachment",
    "gui.life_contract.shop.group.accessory.pendant",
    "gui.life_contract.shop.group.accessory.ring",
    "gui.life_contract.shop.group.accessory.charm",
    "gui.life_contract.shop.group.accessory.crown",
    "gui.life_contract.shop.group.accessory.amulet",
    "gui.life_contract.shop.group.accessory.consumable",
    "gui.life_contract.shop.group.accessory.material",
];
const uiKeys = [
    "gui.life_contract.shop.balance",
    "gui.life_contract.shop.hint",
    "gui.life_contract.shop.empty",
    "gui.life_contract.shop.need_more",
    "gui.life_contract.shop.not_enough_detail",
    "gui.life_contract.shop.tooltip_hint",
    "gui.life_contract.shop.reward",
    "gui.life_contract.shop.reason.player_kill",
    "gui.life_contract.shop.reason.first_blood",
    "gui.life_contract.shop.reason.survival",
    "gui.life_contract.shop.reason.shrink",
    "gui.life_contract.shop.reason.spore_surge",
    "gui.life_contract.shop.section.supply",
    "gui.life_contract.shop.section.tacz_ammo",
    "gui.life_contract.shop.section.contract_ammo",
    "gui.life_contract.shop.section.attachment",
    "gui.life_contract.shop.section.pistol",
    "gui.life_contract.shop.section.wool",
];

// 商品说明键：固定表里每条 Entry 的最后一个字符串就是 desc
const descSlugs = new Set(["wool", "contract_ammo", "tacz_ammo", "pistol", "attachment", "accessory"]);
for (const chunk of catalog.split("new Entry(").slice(1)) {
    const end = chunk.indexOf("),");
    if (end < 0) continue;
    const literals = [...chunk.slice(0, end).matchAll(/"([^"]*)"/g)].map((m) => m[1]);
    if (literals.length >= 2) {
        descSlugs.add(literals[literals.length - 1]);
    }
}

for (const [lang, parsed] of Object.entries(langs)) {
    for (const key of [...categoryKeys, ...groupKeys, ...uiKeys]) {
        if (typeof parsed[key] !== "string" || parsed[key].length === 0) {
            throw new Error(lang + ": missing translation " + key);
        }
    }
    for (const slug of descSlugs) {
        const key = "gui.life_contract.shop.desc." + slug;
        if (typeof parsed[key] !== "string" || parsed[key].length === 0) {
            throw new Error(lang + ": missing item description " + key);
        }
    }
}

// 8) 升华获取途径：击杀 / 首杀 / 存活里程碑 / 缩圈 / 事件
requireText(drops, "SublimationRewards.awardFirstBlood(killer)", "first blood reward");
requireText(drops, "SublimationRewards.awardPlayerKill(killer)", "player kill reward");
requireText(drops, "resetFirstBlood()", "first blood reset");
requireText(events, "awardSurvivalMilestone()", "survival milestone hook");
requireText(events, "SublimationRewards.SURVIVAL_REWARD", "survival reward value");
requireText(border, "SublimationRewards.SHRINK_REWARD", "border shrink reward");
requireText(worldEvents, "SublimationRewards.SPORE_SURGE_REWARD", "spore surge reward");
requireText(rewards, "public static void award(ServerPlayer player, int amount, String reasonKey)", "award entry point");
requireText(rewards, "player.drop(stack, false)", "overflow must drop, not vanish");

console.log("Shop content verification passed (catalog + purchase + UI + economy).");
