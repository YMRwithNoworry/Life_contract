package org.alku.life_contract.accessory;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 饰品效果类型。
 * <p>
 * 数值含义随上下文变化：
 * <ul>
 *   <li>属性类：佩戴时直接加在属性上（{@code PERCENT} 为真的按百分比乘算）；</li>
 *   <li>持续药水类：佩戴时 value = 等级，作为消耗品使用时 value = 持续秒数；</li>
 *   <li>HEAL / FEED / XP 只用于消耗品。</li>
 * </ul>
 */
public enum AccessoryEffectType {
    MAX_HEALTH(Kind.ATTRIBUTE, Attributes.MAX_HEALTH, AttributeModifier.Operation.ADD_VALUE, false, null),
    ARMOR(Kind.ATTRIBUTE, Attributes.ARMOR, AttributeModifier.Operation.ADD_VALUE, false, null),
    ARMOR_TOUGHNESS(Kind.ATTRIBUTE, Attributes.ARMOR_TOUGHNESS, AttributeModifier.Operation.ADD_VALUE, false, null),
    ATTACK_DAMAGE(Kind.ATTRIBUTE, Attributes.ATTACK_DAMAGE, AttributeModifier.Operation.ADD_VALUE, false, null),
    ATTACK_SPEED(Kind.ATTRIBUTE, Attributes.ATTACK_SPEED, AttributeModifier.Operation.ADD_VALUE, false, null),
    MOVEMENT_SPEED_PERCENT(Kind.ATTRIBUTE, Attributes.MOVEMENT_SPEED,
            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, true, null),
    KNOCKBACK_RESISTANCE(Kind.ATTRIBUTE, Attributes.KNOCKBACK_RESISTANCE,
            AttributeModifier.Operation.ADD_VALUE, false, null),
    LUCK(Kind.ATTRIBUTE, Attributes.LUCK, AttributeModifier.Operation.ADD_VALUE, false, null),
    SUBLIMATION_PERCENT(Kind.SUBLIMATION, null, null, true, null),
    LIFESTEAL_PERCENT(Kind.LIFESTEAL, null, null, true, null),
    DAMAGE_REDUCTION_PERCENT(Kind.DAMAGE_REDUCTION, null, null, true, null),
    NIGHT_VISION(Kind.EFFECT, null, null, false, MobEffects.NIGHT_VISION),
    FIRE_RESISTANCE(Kind.EFFECT, null, null, false, MobEffects.FIRE_RESISTANCE),
    WATER_BREATHING(Kind.EFFECT, null, null, false, MobEffects.WATER_BREATHING),
    REGENERATION(Kind.EFFECT, null, null, false, MobEffects.REGENERATION),
    SATURATION(Kind.EFFECT, null, null, false, MobEffects.SATURATION),
    JUMP_BOOST(Kind.EFFECT, null, null, false, MobEffects.JUMP),
    HASTE(Kind.EFFECT, null, null, false, MobEffects.DIG_SPEED),
    RESISTANCE(Kind.EFFECT, null, null, false, MobEffects.DAMAGE_RESISTANCE),
    HEAL(Kind.HEAL, null, null, false, null),
    FEED(Kind.FEED, null, null, false, null),
    XP(Kind.EXPERIENCE, null, null, false, null);

    public enum Kind {
        ATTRIBUTE,
        EFFECT,
        SUBLIMATION,
        LIFESTEAL,
        DAMAGE_REDUCTION,
        HEAL,
        FEED,
        EXPERIENCE
    }

    private final Kind kind;
    private final Holder<Attribute> attribute;
    private final AttributeModifier.Operation operation;
    private final boolean percent;
    private final Holder<MobEffect> mobEffect;

    AccessoryEffectType(Kind kind, Holder<Attribute> attribute, AttributeModifier.Operation operation,
                        boolean percent, Holder<MobEffect> mobEffect) {
        this.kind = kind;
        this.attribute = attribute;
        this.operation = operation;
        this.percent = percent;
        this.mobEffect = mobEffect;
    }

    public Kind kind() {
        return kind;
    }

    public Holder<Attribute> attribute() {
        return attribute;
    }

    public AttributeModifier.Operation operation() {
        return operation;
    }

    /** 数值是否以百分比表示（内部会除以 100）。 */
    public boolean isPercent() {
        return percent;
    }

    public Holder<MobEffect> mobEffect() {
        return mobEffect;
    }

    /** 直接加在属性上的修饰值。 */
    public double modifierAmount(double value) {
        return percent ? value / 100.0D : value;
    }

    public static AccessoryEffectType byId(String id) {
        if (id == null) {
            return null;
        }
        for (AccessoryEffectType type : values()) {
            if (type.name().equalsIgnoreCase(id)) {
                return type;
            }
        }
        return null;
    }
}