package org.alku.life_contract;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketBuyTrade {
    private int slot;

    public PacketBuyTrade(int slot) {
        this.slot = slot;
    }

    public PacketBuyTrade(FriendlyByteBuf buf) {
        this.slot = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(slot);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                handleBuyTrade(player, slot);
            }
        });
        return true;
    }

    private static void handleBuyTrade(ServerPlayer player, int slot) {
        TradeConfig.TradeItem trade = TradeConfig.getTrade(slot);
        if (trade == null || trade.getOutput().isEmpty()) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§c这个交易无法购买！"));
            return;
        }

        int requiredLevels = trade.getExpLevels();
        int playerLevels = player.experienceLevel;

        if (playerLevels < requiredLevels) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§c经验等级不足！需要 " + requiredLevels + " 级"));
            return;
        }

        player.giveExperienceLevels(-requiredLevels);
        giveItem(player, trade.getOutput());
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§a交易成功！"));
    }

    private static void giveItem(ServerPlayer player, ItemStack itemStack) {
        if (!player.addItem(itemStack.copy())) {
            player.drop(itemStack.copy(), false);
        }
    }
}
