package org.alku.life_contract.market;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.alku.life_contract.airdrop.Airdrop;
import org.alku.life_contract.ContractEvents;
import org.alku.life_contract.Life_contract;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class BulletShopService {
    private static final int PRICE = 30;
    private static final int QUANTITY = 5;
    public static final int SIGNAL_GUN_PRICE = 100;
    public static final int COOKED_BEEF_PRICE = 10;

    private BulletShopService() {
    }

    public static List<Item> findAmmoItems(net.minecraft.world.entity.player.Player player) {
        String contractMod = ContractEvents.getEffectiveContractMod(player);
        if (contractMod == null || contractMod.isBlank()) return List.of();

        return BuiltInRegistries.ITEM.entrySet().stream()
                .filter(entry -> entry.getKey().location().getNamespace().equals(contractMod))
                .filter(entry -> isAmmoPath(entry.getKey().location()))
                .map(java.util.Map.Entry::getValue)
                .sorted(Comparator.comparing(item -> item.getDefaultInstance().getHoverName().getString()))
                .toList();
    }

    public static Component purchase(ServerPlayer player, Item item) {
        int price;
        int quantity;
        if (item == Airdrop.DISPOSABLE_FLARE_GUN.get()) {
            price = SIGNAL_GUN_PRICE;
            quantity = 1;
        } else if (item == Items.COOKED_BEEF) {
            price = COOKED_BEEF_PRICE;
            quantity = 1;
        } else {
            String contractMod = ContractEvents.getEffectiveContractMod(player);
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            if (contractMod == null || id == null || !contractMod.equals(id.getNamespace()) || !isAmmoPath(id)) {
                return Component.translatable("gui.life_contract.shop.invalid_item");
            }
            price = PRICE;
            quantity = QUANTITY;
        }

        ItemStack reward = new ItemStack(item, quantity);
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
