package org.alku.border_weaver.handler;

import org.alku.border_weaver.Border_weaver;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = Border_weaver.MODID)
public class CountdownHandler {
    private static int ticksLeft = -1;
    private static int totalShrinkTicks = -1;
    private static int currentShrinkTick = 0;

    private static double startX, startZ, startSize;
    private static double targetX, targetZ, targetSize;
    private static ServerLevel targetLevel;

    private static ServerBossEvent BOSS_BAR = null;

    // --- 公开数据供 EventHandler 读取 ---
    public static boolean isTaskActive() {
        return ticksLeft > 0 || (totalShrinkTicks > 0 && currentShrinkTick <= totalShrinkTicks);
    }

    public static double getTargetX() { return targetX; }
    public static double getTargetZ() { return targetZ; }
    public static double getTargetSize() { return targetSize; }
    // ----------------------------------------

    private static ServerBossEvent getBossBar() {
        if (BOSS_BAR == null) {
            BOSS_BAR = new ServerBossEvent(
                    Component.literal("等待指令..."),
                    BossEvent.BossBarColor.RED,
                    BossEvent.BossBarOverlay.PROGRESS
            );
            BOSS_BAR.setVisible(false);
        }
        return BOSS_BAR;
    }

    public static void startCountdown(ServerLevel level, double x, double z, double size, int delaySeconds, int durationSeconds) {
        targetLevel = level;
        WorldBorder border = level.getWorldBorder();

        startX = border.getCenterX();
        startZ = border.getCenterZ();
        startSize = border.getSize();

        targetX = x;
        targetZ = z;
        targetSize = size;

        totalShrinkTicks = durationSeconds * 20;
        currentShrinkTick = 0;
        ticksLeft = delaySeconds * 20;

        ServerBossEvent bar = getBossBar();
        bar.setVisible(true);
        level.getServer().getPlayerList().getPlayers().forEach(bar::addPlayer);
    }

    public static void stopCountdown() {
        ticksLeft = -1;
        totalShrinkTicks = -1;
        if (BOSS_BAR != null) {
            BOSS_BAR.setVisible(false);
            BOSS_BAR.removeAllPlayers();
        }
        targetLevel = null;
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || targetLevel == null) return;

        ServerBossEvent bar = getBossBar();

        // --- 倒计时阶段 ---
        if (ticksLeft > 0) {
            if (ticksLeft % 20 == 0) {
                int seconds = ticksLeft / 20;
                bar.setName(Component.literal("§e⚠ 边界收缩倒计时: §c" + seconds + "s §7| 目标中心: " + (int)targetX + "," + (int)targetZ));
                bar.setProgress(Math.max(0.0f, Math.min(1.0f, (float) ticksLeft / (ticksLeft + 20))));

                if (seconds <= 5) {
                    float pitch = 1.0f + (5 - seconds) * 0.1f;
                    // 【修正】根据报错，NOTE_BLOCK_PLING 需要 .get()
                    playSound(SoundEvents.NOTE_BLOCK_PLING.get(), 1.0f, pitch);
                }
            }
            // 显示目标区域的轮廓
            spawnTargetVisuals(targetX, targetZ, targetSize);
            ticksLeft--;
        }
        // --- 收缩阶段 ---
        else if (totalShrinkTicks > 0 && currentShrinkTick <= totalShrinkTicks) {
            if (currentShrinkTick == 0) {
                // 【保持原样】WITHER_SPAWN 不需要 .get()
                playSound(SoundEvents.WITHER_SPAWN, 1.0f, 0.5f);
                targetLevel.getServer().getPlayerList().broadcastSystemMessage(Component.literal("§4⚡ 警告：安全区开始收缩！"), false);
            }

            WorldBorder border = targetLevel.getWorldBorder();
            float progress = (float) currentShrinkTick / totalShrinkTicks;

            double curX = startX + (targetX - startX) * progress;
            double curZ = startZ + (targetZ - startZ) * progress;
            double curSize = startSize + (targetSize - startSize) * progress;

            border.setCenter(curX, curZ);
            border.setSize(curSize);

            bar.setName(Component.literal("§4⚡ 区域正在平滑收缩... §f" + (int)(progress * 100) + "%"));
            bar.setProgress(Math.max(0.0f, Math.min(1.0f, 1.0f - progress)));
            bar.setColor(BossEvent.BossBarColor.PURPLE);

            // 收缩时继续显示目标区域
            spawnTargetVisuals(targetX, targetZ, targetSize);

            // BEACON_AMBIENT 也不需要 .get() (根据 SoundEvent 类型推断)
            if (currentShrinkTick % 60 == 0) {
                playSound(SoundEvents.BEACON_AMBIENT, 1.0f, 0.5f);
            }

            if (currentShrinkTick % 20 == 0) {
                try {
                    List<Entity> toRemove = new ArrayList<>();
                    for (Entity e : targetLevel.getAllEntities()) {
                        if (e instanceof LivingEntity && !(e instanceof Player)) {
                            if (!border.isWithinBounds(e.getX(), e.getZ())) {
                                toRemove.add(e);
                            }
                        }
                    }
                    toRemove.forEach(Entity::discard);
                } catch (Exception e) {
                    Border_weaver.LOGGER.error("清理实体异常: " + e.getMessage());
                }
            }

            currentShrinkTick++;
            if (currentShrinkTick > totalShrinkTicks) {
                // 【保持原样】PLAYER_LEVELUP 不需要 .get()
                playSound(SoundEvents.PLAYER_LEVELUP, 1.0f, 1.0f);
                targetLevel.getServer().getPlayerList().broadcastSystemMessage(Component.literal("§a✔ 边界收缩已完成！"), false);
                stopCountdown();
            }
        }
    }

    private static void playSound(net.minecraft.sounds.SoundEvent sound, float volume, float pitch) {
        if (targetLevel != null && sound != null) {
            targetLevel.playSound(null, targetX, 100, targetZ, sound, SoundSource.MASTER, volume * 1000f, pitch);
        }
    }

    private static void spawnTargetVisuals(double cX, double cZ, double size) {
        if (targetLevel.getGameTime() % 10 != 0) return;

        double halfSize = size / 2.0;
        double minX = cX - halfSize;
        double maxX = cX + halfSize;
        double minZ = cZ - halfSize;
        double maxZ = cZ + halfSize;

        // 中心光柱
        spawnPillar(cX, cZ, true);

        // 四角光柱
        spawnPillar(minX, minZ, false);
        spawnPillar(maxX, minZ, false);
        spawnPillar(minX, maxZ, false);
        spawnPillar(maxX, maxZ, false);
    }

    private static void spawnPillar(double x, double z, boolean withFlame) {
        for (int y = -64; y < 320; y += 15) {
            targetLevel.sendParticles(ParticleTypes.END_ROD, x, y, z, 1, 0, 0, 0, 0.0);
        }
        if (withFlame) {
            double surfaceY = targetLevel.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, (int)x, (int)z);
            targetLevel.sendParticles(ParticleTypes.FLAME, x, surfaceY + 1, z, 2, 0.1, 0.1, 0.1, 0.02);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (BOSS_BAR != null && BOSS_BAR.isVisible() && event.getEntity() instanceof ServerPlayer sp) {
            BOSS_BAR.addPlayer(sp);
        }
    }
}