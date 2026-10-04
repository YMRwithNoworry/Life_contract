package org.alku.life_contract.market;

import net.minecraft.world.item.ItemStack;

/**
 * 升华商店里的一件商品。
 * <p>
 * <b>服务端只认 {@link #id()}</b>：客户端点"购买"时只发商品 id 与份数，
 * 价格、数量、发放内容全部由服务端查表决定 —— 客户端改不了价格，
 * 界面与服务端逻辑也不会因为不同步而发错东西。
 *
 * @param id             稳定 id（界面、脚本、以后的配置文件都用它）
 * @param category       一级分类（界面顶部的分类标签）
 * @param sectionKey     分类内部的二级分组标题语言键，可为 {@code null}（不分组）
 * @param template       发放模板（保留物品组件：TaCZ 弹种、药水效果等）
 * @param price          单份价格（升华）
 * @param quantity       单份数量
 * @param descriptionKey 一句话用途说明的语言键
 */
public record ShopProduct(String id, ShopCategory category, String sectionKey, ItemStack template,
                          int price, int quantity, String descriptionKey) {

    public ShopProduct {
        // 模板可能在别处被继续使用，这里存一份副本，避免外部改动影响商店
        template = template.copy();
    }

    /** 展示用名称（带物品本身的翻译）。 */
    public String displayName() {
        return template.getHoverName().getString();
    }
}
