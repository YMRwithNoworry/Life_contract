package org.alku.border_weaver.handler;

import org.alku.border_weaver.Border_weaver;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Border_weaver.MODID)
public class BorderEventHandler {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player instanceof ServerPlayer player) {
            Level level = player.level();
            WorldBorder border = level.getWorldBorder();
            long time = level.getGameTime();

            // 1. 伤害检测 (每秒一次) - 始终基于当前真实边界
            if (time % 20 == 0) {
                if (!border.isWithinBounds(player.getX(), player.getZ())) {
                    player.hurt(level.damageSources().outOfBorder(), 2.0f);
                    player.displayClientMessage(Component.literal("§c☠ 警告：你处于边界外！迅速撤离！"), true);
                    return; // 受到伤害时优先显示警告，覆盖导航信息
                }
            }

            // 2. HUD 导航显示 (每 0.25 秒刷新)
            if (time % 5 == 0) {
                // 如果边界是默认无限大，且没有正在进行的收缩任务，则不显示任何信息
                if (border.getSize() > 5.9E7 && !CountdownHandler.isTaskActive()) {
                    return;
                }

                // --- 导航逻辑 ---
                if (CountdownHandler.isTaskActive()) {
                    // A. 如果有收缩任务，导航目标是【未来的安全区】
                    double targetX = CountdownHandler.getTargetX();
                    double targetZ = CountdownHandler.getTargetZ();
                    double targetSize = CountdownHandler.getTargetSize();
                    double halfSize = targetSize / 2.0;

                    // 计算玩家离【目标安全区边缘】的距离
                    double distX = Math.max(0, Math.abs(player.getX() - targetX) - halfSize);
                    double distZ = Math.max(0, Math.abs(player.getZ() - targetZ) - halfSize);
                    // 切比雪夫距离（进入方形区域的最短路径通常取决于较远的那个轴）
                    double distToSafeZone = Math.max(distX, distZ);

                    if (distToSafeZone <= 0) {
                        player.displayClientMessage(Component.literal("§a✔ 已在预计安全区内"), true);
                    } else {
                        player.displayClientMessage(Component.literal("§e⚠ 距预计安全区: §l" + String.format("%.1f", distToSafeZone) + "m"), true);
                    }

                }
            }
        }
    }

    @SubscribeEvent
    public static void onCheckSpawn(MobSpawnEvent.FinalizeSpawn event) {
        if (event.getLevel() instanceof Level level) {
            WorldBorder border = level.getWorldBorder();
            if (!border.isWithinBounds(event.getX(), event.getZ())) {
                event.setSpawnCancelled(true);
                event.setResult(Event.Result.DENY);
            }
        }
    }
}