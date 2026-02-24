package org.alku.life_contract.revive;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class PacketReviveTeammate {

    private final UUID teammateUUID;

    public PacketReviveTeammate(UUID teammateUUID) {
        this.teammateUUID = teammateUUID;
    }

    public static void encode(PacketReviveTeammate msg, FriendlyByteBuf buffer) {
        buffer.writeUUID(msg.teammateUUID);
    }

    public static PacketReviveTeammate decode(FriendlyByteBuf buffer) {
        return new PacketReviveTeammate(buffer.readUUID());
    }

    public static void handle(PacketReviveTeammate msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                ReviveTeammateSystem.reviveTeammate(player, msg.teammateUUID);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
