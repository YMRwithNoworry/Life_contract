package org.alku.border_weaver.handler;

import org.alku.border_weaver.Border_weaver;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Border_weaver.MODID)
public class AutoTaskHandler {
    private static boolean isActive = false;
    private static ServerLevel targetLevel = null;
    private static int intervalTicks = 0;
    private static int ticksUntilNext = 0;
    private static double shrinkPercentage = 0.0;
    private static int countdownSeconds = 0;
    private static int shrinkTimeSeconds = 0;

    private static ServerBossEvent BOSS_BAR = null;

    public static boolean isTaskActive() {
        return isActive;
    }

    private static ServerBossEvent getBossBar() {
        if (BOSS_BAR == null) {
            BOSS_BAR = new ServerBossEvent(
                    Component.literal("自动任务未启动"),
                    BossEvent.BossBarColor.BLUE,
                    BossEvent.BossBarOverlay.PROGRESS
            );
            BOSS_BAR.setVisible(false);
        }
        return BOSS_BAR;
    }

    public static void startAutoTask(ServerLevel level, int intervalSeconds, double percentage, int countdown, int shrinkTime) {
        if (isActive) {
            return;
        }

        targetLevel = level;
        intervalTicks = intervalSeconds * 20;
        ticksUntilNext = intervalTicks;
        shrinkPercentage = percentage;
        countdownSeconds = countdown;
        shrinkTimeSeconds = shrinkTime;
        isActive = true;

        ServerBossEvent bar = getBossBar();
        bar.setVisible(true);
        bar.setColor(BossEvent.BossBarColor.BLUE);
        level.getServer().getPlayerList().getPlayers().forEach(bar::addPlayer);

        level.getServer().getPlayerList().broadcastSystemMessage(
                Component.literal("§b[Border Weaver]§a 自动缩圈任务已启动！每 " + intervalSeconds + " 秒缩圈 " + (int)(percentage * 100) + "%"),
                false
        );
    }

    public static void stopAutoTask() {
        if (!isActive) {
            return;
        }

        isActive = false;
        if (BOSS_BAR != null) {
            BOSS_BAR.setVisible(false);
            BOSS_BAR.removeAllPlayers();
        }

        if (targetLevel != null) {
            targetLevel.getServer().getPlayerList().broadcastSystemMessage(
                    Component.literal("§c[Border Weaver]§f 自动缩圈任务已停止。"),
                    false
            );
        }

        targetLevel = null;
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !isActive || targetLevel == null) {
            return;
        }

        ServerBossEvent bar = getBossBar();

        if (CountdownHandler.isTaskActive()) {
            bar.setName(Component.literal("§6自动任务: 等待当前缩圈完成..."));
            bar.setProgress(0.0f);
            return;
        }

        ticksUntilNext--;

        if (ticksUntilNext <= 0) {
            triggerShrink();
            ticksUntilNext = intervalTicks;
        } else {
            int remainingSeconds = ticksUntilNext / 20;
            int minutes = remainingSeconds / 60;
            int seconds = remainingSeconds % 60;

            bar.setName(Component.literal("§b自动任务: 下次缩圈 §e" + minutes + "m " + seconds + "s"));
            bar.setProgress((float) ticksUntilNext / intervalTicks);
        }
    }

    private static void triggerShrink() {
        if (targetLevel == null || CountdownHandler.isTaskActive()) {
            return;
        }

        WorldBorder border = targetLevel.getWorldBorder();
        double currentSize = border.getSize();
        double newSize = Math.max(1.0, currentSize * shrinkPercentage);

        double range = Math.max(0, (currentSize - newSize) / 2.0);
        RandomSource random = targetLevel.getRandom();
        double newX = border.getCenterX() + (random.nextDouble() * 2 - 1) * range;
        double newZ = border.getCenterZ() + (random.nextDouble() * 2 - 1) * range;

        CountdownHandler.startCountdown(targetLevel, newX, newZ, newSize, countdownSeconds, shrinkTimeSeconds);

        targetLevel.getServer().getPlayerList().broadcastSystemMessage(
                Component.literal("§b[Border Weaver]§6 自动缩圈触发！目标大小: §e" + String.format("%.2f", newSize)),
                false
        );
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (BOSS_BAR != null && BOSS_BAR.isVisible() && event.getEntity() instanceof ServerPlayer sp) {
            BOSS_BAR.addPlayer(sp);
        }
    }
}
