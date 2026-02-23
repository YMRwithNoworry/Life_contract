package org.alku.life_contract;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketSaveTrade {
    private int expLevels;

    public PacketSaveTrade(int expLevels) {
        this.expLevels = expLevels;
    }

    public PacketSaveTrade(FriendlyByteBuf buf) {
        this.expLevels = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(expLevels);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null && player.containerMenu instanceof TradeSetupMenu) {
                TradeSetupMenu menu = (TradeSetupMenu) player.containerMenu;
                ItemStack output = menu.getContainer().getItem(0);
                
                if (!output.isEmpty() && expLevels > 0) {
                    TradeConfig.addTrade(expLevels, output.copy());
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§a交易添加成功！"));
                    player.closeContainer();
                } else {
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§c请先放入获得物品并设置经验等级！"));
                }
            }
        });
        return true;
    }
}
