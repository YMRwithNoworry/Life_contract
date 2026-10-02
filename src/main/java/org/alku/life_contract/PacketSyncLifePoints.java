package org.alku.life_contract;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PacketSyncLifePoints implements CustomPacketPayload {
    public static final Type<PacketSyncLifePoints> TYPE = NetworkHandler.type("sync_life_points");
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketSyncLifePoints> STREAM_CODEC =
            NetworkHandler.codec((buffer, packet) -> packet.encode(buffer), PacketSyncLifePoints::new);

    private final List<PlayerLifePoints> players;

    public record PlayerLifePoints(UUID uuid, int lifePoints) {
    }

    public PacketSyncLifePoints(List<PlayerLifePoints> players) {
        this.players = players != null ? players : new ArrayList<>();
    }

    public PacketSyncLifePoints(FriendlyByteBuf buffer) {
        int count = buffer.readVarInt();
        players = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            players.add(new PlayerLifePoints(buffer.readUUID(), buffer.readVarInt()));
        }
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(players.size());
        for (PlayerLifePoints player : players) {
            buffer.writeUUID(player.uuid());
            buffer.writeVarInt(player.lifePoints());
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketSyncLifePoints packet, IPayloadContext context) {
        context.enqueueWork(() -> ClientDataStorage.setPlayerLifePoints(packet.players));
    }
}
