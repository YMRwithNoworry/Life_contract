package org.alku.life_contract;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class PacketSyncLifePoints {
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

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientDataStorage.setPlayerLifePoints(players)));
        context.setPacketHandled(true);
    }
}
