package org.alku.life_contract.accessory;

/** 主动技种类。数值含义见 {@link AccessoryActive}。 */
public enum AccessoryActiveKind {
    /** 爆发：力量 + 迅捷。 */
    BURST,
    /** 护盾：抗性提升 + 伤害吸收（value = 吸收点数）。 */
    WARD,
    /** 治疗：立即回复 value 点生命 + 再生。 */
    MEND,
    /** 冲刺：沿视线弹射 + 短暂迅捷。 */
    DASH,
    /** 潜行：隐身 + 迅捷。 */
    PHASE,
    /** 净化：清除全部负面效果 + 短暂再生。 */
    PURGE,
    /** 标记：给附近敌人发光 + 虚弱。 */
    MARK,
    /** 集结：给附近队友力量 + 抗性。 */
    RALLY;

    public static AccessoryActiveKind byId(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        for (AccessoryActiveKind kind : values()) {
            if (kind.name().equalsIgnoreCase(id)) {
                return kind;
            }
        }
        return null;
    }
}
