package org.alku.life_contract;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class PacketSyncContract {
    private final UUID playerUUID;
    private final String playerName;
    private final String contractMod;
    private final String leaderName;
    private final UUID leaderUUID;
    private final boolean hasLeader;
    private final int teamNumber;
    private final String profession;

    public PacketSyncContract(Player player) {
        this.playerUUID = player.getUUID();
        this.playerName = player.getName().getString();
        CompoundTag data = player.getPersistentData();
        this.contractMod = data.getString(SoulContractItem.TAG_CONTRACT_MOD);
        this.leaderName = data.getString(TeamOrganizerItem.TAG_LEADER_NAME);
        this.teamNumber = data.contains(TeamOrganizerItem.TAG_TEAM_NUMBER) ? data.getInt(TeamOrganizerItem.TAG_TEAM_NUMBER) : -1;
        
        this.profession = data.getString("LifeContractProfession");

        if (data.hasUUID(TeamOrganizerItem.TAG_LEADER_UUID)) {
            this.leaderUUID = data.getUUID(TeamOrganizerItem.TAG_LEADER_UUID);
            this.hasLeader = true;
        } else {
            this.leaderUUID = UUID.randomUUID();
            this.hasLeader = false;
        }
    }

    public PacketSyncContract(FriendlyByteBuf buffer) {
        this.playerUUID = buffer.readUUID();
        this.playerName = buffer.readUtf();
        this.contractMod = buffer.readUtf();
        this.leaderName = buffer.readUtf();
        this.hasLeader = buffer.readBoolean();
        this.leaderUUID = buffer.readUUID();
        this.teamNumber = buffer.readInt();
        this.profession = buffer.readUtf();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUUID(playerUUID);
        buffer.writeUtf(playerName);
        buffer.writeUtf(contractMod);
        buffer.writeUtf(leaderName);
        buffer.writeBoolean(hasLeader);
        buffer.writeUUID(leaderUUID);
        buffer.writeInt(teamNumber);
        buffer.writeUtf(profession);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                try {
                    Class<?> proxyClass = Class.forName("org.alku.life_contract.ClientProxy");
                    proxyClass.getMethod("syncContractData", UUID.class, String.class, String.class, 
                            String.class, UUID.class, int.class, String.class)
                            .invoke(null, playerUUID, playerName, contractMod, leaderName, 
                                    hasLeader ? leaderUUID : null, teamNumber, profession);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
        context.setPacketHandled(true);
    }
}
