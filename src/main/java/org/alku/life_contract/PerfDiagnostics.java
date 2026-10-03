package org.alku.life_contract;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import org.alku.life_contract.airdrop.event.AirdropNavigator;
import org.alku.life_contract.events.WorldEventManager;
import org.alku.life_contract.follower.FollowerEvents;
import org.alku.life_contract.mutation.MutationCombatEvents;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 性能诊断：把「服务端实际 TPS」和「本模组可能无限增长的状态」一次性打出来。
 * <p>
 * 采样只在执行指令时发生（TPS 用两次执行之间的 tick 数 / 时间差计算），
 * 因此不占用任何每 tick 开销，可以常驻。
 */
@EventBusSubscriber(modid = Life_contract.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class PerfDiagnostics {

    private static long lastSampleTick = -1L;
    private static long lastSampleMillis;
    private static long lastSampleJoins;
    private static long lastSampleLeaves;

    /** 实体进出世界的累计计数：用来判断实体是不是"只增不减"。 */
    private static long entityJoins;
    private static long entityLeaves;

    private PerfDiagnostics() {
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide()) {
            entityJoins++;
        }
    }

    @SubscribeEvent
    public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        if (!event.getLevel().isClientSide()) {
            entityLeaves++;
        }
    }

    public static void report(CommandSourceStack source) {
        MinecraftServer server = source.getServer();
        source.sendSuccess(() -> Component.literal("§6§l[性能诊断]"), false);
        reportTickRate(server, source);
        reportLevelEntities(server, source);
        reportModuleState(server, source);
        reportProfiler(source);
    }

    /** 打开计时后才有内容：各热路径的调用次数与耗时。 */
    private static void reportProfiler(CommandSourceStack source) {
        if (!PerfProfiler.isEnabled()) {
            source.sendSuccess(() -> Component.literal(
                    "§7分段计时: §8未开启 §7(执行 §f/contract perf on §7开始统计)"), false);
            return;
        }

        List<String> lines = PerfProfiler.report();
        if (lines.isEmpty()) {
            source.sendSuccess(() -> Component.literal("§7分段计时: §e已开启，等待下一次采样"), false);
            return;
        }
        source.sendSuccess(() -> Component.literal("§7分段计时 (自开启或上次重置以来):"), false);
        for (String line : lines) {
            source.sendSuccess(() -> Component.literal("  " + line), false);
        }
    }

    /** TPS：用两次执行之间的 tick 增量 / 时间增量计算，不做任何后台采样。 */
    private static void reportTickRate(MinecraftServer server, CommandSourceStack source) {
        long nowTick = server.getTickCount();
        long nowMillis = System.currentTimeMillis();

        if (lastSampleTick < 0L) {
            lastSampleTick = nowTick;
            lastSampleMillis = nowMillis;
            lastSampleJoins = entityJoins;
            lastSampleLeaves = entityLeaves;
            source.sendSuccess(() -> Component.literal("§7TPS: §e首次采样，请再次执行该指令"), false);
            return;
        }

        long elapsedMillis = Math.max(1L, nowMillis - lastSampleMillis);
        long elapsedTicks = Math.max(0L, nowTick - lastSampleTick);
        double tps = elapsedTicks * 1000.0D / elapsedMillis;
        String color = tps >= 19.0D ? "§a" : tps >= 15.0D ? "§e" : "§c";
        String line = String.format("§7TPS: %s%.2f §7(采样 %.1f 秒 / %d tick)", color, tps,
                elapsedMillis / 1000.0D, elapsedTicks);
        source.sendSuccess(() -> Component.literal(line), false);

        long joinDelta = entityJoins - lastSampleJoins;
        long leaveDelta = entityLeaves - lastSampleLeaves;
        double seconds = Math.max(0.001D, elapsedMillis / 1000.0D);
        String churn = String.format("§7实体变化: §a+%d §7/ §c-%d §7(新增 %.1f/s, 消失 %.1f/s)",
                joinDelta, leaveDelta, joinDelta / seconds, leaveDelta / seconds);
        source.sendSuccess(() -> Component.literal(churn), false);

        lastSampleTick = nowTick;
        lastSampleMillis = nowMillis;
        lastSampleJoins = entityJoins;
        lastSampleLeaves = entityLeaves;
    }

    /** 每个维度的实体总数与占比最高的几类：实体爆炸是 TPS 下降最常见的原因。 */
    private static void reportLevelEntities(MinecraftServer server, CommandSourceStack source) {
        for (ServerLevel level : server.getAllLevels()) {
            Map<String, Integer> byType = new HashMap<>();
            int total = 0;
            for (Entity entity : level.getAllEntities()) {
                total++;
                ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
                byType.merge(id == null ? "unknown" : id.toString(), 1, Integer::sum);
            }

            final int entityTotal = total;
            final String topTypes = byType.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(4)
                    .map(entry -> entry.getKey() + "×" + entry.getValue())
                    .collect(Collectors.joining("§7, §f"));
            String dimension = level.dimension().location().getPath();
            source.sendSuccess(() -> Component.literal("§7" + dimension + "§f: 实体 §b" + entityTotal
                    + (topTypes.isEmpty() ? "" : " §7[" + topTypes + "§7]")), false);
        }
    }

    /** 本模组自己维护的索引与状态，重点看有没有只增不减的表。 */
    private static void reportModuleState(MinecraftServer server, CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("§7跟随/友军: §f" + FollowerEvents.debugSummary()), false);
        source.sendSuccess(() -> Component.literal("§7空投导航: §f" + AirdropNavigator.debugSummary()), false);
        source.sendSuccess(() -> Component.literal("§7守卫: §f" + TeamIronGolemSystem.debugSummary()
                + " §7| 攻击记录: §f" + ContractEvents.debugSummary()
                + " §7| 标记: §f" + MutationCombatEvents.debugSummary()), false);

        int trackedLives = 0;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (PlayerLivesSystem.isTracked(player)) {
                trackedLives++;
            }
        }
        final int livesCount = trackedLives;
        source.sendSuccess(() -> Component.literal("§7命数: §b" + livesCount + " §7名在线玩家"
                + " §7| 安全气泡: §b" + WorldEventManager.getSafeBubbles().size()
                + " §7| 悬赏: §b" + (WorldEventManager.isBountyActive() ? "有" : "无")
                + " §7| 孢子雨: §b" + (WorldEventManager.isSporeRainActive() ? "开" : "关")), false);
    }
}
