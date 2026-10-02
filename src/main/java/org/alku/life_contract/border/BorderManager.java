package org.alku.life_contract.border;
import net.neoforged.fml.common.EventBusSubscriber;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.BossEvent;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

import org.alku.life_contract.Life_contract;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = Life_contract.MODID, bus = EventBusSubscriber.Bus.GAME)
public class BorderManager {
    private static BorderData currentBorder = null;
    private static ShrinkTask shrinkTask = null;
    private static final ServerBossEvent shrinkBossBar = new ServerBossEvent(
            Component.literal("边界收缩倒计时: 3:00"), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS);
    private static final int GAME_BORDER_SHRINK_INTERVAL_SECONDS = 3 * 60;
    private static final double GAME_BORDER_SHRINK_PERCENTAGE = 10.0D;
    
    public static class BorderData {
        private final ServerLevel level;
        private double centerX;
        private double centerZ;
        private double currentSize;
        private double targetSize;
        private final double initialSize;
        
        public BorderData(ServerLevel level, double centerX, double centerZ, double size) {
            this.level = level;
            this.centerX = centerX;
            this.centerZ = centerZ;
            this.currentSize = size;
            this.targetSize = size;
            this.initialSize = size;
        }
        
        public ServerLevel getLevel() { return level; }
        public double getCenterX() { return centerX; }
        public double getCenterZ() { return centerZ; }
        public double getCurrentSize() { return currentSize; }
        public double getInitialSize() { return initialSize; }
        
        public void setTargetSize(double targetSize) {
            this.targetSize = Math.max(10, targetSize);
        }
        
        public double getTargetSize() { return targetSize; }
        
        public AABB getBounds() {
            double halfSize = currentSize / 2;
            return new AABB(
                centerX - halfSize, level.getMinBuildHeight(), centerZ - halfSize,
                centerX + halfSize, level.getMaxBuildHeight(), centerZ + halfSize
            );
        }
        
        public boolean isInside(Vec3 pos) {
            double halfSize = currentSize / 2;
            return pos.x >= centerX - halfSize && pos.x <= centerX + halfSize &&
                   pos.z >= centerZ - halfSize && pos.z <= centerZ + halfSize;
        }
        
        public boolean isInside(BlockPos pos) {
            double halfSize = currentSize / 2;
            return pos.getX() >= centerX - halfSize && pos.getX() <= centerX + halfSize &&
                   pos.getZ() >= centerZ - halfSize && pos.getZ() <= centerZ + halfSize;
        }
        
        public double getDistanceToBorder(Vec3 pos) {
            double halfSize = currentSize / 2;
            double dx = Math.max(Math.abs(pos.x - centerX) - halfSize, 0);
            double dz = Math.max(Math.abs(pos.z - centerZ) - halfSize, 0);
            return Math.sqrt(dx * dx + dz * dz);
        }
        
        public void updateSize(double newSize) {
            this.currentSize = Math.max(10, newSize);
            applyToLevel();
        }

        public void transitionSize(double newSize, long durationTicks) {
            updateSize(newSize);
        }
        
        public void applyToLevel() {
            net.minecraft.world.level.border.WorldBorder worldBorder = level.getWorldBorder();
            worldBorder.setCenter(centerX, centerZ);
            worldBorder.setSize(currentSize);
        }
    }
    
    public static class ShrinkTask {
        private final BorderData border;
        private final int intervalSeconds;
        private final double shrinkPercentage;
        private final int totalDurationSeconds;
        private long lastShrinkTick;
        private long startTick;
        private boolean running;
        private int shrinkCount;
        
        public ShrinkTask(BorderData border, int intervalSeconds, double shrinkPercentage, int totalDurationSeconds) {
            this.border = border;
            this.intervalSeconds = intervalSeconds;
            this.shrinkPercentage = shrinkPercentage;
            this.totalDurationSeconds = totalDurationSeconds;
            this.running = true;
            this.shrinkCount = 0;
        }
        
        public void start(long currentTick) {
            this.startTick = currentTick;
            this.lastShrinkTick = currentTick;
            this.running = true;
        }
        
        public void tick(long currentTick) {
            if (!running) return;
            
            long elapsedTicks = currentTick - startTick;
            long elapsedSeconds = elapsedTicks / 20;
            
            if (totalDurationSeconds > 0 && elapsedSeconds >= totalDurationSeconds) {
                stopShrink();
                return;
            }
            
            long ticksSinceLastShrink = currentTick - lastShrinkTick;
            if (ticksSinceLastShrink >= intervalSeconds * 20) {
                performShrink();
                lastShrinkTick = currentTick;
            }
        }
        
        private void performShrink() {
            double currentSize = border.getCurrentSize();
            double newSize = currentSize * (1 - shrinkPercentage / 100.0);
            border.setTargetSize(newSize);
            border.updateSize(newSize);
            shrinkCount++;
            
            broadcastMessage(Component.literal("§c[边界] §f边界已缩小！当前大小: §e" + 
                String.format("%.1f", newSize) + " §f格"));
        }
        
        public void stop() {
            running = false;
        }
        
        public boolean isRunning() { return running; }
        public int getShrinkCount() { return shrinkCount; }
        public int getIntervalSeconds() { return intervalSeconds; }
        public double getShrinkPercentage() { return shrinkPercentage; }
        public int getTotalDurationSeconds() { return totalDurationSeconds; }
        public BorderData getBorder() { return border; }

        public int getSecondsUntilNextShrink(long currentTick) {
            long elapsedTicks = Math.max(0, currentTick - lastShrinkTick);
            return (int) Math.max(0, intervalSeconds - elapsedTicks / 20);
        }
    }
    
    public static boolean createBorder(ServerPlayer centerPlayer, double size) {
        if (centerPlayer == null) return false;
        
        ServerLevel level = centerPlayer.serverLevel();
        double centerX = centerPlayer.getX();
        double centerZ = centerPlayer.getZ();
        
        return createBorder(level, centerX, centerZ, size);
    }
    
    public static boolean createBorder(ServerLevel level, double centerX, double centerZ, double size) {
        stopShrink();
        currentBorder = new BorderData(level, centerX, centerZ, size);
        currentBorder.applyToLevel();
        return true;
    }

    public static boolean startGameBorder(ServerLevel level, double centerX, double centerZ) {
        if (!createBorder(level, centerX, centerZ, 1000.0D)) {
            return false;
        }
        return startShrink(GAME_BORDER_SHRINK_INTERVAL_SECONDS, GAME_BORDER_SHRINK_PERCENTAGE, 0);
    }
    
    public static boolean startShrink(int intervalSeconds, double shrinkPercentage, int totalDurationSeconds) {
        if (currentBorder == null) return false;
        if (shrinkTask != null && shrinkTask.isRunning()) {
            shrinkTask.stop();
        }
        
        shrinkTask = new ShrinkTask(currentBorder, intervalSeconds, shrinkPercentage, totalDurationSeconds);
        shrinkTask.start(currentBorder.getLevel().getGameTime());
        for (ServerPlayer player : currentBorder.getLevel().players()) {
            shrinkBossBar.addPlayer(player);
        }
        updateShrinkBossBar(currentBorder.getLevel().getGameTime());
        
        return true;
    }
    
    public static void stopShrink() {
        if (shrinkTask != null) {
            shrinkTask.stop();
            shrinkTask = null;
        }
        shrinkBossBar.removeAllPlayers();
        if (currentBorder != null) {
            for (ServerPlayer player : currentBorder.getLevel().players()) {
                org.alku.life_contract.NetworkHandler.sendToPlayer(player,
                        new BorderStatusPayload(false, true, "", 0));
            }
        }
    }
    
    public static void resetBorder() {
        stopShrink();
        if (currentBorder != null) {
            net.minecraft.world.level.border.WorldBorder worldBorder = currentBorder.getLevel().getWorldBorder();
            worldBorder.setCenter(0, 0);
            worldBorder.setSize(60000000);
            currentBorder = null;
        }
    }
    
    public static BorderData getCurrentBorder() { return currentBorder; }
    public static ShrinkTask getShrinkTask() { return shrinkTask; }
    public static boolean hasBorder() { return currentBorder != null; }
    public static boolean isShrinking() { return shrinkTask != null && shrinkTask.isRunning(); }

    private static void syncPlayerBorderStatus(ServerPlayer player) {
        if (!isPlayerInBorderLevel(player)) {
            org.alku.life_contract.NetworkHandler.sendToPlayer(player,
                    new BorderStatusPayload(false, true, "", 0));
            return;
        }

        net.minecraft.world.level.border.WorldBorder worldBorder = currentBorder.getLevel().getWorldBorder();
        double predictedSize = Math.max(10.0D,
                worldBorder.getSize() * (1.0D - shrinkTask.getShrinkPercentage() / 100.0D));
        double halfSize = predictedSize / 2.0D;
        double centerX = worldBorder.getCenterX();
        double centerZ = worldBorder.getCenterZ();
        double minX = centerX - halfSize;
        double maxX = centerX + halfSize;
        double minZ = centerZ - halfSize;
        double maxZ = centerZ + halfSize;
        double targetX = Math.max(minX, Math.min(maxX, player.getX()));
        double targetZ = Math.max(minZ, Math.min(maxZ, player.getZ()));
        double dx = targetX - player.getX();
        double dz = targetZ - player.getZ();
        boolean inside = dx == 0.0D && dz == 0.0D;
        org.alku.life_contract.NetworkHandler.sendToPlayer(player,
                new BorderStatusPayload(true, inside, inside ? "" : directionTo(dx, dz),
                        inside ? 0 : (int) Math.ceil(Math.hypot(dx, dz))));
    }

    private static String directionTo(double dx, double dz) {
        String horizontal = dx < 0 ? "西" : dx > 0 ? "东" : "";
        String vertical = dz < 0 ? "北" : dz > 0 ? "南" : "";
        if (horizontal.isEmpty()) return vertical;
        if (vertical.isEmpty()) return horizontal;
        return horizontal + vertical;
    }

    private static void updateShrinkBossBar(long currentTick) {
        if (shrinkTask == null || !shrinkTask.isRunning()) return;
        int remaining = shrinkTask.getSecondsUntilNextShrink(currentTick);
        int minutes = remaining / 60;
        int seconds = remaining % 60;
        shrinkBossBar.setName(Component.literal(String.format("边界收缩倒计时: %d:%02d", minutes, seconds)));
        shrinkBossBar.setProgress(shrinkTask.getIntervalSeconds() <= 0 ? 0.0F
                : (float) remaining / shrinkTask.getIntervalSeconds());
    }
    
    private static void broadcastMessage(Component message) {
        if (currentBorder == null) return;
        
        for (ServerPlayer player : currentBorder.getLevel().getPlayers(p -> true)) {
            player.sendSystemMessage(message);
        }
    }
    
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (shrinkTask == null || !shrinkTask.isRunning()) return;

        long currentTick = currentBorder.getLevel().getGameTime();
        shrinkTask.tick(currentTick);
        if (shrinkTask != null && shrinkTask.isRunning() && currentTick % 20 == 0) {
            updateShrinkBossBar(currentTick);
            for (ServerPlayer player : currentBorder.getLevel().getServer().getPlayerList().getPlayers()) {
                syncPlayerBorderStatus(player);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && isPlayerInBorderLevel(player)) {
            shrinkBossBar.addPlayer(player);
            syncPlayerBorderStatus(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (currentBorder == null || !isShrinking()) {
            shrinkBossBar.removePlayer(player);
        } else if (player.serverLevel() == currentBorder.getLevel()) {
            shrinkBossBar.addPlayer(player);
            syncPlayerBorderStatus(player);
        } else {
            shrinkBossBar.removePlayer(player);
            syncPlayerBorderStatus(player);
        }
    }

    private static boolean isPlayerInBorderLevel(ServerPlayer player) {
        return currentBorder != null && isShrinking() && player.serverLevel() == currentBorder.getLevel();
    }
}
