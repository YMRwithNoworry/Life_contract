package org.alku.life_contract.accessory;

import java.util.List;

/**
 * 派系共鸣：生效的佩戴类饰品中，同派系达到 2 件触发共鸣 I，达到 3 件触发共鸣 II。
 *
 * @param faction 派系
 * @param nameZh  共鸣名称
 * @param nameEn  共鸣名称（英文）
 */
public record AccessoryResonance(
        AccessoryFaction faction,
        String nameZh,
        String nameEn,
        String desc2Zh,
        String desc2En,
        List<AccessoryEffect> effects2,
        String desc3Zh,
        String desc3En,
        List<AccessoryEffect> effects3) {

    /** 共鸣档位：0 = 未触发，1 = 共鸣 I，2 = 共鸣 II。 */
    public static int tierFor(int sameFactionCount) {
        if (sameFactionCount >= 3) {
            return 2;
        }
        return sameFactionCount >= 2 ? 1 : 0;
    }

    public List<AccessoryEffect> effectsForTier(int tier) {
        if (tier >= 2) {
            return effects3;
        }
        return tier >= 1 ? effects2 : List.of();
    }

    public String descriptionForTier(int tier, boolean chinese) {
        if (tier >= 2) {
            return chinese ? desc3Zh : desc3En;
        }
        return tier >= 1 ? (chinese ? desc2Zh : desc2En) : "";
    }
}
