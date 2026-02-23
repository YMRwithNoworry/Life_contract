package org.alku.life_contract;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketRemoveTrade {
    private final int slotIndex;

    public PacketRemoveTrade(int slotIndex) {
        this.slotIndex = slotIndex;
    }

    public PacketRemoveTrade(FriendlyByteBuf buf) {
        this.slotIndex = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(slotIndex);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                if (TradeConfig.removeTrade(slotIndex)) {
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§a已删除该交易！"));
                } else {
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§c删除失败！"));
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
