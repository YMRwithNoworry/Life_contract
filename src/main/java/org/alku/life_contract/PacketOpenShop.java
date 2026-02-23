package org.alku.life_contract;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketOpenShop {

    public PacketOpenShop() {
    }

    public PacketOpenShop(FriendlyByteBuf buf) {
    }

    public void toBytes(FriendlyByteBuf buf) {
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getSender() != null) {
                NetworkHandler.openShop(ctx.get().getSender());
            }
        });
        return true;
    }
}