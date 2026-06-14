package org.alku.life_contract.events;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class GameDataStorage extends SavedData {
    
    private boolean gameActive = false;
    private boolean gamePaused = false;
    private long gameStartTick = 0;
    private long pausedTick = 0;
    
    private boolean sporeSurgeTriggered = false;
    private boolean purificationRiftTriggered = false;
    private boolean endgameOverloadTriggered = false;
    
    private int initialPlayerCount = 0;
    private int totalEliminations = 0;
    private UUID bountyTarget = null;
    private int bountyKillReward = 0;
    private double borderDamageMultiplier = 1.0;
    
    private long sporeSurgeStartTick = 0;
    
    private final List<BubbleSaveData> safeBubbles = new ArrayList<>();
    private final Map<UUID, PlayerStatsSaveData> playerStats = new HashMap<>();
    private final Set<UUID> gameStartPlayerIds = new HashSet<>();
    
    public static class BubbleSaveData {
        public final double x, y, z;
        public final double radius;
        public final int durationTicks;
        public final int colorIndex;
        
        public BubbleSaveData(double x, double y, double z, double radius, int durationTicks, int colorIndex) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.radius = radius;
            this.durationTicks = durationTicks;
            this.colorIndex = colorIndex;
        }
    }
    
    public static class PlayerStatsSaveData {
        public final int kills;
        public final int deaths;
        
        public PlayerStatsSaveData(int kills, int deaths) {
            this.kills = kills;
            this.deaths = deaths;
        }
    }
    
    public GameDataStorage() {
    }
    
    public static GameDataStorage load(CompoundTag tag) {
        GameDataStorage data = new GameDataStorage();
        
        data.gameActive = tag.getBoolean("GameActive");
        data.gamePaused = tag.getBoolean("GamePaused");
        data.gameStartTick = tag.getLong("GameStartTick");
        data.pausedTick = tag.getLong("PausedTick");
        
        data.sporeSurgeTriggered = tag.getBoolean("SporeSurgeTriggered");
        data.purificationRiftTriggered = tag.getBoolean("PurificationRiftTriggered");
        data.endgameOverloadTriggered = tag.getBoolean("EndgameOverloadTriggered");
        
        data.initialPlayerCount = tag.getInt("InitialPlayerCount");
        data.totalEliminations = tag.getInt("TotalEliminations");
        data.borderDamageMultiplier = tag.getDouble("BorderDamageMultiplier");
        data.sporeSurgeStartTick = tag.getLong("SporeSurgeStartTick");
        
        if (tag.contains("BountyTarget")) {
            data.bountyTarget = tag.getUUID("BountyTarget");
        }
        data.bountyKillReward = tag.getInt("BountyKillReward");
        
        ListTag bubblesList = tag.getList("SafeBubbles", Tag.TAG_COMPOUND);
        for (int i = 0; i < bubblesList.size(); i++) {
            CompoundTag bubbleTag = bubblesList.getCompound(i);
            data.safeBubbles.add(new BubbleSaveData(
                bubbleTag.getDouble("X"),
                bubbleTag.getDouble("Y"),
                bubbleTag.getDouble("Z"),
                bubbleTag.getDouble("Radius"),
                bubbleTag.getInt("Duration"),
                bubbleTag.getInt("ColorIndex")
            ));
        }
        
        CompoundTag statsTag = tag.getCompound("PlayerStats");
        for (String key : statsTag.getAllKeys()) {
            CompoundTag playerTag = statsTag.getCompound(key);
            UUID uuid = UUID.fromString(key);
            data.playerStats.put(uuid, new PlayerStatsSaveData(
                playerTag.getInt("Kills"),
                playerTag.getInt("Deaths")
            ));
        }

        ListTag gameStartPlayersList = tag.getList("GameStartPlayerIds", Tag.TAG_STRING);
        for (int i = 0; i < gameStartPlayersList.size(); i++) {
            data.gameStartPlayerIds.add(UUID.fromString(gameStartPlayersList.getString(i)));
        }
        
        return data;
    }
    
    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putBoolean("GameActive", gameActive);
        tag.putBoolean("GamePaused", gamePaused);
        tag.putLong("GameStartTick", gameStartTick);
        tag.putLong("PausedTick", pausedTick);
        
        tag.putBoolean("SporeSurgeTriggered", sporeSurgeTriggered);
        tag.putBoolean("PurificationRiftTriggered", purificationRiftTriggered);
        tag.putBoolean("EndgameOverloadTriggered", endgameOverloadTriggered);
        
        tag.putInt("InitialPlayerCount", initialPlayerCount);
        tag.putInt("TotalEliminations", totalEliminations);
        tag.putDouble("BorderDamageMultiplier", borderDamageMultiplier);
        tag.putLong("SporeSurgeStartTick", sporeSurgeStartTick);
        
        if (bountyTarget != null) {
            tag.putUUID("BountyTarget", bountyTarget);
        }
        tag.putInt("BountyKillReward", bountyKillReward);
        
        ListTag bubblesList = new ListTag();
        for (BubbleSaveData bubble : safeBubbles) {
            CompoundTag bubbleTag = new CompoundTag();
            bubbleTag.putDouble("X", bubble.x);
            bubbleTag.putDouble("Y", bubble.y);
            bubbleTag.putDouble("Z", bubble.z);
            bubbleTag.putDouble("Radius", bubble.radius);
            bubbleTag.putInt("Duration", bubble.durationTicks);
            bubbleTag.putInt("ColorIndex", bubble.colorIndex);
            bubblesList.add(bubbleTag);
        }
        tag.put("SafeBubbles", bubblesList);
        
        CompoundTag statsTag = new CompoundTag();
        for (Map.Entry<UUID, PlayerStatsSaveData> entry : playerStats.entrySet()) {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putInt("Kills", entry.getValue().kills);
            playerTag.putInt("Deaths", entry.getValue().deaths);
            statsTag.put(entry.getKey().toString(), playerTag);
        }
        tag.put("PlayerStats", statsTag);

        ListTag gameStartPlayersList = new ListTag();
        for (UUID playerId : gameStartPlayerIds) {
            gameStartPlayersList.add(StringTag.valueOf(playerId.toString()));
        }
        tag.put("GameStartPlayerIds", gameStartPlayersList);
        
        return tag;
    }
    
    public void saveFromManager() {
        this.gameActive = GameEventManager.isGameActive();
        this.gamePaused = GameEventManager.isGamePaused();
        this.gameStartTick = GameEventManager.getGameStartTick();
        this.pausedTick = GameEventManager.getPausedTick();
        
        this.sporeSurgeTriggered = GameEventManager.isSporeSurgeTriggered();
        this.purificationRiftTriggered = GameEventManager.isPurificationRiftTriggered();
        this.endgameOverloadTriggered = GameEventManager.isEndgameOverloadTriggered();
        
        this.initialPlayerCount = GameEventManager.getInitialPlayerCount();
        this.totalEliminations = GameEventManager.getTotalEliminations();
        this.bountyTarget = GameEventManager.getBountyTarget();
        this.bountyKillReward = GameEventManager.getBountyKillReward();
        this.borderDamageMultiplier = GameEventManager.getBorderDamageMultiplier();
        this.sporeSurgeStartTick = GameEventManager.getSporeSurgeStartTick();
        
        this.safeBubbles.clear();
        for (GameEventManager.SafeBubble bubble : GameEventManager.getSafeBubbles()) {
            Vec3 pos = bubble.getPosition();
            this.safeBubbles.add(new BubbleSaveData(
                pos.x, pos.y, pos.z,
                bubble.getRadius(),
                bubble.getDurationTicks(),
                bubble.getColorIndex()
            ));
        }
        
        this.playerStats.clear();
        this.playerStats.putAll(GameEventManager.getPlayerStatsForSave());

        this.gameStartPlayerIds.clear();
        this.gameStartPlayerIds.addAll(GameEventManager.getGameStartPlayerIdsForSave());
        
        setDirty();
    }
    
    public void restoreToManager(ServerLevel level) {
        if (gameActive) {
            GameEventManager.restoreGameState(
                level,
                gameStartTick,
                pausedTick,
                gamePaused,
                sporeSurgeTriggered,
                purificationRiftTriggered,
                endgameOverloadTriggered,
                initialPlayerCount,
                totalEliminations,
                bountyTarget,
                bountyKillReward,
                borderDamageMultiplier,
                sporeSurgeStartTick,
                safeBubbles,
                playerStats,
                gameStartPlayerIds
            );
        }
    }
    
    public boolean isGameActive() { return gameActive; }
    
    private static final String DATA_NAME = "life_contract_game_data";
    
    public static GameDataStorage get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
            GameDataStorage::load,
            GameDataStorage::new,
            DATA_NAME
        );
    }
}
