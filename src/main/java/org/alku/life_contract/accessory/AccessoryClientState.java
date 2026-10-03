package org.alku.life_contract.accessory;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * 客户端持有的饰品运行时状态，由 {@link AccessoryStatePayload} 同步。
 * <p>
 * 详情卡用它显示"你当前生效的同派系件数"与"该饰品当前充能层数"。
 */
public final class AccessoryClientState {

    private static final Map<AccessoryFaction, Integer> FACTION_COUNTS = new EnumMap<>(AccessoryFaction.class);
    private static final Map<String, Integer> CHARGE_STACKS = new HashMap<>();

    private AccessoryClientState() {
    }

    public static void update(AccessoryStatePayload payload) {
        FACTION_COUNTS.clear();
        payload.factionCounts().forEach((id, count) -> {
            AccessoryFaction faction = AccessoryFaction.byId(id);
            if (faction != AccessoryFaction.NONE && count > 0) {
                FACTION_COUNTS.put(faction, count);
            }
        });

        CHARGE_STACKS.clear();
        CHARGE_STACKS.putAll(payload.chargeStacks());
    }

    /** 已触发的共鸣：派系 -> 档位（1 = 共鸣 I，2 = 共鸣 II）。 */
    public static Map<AccessoryFaction, Integer> resonanceTiers() {
        Map<AccessoryFaction, Integer> result = new EnumMap<>(AccessoryFaction.class);
        FACTION_COUNTS.forEach((faction, count) -> {
            int tier = AccessoryResonance.tierFor(count);
            if (tier > 0) {
                result.put(faction, tier);
            }
        });
        return result;
    }

    /** 该派系当前生效的件数。 */
    public static int factionCount(AccessoryFaction faction) {
        return FACTION_COUNTS.getOrDefault(faction, 0);
    }

    /** 该饰品当前的充能层数；未同步到返回 -1（表示"不确定"，详情卡不显示这一行）。 */
    public static int chargeStacks(String accessoryId) {
        Integer stacks = CHARGE_STACKS.get(accessoryId);
        return stacks == null ? -1 : stacks;
    }

    public static void clear() {
        FACTION_COUNTS.clear();
        CHARGE_STACKS.clear();
    }
}
