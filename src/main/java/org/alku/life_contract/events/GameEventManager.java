package org.alku.life_contract.events;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import org.alku.life_contract.ContractEvents;
import org.alku.life_contract.Life_contract;
import org.alku.life_contract.NetworkHandler;
import org.alku.life_contract.ModPoolConfig;
import org.alku.life_contract.SoulContractItem;
import org.alku.life_contract.TeamOrganizerItem;
import org.alku.life_contract.compat.CaerulaArborCompat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Collections;

@Mod.EventBusSubscriber(modid = Life_contract.MODID)
public final class GameEventManager {
    private static boolean gameActive;
    private static boolean gamePaused;
    private static long gameStartTick;
    private static long pausedTick;
    private static ServerLevel currentLevel;
    private static final Set<UUID> gameStartPlayerIds = new HashSet<>();

    private GameEventManager() {
    }

    public static StartResult startGame(ServerLevel level, double centerX, double centerZ) {
        StartResult allocation = assignRandomTeams(level);
        if (!allocation.success()) return allocation;
        currentLevel = level;
        gameActive = true;
        gamePaused = false;
        gameStartTick = level.getGameTime();
        pausedTick = 0;
        gameStartPlayerIds.clear();

        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            if (player.gameMode.getGameModeForPlayer() == GameType.SURVIVAL
                    || player.gameMode.getGameModeForPlayer() == GameType.ADVENTURE) {
                gameStartPlayerIds.add(player.getUUID());
            }
        }

        WorldBorder border = level.getWorldBorder();
        border.setCenter(centerX, centerZ);
        border.setSize(600.0);
        syncToAllClients();
        return allocation;
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
        gameStartPlayerIds.clear();
        syncToAllClients();
        currentLevel = null;
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
        player.setGameMode(GameType.SURVIVAL);
        gameStartPlayerIds.add(player.getUUID());
        ContractEvents.syncData(player);
        syncToAllClients();
        return true;
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && gameActive && !gamePaused && currentLevel != null
                && currentLevel.getGameTime() % 20 == 0) {
            syncToAllClients();
        }
    }

    public static void syncToAllClients() {
        if (currentLevel == null) return;

        List<org.alku.life_contract.PacketSyncLifePoints.PlayerLifePoints> lifePoints = new ArrayList<>();
        for (ServerPlayer player : currentLevel.getServer().getPlayerList().getPlayers()) {
            lifePoints.add(new org.alku.life_contract.PacketSyncLifePoints.PlayerLifePoints(
                    player.getUUID(), CaerulaArborCompat.getLifePoints(player)));
        }
        NetworkHandler.CHANNEL.send(PacketDistributor.ALL.noArg(),
                new org.alku.life_contract.PacketSyncLifePoints(lifePoints));
    }
}
