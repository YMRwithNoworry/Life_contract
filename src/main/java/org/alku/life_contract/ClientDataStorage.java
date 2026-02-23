package org.alku.life_contract;

import net.minecraft.core.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientDataStorage {
    public static final Map<UUID, PlayerData> PLAYER_DATA_CACHE = new HashMap<>();
    public static final Map<BlockPos, MineralGeneratorData> MINERAL_GENERATOR_CACHE = new HashMap<>();
    
    private static UUID markedTargetUUID = null;
    private static String markedTargetName = "";
    private static int markedTargetX = 0;
    private static int markedTargetY = 0;
    private static int markedTargetZ = 0;
    
    private static int healerCooldown = 0;

    public static class PlayerData {
        public String contractMod = "";
        public String leaderName = "";
        public UUID leaderUUID = null;
        public String playerName = "";
        public UUID playerUUID = null;
        public int teamNumber = -1;
        public String profession = "";

        public PlayerData(UUID uuid, String name) {
            this.playerUUID = uuid;
            this.playerName = name;
        }
    }

    public static class MineralGeneratorData {
        public String mineralType;
        public int interval;
        public boolean enabled;
        public long lastTick;
        public long serverTick;

        public MineralGeneratorData(String mineralType, int interval, boolean enabled, long lastTick, long serverTick) {
            this.mineralType = mineralType;
            this.interval = interval;
            this.enabled = enabled;
            this.lastTick = lastTick;
            this.serverTick = serverTick;
        }
    }

    public static void update(UUID uuid, String name, String mod, String leaderName, UUID leaderUUID, int teamNumber, String profession) {
        PlayerData data = PLAYER_DATA_CACHE.computeIfAbsent(uuid, k -> new PlayerData(uuid, name));
        data.playerName = name;
        data.contractMod = mod;
        data.leaderName = leaderName;
        data.leaderUUID = leaderUUID;
        data.teamNumber = teamNumber;
        data.profession = profession;
    }

    public static PlayerData get(UUID uuid) {
        return PLAYER_DATA_CACHE.get(uuid);
    }

    public static void setMineralGeneratorData(BlockPos pos, String mineralType, int interval, boolean enabled, long lastTick, long serverTick) {
        MINERAL_GENERATOR_CACHE.put(pos, new MineralGeneratorData(mineralType, interval, enabled, lastTick, serverTick));
    }

    public static MineralGeneratorData getMineralGeneratorData(BlockPos pos) {
        return MINERAL_GENERATOR_CACHE.get(pos);
    }

    public static String getSelfProfessionId() {
        try {
            Class<?> proxyClass = Class.forName("org.alku.life_contract.ClientProxy");
            return (String) proxyClass.getMethod("getSelfProfessionId").invoke(null);
        } catch (Exception e) {
            return "";
        }
    }

    public static String getProfessionId() {
        return getSelfProfessionId();
    }

    public static void removeMineralGeneratorData(BlockPos pos) {
        MINERAL_GENERATOR_CACHE.remove(pos);
    }

    public static void clearMineralGeneratorData() {
        MINERAL_GENERATOR_CACHE.clear();
    }

    public static void setMarkedTarget(UUID uuid, String name, int x, int y, int z) {
        markedTargetUUID = uuid;
        markedTargetName = name != null ? name : "";
        markedTargetX = x;
        markedTargetY = y;
        markedTargetZ = z;
    }

    public static UUID getMarkedTargetUUID() {
        return markedTargetUUID;
    }

    public static String getMarkedTargetName() {
        return markedTargetName;
    }

    public static int getMarkedTargetX() {
        return markedTargetX;
    }

    public static int getMarkedTargetY() {
        return markedTargetY;
    }

    public static int getMarkedTargetZ() {
        return markedTargetZ;
    }

    public static boolean hasMarkedTarget() {
        return markedTargetUUID != null;
    }

    public static void clearMarkedTarget() {
        markedTargetUUID = null;
        markedTargetName = "";
        markedTargetX = 0;
        markedTargetY = 0;
        markedTargetZ = 0;
    }

    public static int getHealerCooldown() {
        return healerCooldown;
    }

    public static void setHealerCooldown(int cooldown) {
        healerCooldown = cooldown;
    }

    public static void tickHealerCooldown() {
        if (healerCooldown > 0) {
            healerCooldown--;
        }
    }
}
