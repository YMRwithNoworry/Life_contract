package org.alku.life_contract.accessory;

/**
 * 主动技：按 G 键释放，带独立冷却。
 *
 * @param kind          技能种类
 * @param value         主数值（等级 / 治疗量 / 吸收量 / 弹射力度，含义随种类变化）
 * @param durationTicks 持续时长（tick）
 * @param cooldownTicks 冷却时长（tick）
 */
public record AccessoryActive(AccessoryActiveKind kind, double value, int durationTicks, int cooldownTicks) {

    public static final AccessoryActive NONE = new AccessoryActive(null, 0.0D, 0, 0);

    public boolean isEmpty() {
        return kind == null || cooldownTicks <= 0;
    }
}
