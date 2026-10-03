package org.alku.life_contract.accessory;

import java.util.EnumSet;

/**
 * 一条具体效果：类型 + 数值 + 生效情境。
 *
 * @param when 生效条件，{@link AccessoryCondition#ALWAYS} 表示常驻
 */
public record AccessoryEffect(AccessoryEffectType type, double value, AccessoryCondition when) {

    public AccessoryEffect(AccessoryEffectType type, double value) {
        this(type, value, AccessoryCondition.ALWAYS);
    }

    public boolean isAlways() {
        return when == AccessoryCondition.ALWAYS;
    }

    /** 该效果在当前情境下是否生效。 */
    public boolean test(EnumSet<AccessoryCondition> conditions) {
        return conditions.contains(when);
    }

    /** 按倍数缩放数值，条件保持不变（用于充能层数）。 */
    public AccessoryEffect scaled(double factor) {
        return new AccessoryEffect(type, value * factor, when);
    }
}
