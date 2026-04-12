package org.alku.life_contract.events;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.alku.life_contract.ClientDataStorage;

import java.util.ArrayList;
import java.util.List;
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

    public static class BubbleData {
        public final int x, y, z;
        public final double radius;
        
        public BubbleData(int x, int y, int z, double radius) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.radius = radius;
        }
    }

    public PacketSyncEvents(boolean gameActive, boolean sporeSurgeActive, int sporeSurgeRemaining,
                            boolean purificationRiftActive, int safeBubbleRemaining,
                            List<BubbleData> bubblePositions,
                            boolean bountyActive, String bountyTargetName, boolean endgameOverloadActive,
                            boolean sporeRainActive, int sporeRainRemaining) {
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
            this.bubblePositions.add(new BubbleData(x, y, z, radius));
        }
        
        this.bountyActive = buffer.readBoolean();
        this.bountyTargetName = buffer.readUtf();
        this.endgameOverloadActive = buffer.readBoolean();
        this.sporeRainActive = buffer.readBoolean();
        this.sporeRainRemaining = buffer.readInt();
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
        }
        
        buffer.writeBoolean(bountyActive);
        buffer.writeUtf(bountyTargetName);
        buffer.writeBoolean(endgameOverloadActive);
        buffer.writeBoolean(sporeRainActive);
        buffer.writeInt(sporeRainRemaining);
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
                    sporeRainActive, sporeRainRemaining
                );
            });
        });
        context.setPacketHandled(true);
    }
}
