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
    private final boolean sporeSurgeActive;
    private final int sporeSurgeRemaining;
    private final boolean purificationRiftActive;
    private final int safeBubbleRemaining;
    private final List<BubbleData> bubblePositions;
    private final boolean bountyActive;
    private final String bountyTargetName;
    private final boolean endgameOverloadActive;
    private final boolean sporeRainActive;
    private final int sporeRainRemaining;
    private final double borderCenterX;
    private final double borderCenterZ;
    private final double borderSize;
    private final List<PlayerPosData> playerPositions;
    private final UUID bountyTargetUUID;
    private final int bountyTargetX;
    private final int bountyTargetZ;

    public static class BubbleData {
        public final int x, y, z;
        public final double radius;
        public final int colorIndex;

        public BubbleData(int x, int y, int z, double radius, int colorIndex) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.radius = radius;
            this.colorIndex = colorIndex;
        }
    }

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

    public PacketSyncEvents(boolean gameActive, boolean sporeSurgeActive, int sporeSurgeRemaining,
                            boolean purificationRiftActive, int safeBubbleRemaining,
                            List<BubbleData> bubblePositions,
                            boolean bountyActive, String bountyTargetName, boolean endgameOverloadActive,
                            boolean sporeRainActive, int sporeRainRemaining,
                            double borderCenterX, double borderCenterZ, double borderSize,
                            List<PlayerPosData> playerPositions,
                            UUID bountyTargetUUID, int bountyTargetX, int bountyTargetZ) {
        this.gameActive = gameActive;
        this.sporeSurgeActive = sporeSurgeActive;
        this.sporeSurgeRemaining = sporeSurgeRemaining;
        this.purificationRiftActive = purificationRiftActive;
        this.safeBubbleRemaining = safeBubbleRemaining;
        this.bubblePositions = bubblePositions != null ? bubblePositions : new ArrayList<>();
        this.bountyActive = bountyActive;
        this.bountyTargetName = bountyTargetName != null ? bountyTargetName : "";
        this.endgameOverloadActive = endgameOverloadActive;
        this.sporeRainActive = sporeRainActive;
        this.sporeRainRemaining = sporeRainRemaining;
        this.borderCenterX = borderCenterX;
        this.borderCenterZ = borderCenterZ;
        this.borderSize = borderSize;
        this.playerPositions = playerPositions != null ? playerPositions : new ArrayList<>();
        this.bountyTargetUUID = bountyTargetUUID;
        this.bountyTargetX = bountyTargetX;
        this.bountyTargetZ = bountyTargetZ;
    }

    public PacketSyncEvents(FriendlyByteBuf buffer) {
        this.gameActive = buffer.readBoolean();
        this.sporeSurgeActive = buffer.readBoolean();
        this.sporeSurgeRemaining = buffer.readInt();
        this.purificationRiftActive = buffer.readBoolean();
        this.safeBubbleRemaining = buffer.readInt();
        
        int bubbleCount = buffer.readInt();
        this.bubblePositions = new ArrayList<>();
        for (int i = 0; i < bubbleCount; i++) {
            int x = buffer.readInt();
            int y = buffer.readInt();
            int z = buffer.readInt();
            double radius = buffer.readDouble();
            int colorIndex = buffer.readInt();
            this.bubblePositions.add(new BubbleData(x, y, z, radius, colorIndex));
        }
        
        this.bountyActive = buffer.readBoolean();
        this.bountyTargetName = buffer.readUtf();
        this.endgameOverloadActive = buffer.readBoolean();
        this.sporeRainActive = buffer.readBoolean();
        this.sporeRainRemaining = buffer.readInt();
        this.borderCenterX = buffer.readDouble();
        this.borderCenterZ = buffer.readDouble();
        this.borderSize = buffer.readDouble();

        int playerCount = buffer.readInt();
        this.playerPositions = new ArrayList<>();
        for (int i = 0; i < playerCount; i++) {
            UUID uuid = buffer.readUUID();
            String name = buffer.readUtf(64);
            boolean hasLeader = buffer.readBoolean();
            UUID leader = hasLeader ? buffer.readUUID() : null;
            int x = buffer.readInt();
            int z = buffer.readInt();
            float yaw = buffer.readFloat();
            int lifePoints = buffer.readInt();
            this.playerPositions.add(new PlayerPosData(uuid, name, leader, x, z, yaw, lifePoints));
        }

        boolean hasBountyPosition = buffer.readBoolean();
        if (hasBountyPosition) {
            this.bountyTargetUUID = buffer.readUUID();
            this.bountyTargetX = buffer.readInt();
            this.bountyTargetZ = buffer.readInt();
        } else {
            this.bountyTargetUUID = null;
            this.bountyTargetX = 0;
            this.bountyTargetZ = 0;
        }
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBoolean(gameActive);
        buffer.writeBoolean(sporeSurgeActive);
        buffer.writeInt(sporeSurgeRemaining);
        buffer.writeBoolean(purificationRiftActive);
        buffer.writeInt(safeBubbleRemaining);

        buffer.writeInt(bubblePositions.size());
        for (BubbleData bubble : bubblePositions) {
            buffer.writeInt(bubble.x);
            buffer.writeInt(bubble.y);
            buffer.writeInt(bubble.z);
            buffer.writeDouble(bubble.radius);
            buffer.writeInt(bubble.colorIndex);
        }

        buffer.writeBoolean(bountyActive);
        buffer.writeUtf(bountyTargetName);
        buffer.writeBoolean(endgameOverloadActive);
        buffer.writeBoolean(sporeRainActive);
        buffer.writeInt(sporeRainRemaining);
        buffer.writeDouble(borderCenterX);
        buffer.writeDouble(borderCenterZ);
        buffer.writeDouble(borderSize);

        buffer.writeInt(playerPositions.size());
        for (PlayerPosData player : playerPositions) {
            buffer.writeUUID(player.uuid);
            buffer.writeUtf(player.name, 64);
            buffer.writeBoolean(player.leaderUUID != null);
            if (player.leaderUUID != null) {
                buffer.writeUUID(player.leaderUUID);
            }
            buffer.writeInt(player.x);
            buffer.writeInt(player.z);
            buffer.writeFloat(player.yaw);
            buffer.writeInt(player.lifePoints);
        }

        buffer.writeBoolean(bountyTargetUUID != null);
        if (bountyTargetUUID != null) {
            buffer.writeUUID(bountyTargetUUID);
            buffer.writeInt(bountyTargetX);
            buffer.writeInt(bountyTargetZ);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                ClientDataStorage.setEventData(
                    gameActive, sporeSurgeActive, sporeSurgeRemaining,
                    purificationRiftActive, safeBubbleRemaining,
                    bubblePositions,
                    bountyActive, bountyTargetName, endgameOverloadActive,
                    sporeRainActive, sporeRainRemaining,
                    borderCenterX, borderCenterZ, borderSize,
                    playerPositions, bountyTargetUUID, bountyTargetX, bountyTargetZ
                );
            });
        });
        context.setPacketHandled(true);
    }
}
