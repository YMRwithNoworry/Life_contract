package org.alku.life_contract.border;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import org.alku.life_contract.Life_contract;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = Life_contract.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BorderManager {
    private static BorderData currentBorder = null;
    private static ShrinkTask shrinkTask = null;
    
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
            
            if (elapsedSeconds >= totalDurationSeconds) {
                running = false;
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
    }
    
    public static boolean createBorder(ServerPlayer centerPlayer, double size) {
        if (centerPlayer == null) return false;
        
        ServerLevel level = centerPlayer.serverLevel();
        double centerX = centerPlayer.getX();
        double centerZ = centerPlayer.getZ();
        
        currentBorder = new BorderData(level, centerX, centerZ, size);
        currentBorder.applyToLevel();
        
        shrinkTask = null;
        
        return true;
    }
    
    public static boolean createBorder(ServerLevel level, double centerX, double centerZ, double size) {
        currentBorder = new BorderData(level, centerX, centerZ, size);
        currentBorder.applyToLevel();
        shrinkTask = null;
        return true;
    }
    
    public static boolean startShrink(int intervalSeconds, double shrinkPercentage, int totalDurationSeconds) {
        if (currentBorder == null) return false;
        if (shrinkTask != null && shrinkTask.isRunning()) {
            shrinkTask.stop();
        }
        
        shrinkTask = new ShrinkTask(currentBorder, intervalSeconds, shrinkPercentage, totalDurationSeconds);
        shrinkTask.start(currentBorder.getLevel().getGameTime());
        
        return true;
    }
    
    public static void stopShrink() {
        if (shrinkTask != null) {
            shrinkTask.stop();
            shrinkTask = null;
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
    
    private static void broadcastMessage(Component message) {
        if (currentBorder == null) return;
        
        for (ServerPlayer player : currentBorder.getLevel().getPlayers(p -> true)) {
            player.sendSystemMessage(message);
        }
    }
    
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (shrinkTask == null || !shrinkTask.isRunning()) return;
        
        shrinkTask.tick(currentBorder.getLevel().getGameTime());
    }
}
