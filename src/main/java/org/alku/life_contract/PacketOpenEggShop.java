package org.alku.life_contract;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketOpenEggShop {

    public PacketOpenEggShop() {
    }

    public PacketOpenEggShop(FriendlyByteBuf buf) {
    }

    public void encode(FriendlyByteBuf buf) {
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getSender() != null) {
                NetworkHandler.openEggShop(ctx.get().getSender());
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
