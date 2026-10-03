package org.alku.life_contract.market;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.alku.life_contract.airdrop.Airdrop;
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
    public static final int WOOL_PRICE = 5;
    public static final int WOOL_QUANTITY = 16;

    private static final String TACZ_AMMO_ITEM_CLASS = "com.tacz.guns.item.AmmoItem";
    private static final String TACZ_AMMO_INTERFACE = "com.tacz.guns.api.item.IAmmo";

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

    public static Component purchase(ServerPlayer player, ItemStack template) {
        if (template == null || template.isEmpty()) {
            return Component.translatable("gui.life_contract.shop.invalid_item");
        }

        Item item = template.getItem();
        int price;
        int quantity;
        if (item == Airdrop.DISPOSABLE_FLARE_GUN.get()) {
            price = SIGNAL_GUN_PRICE;
            quantity = 1;
        } else if (item == Items.COOKED_BEEF) {
            price = COOKED_BEEF_PRICE;
            quantity = 1;
        } else if (isTaczAmmo(item)) {
            price = TACZ_AMMO_PRICE;
            quantity = TACZ_AMMO_QUANTITY;
        } else if (template.is(ItemTags.WOOL)) {
            price = WOOL_PRICE;
            quantity = WOOL_QUANTITY;
        } else {
            String contractMod = ContractEvents.getEffectiveContractMod(player);
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            if (contractMod == null || id == null || !contractMod.equals(id.getNamespace()) || !isAmmoPath(id)) {
                return Component.translatable("gui.life_contract.shop.invalid_item");
            }
            price = PRICE;
            quantity = QUANTITY;
        }

        // 保留模板上的物品组件（TaCZ 的弹种信息就存在组件里）
        ItemStack reward = template.copyWithCount(Math.min(quantity, Math.max(1, template.getMaxStackSize())));
        if (!canFit(player, reward)) {
            return Component.translatable("gui.life_contract.shop.inventory_full");
        }

        if (countSublimation(player) < price) {
            return Component.translatable("gui.life_contract.shop.not_enough", price);
        }

        consumeSublimation(player, price);
        player.getInventory().add(reward);
        player.getInventory().setChanged();
        return Component.translatable("gui.life_contract.shop.purchased", reward.getHoverName(), quantity, price);
    }

    public static int getAmmoPrice() {
        return PRICE;
    }

    public static int getAmmoQuantity() {
        return QUANTITY;
    }

    // ==================== 工具方法 ====================

    /** TaCZ 弹药判定：只比较类名，避免产生编译期依赖。 */
    private static boolean isTaczAmmo(Item item) {
        return item.getClass().getName().equals(TACZ_AMMO_ITEM_CLASS)
                || implementsInterface(item.getClass(), TACZ_AMMO_INTERFACE);
    }

    private static boolean implementsInterface(Class<?> type, String interfaceName) {
        if (type == null) {
            return false;
        }
        for (Class<?> implemented : type.getInterfaces()) {
            if (implemented.getName().equals(interfaceName) || implementsInterface(implemented, interfaceName)) {
                return true;
            }
        }
        return implementsInterface(type.getSuperclass(), interfaceName);
    }

    private static boolean isAmmoPath(ResourceLocation id) {
        String path = id.getPath().toLowerCase(Locale.ROOT);
        return path.contains("bullet") || path.contains("ammo") || path.contains("round")
                || path.contains("cartridge") || path.contains("shell") || path.contains("shot");
    }

    private static int countSublimation(ServerPlayer player) {
        int count = 0;
        for (ItemStack stack : player.getInventory().items) {
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
    }

    private static boolean canFit(ServerPlayer player, ItemStack reward) {
        int remaining = reward.getCount();
        for (ItemStack slot : player.getInventory().items) {
            if (slot.isEmpty()) {
                remaining -= reward.getMaxStackSize();
            } else if (ItemStack.isSameItemSameComponents(slot, reward)) {
                remaining -= Math.max(0, Math.min(slot.getMaxStackSize(), reward.getMaxStackSize()) - slot.getCount());
            }
            if (remaining <= 0) return true;
        }
        return false;
    }
}
