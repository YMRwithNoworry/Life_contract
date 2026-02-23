package org.alku.life_contract;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketPurchaseItem {
    private int slot;

    public PacketPurchaseItem(int slot) {
        this.slot = slot;
    }

    public PacketPurchaseItem(FriendlyByteBuf buf) {
        this.slot = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(slot);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getSender() != null) {
                NetworkHandler.handlePurchase(ctx.get().getSender(), slot);
            }
        });
        return true;
    }
}