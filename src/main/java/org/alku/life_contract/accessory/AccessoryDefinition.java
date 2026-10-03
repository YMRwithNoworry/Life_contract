package org.alku.life_contract.accessory;

import net.minecraft.resources.ResourceLocation;
import org.alku.life_contract.Life_contract;

import java.util.List;

/** 一条饰品定义，来自 data/life_contract/accessory_catalog.json。 */
public record AccessoryDefinition(
        String id,
        String texture,
        AccessoryCategory category,
        int tier,
        String nameZh,
        String nameEn,
        String effectZh,
        String effectEn,
        List<AccessoryEffect> effects,
        AccessoryEffect consume,
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

    /** 单条效果的数值（同类型多条时累加）。 */
    public double total(AccessoryEffectType type) {
        double sum = 0.0D;
        for (AccessoryEffect effect : effects) {
            if (effect.type() == type) {
                sum += effect.value();
            }
        }
        return sum;
    }

    public boolean has(AccessoryEffectType type) {
        for (AccessoryEffect effect : effects) {
            if (effect.type() == type) {
                return true;
            }
        }
        return false;
    }
}