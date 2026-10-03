package org.alku.life_contract;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 极轻量的分段计时器，用来回答"本模组每 tick 到底花了多少时间"。
 * <p>
 * 默认关闭：{@link #begin()} 在关闭时直接返回 0，{@link #end(String, long)} 遇到 0 立即返回，
 * 因此调用点只多一次方法调用与一次比较，可以长期保留在热路径里。
 * 通过 `/contract perf on` 打开后，再用 `/contract perf` 查看各段落的调用次数与耗时。
 */
public final class PerfProfiler {

    private static final Map<String, Section> SECTIONS = new LinkedHashMap<>();

    private static boolean enabled;

    private PerfProfiler() {
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean value) {
        enabled = value;
        if (!value) {
            SECTIONS.clear();
        }
    }

    /** 开始计时；关闭状态返回 0。 */
    public static long begin() {
        return enabled ? System.nanoTime() : 0L;
    }

    /** 结束计时并累计；start 为 0（未启用）时直接返回。 */
    public static void end(String name, long startNanos) {
        if (startNanos == 0L) {
            return;
        }

        Section section = SECTIONS.computeIfAbsent(name, key -> new Section());
        long elapsed = System.nanoTime() - startNanos;
        section.count++;
        section.totalNanos += elapsed;
        if (elapsed > section.maxNanos) {
            section.maxNanos = elapsed;
        }
    }

    public static void reset() {
        SECTIONS.clear();
    }

    /** 各段落的统计行；未启用时返回空列表。 */
    public static List<String> report() {
        List<String> lines = new ArrayList<>(SECTIONS.size());
        for (Map.Entry<String, Section> entry : SECTIONS.entrySet()) {
            Section section = entry.getValue();
            if (section.count == 0L) {
                continue;
            }
            double averageMicros = section.totalNanos / 1000.0D / section.count;
            double maxMicros = section.maxNanos / 1000.0D;
            double totalMillis = section.totalNanos / 1_000_000.0D;
            lines.add(String.format("§7%s§f: §b%d §f次, 平均 §b%.2fµs§f, 最大 §e%.2fµs§f, 合计 §b%.1fms",
                    entry.getKey(), section.count, averageMicros, maxMicros, totalMillis));
        }
        return lines;
    }

    private static final class Section {
        private long count;
        private long totalNanos;
        private long maxNanos;
    }
}
