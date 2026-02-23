package org.alku.life_contract;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.item.ItemStack;

public class TradeSavedData extends SavedData {
    private static final String DATA_NAME = "life_contract_trades";

    public TradeSavedData() {
    }

    public TradeSavedData(CompoundTag tag) {
        load(tag);
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag tradesList = new ListTag();
        for (int i = 0; i < TradeConfig.getTradeCount(); i++) {
            TradeConfig.TradeItem trade = TradeConfig.getTrade(i);
            if (trade != null) {
                CompoundTag tradeTag = new CompoundTag();
                tradeTag.putInt("expLevels", trade.getExpLevels());
                tradeTag.put("output", trade.getOutput().save(new CompoundTag()));
                tradesList.add(tradeTag);
            }
        }
        tag.put("trades", tradesList);
        return tag;
    }

    public void load(CompoundTag tag) {
        TradeConfig.setLoading(true);
        try {
            TradeConfig.clear();
            ListTag tradesList = tag.getList("trades", Tag.TAG_COMPOUND);
            for (int i = 0; i < tradesList.size(); i++) {
                CompoundTag tradeTag = tradesList.getCompound(i);
                int expLevels = tradeTag.getInt("expLevels");
                ItemStack output = ItemStack.of(tradeTag.getCompound("output"));
                TradeConfig.addTradeNoSave(expLevels, output);
            }
        } finally {
            TradeConfig.setLoading(false);
        }
    }

    public static TradeSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                TradeSavedData::new,
                TradeSavedData::new,
                DATA_NAME
        );
    }

    public static void save(ServerLevel level) {
        TradeSavedData data = get(level);
        data.setDirty();
    }

    public static void load(ServerLevel level) {
        get(level);
    }
}
