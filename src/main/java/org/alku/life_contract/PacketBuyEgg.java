package org.alku.life_contract;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketBuyEgg {
    private int index;

    public PacketBuyEgg(int index) {
        this.index = index;
    }

    public PacketBuyEgg(FriendlyByteBuf buf) {
        this.index = buf.readInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(index);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getSender() != null) {
                NetworkHandler.handleBuyEgg(ctx.get().getSender(), index);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
