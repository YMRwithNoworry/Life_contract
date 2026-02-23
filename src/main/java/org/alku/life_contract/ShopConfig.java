package org.alku.life_contract;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

public class ShopConfig {
    private static final List<ShopItem> SHOP_ITEMS = new ArrayList<>();

    static {
        addDefaultItems();
    }

    private static void addDefaultItems() {
        ItemStack currency = new ItemStack(Items.GOLD_INGOT);

        addItem(new ItemStack(Items.DIAMOND_SWORD), currency, 10);
        addItem(new ItemStack(Items.DIAMOND_PICKAXE), currency, 8);
        addItem(new ItemStack(Items.DIAMOND_AXE), currency, 8);
        addItem(new ItemStack(Items.DIAMOND_SHOVEL), currency, 6);
        addItem(new ItemStack(Items.GOLDEN_APPLE, 8), currency, 15);
        addItem(new ItemStack(Items.COBBLESTONE, 64), currency, 2);
        addItem(new ItemStack(Items.SPLASH_POTION, 2), currency, 5);
        addItem(new ItemStack(Items.ENDER_PEARL, 4), currency, 8);
        
        addItem(ItemStack.EMPTY, currency, 0);
        addItem(new ItemStack(Items.BOW), currency, 6);
        addItem(new ItemStack(Items.ARROW, 32), currency, 3);
        addItem(ItemStack.EMPTY, currency, 0);
        addItem(ItemStack.EMPTY, currency, 0);
        addItem(ItemStack.EMPTY, currency, 0);
        addItem(ItemStack.EMPTY, currency, 0);
        addItem(ItemStack.EMPTY, currency, 0);

        for (int i = 0; i < 9; i++) addItem(ItemStack.EMPTY, currency, 0);

        addItem(new ItemStack(Items.DIAMOND_HELMET), currency, 12);
        addItem(new ItemStack(Items.DIAMOND_CHESTPLATE), currency, 15);
        addItem(new ItemStack(Items.DIAMOND_LEGGINGS), currency, 14);
        addItem(new ItemStack(Items.DIAMOND_BOOTS), currency, 10);
        addItem(ItemStack.EMPTY, currency, 0);
        addItem(ItemStack.EMPTY, currency, 0);
        addItem(ItemStack.EMPTY, currency, 0);
        addItem(ItemStack.EMPTY, currency, 0);
        addItem(ItemStack.EMPTY, currency, 0);

        addItem(new ItemStack(Items.GOLD_BLOCK, 4), currency, 20);
        addItem(new ItemStack(Items.IRON_BLOCK, 8), currency, 8);
        addItem(ItemStack.EMPTY, currency, 0);
        addItem(ItemStack.EMPTY, currency, 0);
        addItem(ItemStack.EMPTY, currency, 0);
        addItem(ItemStack.EMPTY, currency, 0);
        addItem(ItemStack.EMPTY, currency, 0);
        addItem(ItemStack.EMPTY, currency, 0);
        addItem(ItemStack.EMPTY, currency, 0);

        addItem(new ItemStack(Items.TNT, 4), currency, 12);
        addItem(new ItemStack(Items.WATER_BUCKET), currency, 5);
        addItem(new ItemStack(Items.LAVA_BUCKET), currency, 7);
        addItem(ItemStack.EMPTY, currency, 0);
        addItem(ItemStack.EMPTY, currency, 0);
        addItem(ItemStack.EMPTY, currency, 0);
        addItem(ItemStack.EMPTY, currency, 0);
        addItem(ItemStack.EMPTY, currency, 0);
        addItem(ItemStack.EMPTY, currency, 0);

        while (SHOP_ITEMS.size() < 54) {
            addItem(ItemStack.EMPTY, currency, 0);
        }
    }

    public static void addItem(ItemStack result, ItemStack currency, int price) {
        if (SHOP_ITEMS.size() < 54) {
            SHOP_ITEMS.add(new ShopItem(result, currency, price));
        }
    }

    public static void setItem(int slot, ItemStack result, ItemStack currency, int price) {
        if (slot >= 0 && slot < 54) {
            while (SHOP_ITEMS.size() <= slot) {
                SHOP_ITEMS.add(new ShopItem(ItemStack.EMPTY, new ItemStack(Items.GOLD_INGOT), 0));
            }
            SHOP_ITEMS.set(slot, new ShopItem(result, currency, price));
        }
    }

    public static ShopItem getShopItem(int slot) {
        if (slot >= 0 && slot < SHOP_ITEMS.size()) {
            return SHOP_ITEMS.get(slot);
        }
        return null;
    }

    public static List<ShopItem> getAllItems() {
        return new ArrayList<>(SHOP_ITEMS);
    }

    public static void clear() {
        SHOP_ITEMS.clear();
    }
}