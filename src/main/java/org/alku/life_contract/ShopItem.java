package org.alku.life_contract;

import net.minecraft.world.item.ItemStack;

public class ShopItem {
    private ItemStack result;
    private ItemStack currency;
    private int price;

    public ShopItem(ItemStack result, ItemStack currency, int price) {
        this.result = result;
        this.currency = currency;
        this.price = price;
    }

    public ItemStack getResult() {
        return result.copy();
    }

    public ItemStack getCurrency() {
        return currency;
    }

    public int getPrice() {
        return price;
    }
}