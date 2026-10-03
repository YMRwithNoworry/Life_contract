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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 性能诊断：把「服务端实际 TPS」和「本模组可能无限增长的状态」一次性打出来。
 * <p>
 * 指令里的采样只在执行时发生（TPS 用两次执行之间的 tick 数 / 时间差计算），
 * 因此不占用任何每 tick 开销；看门狗（{@link PerfWatchdog}）复用同一套数据生成日志报告。
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

    /**
     * 各实体类型的进出计数 {@code {新增, 消失}}，只在分段计时开启时统计，
     * 用来直接回答"到底是哪类实体在堆积"。关闭时零开销。
     */
    private static final Map<String, long[]> ENTITY_TYPE_CHURN = new HashMap<>();

    private PerfDiagnostics() {
    }

    public static void resetEntityTypeChurn() {
        ENTITY_TYPE_CHURN.clear();
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        entityJoins++;
        if (PerfProfiler.isEnabled()) {
            countEntityType(event.getEntity(), true);
        }
    }

    @SubscribeEvent
    public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        entityLeaves++;
        if (PerfProfiler.isEnabled()) {
            countEntityType(event.getEntity(), false);
        }
    }

    private static void countEntityType(Entity entity, boolean joining) {
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        if (id == null) {
            return;
        }
        long[] counters = ENTITY_TYPE_CHURN.computeIfAbsent(id.toString(), key -> new long[2]);
        counters[joining ? 0 : 1]++;
    }

    // ==================== 数据（指令与看门狗共用） ====================

    /** 单个维度的实体普查结果。 */
    private record LevelCensus(String dimension, int total, List<String> topTypes) {
    }

    /** 一类实体的进出统计。 */
    private record TypeChurn(String type, long joins, long leaves, long net) {
    }

    private static List<LevelCensus> census(MinecraftServer server) {
        List<LevelCensus> result = new ArrayList<>();
        for (ServerLevel level : server.getAllLevels()) {
            Map<String, Integer> byType = new HashMap<>();
            int total = 0;
            for (Entity entity : level.getAllEntities()) {
                total++;
                ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
                byType.merge(id == null ? "unknown" : id.toString(), 1, Integer::sum);
            }

            List<String> topTypes = byType.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(4)
                    .map(entry -> entry.getKey() + "×" + entry.getValue())
                    .collect(Collectors.toList());
            result.add(new LevelCensus(level.dimension().location().getPath(), total, topTypes));
        }
        return result;
    }

    /** 净增长最多的几类实体：如果某类只涨不落，就是它把实体数堆上去的。 */
    private static List<TypeChurn> topNetGrowth(int limit) {
        return ENTITY_TYPE_CHURN.entrySet().stream()
                .map(entry -> new TypeChurn(entry.getKey(), entry.getValue()[0], entry.getValue()[1],
                        entry.getValue()[0] - entry.getValue()[1]))
                .sorted(Comparator.comparingLong(TypeChurn::net).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    /** 本模组自己维护的索引与状态（纯文本），重点看有没有只增不减的表。 */
    private static List<String> moduleStateLines(MinecraftServer server) {
        int trackedLives = 0;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (PlayerLivesSystem.isTracked(player)) {
                trackedLives++;
            }
        }

        List<String> lines = new ArrayList<>();
        lines.add("跟随/友军: " + FollowerEvents.debugSummary());
        lines.add("空投导航: " + AirdropNavigator.debugSummary());
        lines.add("守卫: " + TeamIronGolemSystem.debugSummary()
                + " | 攻击记录: " + ContractEvents.debugSummary()
                + " | 标记: " + MutationCombatEvents.debugSummary());
        lines.add("命数: " + trackedLives + " 名在线玩家"
                + " | 安全气泡: " + WorldEventManager.getSafeBubbles().size()
                + " | 悬赏: " + (WorldEventManager.isBountyActive() ? "有" : "无")
                + " | 孢子雨: " + (WorldEventManager.isSporeRainActive() ? "开" : "关"));
        return lines;
    }

    /** 去掉颜色代码，供日志使用。 */
    private static String stripColors(String text) {
        return text.replaceAll("§.", "");
    }

    /** 供 {@link PerfWatchdog} 写日志的纯文本报告。 */
    public static List<String> collectPlainReport(MinecraftServer server) {
        List<String> lines = new ArrayList<>();
        for (LevelCensus level : census(server)) {
            lines.add(level.dimension() + ": 实体 " + level.total()
                    + (level.topTypes().isEmpty() ? "" : " [" + String.join(", ", level.topTypes()) + "]"));
        }
        for (TypeChurn churn : topNetGrowth(5)) {
            lines.add(String.format("净增长 %s: +%d / -%d (净 %+d)",
                    churn.type(), churn.joins(), churn.leaves(), churn.net()));
        }
        lines.addAll(moduleStateLines(server));
        if (PerfProfiler.isEnabled()) {
            for (String line : PerfProfiler.report()) {
                lines.add(stripColors(line));
            }
        }
        return lines;
    }

    // ==================== 指令输出 ====================

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
        reportEntityTypeChurn(source, seconds);

        lastSampleTick = nowTick;
        lastSampleMillis = nowMillis;
        lastSampleJoins = entityJoins;
        lastSampleLeaves = entityLeaves;
    }

    private static void reportEntityTypeChurn(CommandSourceStack source, double seconds) {
        if (!PerfProfiler.isEnabled()) {
            return;
        }
        if (ENTITY_TYPE_CHURN.isEmpty()) {
            source.sendSuccess(() -> Component.literal("§7实体净增长: §8等待采样"), false);
            return;
        }

        source.sendSuccess(() -> Component.literal("§7实体净增长 Top:"), false);
        for (TypeChurn churn : topNetGrowth(5)) {
            String line = String.format("  §f%s§7: +%d / -%d §7(净 %+d, %.2f/s)",
                    churn.type(), churn.joins(), churn.leaves(), churn.net(), churn.net() / seconds);
            source.sendSuccess(() -> Component.literal(line), false);
        }
    }

    /** 每个维度的实体总数与占比最高的几类：实体爆炸是 TPS 下降最常见的原因。 */
    private static void reportLevelEntities(MinecraftServer server, CommandSourceStack source) {
        for (LevelCensus level : census(server)) {
            final String topTypes = level.topTypes().stream().collect(Collectors.joining("§7, §f"));
            source.sendSuccess(() -> Component.literal("§7" + level.dimension() + "§f: 实体 §b" + level.total()
                    + (topTypes.isEmpty() ? "" : " §7[" + topTypes + "§7]")), false);
        }
    }

    private static void reportModuleState(MinecraftServer server, CommandSourceStack source) {
        for (String line : moduleStateLines(server)) {
            source.sendSuccess(() -> Component.literal("§7" + line), false);
        }
    }
}
