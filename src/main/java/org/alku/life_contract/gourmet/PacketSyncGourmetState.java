package org.alku.life_contract.gourmet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketSyncGourmetState {

    private final int umami;
    private final int emergencyCooldown;
    private final int flavorBombCooldown;
    private final int warmFeedCooldown;
    private final int godChefCooldown;
    private final boolean godChefMode;

    public PacketSyncGourmetState(int umami, int emergencyCooldown, int flavorBombCooldown, 
                                   int warmFeedCooldown, int godChefCooldown, boolean godChefMode) {
        this.umami = umami;
        this.emergencyCooldown = emergencyCooldown;
        this.flavorBombCooldown = flavorBombCooldown;
        this.warmFeedCooldown = warmFeedCooldown;
        this.godChefCooldown = godChefCooldown;
        this.godChefMode = godChefMode;
    }

    public static void encode(PacketSyncGourmetState msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.umami);
        buffer.writeInt(msg.emergencyCooldown);
        buffer.writeInt(msg.flavorBombCooldown);
        buffer.writeInt(msg.warmFeedCooldown);
        buffer.writeInt(msg.godChefCooldown);
        buffer.writeBoolean(msg.godChefMode);
    }

    public static PacketSyncGourmetState decode(FriendlyByteBuf buffer) {
        return new PacketSyncGourmetState(
            buffer.readInt(),
            buffer.readInt(),
            buffer.readInt(),
            buffer.readInt(),
            buffer.readInt(),
            buffer.readBoolean()
        );
    }

    public static void handle(PacketSyncGourmetState msg, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ClientGourmetState.setUmami(msg.umami);
            ClientGourmetState.setEmergencyCooldown(msg.emergencyCooldown);
            ClientGourmetState.setFlavorBombCooldown(msg.flavorBombCooldown);
            ClientGourmetState.setWarmFeedCooldown(msg.warmFeedCooldown);
            ClientGourmetState.setGodChefCooldown(msg.godChefCooldown);
            ClientGourmetState.setGodChefMode(msg.godChefMode);
        });
        context.get().setPacketHandled(true);
    }
}
