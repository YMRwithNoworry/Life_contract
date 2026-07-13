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

    public static void startGame(ServerLevel level, double centerX, double centerZ) {
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
    }

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

        List<PacketSyncEvents.PlayerPosData> positions = new ArrayList<>();
        for (ServerPlayer player : currentLevel.getServer().getPlayerList().getPlayers()) {
            UUID leader = ContractEvents.getLeaderUUID(player);
            positions.add(new PacketSyncEvents.PlayerPosData(
                    player.getUUID(), player.getGameProfile().getName(), leader,
                    (int) player.getX(), (int) player.getZ(), player.getYRot(),
                    CaerulaArborCompat.getLifePoints(player)));
        }

        WorldBorder border = currentLevel.getWorldBorder();
        NetworkHandler.CHANNEL.send(PacketDistributor.ALL.noArg(), new PacketSyncEvents(
                gameActive, border.getCenterX(), border.getCenterZ(), border.getSize(), positions));
    }
}
