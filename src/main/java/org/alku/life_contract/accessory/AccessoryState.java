package org.alku.life_contract.accessory;

import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 饰品的运行时状态：击杀充能层数、主动技冷却、最近击杀时间。
 * <p>
 * 全部按玩家 UUID 索引；玩家退出或重生时清空，避免状态泄漏。
 */
public final class AccessoryState {

    /** player -> 饰品 id -> 充能层数。 */
    private static final Map<UUID, Map<String, Integer>> CHARGE = new HashMap<>();
    /** player -> 饰品 id -> 充能上次衰减的 tick。 */
    private static final Map<UUID, Map<String, Integer>> CHARGE_DECAY = new HashMap<>();
    /** player -> 饰品 id -> 主动技冷却结束 tick。 */
    private static final Map<UUID, Map<String, Integer>> COOLDOWN = new HashMap<>();
    /** player -> 最近一次击杀的 tick。 */
    private static final Map<UUID, Integer> LAST_KILL = new HashMap<>();

    private AccessoryState() {
    }

    // ==================== 击杀充能 ====================

    public static int charge(ServerPlayer player, String accessoryId) {
        Map<String, Integer> map = CHARGE.get(player.getUUID());
        return map == null ? 0 : map.getOrDefault(accessoryId, 0);
    }

    public static void setCharge(ServerPlayer player, String accessoryId, int value) {
        CHARGE.computeIfAbsent(player.getUUID(), key -> new HashMap<>()).put(accessoryId, Math.max(0, value));
    }

    public static void addCharge(ServerPlayer player, String accessoryId, int delta, int max) {
        int now = charge(player, accessoryId);
        setCharge(player, accessoryId, Math.max(0, Math.min(max, now + delta)));
    }

    public static int lastDecayTick(ServerPlayer player, String accessoryId) {
        Map<String, Integer> map = CHARGE_DECAY.get(player.getUUID());
        return map == null ? 0 : map.getOrDefault(accessoryId, 0);
    }

    public static void setLastDecayTick(ServerPlayer player, String accessoryId, int tick) {
        CHARGE_DECAY.computeIfAbsent(player.getUUID(), key -> new HashMap<>()).put(accessoryId, tick);
    }

    // ==================== 主动技冷却 ====================

    public static int cooldownEnd(ServerPlayer player, String accessoryId) {
        Map<String, Integer> map = COOLDOWN.get(player.getUUID());
        return map == null ? 0 : map.getOrDefault(accessoryId, 0);
    }

    public static void setCooldownEnd(ServerPlayer player, String accessoryId, int tick) {
        COOLDOWN.computeIfAbsent(player.getUUID(), key -> new HashMap<>()).put(accessoryId, tick);
    }

    /** 剩余冷却 tick，0 表示可用。 */
    public static int cooldownRemaining(ServerPlayer player, String accessoryId) {
        int tick = player.getServer() == null ? 0 : player.getServer().getTickCount();
        return Math.max(0, cooldownEnd(player, accessoryId) - tick);
    }

    // ==================== 最近击杀 ====================

    public static int lastKillTick(ServerPlayer player) {
        return LAST_KILL.getOrDefault(player.getUUID(), Integer.MIN_VALUE / 2);
    }

    public static void markKill(ServerPlayer player) {
        if (player.getServer() != null) {
            LAST_KILL.put(player.getUUID(), player.getServer().getTickCount());
        }
    }

    // ==================== 清理 ====================

    public static void clear(UUID uuid) {
        CHARGE.remove(uuid);
        CHARGE_DECAY.remove(uuid);
        COOLDOWN.remove(uuid);
        LAST_KILL.remove(uuid);
    }
}
