package org.alku.life_contract.market;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.alku.life_contract.ContractEvents;
import org.alku.life_contract.Life_contract;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * 升华商店的兑换逻辑。
 * <p>
 * 商品分四类：固定补给（信号枪、牛排）、契约模组弹药、TaCZ 弹药、羊毛。
 * 对 TaCZ 只做<b>反射</b>调用（{@code com.tacz.guns.item.AmmoItem#fillItemCategory}），
 * 因此本模组在没有安装 TaCZ 时依然可以正常加载，装了则自动多出弹药分区。
 */
public final class BulletShopService {
    private static final int PRICE = 30;
    private static final int QUANTITY = 5;
    public static final int SIGNAL_GUN_PRICE = 100;
    public static final int COOKED_BEEF_PRICE = 10;

    /** TaCZ 弹药：枪械主弹药，价格与契约弹药保持一致。 */
    public static final int TACZ_AMMO_PRICE = 30;
    public static final int TACZ_AMMO_QUANTITY = 5;

    /** 羊毛：方便搭掩体/铺路，价格刻意压低。 */
    public static final int WOOL_PRICE = 2;
    public static final int WOOL_QUANTITY = 16;

    /** TaCZ 枪械配件：永久强化，按件计价。 */
    public static final int ATTACHMENT_PRICE = 50;
    public static final int ATTACHMENT_QUANTITY = 1;

    /** TaCZ 手枪：商店只上架手枪这一类枪械，避免开局就人手一把步枪。 */
    public static final int PISTOL_PRICE = 150;
    public static final int PISTOL_QUANTITY = 1;

    private static final String TACZ_AMMO_ITEM_CLASS = "com.tacz.guns.item.AmmoItem";
    private static final String TACZ_ATTACHMENT_ITEM_CLASS = "com.tacz.guns.item.AttachmentItem";
    private static final String TACZ_ATTACHMENT_TYPE_CLASS = "com.tacz.guns.api.item.attachment.AttachmentType";
    private static final String TACZ_GUN_ITEM_CLASS = "com.tacz.guns.item.ModernKineticGunItem";
    private static final String TACZ_GUN_TAB_TYPE_CLASS = "com.tacz.guns.api.item.GunTabType";
    /** TaCZ 创造模式物品栏里的手枪分类名。 */
    private static final String TACZ_PISTOL_TAB = "PISTOL";

    private BulletShopService() {
    }

    // ==================== 商品列表 ====================

    /** 契约模组自带的弹药（按模组 id + 命名规则筛选）。 */
    public static List<ItemStack> findContractAmmoStacks(net.minecraft.world.entity.player.Player player) {
        String contractMod = ContractEvents.getEffectiveContractMod(player);
        if (contractMod == null || contractMod.isBlank()) {
            return List.of();
        }

        return BuiltInRegistries.ITEM.entrySet().stream()
                .filter(entry -> entry.getKey().location().getNamespace().equals(contractMod))
                .filter(entry -> isAmmoPath(entry.getKey().location()))
                .map(entry -> new ItemStack(entry.getValue()))
                .sorted(Comparator.comparing(stack -> stack.getHoverName().getString()))
                .toList();
    }

    /**
     * TaCZ 的全部可用弹药。
     * <p>
     * TaCZ 的弹药是"数据驱动"的：物品只有 {@code tacz:ammo} 一个，具体弹种写在物品组件的
     * custom_data 里，由枪包（gun pack）定义。这里反射调用它自己的
     * {@code fillItemCategory()}（创造模式物品栏填充逻辑），因此能自动覆盖所有已加载枪包的弹种，
     * 不需要在本模组里硬编码弹种 id，也不会因为 TaCZ 更新而失效。
     */
    public static List<ItemStack> findTaczAmmoStacks() {
        try {
            Class<?> ammoItemClass = Class.forName(TACZ_AMMO_ITEM_CLASS);
            Object result = ammoItemClass.getMethod("fillItemCategory").invoke(null);
            if (!(result instanceof List<?> list)) {
                return List.of();
            }

            List<ItemStack> stacks = new ArrayList<>(list.size());
            for (Object entry : list) {
                if (entry instanceof ItemStack stack && !stack.isEmpty()) {
                    stacks.add(stack);
                }
            }
            return stacks;
        } catch (Throwable throwable) {
            // 未安装 TaCZ、版本不兼容或枪包尚未加载时，商店里就不显示这一分区
            return List.of();
        }
    }

    /**
     * TaCZ 的手枪（只取 {@code GunTabType.PISTOL} 这一类）。
     * <p>
     * 枪和弹药一样是数据驱动的（物品只有 {@code tacz:modern_kinetic_gun}，具体枪械写在组件里），
     * 因此这里反射取出手枪分类常量，再调用 TaCZ 自己的
     * {@code AbstractGunItem.fillItemCategory(GunTabType)} 列出该类全部枪械，
     * 所有已加载枪包的手枪都会包含进来。
     */
    public static List<ItemStack> findTaczPistolStacks() {
        try {
            Class<?> gunItemClass = Class.forName(TACZ_GUN_ITEM_CLASS);
            Class<?> gunTabTypeClass = Class.forName(TACZ_GUN_TAB_TYPE_CLASS);

            Object pistolTab = null;
            Object[] tabTypes = gunTabTypeClass.getEnumConstants();
            if (tabTypes != null) {
                for (Object tabType : tabTypes) {
                    if (tabType instanceof Enum<?> value && TACZ_PISTOL_TAB.equals(value.name())) {
                        pistolTab = tabType;
                        break;
                    }
                }
            }
            if (pistolTab == null) {
                return List.of();
            }

            Object result = gunItemClass.getMethod("fillItemCategory", gunTabTypeClass).invoke(null, pistolTab);
            if (!(result instanceof List<?> list)) {
                return List.of();
            }

            List<ItemStack> stacks = new ArrayList<>(list.size());
            for (Object entry : list) {
                if (entry instanceof ItemStack stack && !stack.isEmpty()) {
                    stacks.add(stack);
                }
            }
            stacks.sort(Comparator.comparing(stack -> stack.getHoverName().getString()));
            return stacks;
        } catch (Throwable throwable) {
            // 未安装 TaCZ 或版本不兼容时不显示该分区
            return List.of();
        }
    }

    /**
     * TaCZ 的全部枪械配件（瞄具、握把、枪口、弹匣、枪托等）。
     * <p>
     * TaCZ 的 {@code AttachmentItem.fillItemCategory(AttachmentType)} 需要传入配件类别，
     * 因此先反射取出 {@code AttachmentType} 枚举的全部常量，逐类调用后合并；
     * 该方法内部已过滤隐藏配件，商店不会出现调试用配件。
     */
    public static List<ItemStack> findTaczAttachmentStacks() {
        try {
            Class<?> attachmentItemClass = Class.forName(TACZ_ATTACHMENT_ITEM_CLASS);
            Class<?> attachmentTypeClass = Class.forName(TACZ_ATTACHMENT_TYPE_CLASS);
            Object[] types = attachmentTypeClass.getEnumConstants();
            if (types == null || types.length == 0) {
                return List.of();
            }

            java.lang.reflect.Method fillItemCategory =
                    attachmentItemClass.getMethod("fillItemCategory", attachmentTypeClass);
            List<ItemStack> stacks = new ArrayList<>();
            for (Object type : types) {
                Object result = fillItemCategory.invoke(null, type);
                if (!(result instanceof List<?> list)) {
                    continue;
                }
                for (Object entry : list) {
                    if (entry instanceof ItemStack stack && !stack.isEmpty()) {
                        stacks.add(stack);
                    }
                }
            }
            stacks.sort(Comparator.comparing(stack -> stack.getHoverName().getString()));
            return stacks;
        } catch (Throwable throwable) {
            // 未安装 TaCZ 或版本不兼容时不显示该分区
            return List.of();
        }
    }

    /** 羊毛：走原版 {@code #minecraft:wool} 标签，模组新增的羊毛也会一并出现在商店里。 */
    public static List<ItemStack> findWoolStacks() {
        List<ItemStack> stacks = new ArrayList<>();
        for (var holder : BuiltInRegistries.ITEM.getTagOrEmpty(ItemTags.WOOL)) {
            stacks.add(new ItemStack(holder.value()));
        }
        stacks.sort(Comparator.comparing(stack -> stack.getHoverName().getString()));
        return stacks;
    }

    // ==================== 兑换 ====================

    /** 单次最多买几份：防手滑，也避免一次把队伍经济清空。 */
    public static final int MAX_BUNDLES = 8;

    /** 兑换结果：{@code success} 决定界面反馈的颜色，{@code message} 直接显示给玩家。 */
    public record PurchaseResult(boolean success, Component message) {
    }

    /**
     * 按商品 id 兑换。
     * <p>
     * 只信 id：价格、数量、发放内容全部由 {@link ShopCatalog} 查表得到，
     * 客户端既改不了价格，也不会因为界面与服务端不同步而发错东西。
     *
     * @param bundles 买几份（1 ~ {@link #MAX_BUNDLES}）；右键快速购买就是传 5
     */
    public static PurchaseResult purchase(ServerPlayer player, String productId, int bundles) {
        ShopProduct product = ShopCatalog.byId(player, productId);
        if (product == null) {
            return new PurchaseResult(false, Component.translatable("gui.life_contract.shop.invalid_item"));
        }

        int count = Mth.clamp(bundles, 1, MAX_BUNDLES);
        ItemStack template = product.template();
        int totalPrice = product.price() * count;
        int totalQuantity = product.quantity() * count;

        // 先算背包空间，再看钱：两边都不够时优先提示"背包满"，那是玩家能立刻解决的问题
        if (freeSpaceFor(player, template) < totalQuantity) {
            return new PurchaseResult(false, Component.translatable("gui.life_contract.shop.inventory_full"));
        }

        int balance = countSublimation(player);
        if (balance < totalPrice) {
            return new PurchaseResult(false, Component.translatable(
                    "gui.life_contract.shop.not_enough_detail", totalPrice, balance));
        }

        consumeSublimation(player, totalPrice);
        give(player, template, totalQuantity);
        return new PurchaseResult(true, Component.translatable("gui.life_contract.shop.purchased",
                template.getHoverName(), totalQuantity, totalPrice));
    }

    public static int getAmmoPrice() {
        return PRICE;
    }

    public static int getAmmoQuantity() {
        return QUANTITY;
    }

    // ==================== 工具方法 ====================

    private static boolean isAmmoPath(ResourceLocation id) {
        String path = id.getPath().toLowerCase(Locale.ROOT);
        return path.contains("bullet") || path.contains("ammo") || path.contains("round")
                || path.contains("cartridge") || path.contains("shell") || path.contains("shot");
    }

    /** 玩家背包里的升华总数（界面与兑换共用同一套口径）。 */
    public static int countSublimation(ServerPlayer player) {
        return countSublimation(player.getInventory().items);
    }

    /** 任意物品栏里的升华总数（队伍背包也用它）。 */
    public static int countSublimation(Iterable<ItemStack> stacks) {
        int count = 0;
        for (ItemStack stack : stacks) {
            if (stack.is(Life_contract.SUBLIMATION.get())) count += stack.getCount();
        }
        return count;
    }

    private static void consumeSublimation(ServerPlayer player, int amount) {
        int remaining = amount;
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.is(Life_contract.SUBLIMATION.get())) continue;
            int consumed = Math.min(remaining, stack.getCount());
            stack.shrink(consumed);
            remaining -= consumed;
            if (remaining == 0) break;
        }
        player.getInventory().setChanged();
        // 立刻把背包同步给客户端，界面上的"升华余额"才能马上变
        player.containerMenu.broadcastChanges();
    }

    /** 背包还能装下多少个该物品（空槽按整组算，已有同类堆叠按剩余空间算）。 */
    private static int freeSpaceFor(ServerPlayer player, ItemStack template) {
        int max = Math.max(1, template.getMaxStackSize());
        int free = 0;
        for (ItemStack slot : player.getInventory().items) {
            if (slot.isEmpty()) {
                free += max;
            } else if (ItemStack.isSameItemSameComponents(slot, template)) {
                free += Math.max(0, Math.min(slot.getMaxStackSize(), max) - slot.getCount());
            }
        }
        return free;
    }

    /** 发放奖励：超出单组上限就拆成多组；背包塞不下时掉在脚下，绝不凭空吞掉。 */
    private static void give(ServerPlayer player, ItemStack template, int total) {
        int remaining = total;
        int max = Math.max(1, template.getMaxStackSize());
        while (remaining > 0) {
            int chunk = Math.min(remaining, max);
            ItemStack stack = template.copyWithCount(chunk);
            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
            remaining -= chunk;
        }
        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
    }
}
