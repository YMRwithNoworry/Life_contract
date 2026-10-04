package org.alku.life_contract.market;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import org.alku.life_contract.Life_contract;
import org.alku.life_contract.airdrop.Airdrop;
import org.alku.life_contract.accessory.AccessoryCatalog;
import org.alku.life_contract.accessory.AccessoryCategory;
import org.alku.life_contract.accessory.AccessoryDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * 升华商店的商品总表。
 * <p>
 * 分成两部分：
 * <ul>
 *     <li><b>固定商品</b>：写在本类的 {@link #FIXED} 表里，加商品/改价格只改这一张表；</li>
 *     <li><b>动态商品</b>：TaCZ 弹药/手枪/配件、契约模组弹药、羊毛（16 色）、饰品（数据驱动）——
 *         这些依赖当前安装的模组与玩家契约，运行时再枚举。</li>
 * </ul>
 * 所有商品都有稳定 id，服务端兑换只认 id（见 {@link BulletShopService#purchase}）。
 */
public final class ShopCatalog {

    /** 固定商品表：加一行就是加一件商品。 */
    private record Entry(String id, ShopCategory category, String section, Supplier<ItemStack> stack,
                         int price, int quantity, String desc) {
    }

    private static final String GROUP_SUPPLY = "gui.life_contract.shop.group.supply";
    private static final String GROUP_GEAR = "gui.life_contract.shop.group.gear";
    private static final String GROUP_COMBAT = "gui.life_contract.shop.group.combat";
    private static final String GROUP_SPECIAL = "gui.life_contract.shop.group.special";

    private static ItemStack of(Item item, int count) {
        return new ItemStack(item, count);
    }

    private static ItemStack potion(net.minecraft.core.Holder<net.minecraft.world.item.alchemy.Potion> potion) {
        return PotionContents.createItemStack(Items.POTION, potion);
    }

    private static final List<Entry> FIXED = List.of(
            // ---------- 补给：活下来 ----------
            new Entry("supply.torch", ShopCategory.SURVIVAL, GROUP_SUPPLY, () -> of(Items.TORCH, 16), 4, 16, "torch"),
            new Entry("supply.bread", ShopCategory.SURVIVAL, GROUP_SUPPLY, () -> of(Items.BREAD, 3), 6, 3, "bread"),
            new Entry("supply.cooked_beef", ShopCategory.SURVIVAL, GROUP_SUPPLY,
                    () -> of(Items.COOKED_BEEF, 1), 10, 1, "cooked_beef"),
            new Entry("supply.cobblestone", ShopCategory.SURVIVAL, GROUP_SUPPLY,
                    () -> of(Items.COBBLESTONE, 64), 3, 64, "cobblestone"),
            new Entry("supply.oak_planks", ShopCategory.SURVIVAL, GROUP_SUPPLY,
                    () -> of(Items.OAK_PLANKS, 32), 4, 32, "oak_planks"),
            new Entry("supply.ladder", ShopCategory.SURVIVAL, GROUP_SUPPLY, () -> of(Items.LADDER, 24), 5, 24, "ladder"),
            new Entry("supply.bucket", ShopCategory.SURVIVAL, GROUP_SUPPLY, () -> of(Items.BUCKET, 1), 12, 1, "bucket"),

            // ---------- 装备：效率与保命 ----------
            new Entry("gear.iron_pickaxe", ShopCategory.GEAR, GROUP_GEAR,
                    () -> of(Items.IRON_PICKAXE, 1), 35, 1, "iron_pickaxe"),
            new Entry("gear.iron_axe", ShopCategory.GEAR, GROUP_GEAR, () -> of(Items.IRON_AXE, 1), 30, 1, "iron_axe"),
            new Entry("gear.iron_sword", ShopCategory.GEAR, GROUP_GEAR,
                    () -> of(Items.IRON_SWORD, 1), 30, 1, "iron_sword"),
            new Entry("gear.shield", ShopCategory.GEAR, GROUP_GEAR, () -> of(Items.SHIELD, 1), 25, 1, "shield"),
            new Entry("gear.iron_helmet", ShopCategory.GEAR, GROUP_GEAR,
                    () -> of(Items.IRON_HELMET, 1), 30, 1, "iron_helmet"),
            new Entry("gear.iron_chestplate", ShopCategory.GEAR, GROUP_GEAR,
                    () -> of(Items.IRON_CHESTPLATE, 1), 45, 1, "iron_chestplate"),

            // ---------- 战斗辅助 ----------
            new Entry("combat.arrow", ShopCategory.COMBAT, GROUP_COMBAT, () -> of(Items.ARROW, 32), 10, 32, "arrow"),
            new Entry("combat.golden_apple", ShopCategory.COMBAT, GROUP_COMBAT,
                    () -> of(Items.GOLDEN_APPLE, 1), 80, 1, "golden_apple"),
            new Entry("combat.healing_potion", ShopCategory.COMBAT, GROUP_COMBAT,
                    () -> potion(Potions.HEALING), 30, 1, "healing_potion"),
            new Entry("combat.regeneration_potion", ShopCategory.COMBAT, GROUP_COMBAT,
                    () -> potion(Potions.REGENERATION), 35, 1, "regeneration_potion"),
            new Entry("combat.strength_potion", ShopCategory.COMBAT, GROUP_COMBAT,
                    () -> potion(Potions.STRENGTH), 40, 1, "strength_potion"),
            new Entry("combat.swiftness_potion", ShopCategory.COMBAT, GROUP_COMBAT,
                    () -> potion(Potions.SWIFTNESS), 25, 1, "swiftness_potion"),
            new Entry("combat.fire_resistance_potion", ShopCategory.COMBAT, GROUP_COMBAT,
                    () -> potion(Potions.FIRE_RESISTANCE), 30, 1, "fire_resistance_potion"),
            new Entry("combat.ender_pearl", ShopCategory.COMBAT, GROUP_COMBAT,
                    () -> of(Items.ENDER_PEARL, 2), 40, 2, "ender_pearl"),

            // ---------- 特殊：信号枪与稀有材料 ----------
            new Entry("special.flare_gun", ShopCategory.SPECIAL, GROUP_SPECIAL,
                    () -> new ItemStack(Airdrop.DISPOSABLE_FLARE_GUN.get()), 100, 1, "flare_gun"),
            new Entry("special.iron_ingot", ShopCategory.SPECIAL, GROUP_SPECIAL,
                    () -> of(Items.IRON_INGOT, 8), 24, 8, "iron_ingot"),
            new Entry("special.gold_ingot", ShopCategory.SPECIAL, GROUP_SPECIAL,
                    () -> of(Items.GOLD_INGOT, 4), 24, 4, "gold_ingot"),
            new Entry("special.redstone", ShopCategory.SPECIAL, GROUP_SPECIAL,
                    () -> of(Items.REDSTONE, 16), 10, 16, "redstone"),
            new Entry("special.glowstone", ShopCategory.SPECIAL, GROUP_SPECIAL,
                    () -> of(Items.GLOWSTONE_DUST, 8), 12, 8, "glowstone"),
            new Entry("special.gunpowder", ShopCategory.SPECIAL, GROUP_SPECIAL,
                    () -> of(Items.GUNPOWDER, 8), 12, 8, "gunpowder"));

    private ShopCatalog() {
    }

    /** 当前玩家能看到的全部商品（顺序即界面顺序）。 */
    public static List<ShopProduct> productsFor(Player player) {
        List<ShopProduct> products = new ArrayList<>(64);

        for (Entry entry : FIXED) {
            products.add(toProduct(entry));
        }

        // 羊毛：16 色，价格刻意压低（搭掩体/铺路）
        List<ItemStack> wool = BulletShopService.findWoolStacks();
        if (!wool.isEmpty()) {
            String group = "gui.life_contract.shop.group.wool";
            for (ItemStack stack : wool) {
                products.add(new ShopProduct("supply.wool." + itemId(stack), ShopCategory.SURVIVAL, group, stack,
                        BulletShopService.WOOL_PRICE, BulletShopService.WOOL_QUANTITY, "wool"));
            }
        }

        // 契约模组弹药
        List<ItemStack> contractAmmo = BulletShopService.findContractAmmoStacks(player);
        String ammoGroup = "gui.life_contract.shop.group.contract_ammo";
        for (ItemStack stack : contractAmmo) {
            products.add(new ShopProduct("ammo.contract." + itemId(stack), ShopCategory.AMMO, ammoGroup, stack,
                    BulletShopService.getAmmoPrice(), BulletShopService.getAmmoQuantity(), "contract_ammo"));
        }

        // TaCZ 弹药 / 手枪 / 配件（未安装 TaCZ 时列表为空）
        List<ItemStack> taczAmmo = BulletShopService.findTaczAmmoStacks();
        if (!taczAmmo.isEmpty()) {
            String group = "gui.life_contract.shop.group.tacz_ammo";
            for (ItemStack stack : taczAmmo) {
                products.add(new ShopProduct("ammo.tacz." + itemId(stack), ShopCategory.AMMO, group, stack,
                        BulletShopService.TACZ_AMMO_PRICE, BulletShopService.TACZ_AMMO_QUANTITY, "tacz_ammo"));
            }
        }
        List<ItemStack> pistols = BulletShopService.findTaczPistolStacks();
        if (!pistols.isEmpty()) {
            String group = "gui.life_contract.shop.group.pistol";
            for (ItemStack stack : pistols) {
                products.add(new ShopProduct("firearm.pistol." + itemId(stack), ShopCategory.FIREARM, group, stack,
                        BulletShopService.PISTOL_PRICE, BulletShopService.PISTOL_QUANTITY, "pistol"));
            }
        }
        List<ItemStack> attachments = BulletShopService.findTaczAttachmentStacks();
        if (!attachments.isEmpty()) {
            String group = "gui.life_contract.shop.group.attachment";
            for (ItemStack stack : attachments) {
                products.add(new ShopProduct("firearm.attachment." + itemId(stack), ShopCategory.FIREARM, group, stack,
                        BulletShopService.ATTACHMENT_PRICE, BulletShopService.ATTACHMENT_QUANTITY, "attachment"));
            }
        }

        // 饰品与饰品材料（数据驱动）
        for (AccessoryCategory category : AccessoryCategory.values()) {
            List<AccessoryDefinition> definitions = AccessoryCatalog.byCategory(category);
            if (definitions.isEmpty()) {
                continue;
            }
            String group = "gui.life_contract.shop.group.accessory." + category.id();
            for (AccessoryDefinition definition : definitions) {
                Item item = BuiltInRegistries.ITEM.get(definition.itemId());
                if (item == Items.AIR) {
                    continue;
                }
                products.add(new ShopProduct("accessory." + definition.id(), ShopCategory.ACCESSORY, group,
                        new ItemStack(item), definition.price(), 1, "accessory"));
            }
        }

        return List.copyOf(products);
    }

    /** 按 id 找商品；找不到返回 {@code null}。 */
    public static ShopProduct byId(Player player, String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        for (ShopProduct product : productsFor(player)) {
            if (product.id().equals(id)) {
                return product;
            }
        }
        return null;
    }

    public static int countIn(Player player, ShopCategory category) {
        int count = 0;
        for (ShopProduct product : productsFor(player)) {
            if (product.category() == category) {
                count++;
            }
        }
        return count;
    }

    private static ShopProduct toProduct(Entry entry) {
        ItemStack stack = entry.stack().get();
        return new ShopProduct(entry.id(), entry.category(), entry.section(), stack,
                entry.price(), Math.min(entry.quantity(), Math.max(1, stack.getMaxStackSize())), entry.desc());
    }

    private static String itemId(ItemStack stack) {
        var id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id == null ? "unknown" : id.getNamespace() + "." + id.getPath();
    }

    /** 语言键：商品用途说明。 */
    public static String descriptionKey(String desc) {
        return "gui.life_contract.shop.desc." + desc;
    }

    static {
        // 触发一次，尽早暴露固定表里的物品 id 拼写错误（例如 AIR）
        for (Entry entry : FIXED) {
            if (entry.stack().get().isEmpty()) {
                Life_contract.LOGGER.error("Shop product {} resolved to an empty stack", entry.id());
            }
        }
    }
}
