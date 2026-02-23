package org.alku.life_contract;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class PacketSyncImpostorState {
    private final UUID playerUUID;
    private final boolean isDisguised;
    private final String disguiseName;
    private final UUID disguiseTeamLeader;
    
    private static final Map<UUID, ClientDisguiseData> CLIENT_DISGUISE_MAP = new HashMap<>();
    
    public static class ClientDisguiseData {
        public final String disguiseName;
        public final UUID disguiseTeamLeader;
        
        public ClientDisguiseData(String disguiseName, UUID disguiseTeamLeader) {
            this.disguiseName = disguiseName;
            this.disguiseTeamLeader = disguiseTeamLeader;
        }
    }
    
    public PacketSyncImpostorState(UUID playerUUID, boolean isDisguised, ImpostorSystem.DisguiseData data) {
        this.playerUUID = playerUUID;
        this.isDisguised = isDisguised;
        this.disguiseName = data != null ? data.targetName : "";
        this.disguiseTeamLeader = data != null ? data.targetTeamLeader : null;
    }
    
    public PacketSyncImpostorState(UUID playerUUID, boolean isDisguised, String disguiseName, UUID disguiseTeamLeader) {
        this.playerUUID = playerUUID;
        this.isDisguised = isDisguised;
        this.disguiseName = disguiseName;
        this.disguiseTeamLeader = disguiseTeamLeader;
    }
    
    public static void encode(PacketSyncImpostorState msg, FriendlyByteBuf buffer) {
        buffer.writeUUID(msg.playerUUID);
        buffer.writeBoolean(msg.isDisguised);
        if (msg.isDisguised) {
            buffer.writeUtf(msg.disguiseName);
            buffer.writeBoolean(msg.disguiseTeamLeader != null);
            if (msg.disguiseTeamLeader != null) {
                buffer.writeUUID(msg.disguiseTeamLeader);
            }
        }
    }
    
    public static PacketSyncImpostorState decode(FriendlyByteBuf buffer) {
        UUID playerUUID = buffer.readUUID();
        boolean isDisguised = buffer.readBoolean();
        String disguiseName = "";
        UUID disguiseTeamLeader = null;
        
        if (isDisguised) {
            disguiseName = buffer.readUtf();
            boolean hasTeamLeader = buffer.readBoolean();
            if (hasTeamLeader) {
                disguiseTeamLeader = buffer.readUUID();
            }
        }
        
        return new PacketSyncImpostorState(playerUUID, isDisguised, disguiseName, disguiseTeamLeader);
    }
    
    public static void handle(PacketSyncImpostorState msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            handleClientSide(msg);
        });
        context.setPacketHandled(true);
    }
    
    @OnlyIn(Dist.CLIENT)
    private static void handleClientSide(PacketSyncImpostorState msg) {
        if (msg.isDisguised) {
            CLIENT_DISGUISE_MAP.put(msg.playerUUID, new ClientDisguiseData(msg.disguiseName, msg.disguiseTeamLeader));
        } else {
            CLIENT_DISGUISE_MAP.remove(msg.playerUUID);
        }
    }
    
    public static ClientDisguiseData getClientDisguiseData(UUID playerUUID) {
        return CLIENT_DISGUISE_MAP.get(playerUUID);
    }
    
    public static boolean isClientDisguised(UUID playerUUID) {
        return CLIENT_DISGUISE_MAP.containsKey(playerUUID);
    }
    
    public static String getClientDisguiseName(UUID playerUUID) {
        ClientDisguiseData data = CLIENT_DISGUISE_MAP.get(playerUUID);
        return data != null ? data.disguiseName : null;
    }
    
    public static UUID getClientDisguiseTeam(UUID playerUUID) {
        ClientDisguiseData data = CLIENT_DISGUISE_MAP.get(playerUUID);
        return data != null ? data.disguiseTeamLeader : null;
    }
    
    public static void clearClientData() {
        CLIENT_DISGUISE_MAP.clear();
    }
}
