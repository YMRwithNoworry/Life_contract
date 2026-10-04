package org.alku.life_contract.events;
import net.neoforged.fml.common.EventBusSubscriber;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import org.alku.life_contract.ContractEvents;
import org.alku.life_contract.Life_contract;
import org.alku.life_contract.NetworkHandler;
import org.alku.life_contract.ModPoolConfig;
import org.alku.life_contract.PerfProfiler;
import org.alku.life_contract.SoulContractItem;
import org.alku.life_contract.TeamOrganizerItem;
import org.alku.life_contract.border.BorderManager;
import org.alku.life_contract.endgame.StrongholdEndgameManager;
import org.alku.life_contract.world.SurfaceFinder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

@EventBusSubscriber(modid = Life_contract.MODID)
public final class GameEventManager {
    private static final long INITIAL_DEBUFF_PROTECTION_SECONDS = 120L;
    private static final long GAME_START_DAMAGE_PROTECTION_SECONDS = 10L;
    /** 开局迁移时参赛玩家相对边界中心的最大偏移，必须小于边界半边长（600/2=300）。 */
    private static final double GAME_START_RELOCATION_RADIUS = 240.0D;
    private static boolean gameActive;
    private static boolean gamePaused;
    private static long gameStartTick;
    private static long pausedTick;
    private static ServerLevel currentLevel;
    private static final Set<UUID> gameStartPlayerIds = new HashSet<>();
    private static final Map<UUID, UUID> gamePlayerTeams = new LinkedHashMap<>();
    private static final Map<UUID, String> gamePlayerNames = new LinkedHashMap<>();
    private static final Map<UUID, Boolean> gamePlayerActive = new LinkedHashMap<>();
    private static final Map<UUID, Integer> gameTeamNumbers = new LinkedHashMap<>();
    private static final Map<UUID, Integer> lastSyncedLifePoints = new LinkedHashMap<>();
    private static int initialTeamCount;
    /** 已经发放过的存活里程碑次数（每局重置）。 */
    private static int survivalMilestonesAwarded;

    private GameEventManager() {
    }

    public static StartResult startGame(ServerLevel level, double centerX, double centerZ) {
        ServerLevel gameLevel = level.getServer().overworld();
        StartResult validation = validateRandomTeams(gameLevel);
        if (!validation.success()) {
            return validation;
        }

        BlockPos searchOrigin = level == gameLevel
                ? BlockPos.containing(centerX, 0.0D, centerZ)
                : gameLevel.getSharedSpawnPos();
        StrongholdEndgameManager.PreparationResult portal =
                StrongholdEndgameManager.prepareForGame(gameLevel, searchOrigin);
        if (!portal.success()) {
            return new StartResult(false, validation.players(), validation.teams(), portal.message());
        }

        StartResult allocation = assignRandomTeams(gameLevel);
        if (!allocation.success()) {
            StrongholdEndgameManager.clearSession();
            return allocation;
        }

        currentLevel = gameLevel;
        gameActive = true;
        gamePaused = false;
        gameStartTick = gameLevel.getGameTime();
        pausedTick = 0;
        gameStartPlayerIds.clear();
        gamePlayerTeams.clear();
        gamePlayerNames.clear();
        gamePlayerActive.clear();
        gameTeamNumbers.clear();
        lastSyncedLifePoints.clear();
        survivalMilestonesAwarded = 0;
        org.alku.life_contract.items.SublimationDropHandler.resetFirstBlood();

        for (ServerPlayer player : gameLevel.getServer().getPlayerList().getPlayers()) {
            if (player.gameMode.getGameModeForPlayer() == GameType.SURVIVAL
                    || player.gameMode.getGameModeForPlayer() == GameType.ADVENTURE) {
                registerParticipant(player);
            }
        }
        initialTeamCount = new HashSet<>(gamePlayerTeams.values()).size();

        // 边界中心必须与末地要塞传送门一致：终局要求传送门始终留在缩圈范围内，
        // 否则缩圈一开始传送门就落到圈外，玩家永远打不到终局。
        BlockPos portalCenter = portal.portalCenter();
        double borderCenterX = portalCenter != null ? portalCenter.getX() + 0.5D : centerX;
        double borderCenterZ = portalCenter != null ? portalCenter.getZ() + 0.5D : centerZ;
        BlockPos borderCenter = BlockPos.containing(borderCenterX, 0.0D, borderCenterZ);
        BorderManager.startGameBorder(gameLevel, borderCenterX, borderCenterZ);
        relocateParticipantsInsideBorder(gameLevel, centerX, centerZ, borderCenter);
        WorldEventManager.startGame(gameLevel, gameStartPlayerIds.size());
        org.alku.life_contract.WaypointSync.broadcast();
        syncToAllClients();
        return new StartResult(
                true,
                allocation.players(),
                allocation.teams(),
                allocation.message() + "；" + portal.message()
                        + "；边界已以末地要塞传送门为中心设置为 600×600，参赛玩家已迁入边界内");
    }

    private static void relocateParticipantsInsideBorder(ServerLevel level, double previousCenterX,
                                                         double previousCenterZ, BlockPos borderCenter) {
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            if (!gameStartPlayerIds.contains(player.getUUID())) {
                continue;
            }

            double offsetX = Math.max(-GAME_START_RELOCATION_RADIUS,
                    Math.min(GAME_START_RELOCATION_RADIUS, player.getX() - previousCenterX));
            double offsetZ = Math.max(-GAME_START_RELOCATION_RADIUS,
                    Math.min(GAME_START_RELOCATION_RADIUS, player.getZ() - previousCenterZ));
            int targetX = (int) Math.floor(borderCenter.getX() + offsetX);
            int targetZ = (int) Math.floor(borderCenter.getZ() + offsetZ);
            // 地表必须用 SurfaceFinder：它会先加载区块、再用真实方块校验高度图，
            // 否则区块未加载或旧存档高度基准变化时，平台会被建到地底或天上
            int targetY = Math.min(level.getMaxBuildHeight() - 2,
                    SurfaceFinder.findSurfaceY(level, targetX, targetZ) + 1);
            buildSpawnPlatform(level, targetX, targetY - 1, targetZ);
            clearSpawnSpace(level, targetX, targetY, targetZ);
            player.teleportTo(level,
                    targetX + 0.5D, targetY, targetZ + 0.5D,
                    Collections.emptySet(), player.getYRot(), player.getXRot());
            player.setDeltaMovement(Vec3.ZERO);
            player.fallDistance = 0.0F;
            player.clearFire();
        }
    }

    private static void clearSpawnSpace(ServerLevel level, int x, int y, int z) {
        BlockPos.MutableBlockPos position = new BlockPos.MutableBlockPos();
        for (int offsetY = 0; offsetY < 3 && y + offsetY < level.getMaxBuildHeight(); offsetY++) {
            position.set(x, y + offsetY, z);
            if (!level.getBlockState(position).isAir()) {
                level.setBlockAndUpdate(position, Blocks.AIR.defaultBlockState());
            }
        }
    }

    private static void buildSpawnPlatform(ServerLevel level, int centerX, int platformY, int centerZ) {
        BlockPos.MutableBlockPos platformPos = new BlockPos.MutableBlockPos();
        for (int offsetX = -2; offsetX <= 2; offsetX++) {
            for (int offsetZ = -2; offsetZ <= 2; offsetZ++) {
                platformPos.set(centerX + offsetX, platformY, centerZ + offsetZ);
                level.setBlockAndUpdate(platformPos, Blocks.COBBLESTONE.defaultBlockState());
            }
        }
    }

    private static StartResult validateRandomTeams(ServerLevel level) {
        int playerCount = (int) level.getServer().getPlayerList().getPlayers().stream()
                .filter(player -> player.gameMode.getGameModeForPlayer() == GameType.SURVIVAL
                        || player.gameMode.getGameModeForPlayer() == GameType.ADVENTURE)
                .count();
        if (playerCount == 0) {
            return new StartResult(false, 0, 0, "没有可参与游戏的玩家");
        }

        int loadedModCount = ModPoolConfig.getLoadedModPool().size();
        if (loadedModCount == 0) {
            return new StartResult(false, playerCount, 0,
                    "感染模组池中没有已加载模组，请在模组配置页面勾选至少一个模组");
        }

        int teamCount = Math.min(playerCount, Math.min(ModPoolConfig.getTeamCount(), loadedModCount));
        return new StartResult(true, playerCount, teamCount, "");
    }

    private static StartResult assignRandomTeams(ServerLevel level) {
        List<ServerPlayer> players = new ArrayList<>(level.getServer().getPlayerList().getPlayers().stream()
                .filter(player -> player.gameMode.getGameModeForPlayer() == GameType.SURVIVAL
                        || player.gameMode.getGameModeForPlayer() == GameType.ADVENTURE)
                .toList());
        if (players.isEmpty()) return new StartResult(false, 0, 0, "没有可参与游戏的玩家");

        List<String> pool = new ArrayList<>(ModPoolConfig.getLoadedModPool());
        if (pool.isEmpty()) {
            return new StartResult(false, players.size(), 0,
                    "感染模组池中没有已加载模组，请在模组配置页面勾选至少一个模组");
        }

        Collections.shuffle(players);
        Collections.shuffle(pool);
        int teamCount = Math.min(players.size(), Math.min(ModPoolConfig.getTeamCount(), pool.size()));
        for (int index = 0; index < players.size(); index++) {
            ServerPlayer player = players.get(index);
            ServerPlayer leader = players.get(index % teamCount);
            String modId = pool.get(index % teamCount);
            player.getPersistentData().putUUID(TeamOrganizerItem.TAG_LEADER_UUID, leader.getUUID());
            player.getPersistentData().putString(TeamOrganizerItem.TAG_LEADER_NAME, leader.getName().getString());
            player.getPersistentData().putInt(TeamOrganizerItem.TAG_TEAM_NUMBER, index % teamCount + 1);
            player.getPersistentData().putString(SoulContractItem.TAG_CONTRACT_MOD, modId);
            ContractEvents.syncData(player);
        }
        return new StartResult(true, players.size(), teamCount,
                "已将 " + players.size() + " 名玩家随机分为 " + teamCount + " 队，并分配不同感染模组");
    }

    public record StartResult(boolean success, int players, int teams, String message) {}

    public static void pauseGame() {
        if (gameActive && !gamePaused && currentLevel != null) {
            gamePaused = true;
            pausedTick = currentLevel.getGameTime();
            syncToAllClients();
        }
    }

    public static void resumeGame() {
        if (gameActive && gamePaused && currentLevel != null) {
            gameStartTick += currentLevel.getGameTime() - pausedTick;
            gamePaused = false;
            pausedTick = 0;
            syncToAllClients();
        }
    }

    public static void stopGame() {
        gameActive = false;
        gamePaused = false;
        gameStartTick = 0;
        pausedTick = 0;
        for (ServerPlayer player : gameStartPlayerIds.stream()
                .map(uuid -> currentLevel != null ? currentLevel.getServer().getPlayerList().getPlayer(uuid) : null)
                .filter(java.util.Objects::nonNull)
                .toList()) {
            org.alku.life_contract.PlayerLivesSystem.clearLives(player);
        }
        gameStartPlayerIds.clear();
        gamePlayerTeams.clear();
        gamePlayerNames.clear();
        gamePlayerActive.clear();
        gameTeamNumbers.clear();
        initialTeamCount = 0;
        lastSyncedLifePoints.clear();
        BorderManager.stopShrink();
        WorldEventManager.reset();
        org.alku.life_contract.WaypointSync.clear();
        syncToAllClients();
        StrongholdEndgameManager.clearSession();
        currentLevel = null;
    }

    /** 终局 Boss 被击杀：击杀者所属队伍直接获胜。 */
    public static boolean declareDragonWinner(ServerPlayer winner, net.minecraft.resources.ResourceLocation bossId) {
        if (!gameActive || currentLevel == null || winner.getServer() == null
                || !gameStartPlayerIds.contains(winner.getUUID())) {
            return false;
        }

        // spore 的朽翼魔注册名是 spore:verfall（不是 spore:verfalldrache），按命名空间判断更稳
        String bossName = bossId != null && "spore".equals(bossId.getNamespace())
                ? "朽翼魔 Verfalldrache"
                : "诡异末影龙";
        UUID teamId = gamePlayerTeams.get(winner.getUUID());
        return declareTeamWinner(
                teamId,
                "§e" + winner.getName().getString() + " §f击杀了终局 Boss §d" + bossName,
                "§e" + winner.getName().getString() + " §f击杀了 §d" + bossName);
    }

    private static boolean declareLastStandingTeam(UUID teamId) {
        return declareTeamWinner(
                teamId,
                "§f其他队伍均已淘汰",
                "§f成为最后存活的队伍");
    }

    private static boolean declareTeamWinner(UUID teamId, String resultText, String subtitleText) {
        if (!gameActive || currentLevel == null || teamId == null) {
            return false;
        }

        int teamNumber = gameTeamNumbers.getOrDefault(teamId, 0);
        String teamName = teamNumber > 0 ? "第 " + teamNumber + " 队" : "获胜阵营";
        String memberNames = gamePlayerTeams.entrySet().stream()
                .filter(entry -> teamId.equals(entry.getValue()))
                .map(Map.Entry::getKey)
                .map(playerId -> gamePlayerNames.getOrDefault(playerId, playerId.toString()))
                .collect(Collectors.joining("、"));
        Component announcement = Component.literal(
                "§6§l[游戏结束] " + resultText + "，§6" + teamName + " §f获得胜利！队员：§b" + memberNames);
        currentLevel.getServer().getPlayerList().broadcastSystemMessage(announcement, false);

        Component title = Component.literal("§6§l" + teamName + "胜利");
        Component subtitle = Component.literal(subtitleText);
        for (ServerPlayer player : currentLevel.getServer().getPlayerList().getPlayers()) {
            player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 100, 20));
            player.connection.send(new ClientboundSetTitleTextPacket(title));
            player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
            player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,
                    SoundSource.MASTER, 1.0F, 1.0F);
        }

        stopGame();
        return true;
    }

    private static void registerParticipant(ServerPlayer player) {
        UUID playerId = player.getUUID();
        UUID teamId = ContractEvents.getLeaderUUID(player);
        if (teamId == null) {
            teamId = playerId;
        }
        // 参赛（含中途入队）玩家的命数统一重置为默认 5 条
        org.alku.life_contract.PlayerLivesSystem.resetLives(player);
        gameStartPlayerIds.add(playerId);
        gamePlayerTeams.put(playerId, teamId);
        gamePlayerNames.put(playerId, player.getName().getString());
        gamePlayerActive.put(playerId, player.gameMode.getGameModeForPlayer() != GameType.SPECTATOR);
        gameTeamNumbers.putIfAbsent(teamId,
                player.getPersistentData().getInt(TeamOrganizerItem.TAG_TEAM_NUMBER));
        player.getActiveEffects().stream()
                .filter(instance -> instance.getEffect().value().getCategory() == MobEffectCategory.HARMFUL)
                .map(instance -> instance.getEffect())
                .toList()
                .forEach(player::removeEffect);
    }

    /**
     * 存活里程碑：每 {@link org.alku.life_contract.market.SublimationRewards#SURVIVAL_INTERVAL_SECONDS} 秒
     * 给还在场上的参赛玩家发一次升华，作为稳定的基础收入。
     * <p>
     * 有了它，就算一局里没怎么打架，玩家也能攒到买得起补给与弹药的钱，
     * 商店不会因为"没钱"而变成摆设。
     */
    private static void awardSurvivalMilestone() {
        int reached = (int) (getElapsedSeconds()
                / org.alku.life_contract.market.SublimationRewards.SURVIVAL_INTERVAL_SECONDS);
        if (reached <= survivalMilestonesAwarded) {
            return;
        }
        survivalMilestonesAwarded = reached;
        awardParticipants(org.alku.life_contract.market.SublimationRewards.SURVIVAL_REWARD,
                "gui.life_contract.shop.reason.survival");
    }

    /** 给所有"还在场上"的参赛玩家发升华。 */
    private static void awardParticipants(int amount, String reasonKey) {
        if (currentLevel == null) {
            return;
        }
        for (UUID id : gameStartPlayerIds) {
            ServerPlayer player = currentLevel.getServer().getPlayerList().getPlayer(id);
            if (player == null || player.isSpectator() || player.isCreative()) {
                continue;
            }
            org.alku.life_contract.market.SublimationRewards.award(player, amount, reasonKey);
        }
    }

    private static void checkLastTeamStanding() {
        if (initialTeamCount < 2 || currentLevel == null
                || currentLevel.getGameTime() - gameStartTick < 100L) {
            return;
        }

        for (ServerPlayer player : currentLevel.getServer().getPlayerList().getPlayers()) {
            if (gameStartPlayerIds.contains(player.getUUID())) {
                gamePlayerActive.put(player.getUUID(),
                        player.gameMode.getGameModeForPlayer() != GameType.SPECTATOR);
            }
        }

        Set<UUID> activeTeams = gamePlayerTeams.entrySet().stream()
                .filter(entry -> gamePlayerActive.getOrDefault(entry.getKey(), false))
                .map(Map.Entry::getValue)
                .collect(Collectors.toSet());
        if (activeTeams.size() == 1) {
            declareLastStandingTeam(activeTeams.iterator().next());
        }
    }

    public static boolean isGameActive() {
        return gameActive;
    }

    public static boolean isGamePaused() {
        return gamePaused;
    }

    public static boolean isPlayerPartOfGame(UUID playerUUID) {
        return gameStartPlayerIds.contains(playerUUID);
    }

    public static boolean hasInitialDebuffProtection(UUID playerUUID) {
        return gameActive
                && gameStartPlayerIds.contains(playerUUID)
                && getElapsedSeconds() < INITIAL_DEBUFF_PROTECTION_SECONDS;
    }

    @SubscribeEvent
    public static void onGameStartDamageProtection(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer player
                && gameActive
                && gameStartPlayerIds.contains(player.getUUID())
                && getElapsedSeconds() < GAME_START_DAMAGE_PROTECTION_SECONDS) {
            event.setCanceled(true);
        }
    }

    public static long getElapsedSeconds() {
        if (!gameActive || currentLevel == null) return 0;
        long endTick = gamePaused ? pausedTick : currentLevel.getGameTime();
        return Math.max(0, (endTick - gameStartTick) / 20);
    }

    public static void handleLateJoinPlayer(ServerPlayer player) {
        if (gameActive && !gameStartPlayerIds.contains(player.getUUID())) {
            player.setGameMode(GameType.SPECTATOR);
        }
    }

    public static boolean joinSmallestTeam(ServerPlayer player) {
        if (!gameActive) {
            player.sendSystemMessage(Component.literal("§c[生灵契约] 当前没有进行中的游戏。"));
            return false;
        }

        List<ServerPlayer> participants = currentLevel.getServer().getPlayerList().getPlayers().stream()
                .filter(p -> gameStartPlayerIds.contains(p.getUUID()))
                .toList();
        if (participants.isEmpty()) return false;

        Map<UUID, List<ServerPlayer>> teams = new HashMap<>();
        for (ServerPlayer participant : participants) {
            UUID leader = ContractEvents.getLeaderUUID(participant);
            teams.computeIfAbsent(leader != null ? leader : participant.getUUID(), key -> new ArrayList<>())
                    .add(participant);
        }

        UUID leader = teams.entrySet().stream()
                .min(Comparator.comparingInt(entry -> entry.getValue().size()))
                .map(Map.Entry::getKey)
                .orElse(null);
        if (leader == null) return false;

        ServerPlayer leaderPlayer = currentLevel.getServer().getPlayerList().getPlayer(leader);
        player.getPersistentData().putUUID(TeamOrganizerItem.TAG_LEADER_UUID, leader);
        player.getPersistentData().putString(TeamOrganizerItem.TAG_LEADER_NAME,
                leaderPlayer != null ? leaderPlayer.getGameProfile().getName() : leader.toString());
        if (leaderPlayer != null) {
            player.getPersistentData().putInt(TeamOrganizerItem.TAG_TEAM_NUMBER,
                    leaderPlayer.getPersistentData().getInt(TeamOrganizerItem.TAG_TEAM_NUMBER));
            player.getPersistentData().putString(org.alku.life_contract.SoulContractItem.TAG_CONTRACT_MOD,
                    leaderPlayer.getPersistentData().getString(org.alku.life_contract.SoulContractItem.TAG_CONTRACT_MOD));
        }
        player.setGameMode(GameType.SURVIVAL);
        registerParticipant(player);
        ContractEvents.syncData(player);
        syncToAllClients();
        return true;
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        long perfStart = PerfProfiler.begin();
        try {
            tickGame();
        } finally {
            PerfProfiler.end("GameEventManager", perfStart);
        }
    }

    private static void tickGame() {
        if (!gameActive || currentLevel == null) {
            return;
        }

        StrongholdEndgameManager.tick(currentLevel, !gamePaused);
        if (!gamePaused && currentLevel.getGameTime() % 20 == 0) {
            checkLastTeamStanding();
            if (!gameActive) {
                return;
            }
            awardSurvivalMilestone();
            syncToAllClients(false);
        }
    }

    public static void syncToAllClients() {
        syncToAllClients(true);
    }

    private static void syncToAllClients(boolean force) {
        if (currentLevel == null) return;

        Map<UUID, Integer> currentLifePoints = new LinkedHashMap<>();
        for (ServerPlayer player : currentLevel.getServer().getPlayerList().getPlayers()) {
            // 对局中的玩家显示本模组的命数，其余玩家仍显示 caerula_arbor 的数值
            int points = org.alku.life_contract.PlayerLivesSystem.getLivesForDisplay(player);
            currentLifePoints.put(player.getUUID(), points);
        }
        if (!force && currentLifePoints.equals(lastSyncedLifePoints)) return;
        lastSyncedLifePoints.clear();
        lastSyncedLifePoints.putAll(currentLifePoints);
        List<org.alku.life_contract.PacketSyncLifePoints.PlayerLifePoints> lifePoints = new ArrayList<>(currentLifePoints.size());
        currentLifePoints.forEach((uuid, points) -> lifePoints.add(
                new org.alku.life_contract.PacketSyncLifePoints.PlayerLifePoints(uuid, points)));
        NetworkHandler.sendToAllPlayers(new org.alku.life_contract.PacketSyncLifePoints(lifePoints));
    }
}
