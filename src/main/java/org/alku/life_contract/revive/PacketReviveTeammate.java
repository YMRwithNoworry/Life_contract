package org.alku.life_contract.revive;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.alku.life_contract.NetworkHandler;

import java.util.UUID;

public class PacketReviveTeammate implements CustomPacketPayload {
    public static final Type<PacketReviveTeammate> TYPE = NetworkHandler.type("revive_teammate");
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketReviveTeammate> STREAM_CODEC =
            NetworkHandler.codec((buffer, packet) -> encode(packet, buffer), PacketReviveTeammate::decode);

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

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketReviveTeammate packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                ReviveTeammateSystem.reviveTeammate(player, packet.teammateUUID);
            }
        });
    }
}
