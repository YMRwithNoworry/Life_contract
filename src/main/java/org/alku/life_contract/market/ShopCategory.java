package org.alku.life_contract.market;

/**
 * 升华商店的一级分类，对应界面顶部的一排分类标签。
 * <p>
 * 标签文案走语言文件：{@code gui.life_contract.shop.category.<id>}，
 * 以后要加分类只要在这里补一个枚举值 + 两条语言键即可。
 */
public enum ShopCategory {
    /** 食物、火把、建材等活下去要用的东西。 */
    SURVIVAL("survival"),
    /** 工具与护甲。 */
    GEAR("gear"),
    /** 药水、箭、金苹果等战斗辅助。 */
    COMBAT("combat"),
    /** 子弹：TaCZ 弹药与契约模组弹药。 */
    AMMO("ammo"),
    /** 枪械与配件（仅 TaCZ）。 */
    FIREARM("firearm"),
    /** 饰品与饰品材料。 */
    ACCESSORY("accessory"),
    /** 信号枪、稀有材料等。 */
    SPECIAL("special");

    private final String id;

    ShopCategory(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public String labelKey() {
        return "gui.life_contract.shop.category." + id;
    }
}
