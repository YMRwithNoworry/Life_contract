package org.alku.life_contract;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.alku.life_contract.follower.FollowerClientCache;
import org.alku.life_contract.revive.ClientReviveData;
import org.alku.life_contract.revive.ReviveTeammateMenu;
import org.alku.life_contract.revive.ReviveTeammateScreen;
import org.alku.life_contract.revive.ReviveTeammateSystem;

@OnlyIn(Dist.CLIENT)
public class ClientProxy {

    @OnlyIn(Dist.CLIENT)
    public static void syncContractData(java.util.UUID playerUUID, String playerName, String contractMod, 
            String leaderName, java.util.UUID leaderUUID, int teamNumber, String profession) {
        ClientDataStorage.update(playerUUID, playerName, contractMod, leaderName, leaderUUID, teamNumber, profession);

        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            net.minecraft.world.entity.player.Player player = mc.level.getPlayerByUUID(playerUUID);
            if (player != null) {
                net.minecraft.nbt.CompoundTag data = player.getPersistentData();
                data.putString(SoulContractItem.TAG_CONTRACT_MOD, contractMod);
                data.putString(TeamOrganizerItem.TAG_LEADER_NAME, leaderName);
                if (leaderUUID != null) {
                    data.putUUID(TeamOrganizerItem.TAG_LEADER_UUID, leaderUUID);
                } else {
                    data.remove(TeamOrganizerItem.TAG_LEADER_UUID);
                }
                if (teamNumber != -1) {
                    data.putInt(TeamOrganizerItem.TAG_TEAM_NUMBER, teamNumber);
                } else {
                    data.remove(TeamOrganizerItem.TAG_TEAM_NUMBER);
                }
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void registerFollower(java.util.UUID entityUUID, java.util.UUID ownerUUID) {
        FollowerClientCache.registerFollower(entityUUID, ownerUUID);
    }

    @OnlyIn(Dist.CLIENT)
    public static void unregisterFollower(java.util.UUID entityUUID) {
        FollowerClientCache.unregisterFollower(entityUUID);
    }

    @OnlyIn(Dist.CLIENT)
    public static void syncFollowerHunger(java.util.UUID playerUUID, int followerCount, float hungerMultiplier) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            net.minecraft.world.entity.player.Player player = mc.level.getPlayerByUUID(playerUUID);
            if (player != null) {
                net.minecraft.nbt.CompoundTag data = player.getPersistentData();
                data.putInt("FollowerCountClient", followerCount);
                data.putFloat("FollowerHungerMultiplierClient", hungerMultiplier);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void openReviveScreen(java.util.List<ReviveTeammateSystem.DeadTeammateInfo> deadTeammates) {
        ClientReviveData.setDeadTeammates(deadTeammates);
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.closeContainer();
            ReviveTeammateMenu menu = new ReviveTeammateMenu(0, mc.player.getInventory(), deadTeammates);
            ReviveTeammateScreen screen = new ReviveTeammateScreen(menu, mc.player.getInventory(), 
                net.minecraft.network.chat.Component.literal("选择复活的队友"));
            mc.setScreen(screen);
        }
    }
}
