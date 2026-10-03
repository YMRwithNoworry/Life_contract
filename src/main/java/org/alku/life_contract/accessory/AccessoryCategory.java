package org.alku.life_contract.accessory;

/**
 * 饰品类别。
 * <p>
 * 可佩戴类别（吊坠/戒指/护符/王冠/护身符）遵循"同类只生效一件、取品阶最高者"的规则，
 * 因此玩家最多同时获得 5 件饰品的加成；材料与消耗品不参与该规则。
 */
public enum AccessoryCategory {
    PENDANT("pendant", "吊坠", "Pendant", true),
    RING("ring", "戒指", "Ring", true),
    CHARM("charm", "护符", "Charm", true),
    CROWN("crown", "王冠", "Crown", true),
    AMULET("amulet", "护身符", "Amulet", true),
    CONSUMABLE("consumable", "消耗品", "Consumable", false),
    MATERIAL("material", "材料", "Material", false);

    private final String id;
    private final String displayZh;
    private final String displayEn;
    private final boolean equippable;

    AccessoryCategory(String id, String displayZh, String displayEn, boolean equippable) {
        this.id = id;
        this.displayZh = displayZh;
        this.displayEn = displayEn;
        this.equippable = equippable;
    }

    public String id() {
        return id;
    }

    public String displayZh() {
        return displayZh;
    }

    public String displayEn() {
        return displayEn;
    }

    /** 是否属于"同类只生效一件"的佩戴类别。 */
    public boolean isEquippable() {
        return equippable;
    }

    public static AccessoryCategory byId(String id) {
        for (AccessoryCategory category : values()) {
            if (category.id.equalsIgnoreCase(id)) {
                return category;
            }
        }
        return null;
    }
}