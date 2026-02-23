package org.alku.life_contract;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketHealerActiveHeal {

    public PacketHealerActiveHeal() {
    }

    public static void encode(PacketHealerActiveHeal msg, FriendlyByteBuf buffer) {
    }

    public static PacketHealerActiveHeal decode(FriendlyByteBuf buffer) {
        return new PacketHealerActiveHeal();
    }

    public static void handle(PacketHealerActiveHeal msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                HealerSystem.useActiveHeal(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
