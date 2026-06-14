package org.alku.life_contract.events;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.alku.life_contract.Life_contract;
import org.alku.life_contract.NetworkHandler;
import org.alku.life_contract.PlayerInfectionSystem;
import org.alku.life_contract.ContractEvents;
import org.alku.life_contract.SoulContractItem;
import org.alku.life_contract.TeamOrganizerItem;
import org.alku.life_contract.compat.CaerulaArborCompat;
import net.minecraftforge.network.PacketDistributor;

import java.util.*;

@Mod.EventBusSubscriber(modid = Life_contract.MODID)
public class GameEventManager {
    
    private static boolean gameActive = false;
    private static boolean gamePaused = false;
    private static long gameStartTick = 0;
    private static long pausedTick = 0;
    private static ServerLevel currentLevel = null;
    
    private static boolean sporeSurgeTriggered = false;
    private static boolean purificationRiftTriggered = false;
    private static boolean endgameOverloadTriggered = false;
    
    private static boolean sporeSurgeManuallyStopped = false;
    private static boolean purificationRiftManuallyStopped = false;
    private static boolean endgameOverloadManuallyStopped = false;
    
    private static int initialPlayerCount = 0;
    private static final Set<UUID> gameStartPlayerIds = new HashSet<>();
    
    private static int totalEliminations = 0;
    private static UUID bountyTarget = null;
    private static int bountyKillReward = 0;
    private static final Map<UUID, Integer> sporeRainExposureTicks = new HashMap<>();
    
    private static final List<SafeBubble> safeBubbles = new ArrayList<>();
    private static double borderDamageMultiplier = 1.0;
    
    private static long sporeSurgeStartTick = 0;
    private static final int SPORE_SURGE_DURATION_SECONDS = 45;
    
    private static final int SPORE_SURGE_MINUTE = 5;
    private static final int PURIFICATION_RIFT_MINUTE = 9;
    private static final int ENDGAME_PLAYER_COUNT = 3;
    private static final int ELIMINATION_THRESHOLD = 2;
    private static final double SAFE_BUBBLE_RADIUS = 15.0;
    
    private static final int RANDOM_EVENT_CHECK_INTERVAL = 300;
    private static final int RANDOM_EVENT_MIN_INTERVAL = 300;
    private static final double RANDOM_EVENT_CHANCE = 0.3;
    
    private static long lastRandomEventTick = 0;
    private static boolean sporeSurgeActive = false;
    private static boolean purificationRiftActive = false;
    private static boolean bountyActive = false;
    private static boolean sporeRainActive = false;
    private static long sporeRainStartTick = 0;
    private static final int SPORE_RAIN_DURATION_SECONDS = 60;
    private static final int SPORE_RAIN_INFECTION_INTERVAL_TICKS = 10;
    private static final int SPORE_RAIN_INFECTION_AMOUNT = 2;
    private static final int SPORE_RAIN_RECOVERY_INTERVAL_TICKS = 20;
    private static final int SPORE_RAIN_RECOVERY_AMOUNT = 1;
    
    private static final String[] ELITE_MOB_IDS = {
        "spore:knight", "spore:griefer", "spore:braiomil", "spore:leaper",
        "spore:slasher", "spore:spitter", "spore:howler", "spore:stalker",
        "spore:brute", "spore:scavenger"
    };
    
    public static class SafeBubble {
        private final Vec3 position;
        private final double radius;
        private int durationTicks;
        private final int colorIndex;
        
        public SafeBubble(Vec3 position, double radius, int durationTicks, int colorIndex) {
            this.position = position;
            this.radius = radius;
            this.durationTicks = durationTicks;
            this.colorIndex = colorIndex;
        }
        
        public Vec3 getPosition() { return position; }
        public double getRadius() { return radius; }
        public int getDurationTicks() { return durationTicks; }
        public int getColorIndex() { return colorIndex; }
        public void tick() { durationTicks--; }
        public boolean isExpired() { return durationTicks <= 0; }
        
        public boolean isInside(Vec3 pos) {
            return position.distanceTo(pos) <= radius;
        }
        
        public AABB getBounds() {
            return new AABB(
                position.x - radius, position.y - radius, position.z - radius,
                position.x + radius, position.y + radius, position.z + radius
            );
        }
    }
    
    public static void startGame(ServerLevel level, double centerX, double centerZ) {
        gameActive = true;
        gamePaused = false;
        gameStartTick = level.getGameTime();
        pausedTick = 0;
        currentLevel = level;
        
        sporeSurgeTriggered = false;
        purificationRiftTriggered = false;
        endgameOverloadTriggered = false;
        sporeSurgeManuallyStopped = false;
        purificationRiftManuallyStopped = false;
        endgameOverloadManuallyStopped = false;
        totalEliminations = 0;
        bountyTarget = null;
        bountyKillReward = 0;
        borderDamageMultiplier = 1.0;
        safeBubbles.clear();
        sporeSurgeStartTick = 0;
        initialPlayerCount = getSurvivalPlayerCount();
        gameStartPlayerIds.clear();
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            if (isActiveGamePlayer(player)) {
                gameStartPlayerIds.add(player.getUUID());
            }
        }
        
        WorldBorder worldBorder = level.getWorldBorder();
        worldBorder.setCenter(centerX, centerZ);
        worldBorder.setSize(600);
        
        org.alku.life_contract.border.BorderManager.createBorder(level, centerX, centerZ, 600);
        
        broadcastMessage(Component.literal("§6[游戏事件] §f游戏开始！事件系统已激活。初始玩家数: §e" + initialPlayerCount));
        broadcastMessage(Component.literal("§b[游戏事件] §f世界边界已收缩至 §e600x600 §f区域，中心: §a" + (int)centerX + ", " + (int)centerZ));
    }
    
    public static void startGame(ServerLevel level) {
        startGame(level, 0, 0);
    }
    
    public static void pauseGame() {
        if (!gameActive || gamePaused) return;
        
        gamePaused = true;
        pausedTick = currentLevel.getGameTime();
        broadcastMessage(Component.literal("§e[游戏事件] §f游戏已暂停。"));
    }
    
    public static void resumeGame() {
        if (!gameActive || !gamePaused) return;
        
        long currentTime = currentLevel.getGameTime();
        long pausedDuration = currentTime - pausedTick;
        gameStartTick += pausedDuration;
        gamePaused = false;
        
        broadcastMessage(Component.literal("§a[游戏事件] §f游戏已恢复。"));
    }
    
    public static void stopGame() {
        gameActive = false;
        gamePaused = false;
        currentLevel = null;
        safeBubbles.clear();
        bountyTarget = null;
        bountyKillReward = 0;
        totalEliminations = 0;
        borderDamageMultiplier = 1.0;
        sporeRainExposureTicks.clear();
        
        sporeSurgeTriggered = false;
        purificationRiftTriggered = false;
        endgameOverloadTriggered = false;
        sporeSurgeManuallyStopped = false;
        purificationRiftManuallyStopped = false;
        endgameOverloadManuallyStopped = false;
        sporeSurgeStartTick = 0;
        initialPlayerCount = 0;
        gameStartPlayerIds.clear();
        
        broadcastMessage(Component.literal("§c[游戏事件] §f游戏已结束，所有事件已清除。"));
    }
    
    public static boolean isGameActive() { return gameActive; }
    public static boolean isGamePaused() { return gamePaused; }
    public static boolean isPlayerPartOfGame(UUID playerUUID) { return gameStartPlayerIds.contains(playerUUID); }
    public static double getBorderDamageMultiplier() { return borderDamageMultiplier; }
    public static List<SafeBubble> getSafeBubbles() { return safeBubbles; }
    public static UUID getBountyTarget() { return bountyTarget; }
    
    public static Set<UUID> getGameStartPlayerIdsForSave() {
        return new HashSet<>(gameStartPlayerIds);
    }
    
    public static void handleLateJoinPlayer(ServerPlayer player) {
        if (!gameActive || isPlayerPartOfGame(player.getUUID())) {
            return;
        }

        player.setGameMode(GameType.SPECTATOR);
    }
    
    public static boolean joinSmallestTeam(ServerPlayer player) {
        if (!gameActive || currentLevel == null) {
            player.sendSystemMessage(Component.literal("§c[生灵契约] 游戏尚未开始。"));
            return false;
        }

        if (isPlayerPartOfGame(player.getUUID())) {
            player.sendSystemMessage(Component.literal("§e[生灵契约] 你已经是本局游戏玩家。"));
            return false;
        }

        ServerPlayer leader = findSmallestTeamLeader();
        if (leader == null) {
            player.sendSystemMessage(Component.literal("§c[生灵契约] 没有可加入的队伍。"));
            return false;
        }

        assignPlayerToLeaderTeam(player, leader);
        gameStartPlayerIds.add(player.getUUID());
        player.setGameMode(GameType.SURVIVAL);
        PlayerInfectionSystem.resetInfection(player);

        player.sendSystemMessage(Component.literal("§a[生灵契约] 你已加入人数最少的队伍：§e" + leader.getName().getString()));
        broadcastMessage(Component.literal("§a[生灵契约] §f" + player.getName().getString() + " §f加入了队伍 §e" + leader.getName().getString()));
        saveGameData();
        return true;
    }
    public static long getElapsedSeconds() {
        if (!gameActive || currentLevel == null) return 0;
        return (currentLevel.getGameTime() - gameStartTick) / 20;
    }
    
    public static boolean isSporeSurgeTriggered() { return sporeSurgeTriggered; }
    public static boolean isPurificationRiftTriggered() { return purificationRiftTriggered; }
    public static boolean isEndgameOverloadTriggered() { return endgameOverloadTriggered; }
    
    public static int getSporeSurgeRemainingSeconds() {
        if (!sporeSurgeTriggered || sporeSurgeStartTick == 0 || currentLevel == null) return 0;
        long elapsed = (currentLevel.getGameTime() - sporeSurgeStartTick) / 20;
        int remaining = SPORE_SURGE_DURATION_SECONDS - (int) elapsed;
        return Math.max(0, remaining);
    }
    
    public static boolean isSporeSurgeActive() {
        return sporeSurgeTriggered && getSporeSurgeRemainingSeconds() > 0;
    }
    
    public static int getSafeBubbleRemainingSeconds() {
        if (safeBubbles.isEmpty()) return 0;
        return safeBubbles.get(0).getDurationTicks() / 20;
    }
    
    public static int getSporeRainRemainingSeconds() {
        if (!sporeRainActive || sporeRainStartTick == 0 || currentLevel == null) return 0;
        long elapsed = (currentLevel.getGameTime() - sporeRainStartTick) / 20;
        int remaining = SPORE_RAIN_DURATION_SECONDS - (int) elapsed;
        return Math.max(0, remaining);
    }
    
    public static boolean hasActiveBounty() {
        return bountyTarget != null;
    }
    
    public static String getBountyTargetName() {
        if (bountyTarget == null || currentLevel == null) return null;
        ServerPlayer player = currentLevel.getServer().getPlayerList().getPlayer(bountyTarget);
        return player != null ? player.getName().getString() : null;
    }
    
    public static void forceTriggerSporeSurge(ServerLevel level) {
        if (level == null) return;
        if (currentLevel == null) {
            currentLevel = level;
        }
        sporeSurgeStartTick = level.getGameTime();
        sporeSurgeManuallyStopped = false;
        triggerSporeSurge();
        sporeSurgeTriggered = true;
    }
    
    public static void forceTriggerBountyHunt() {
        triggerBountyHunt();
    }
    
    public static void forceTriggerPurificationRift(ServerLevel level) {
        if (level == null) return;
        if (currentLevel == null) {
            currentLevel = level;
        }
        purificationRiftManuallyStopped = false;
        triggerPurificationRift();
        purificationRiftTriggered = true;
    }
    
    public static void forceTriggerEndgameOverload(ServerLevel level) {
        if (level == null) return;
        if (currentLevel == null) {
            currentLevel = level;
        }
        endgameOverloadManuallyStopped = false;
        triggerEndgameOverload();
        endgameOverloadTriggered = true;
    }
    
    public static void forceTriggerSporeRain(ServerLevel level) {
        if (level == null) return;
        if (currentLevel == null) {
            currentLevel = level;
        }
        long currentTick = level.getGameTime();
        triggerSporeRain(currentTick);
        sporeRainActive = true;
        broadcastMessage(Component.literal("§2[孢子雨] §f孢子雨事件已强制触发！"));
    }
    
    public static void stopSporeRain() {
        sporeRainActive = false;
        sporeRainStartTick = 0;
        sporeRainExposureTicks.clear();
        broadcastMessage(Component.literal("§a[游戏事件] §f孢子雨事件已停止。"));
    }
    
    public static void stopSporeSurge() {
        sporeSurgeTriggered = false;
        sporeSurgeStartTick = 0;
        sporeSurgeManuallyStopped = true;
        broadcastMessage(Component.literal("§a[游戏事件] §f孢潮推进事件已停止。"));
    }
    
    public static void stopPurificationRift() {
        purificationRiftTriggered = false;
        safeBubbles.clear();
        purificationRiftManuallyStopped = true;
        broadcastMessage(Component.literal("§a[游戏事件] §f净化裂隙事件已停止。"));
    }
    
    public static void stopEndgameOverload() {
        endgameOverloadTriggered = false;
        borderDamageMultiplier = 1.0;
        endgameOverloadManuallyStopped = true;
        broadcastMessage(Component.literal("§a[游戏事件] §f终局过载事件已停止。"));
    }
    
    public static void clearBounty() {
        bountyTarget = null;
        bountyKillReward = 0;
        broadcastMessage(Component.literal("§a[游戏事件] §f悬赏已清除。"));
    }
    
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!gameActive || currentLevel == null) return;
        if (gamePaused) return;
        
        long currentTick = currentLevel.getGameTime();
        long elapsedTicks = currentTick - gameStartTick;
        long elapsedSeconds = elapsedTicks / 20;
        
        if (!sporeSurgeTriggered && !sporeSurgeManuallyStopped && elapsedSeconds >= SPORE_SURGE_MINUTE * 60) {
            sporeSurgeStartTick = currentLevel.getGameTime();
            triggerSporeSurge();
            sporeSurgeTriggered = true;
            sporeSurgeActive = true;
        }
        
        if (!purificationRiftTriggered && !purificationRiftManuallyStopped && elapsedSeconds >= PURIFICATION_RIFT_MINUTE * 60) {
            triggerPurificationRift();
            purificationRiftTriggered = true;
            purificationRiftActive = true;
        }
        
        if (!endgameOverloadTriggered && !endgameOverloadManuallyStopped && initialPlayerCount > ENDGAME_PLAYER_COUNT) {
            int survivalPlayers = getSurvivalPlayerCount();
            if (survivalPlayers <= ENDGAME_PLAYER_COUNT && survivalPlayers > 0) {
                triggerEndgameOverload();
                endgameOverloadTriggered = true;
            }
        }
        
        if (currentTick % RANDOM_EVENT_CHECK_INTERVAL == 0 && 
            currentTick - lastRandomEventTick >= RANDOM_EVENT_MIN_INTERVAL) {
            tryTriggerRandomEvent(currentTick);
        }
        
        if (sporeSurgeActive && sporeSurgeStartTick > 0) {
            long surgeElapsed = (currentTick - sporeSurgeStartTick) / 20;
            if (surgeElapsed >= SPORE_SURGE_DURATION_SECONDS) {
                sporeSurgeActive = false;
                sporeSurgeTriggered = false;
                sporeSurgeManuallyStopped = false;
            }
        }
        
        if (purificationRiftActive && safeBubbles.isEmpty()) {
            purificationRiftActive = false;
            purificationRiftTriggered = false;
            purificationRiftManuallyStopped = false;
        }
        
        if (sporeRainActive && sporeRainStartTick > 0) {
            long rainElapsed = (currentTick - sporeRainStartTick) / 20;
            if (rainElapsed >= SPORE_RAIN_DURATION_SECONDS) {
                sporeRainActive = false;
                sporeRainStartTick = 0;
                sporeRainExposureTicks.clear();
                broadcastMessage(Component.literal("§2[孢子雨] §f孢子雨已停止！"));
            }
        }
        
        tickSporeRain(currentTick);
        
        tickSafeBubbles();
        
        if (currentLevel.getGameTime() % 4 == 0) {
            syncToAllClients();
        }
    }
    
    private static void tryTriggerRandomEvent(long currentTick) {
        if (currentLevel == null) return;
        
        Random random = new Random();
        if (random.nextDouble() > RANDOM_EVENT_CHANCE) return;
        
        List<String> availableEvents = new ArrayList<>();
        
        if (!sporeSurgeActive && !sporeSurgeManuallyStopped) {
            availableEvents.add("spore_surge");
        }
        if (!purificationRiftActive && !purificationRiftManuallyStopped) {
            availableEvents.add("purification_rift");
        }
        if (!bountyActive) {
            availableEvents.add("bounty");
        }
        if (!sporeRainActive) {
            availableEvents.add("spore_rain");
        }
        
        if (availableEvents.isEmpty()) return;
        
        String selectedEvent = availableEvents.get(random.nextInt(availableEvents.size()));
        
        switch (selectedEvent) {
            case "spore_surge":
                sporeSurgeStartTick = currentTick;
                triggerSporeSurge();
                sporeSurgeTriggered = true;
                sporeSurgeActive = true;
                broadcastMessage(Component.literal("§c[随机事件] §f孢潮推进事件已随机触发！"));
                break;
            case "purification_rift":
                triggerPurificationRift();
                purificationRiftTriggered = true;
                purificationRiftActive = true;
                broadcastMessage(Component.literal("§b[随机事件] §f净化裂隙事件已随机触发！"));
                break;
            case "bounty":
                triggerBountyHunt();
                bountyActive = true;
                broadcastMessage(Component.literal("§e[随机事件] §f悬赏事件已随机触发！"));
                break;
            case "spore_rain":
                triggerSporeRain(currentTick);
                sporeRainActive = true;
                broadcastMessage(Component.literal("§2[随机事件] §f孢子雨事件已随机触发！寻找遮蔽物！"));
                break;
        }
        
        lastRandomEventTick = currentTick;
    }
    
    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!gameActive || currentLevel == null) return;
        
        if (event.getEntity() instanceof ServerPlayer victim) {
            if (victim.gameMode.getGameModeForPlayer() != GameType.SURVIVAL && 
                victim.gameMode.getGameModeForPlayer() != GameType.ADVENTURE) {
                return;
            }

            int lifePoints = CaerulaArborCompat.getLifePoints(victim);
            if (lifePoints == 1) {
                victim.setGameMode(GameType.SPECTATOR);
                broadcastMessage(Component.literal("§c[出局] §f" + victim.getGameProfile().getName() + " 生命数耗尽，已出局。"));
            }
            
            totalEliminations++;
            
            if (event.getSource().getEntity() instanceof ServerPlayer killer) {
                handleKillReward(killer, victim);
            }
            
            if (totalEliminations % ELIMINATION_THRESHOLD == 0) {
                triggerBountyHunt();
            }
        }
    }
    
    private static void triggerSporeSurge() {
        broadcastMessage(Component.literal("§c[孢潮推进] §f第5分钟！感染精英正在涌入！"));
        
        WorldBorder worldBorder = currentLevel.getWorldBorder();
        if (worldBorder.getSize() <= 0) {
            return;
        }
        
        boolean sporeModLoaded = net.minecraftforge.fml.ModList.get().isLoaded("spore");
        
        if (!sporeModLoaded) {
            broadcastMessage(Component.literal("§e[孢潮推进] §f警告: Fungal Infection: Spore 模组未加载，使用备用怪物！"));
        }
        
        Random random = new Random();
        int spawnCount = 15 + random.nextInt(16);
        
        for (int i = 0; i < spawnCount; i++) {
            spawnRandomElite(worldBorder, random, sporeModLoaded);
        }
    }
    
    private static void spawnRandomElite(WorldBorder worldBorder, Random random, boolean sporeModLoaded) {
        double borderSize = worldBorder.getSize();
        double centerX = worldBorder.getCenterX();
        double centerZ = worldBorder.getCenterZ();
        
        double halfSize = borderSize / 2;
        double x = centerX + (random.nextDouble() * 2 - 1) * halfSize;
        double z = centerZ + (random.nextDouble() * 2 - 1) * halfSize;
        double y = currentLevel.getHeightmapPos(
            net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            new BlockPos((int)x, 0, (int)z)
        ).getY();
        
        BlockPos spawnPos = new BlockPos((int)x, (int)y, (int)z);
        
        Entity entity = null;
        
        if (sporeModLoaded) {
            String mobId = ELITE_MOB_IDS[random.nextInt(ELITE_MOB_IDS.length)];
            
            try {
                ResourceLocation resourceId = ResourceLocation.parse(mobId);
                EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(resourceId);
                
                if (entityType != null) {
                    entity = entityType.create(currentLevel);
                }
            } catch (Exception e) {
            }
        }
        
        if (entity == null) {
            EntityType<?> fallbackType = getFallbackEntityType(random);
            if (fallbackType != null) {
                entity = fallbackType.create(currentLevel);
                if (entity instanceof LivingEntity livingEntity) {
                    livingEntity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 600, 1, false, true));
                    livingEntity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 600, 1, false, true));
                    livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600, 0, false, true));
                }
            }
        }
        
        if (entity != null) {
            entity.setPos(x, y, z);
            currentLevel.addFreshEntity(entity);
            
            currentLevel.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR,
                x, y + 1, z, 30, 1, 1, 1, 0.1);
            currentLevel.playSound(null, x, y, z,
                SoundEvents.SPORE_BLOSSOM_BREAK, SoundSource.HOSTILE, 1.0f, 0.5f);
        }
    }
    
    private static EntityType<?> getFallbackEntityType(Random random) {
        EntityType<?>[] fallbackTypes = {
            EntityType.ZOMBIE,
            EntityType.SKELETON,
            EntityType.SPIDER,
            EntityType.ENDERMAN,
            EntityType.WITCH,
            EntityType.CREEPER
        };
        return fallbackTypes[random.nextInt(fallbackTypes.length)];
    }
    
    private static void triggerBountyHunt() {
        List<ServerPlayer> players = getSurvivalPlayers();
        if (players.isEmpty()) return;
        
        ServerPlayer topKD = null;
        double bestKD = -1;
        
        for (ServerPlayer player : players) {
            double kd = calculateKD(player);
            if (kd > bestKD) {
                bestKD = kd;
                topKD = player;
            }
        }
        
        if (topKD != null) {
            bountyTarget = topKD.getUUID();
            bountyKillReward = (int) topKD.getMaxHealth() + 2;
            
            broadcastMessage(Component.literal("§e[清道夫悬赏] §fK/D最高者 §c" + topKD.getName().getString() + 
                " §f已被标记！击杀奖励: §a+" + bountyKillReward + " §f最大生命值！"));
            
            topKD.sendSystemMessage(Component.literal("§c[清道夫悬赏] §f你已被标记为悬赏目标！"));
        }
    }
    
    private static void triggerSporeRain(long currentTick) {
        sporeRainStartTick = currentTick;
        sporeRainActive = true;
        sporeRainExposureTicks.clear();
        broadcastMessage(Component.literal("§2[孢子雨] §f孢子雨降临！暴露在天空下的玩家将增加感染值！"));
    }
    
    private static double calculateKD(ServerPlayer player) {
        PlayerStats stats = playerStatsMap.computeIfAbsent(player.getUUID(), k -> new PlayerStats());
        if (stats.deaths == 0) return stats.kills;
        return (double) stats.kills / stats.deaths;
    }
    
    private static void handleKillReward(ServerPlayer killer, ServerPlayer victim) {
        PlayerStats killerStats = playerStatsMap.computeIfAbsent(killer.getUUID(), k -> new PlayerStats());
        PlayerStats victimStats = playerStatsMap.computeIfAbsent(victim.getUUID(), k -> new PlayerStats());
        
        killerStats.kills++;
        victimStats.deaths++;
        
        if (bountyTarget != null && victim.getUUID().equals(bountyTarget)) {
            killer.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH)
                .addPermanentModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                    UUID.randomUUID(), "bounty_reward", bountyKillReward,
                    net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION));
            
            killer.sendSystemMessage(Component.literal("§a[清道夫悬赏] §f击杀悬赏目标！获得 §e+" + bountyKillReward + " §f最大生命值！"));
            broadcastMessage(Component.literal("§e[清道夫悬赏] §f悬赏目标已被击杀！"));
            
            bountyTarget = null;
            bountyKillReward = 0;
        }
    }
    
    private static void triggerPurificationRift() {
        broadcastMessage(Component.literal("§b[净化裂隙] §f第9分钟！安全气泡已生成！"));
        
        WorldBorder worldBorder = currentLevel.getWorldBorder();
        if (worldBorder.getSize() <= 0) return;
        
        double borderSize = worldBorder.getSize();
        double centerX = worldBorder.getCenterX();
        double centerZ = worldBorder.getCenterZ();
        double halfSize = borderSize / 2;
        
        Random random = new Random();
        
        broadcastMessage(Component.literal("§b[净化裂隙] §f安全气泡坐标："));
        
        ParticleOptions[] bubbleParticles = {
            ParticleTypes.END_ROD,
            ParticleTypes.TOTEM_OF_UNDYING,
            ParticleTypes.SOUL_FIRE_FLAME
        };
        
        String[] bubbleColors = {"§f白色", "§a绿色", "§b蓝色"};
        
        for (int i = 0; i < 3; i++) {
            double x = centerX + (random.nextDouble() - 0.5) * halfSize * 0.8;
            double z = centerZ + (random.nextDouble() - 0.5) * halfSize * 0.8;
            double y = currentLevel.getHeightmapPos(
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                new BlockPos((int)x, 0, (int)z)
            ).getY();
            
            SafeBubble bubble = new SafeBubble(new Vec3(x, y, z), SAFE_BUBBLE_RADIUS, 60 * 20, i);
            safeBubbles.add(bubble);
            
            broadcastMessage(Component.literal("§b  气泡" + (i + 1) + ": §fX:" + (int)x + " Y:" + (int)y + " Z:" + (int)z + " §7(半径: " + (int)SAFE_BUBBLE_RADIUS + "格, 颜色: " + bubbleColors[i] + "§7)"));
            
            currentLevel.sendParticles(bubbleParticles[i],
                x, y + 1, z, 50, 3, 2, 3, 0.05);
        }
    }
    
    private static void tickSporeRain(long currentTick) {
        for (ServerPlayer player : currentLevel.getPlayers(p -> true)) {
            if (player.isCreative() || player.isSpectator()) continue;
            
            UUID playerId = player.getUUID();
            boolean exposedToSporeRain = sporeRainActive && sporeRainStartTick > 0 && isPlayerExposedToRain(player);
            if (exposedToSporeRain) {
                int exposureTicks = sporeRainExposureTicks.getOrDefault(playerId, 0) + 1;
                sporeRainExposureTicks.put(playerId, exposureTicks);
                if (exposureTicks % SPORE_RAIN_INFECTION_INTERVAL_TICKS == 0) {
                    PlayerInfectionSystem.addInfection(player, SPORE_RAIN_INFECTION_AMOUNT);
                }
                if (currentTick % 200 == 0) {
                    player.sendSystemMessage(Component.literal("§2[孢子雨] §f你暴露在孢子雨中！感染值正在增加！"));
                }
            } else if (currentTick % SPORE_RAIN_RECOVERY_INTERVAL_TICKS == 0 && PlayerInfectionSystem.getInfection(player) > 0) {
                sporeRainExposureTicks.remove(playerId);
                PlayerInfectionSystem.addInfection(player, -SPORE_RAIN_RECOVERY_AMOUNT);
            } else {
                sporeRainExposureTicks.remove(playerId);
            }
        }
        
        if (sporeRainActive && currentTick % 2 == 0) {
            spawnSporeRainParticles();
        }
    }
    
    private static boolean isPlayerExposedToRain(ServerPlayer player) {
        BlockPos pos = player.blockPosition();
        return currentLevel.canSeeSky(pos);
    }
    
    private static void spawnSporeRainParticles() {
        for (ServerPlayer player : currentLevel.getPlayers(p -> true)) {
            if (player.isCreative() || player.isSpectator()) continue;
            
            Random random = new Random();
            for (int i = 0; i < 5; i++) {
                double x = player.getX() + (random.nextDouble() - 0.5) * 20;
                double z = player.getZ() + (random.nextDouble() - 0.5) * 20;
                double y = player.getY() + 8 + random.nextDouble() * 5;
                
                currentLevel.sendParticles(ParticleTypes.CRIMSON_SPORE,
                    x, y, z, 3, 0.5, 0, 0.5, 0.02);
                currentLevel.sendParticles(ParticleTypes.WARPED_SPORE,
                    x + random.nextDouble(), y, z + random.nextDouble(), 3, 0.5, 0, 0.5, 0.02);
                currentLevel.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR,
                    x, y - 2, z, 2, 0.3, 0, 0.3, 0.01);
            }
        }
    }
    
    private static void tickSafeBubbles() {
        Iterator<SafeBubble> iterator = safeBubbles.iterator();
        
        while (iterator.hasNext()) {
            SafeBubble bubble = iterator.next();
            bubble.tick();
            
            if (bubble.isExpired()) {
                iterator.remove();
                continue;
            }
            
            AABB bounds = bubble.getBounds();
            List<ServerPlayer> playersInBubble = currentLevel.getEntitiesOfClass(
                ServerPlayer.class, bounds);
            
            for (ServerPlayer player : playersInBubble) {
                if (player.gameMode.getGameModeForPlayer() == GameType.SURVIVAL ||
                    player.gameMode.getGameModeForPlayer() == GameType.ADVENTURE) {
                    
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 1, false, true));
                    player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 60, 2, false, true));
                }
            }
            
            if (currentLevel.getGameTime() % 5 == 0) {
                drawBubbleOutline(bubble);
            }
        }
    }
    
    private static void drawBubbleOutline(SafeBubble bubble) {
        Vec3 center = bubble.getPosition();
        double radius = bubble.getRadius();
        int colorIndex = bubble.getColorIndex();
        
        ParticleOptions[] bubbleParticles = {
            ParticleTypes.END_ROD,
            ParticleTypes.TOTEM_OF_UNDYING,
            ParticleTypes.SOUL_FIRE_FLAME
        };
        
        ParticleOptions particleType = bubbleParticles[colorIndex % bubbleParticles.length];
        
        int points = 36;
        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points;
            double x = center.x + radius * Math.cos(angle);
            double z = center.z + radius * Math.sin(angle);
            double y = center.y + 1;
            
            currentLevel.sendParticles(particleType,
                x, y, z, 1, 0, 0, 0, 0);
        }
        
        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points;
            double x = center.x + radius * Math.cos(angle);
            double z = center.z + radius * Math.sin(angle);
            double y = center.y + 5;
            
            currentLevel.sendParticles(particleType,
                x, y, z, 1, 0, 0, 0, 0);
        }
        
        int verticalPoints = 8;
        for (int i = 0; i <= verticalPoints; i++) {
            double y = center.y + (i * 10.0 / verticalPoints);
            
            double angle1 = 0;
            double angle2 = Math.PI / 2;
            double angle3 = Math.PI;
            double angle4 = 3 * Math.PI / 2;
            
            for (double angle : new double[]{angle1, angle2, angle3, angle4}) {
                double x = center.x + radius * Math.cos(angle);
                double z = center.z + radius * Math.sin(angle);
                
                currentLevel.sendParticles(particleType,
                    x, y, z, 1, 0, 0, 0, 0);
            }
        }
        
        currentLevel.sendParticles(ParticleTypes.HEART,
            center.x, center.y + 1, center.z,
            1, 0, 0, 0, 0);
    }
    
    private static void triggerEndgameOverload() {
        broadcastMessage(Component.literal("§4[终局过载] §f剩余3人！感染升级，缩圈伤害翻倍！"));
        
        borderDamageMultiplier = 2.0;
        
        List<ServerPlayer> players = getSurvivalPlayers();
        for (ServerPlayer player : players) {
            player.addEffect(new MobEffectInstance(MobEffects.WITHER, 600, 1, false, true));
            player.sendSystemMessage(Component.literal("§c[终局过载] §f感染已升级！"));
        }
    }
    
    private static List<ServerPlayer> getSurvivalPlayers() {
        if (currentLevel == null) return new ArrayList<>();
        
        return currentLevel.getPlayers(GameEventManager::isActiveGamePlayer);
    }
    
    private static int getSurvivalPlayerCount() {
        return getSurvivalPlayers().size();
    }
    
    private static boolean isActiveGamePlayer(ServerPlayer player) {
        GameType gameType = player.gameMode.getGameModeForPlayer();
        return gameType == GameType.SURVIVAL || gameType == GameType.ADVENTURE;
    }
    
    private static ServerPlayer findSmallestTeamLeader() {
        if (currentLevel == null) {
            return null;
        }

        Map<UUID, List<ServerPlayer>> teams = new HashMap<>();
        Map<UUID, ServerPlayer> leaders = new HashMap<>();
        for (ServerPlayer player : currentLevel.getServer().getPlayerList().getPlayers()) {
            if (!isPlayerPartOfGame(player.getUUID()) || !isActiveGamePlayer(player)) {
                continue;
            }

            UUID leaderUUID = ContractEvents.getLeaderUUID(player);
            if (leaderUUID == null) {
                leaderUUID = player.getUUID();
            }

            teams.computeIfAbsent(leaderUUID, key -> new ArrayList<>()).add(player);
            if (player.getUUID().equals(leaderUUID)) {
                leaders.put(leaderUUID, player);
            }
        }

        ServerPlayer bestLeader = null;
        int bestSize = Integer.MAX_VALUE;
        List<Map.Entry<UUID, List<ServerPlayer>>> entries = new ArrayList<>(teams.entrySet());
        Collections.shuffle(entries);
        for (Map.Entry<UUID, List<ServerPlayer>> entry : entries) {
            ServerPlayer leader = leaders.get(entry.getKey());
            if (leader == null) {
                leader = currentLevel.getServer().getPlayerList().getPlayer(entry.getKey());
            }
            if (leader == null) {
                continue;
            }

            int size = entry.getValue().size();
            if (size < bestSize) {
                bestSize = size;
                bestLeader = leader;
            }
        }

        return bestLeader;
    }

    private static void assignPlayerToLeaderTeam(ServerPlayer player, ServerPlayer leader) {
        UUID leaderUUID = leader.getUUID();
        player.getPersistentData().putUUID(TeamOrganizerItem.TAG_LEADER_UUID, leaderUUID);
        player.getPersistentData().putString(TeamOrganizerItem.TAG_LEADER_NAME, leader.getName().getString());

        int teamNumber = leader.getPersistentData().contains(TeamOrganizerItem.TAG_TEAM_NUMBER)
            ? leader.getPersistentData().getInt(TeamOrganizerItem.TAG_TEAM_NUMBER)
            : Math.abs(leaderUUID.hashCode() % 9999) + 1;
        leader.getPersistentData().putInt(TeamOrganizerItem.TAG_TEAM_NUMBER, teamNumber);
        player.getPersistentData().putInt(TeamOrganizerItem.TAG_TEAM_NUMBER, teamNumber);

        String leaderMod = leader.getPersistentData().getString(SoulContractItem.TAG_CONTRACT_MOD);
        if (!leaderMod.isEmpty()) {
            player.getPersistentData().putString(SoulContractItem.TAG_CONTRACT_MOD, leaderMod);
        } else {
            player.getPersistentData().remove(SoulContractItem.TAG_CONTRACT_MOD);
        }

        ContractEvents.syncData(leader);
        ContractEvents.syncData(player);
    }
    
    private static void broadcastMessage(Component message) {
        if (currentLevel == null) return;
        
        for (ServerPlayer player : currentLevel.getPlayers(p -> true)) {
            player.sendSystemMessage(message);
        }
    }
    
    private static class PlayerStats {
        int kills = 0;
        int deaths = 0;
    }
    
    private static final Map<UUID, PlayerStats> playerStatsMap = new HashMap<>();
    
    public static void syncToAllClients() {
        if (currentLevel == null) return;
        
        List<PacketSyncEvents.BubbleData> bubbleDataList = new ArrayList<>();
        for (SafeBubble bubble : safeBubbles) {
            Vec3 pos = bubble.getPosition();
            bubbleDataList.add(new PacketSyncEvents.BubbleData(
                (int) pos.x, (int) pos.y, (int) pos.z, bubble.getRadius(), bubble.getColorIndex()
            ));
        }

        WorldBorder border = currentLevel.getWorldBorder();
        List<PacketSyncEvents.PlayerPosData> playerPosList = new ArrayList<>();
        UUID bountyUUID = null;
        int bountyX = 0;
        int bountyZ = 0;
        for (ServerPlayer p : currentLevel.getPlayers(p -> !p.isSpectator())) {
            UUID leader = ContractEvents.getLeaderUUID(p);
            playerPosList.add(new PacketSyncEvents.PlayerPosData(
                p.getUUID(),
                p.getGameProfile().getName(),
                leader,
                (int) p.getX(),
                (int) p.getZ(),
                p.getYRot(),
                CaerulaArborCompat.getLifePoints(p)
            ));
            if (bountyTarget != null && bountyTarget.equals(p.getUUID())) {
                bountyUUID = bountyTarget;
                bountyX = (int) p.getX();
                bountyZ = (int) p.getZ();
            }
        }
        
        PacketSyncEvents packet = new PacketSyncEvents(
            gameActive,
            isSporeSurgeActive(),
            getSporeSurgeRemainingSeconds(),
            purificationRiftTriggered && !safeBubbles.isEmpty(),
            getSafeBubbleRemainingSeconds(),
            bubbleDataList,
            bountyTarget != null,
            getBountyTargetName(),
            endgameOverloadTriggered,
            sporeRainActive,
            getSporeRainRemainingSeconds(),
            border.getCenterX(),
            border.getCenterZ(),
            border.getSize(),
            playerPosList,
            bountyUUID,
            bountyX,
            bountyZ
        );
        
        NetworkHandler.CHANNEL.send(PacketDistributor.ALL.noArg(), packet);
    }
    
    public static long getGameStartTick() { return gameStartTick; }
    public static long getPausedTick() { return pausedTick; }
    public static int getInitialPlayerCount() { return initialPlayerCount; }
    public static int getTotalEliminations() { return totalEliminations; }
    public static int getBountyKillReward() { return bountyKillReward; }
    public static long getSporeSurgeStartTick() { return sporeSurgeStartTick; }
    
    public static Map<UUID, GameDataStorage.PlayerStatsSaveData> getPlayerStatsForSave() {
        Map<UUID, GameDataStorage.PlayerStatsSaveData> result = new HashMap<>();
        for (Map.Entry<UUID, PlayerStats> entry : playerStatsMap.entrySet()) {
            result.put(entry.getKey(), new GameDataStorage.PlayerStatsSaveData(
                entry.getValue().kills,
                entry.getValue().deaths
            ));
        }
        return result;
    }
    
    public static void restoreGameState(ServerLevel level, long startTick, long paused, boolean pausedState,
                                         boolean sporeTriggered, boolean purifTriggered, boolean endgameTriggered,
                                         int initPlayers, int eliminations, UUID bounty, int bountyReward,
                                         double borderMult, long sporeStart, 
                                         List<GameDataStorage.BubbleSaveData> savedBubbles,
                                         Map<UUID, GameDataStorage.PlayerStatsSaveData> savedStats,
                                         Set<UUID> savedGameStartPlayerIds) {
        currentLevel = level;
        gameActive = true;
        gameStartTick = startTick;
        pausedTick = paused;
        gamePaused = pausedState;
        
        sporeSurgeTriggered = sporeTriggered;
        purificationRiftTriggered = purifTriggered;
        endgameOverloadTriggered = endgameTriggered;
        
        initialPlayerCount = initPlayers;
        totalEliminations = eliminations;
        bountyTarget = bounty;
        bountyKillReward = bountyReward;
        borderDamageMultiplier = borderMult;
        sporeSurgeStartTick = sporeStart;
        gameStartPlayerIds.clear();
        if (savedGameStartPlayerIds != null) {
            gameStartPlayerIds.addAll(savedGameStartPlayerIds);
        }
        
        safeBubbles.clear();
        for (GameDataStorage.BubbleSaveData data : savedBubbles) {
            safeBubbles.add(new SafeBubble(
                new Vec3(data.x, data.y, data.z),
                data.radius,
                data.durationTicks,
                data.colorIndex
            ));
        }
        
        playerStatsMap.clear();
        for (Map.Entry<UUID, GameDataStorage.PlayerStatsSaveData> entry : savedStats.entrySet()) {
            PlayerStats stats = new PlayerStats();
            stats.kills = entry.getValue().kills;
            stats.deaths = entry.getValue().deaths;
            playerStatsMap.put(entry.getKey(), stats);
        }
        
        broadcastMessage(Component.literal("§a[游戏事件] §f游戏状态已恢复！"));
    }
    
    public static void saveGameData() {
        if (currentLevel == null) return;
        
        GameDataStorage storage = GameDataStorage.get(currentLevel);
        storage.saveFromManager();
    }
    
    public static void loadGameData(ServerLevel level) {
        GameDataStorage storage = GameDataStorage.get(level);
        if (storage.isGameActive()) {
            storage.restoreToManager(level);
        }
    }
}
