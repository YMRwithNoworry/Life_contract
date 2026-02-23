package org.alku.life_contract;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.List;

public class TradeConfig {
    private static final List<TradeItem> trades = new ArrayList<>();
    private static boolean initialized = false;
    private static boolean isLoading = false;

    public static void init() {
        if (initialized) return;
        initialized = true;
        
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            ServerLevel level = server.overworld();
            TradeSavedData.load(level);
        }
        
        if (trades.isEmpty()) {
            addTradeWithoutSaving(1, new ItemStack(Items.IRON_INGOT, 1));
            addTradeWithoutSaving(10, new ItemStack(Items.GOLD_INGOT, 1));
            addTradeWithoutSaving(100, new ItemStack(Items.DIAMOND, 1));
            addTradeWithoutSaving(1000, new ItemStack(Items.EMERALD, 1));
            addTradeWithoutSaving(50000, new ItemStack(Items.NETHERITE_INGOT, 1));
            saveToFile();
        }
    }

    private static void addTradeWithoutSaving(int expLevels, ItemStack output) {
        trades.add(new TradeItem(expLevels, output.copy()));
    }

    public static void addTradeNoSave(int expLevels, ItemStack output) {
        trades.add(new TradeItem(expLevels, output.copy()));
    }

    public static void addTrade(int expLevels, ItemStack output) {
        trades.add(new TradeItem(expLevels, output.copy()));
        saveToFile();
    }

    public static void clear() {
        trades.clear();
        if (!isLoading) {
            saveToFile();
        }
    }

    public static boolean removeTrade(int index) {
        if (index >= 0 && index < trades.size()) {
            trades.remove(index);
            saveToFile();
            return true;
        }
        return false;
    }

    private static void saveToFile() {
        if (isLoading) return;
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            ServerLevel level = server.overworld();
            TradeSavedData.save(level);
        }
    }

    public static void setLoading(boolean loading) {
        isLoading = loading;
    }

    public static TradeItem getTrade(int index) {
        if (index >= 0 && index < trades.size()) {
            return trades.get(index);
        }
        return null;
    }

    public static int getTradeCount() {
        return trades.size();
    }

    public static class TradeItem {
        private final int expLevels;
        private final ItemStack output;

        public TradeItem(int expLevels, ItemStack output) {
            this.expLevels = expLevels;
            this.output = output;
        }

        public int getExpLevels() {
            return expLevels;
        }

        public ItemStack getOutput() {
            return output;
        }
    }
}
