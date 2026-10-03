package org.alku.life_contract.accessory;

import net.minecraft.ChatFormatting;

/**
 * 饰品派系。佩戴多件同派系饰品会触发共鸣（见 {@link AccessoryResonance}）。
 */
public enum AccessoryFaction {
    NONE("none", "无派系", "None", ChatFormatting.DARK_GRAY),
    CRIMSON("crimson", "绯红", "Crimson", ChatFormatting.RED),
    AZURE("azure", "蔚蓝", "Azure", ChatFormatting.AQUA),
    AMETHYST("amethyst", "紫晶", "Amethyst", ChatFormatting.LIGHT_PURPLE),
    OBSIDIAN("obsidian", "黑曜", "Obsidian", ChatFormatting.DARK_PURPLE),
    VERDANT("verdant", "苍翠", "Verdant", ChatFormatting.GREEN),
    GILDED("gilded", "鎏金", "Gilded", ChatFormatting.GOLD);

    private final String id;
    private final String zh;
    private final String en;
    private final ChatFormatting color;

    AccessoryFaction(String id, String zh, String en, ChatFormatting color) {
        this.id = id;
        this.zh = zh;
        this.en = en;
        this.color = color;
    }

    public String id() {
        return id;
    }

    public String displayZh() {
        return zh;
    }

    public String displayEn() {
        return en;
    }

    public ChatFormatting color() {
        return color;
    }

    public static AccessoryFaction byId(String id) {
        if (id == null || id.isBlank()) {
            return NONE;
        }
        for (AccessoryFaction faction : values()) {
            if (faction.id.equalsIgnoreCase(id)) {
                return faction;
            }
        }
        return NONE;
    }
}
