package org.alku.life_contract.accessory;

import net.minecraft.resources.ResourceLocation;
import org.alku.life_contract.Life_contract;

import java.util.List;

/** 一条饰品定义，来自 data/life_contract/accessory_catalog.json。 */
public record AccessoryDefinition(
        String id,
        String texture,
        AccessoryCategory category,
        AccessoryFaction faction,
        int tier,
        String nameZh,
        String nameEn,
        String effectZh,
        String effectEn,
        List<AccessoryEffect> effects,
        AccessoryCharge charge,
        AccessoryActive active,
        List<AccessoryEffect> consume,
        int price,
        List<String> recipeIngredients,
        int recipeSublimation) {

    public ResourceLocation itemId() {
        return ResourceLocation.fromNamespaceAndPath(Life_contract.MODID, id);
    }

    /** 物品名称的翻译键（原版会按 item.life_contract.<id> 查找）。 */
    public String nameKey() {
        return "item." + Life_contract.MODID + "." + id;
    }

    /** 效果说明的翻译键。 */
    public String effectKey() {
        return "tooltip." + Life_contract.MODID + "." + id + ".effect";
    }

    public boolean isEquippable() {
        return category.isEquippable();
    }

    public boolean isConsumable() {
        return category == AccessoryCategory.CONSUMABLE;
    }

    public boolean isMaterial() {
        return category == AccessoryCategory.MATERIAL;
    }

    public boolean hasFaction() {
        return faction != AccessoryFaction.NONE;
    }

    /** 常驻效果的数值合计（不含情境效果）。 */
    public double total(AccessoryEffectType type) {
        double sum = 0.0D;
        for (AccessoryEffect effect : effects) {
            if (effect.type() == type && effect.isAlways()) {
                sum += effect.value();
            }
        }
        return sum;
    }

    /** 任意条件下是否存在该类型效果（用于 tooltip 判断是否展示）。 */
    public boolean has(AccessoryEffectType type) {
        for (AccessoryEffect effect : effects) {
            if (effect.type() == type) {
                return true;
            }
        }
        return false;
    }

    /** 是否带明确的负面代价。 */
    public boolean hasDrawback() {
        for (AccessoryEffect effect : effects) {
            if (effect.value() < 0.0D) {
                return true;
            }
        }
        return false;
    }
}
