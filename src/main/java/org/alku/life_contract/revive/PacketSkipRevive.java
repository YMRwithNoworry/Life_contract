package org.alku.life_contract.revive;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketSkipRevive {

    public PacketSkipRevive() {
    }

    public static void encode(PacketSkipRevive msg, FriendlyByteBuf buffer) {
    }

    public static PacketSkipRevive decode(FriendlyByteBuf buffer) {
        return new PacketSkipRevive();
    }

    public static void handle(PacketSkipRevive msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                ReviveTeammateSystem.skipRevive(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
