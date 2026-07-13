package org.alku.life_contract.events;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.alku.life_contract.ClientDataStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class PacketSyncEvents {
    private final boolean gameActive;
    private final double borderCenterX;
    private final double borderCenterZ;
    private final double borderSize;
    private final List<PlayerPosData> playerPositions;

    public static class PlayerPosData {
        public final UUID uuid;
        public final String name;
        public final UUID leaderUUID;
        public final int x;
        public final int z;
        public final float yaw;
        public final int lifePoints;

        public PlayerPosData(UUID uuid, String name, UUID leaderUUID, int x, int z, float yaw, int lifePoints) {
            this.uuid = uuid;
            this.name = name != null ? name : "";
            this.leaderUUID = leaderUUID;
            this.x = x;
            this.z = z;
            this.yaw = yaw;
            this.lifePoints = lifePoints;
        }
    }

    public PacketSyncEvents(boolean gameActive, double borderCenterX, double borderCenterZ,
                            double borderSize, List<PlayerPosData> playerPositions) {
        this.gameActive = gameActive;
        this.borderCenterX = borderCenterX;
        this.borderCenterZ = borderCenterZ;
        this.borderSize = borderSize;
        this.playerPositions = playerPositions != null ? playerPositions : new ArrayList<>();
    }

    public PacketSyncEvents(FriendlyByteBuf buffer) {
        gameActive = buffer.readBoolean();
        borderCenterX = buffer.readDouble();
        borderCenterZ = buffer.readDouble();
        borderSize = buffer.readDouble();
        int playerCount = buffer.readInt();
        playerPositions = new ArrayList<>();
        for (int i = 0; i < playerCount; i++) {
            UUID uuid = buffer.readUUID();
            String name = buffer.readUtf(64);
            UUID leader = buffer.readBoolean() ? buffer.readUUID() : null;
            playerPositions.add(new PlayerPosData(uuid, name, leader, buffer.readInt(), buffer.readInt(),
                    buffer.readFloat(), buffer.readInt()));
        }
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBoolean(gameActive);
        buffer.writeDouble(borderCenterX);
        buffer.writeDouble(borderCenterZ);
        buffer.writeDouble(borderSize);
        buffer.writeInt(playerPositions.size());
        for (PlayerPosData player : playerPositions) {
            buffer.writeUUID(player.uuid);
            buffer.writeUtf(player.name, 64);
            buffer.writeBoolean(player.leaderUUID != null);
            if (player.leaderUUID != null) buffer.writeUUID(player.leaderUUID);
            buffer.writeInt(player.x);
            buffer.writeInt(player.z);
            buffer.writeFloat(player.yaw);
            buffer.writeInt(player.lifePoints);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                ClientDataStorage.setGameMapData(gameActive, borderCenterX, borderCenterZ, borderSize, playerPositions)));
        context.setPacketHandled(true);
    }
}
