package org.alku.life_contract;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientProxy {

    @OnlyIn(Dist.CLIENT)
    public static void openProfessionScreen() {
        Minecraft.getInstance().setScreen(new ProfessionScreen());
    }

    @OnlyIn(Dist.CLIENT)
    public static void setTradeShopRemoveMode(boolean isRemoveMode) {
        TradeShopScreen.setRemoveMode(isRemoveMode);
    }

    @OnlyIn(Dist.CLIENT)
    public static void updateMineralGeneratorScreen(String mineralType, int interval, boolean enabled) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof MineralGeneratorScreen screen) {
            screen.updateDataFromServer(mineralType, interval, enabled);
        }
    }

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
                if (profession != null && !profession.isEmpty()) {
                    data.putString("LifeContractProfession", profession);
                } else {
                    data.remove("LifeContractProfession");
                }
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void syncForgetterState(java.util.UUID playerUUID, boolean isInvisible, int remainingTicks) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            net.minecraft.world.entity.player.Player player = mc.level.getPlayerByUUID(playerUUID);
            if (player != null) {
                net.minecraft.nbt.CompoundTag data = player.getPersistentData();
                data.putBoolean("ForgetterInvisibleClient", isInvisible);
                data.putInt("ForgetterRemainingTicksClient", remainingTicks);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void syncMineralGeneratorData(net.minecraft.core.BlockPos pos, String mineralType, 
            int interval, boolean enabled, long lastTick, long serverTick) {
        ClientDataStorage.setMineralGeneratorData(pos, mineralType, interval, enabled, lastTick, serverTick);
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
    public static void removeMineralGeneratorData(net.minecraft.core.BlockPos pos) {
        ClientDataStorage.removeMineralGeneratorData(pos);
    }

    @OnlyIn(Dist.CLIENT)
    public static void setMineralGenerationEnabled(boolean enabled) {
        MineralGenerationConfig.setClientSideEnabled(enabled);
    }

    @OnlyIn(Dist.CLIENT)
    public static void displayMountMessage(boolean isMounting) {
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.displayClientMessage(
                net.minecraft.network.chat.Component.literal(
                    isMounting ? "§a[驯兽师] §r骑乘状态已同步" : "§e[驯兽师] §r解除骑乘状态已同步"
                ),
                true
            );
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static String getSelfProfessionId() {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.player != null) {
            ClientDataStorage.PlayerData data = ClientDataStorage.get(mc.player.getUUID());
            if (data != null && data.profession != null) {
                return data.profession;
            }
        }
        return "";
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
}
