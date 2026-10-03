package org.alku.life_contract;

import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 性能看门狗：服务端 TPS 持续偏低时，自动把一份诊断报告写进日志。
 * <p>
 * 目的是让"卡顿发生的那一刻"就被记录下来——管理员不必正好在卡顿的时候手动执行
 * {@code /contract perf}，事后翻日志即可看到当时的 TPS、各维度实体数、哪类实体在净增长、
 * 以及各热路径的耗时。每 tick 只做一次取模判断，正常情况下零额外开销。
 */
@EventBusSubscriber(modid = Life_contract.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class PerfWatchdog {

    /** 每秒采样一次。 */
    private static final int SAMPLE_INTERVAL_TICKS = 20;
    /** 低于该 TPS 视为卡顿。 */
    private static final double LOW_TPS_THRESHOLD = 15.0D;
    /** 需要连续这么多次采样都偏低才报告，避免瞬时波动刷屏。 */
    private static final int LOW_TPS_STREAK_REQUIRED = 3;
    /** 同一段时间最多每 5 分钟报告一次。 */
    private static final long REPORT_COOLDOWN_MILLIS = 300_000L;

    private static boolean enabled = true;
    private static long lastSampleTick = -1L;
    private static long lastSampleMillis;
    private static int lowTpsStreak;
    private static long lastReportMillis;

    private PerfWatchdog() {
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean value) {
        enabled = value;
        lowTpsStreak = 0;
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!enabled) {
            return;
        }

        MinecraftServer server = event.getServer();
        long nowTick = server.getTickCount();
        if (nowTick % SAMPLE_INTERVAL_TICKS != 0L) {
            return;
        }

        long nowMillis = System.currentTimeMillis();
        if (lastSampleTick < 0L) {
            lastSampleTick = nowTick;
            lastSampleMillis = nowMillis;
            return;
        }

        long elapsedMillis = Math.max(1L, nowMillis - lastSampleMillis);
        long elapsedTicks = Math.max(0L, nowTick - lastSampleTick);
        lastSampleTick = nowTick;
        lastSampleMillis = nowMillis;

        double tps = elapsedTicks * 1000.0D / elapsedMillis;
        if (tps >= LOW_TPS_THRESHOLD) {
            lowTpsStreak = 0;
            return;
        }

        lowTpsStreak++;
        if (lowTpsStreak < LOW_TPS_STREAK_REQUIRED) {
            return;
        }
        if (nowMillis - lastReportMillis < REPORT_COOLDOWN_MILLIS) {
            return;
        }

        lastReportMillis = nowMillis;
        lowTpsStreak = 0;
        logReport(server, tps, elapsedTicks, elapsedMillis);
    }

    private static void logReport(MinecraftServer server, double tps, long elapsedTicks, long elapsedMillis) {
        Life_contract.LOGGER.warn(String.format(
                "[性能看门狗] TPS 持续低于 %.0f，最近采样 %.2f TPS（%d tick / %.1f 秒），自动诊断如下：",
                LOW_TPS_THRESHOLD, tps, elapsedTicks, elapsedMillis / 1000.0D));

        for (String line : PerfDiagnostics.collectPlainReport(server)) {
            Life_contract.LOGGER.warn("[性能看门狗] {}", line);
        }

        if (!PerfProfiler.isEnabled()) {
            PerfProfiler.reset();
            PerfDiagnostics.resetEntityTypeChurn();
            PerfProfiler.setEnabled(true);
            Life_contract.LOGGER.warn("[性能看门狗] 已自动开启分段计时与实体类型统计，"
                    + "若再次触发将附带各热路径耗时；排查完可用 /contract perf off 关闭。");
        }
    }
}
