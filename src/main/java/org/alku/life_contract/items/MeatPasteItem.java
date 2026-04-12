package org.alku.life_contract.items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class MeatPasteItem extends Item {

    public MeatPasteItem() {
        super(new Properties()
            .stacksTo(64)
            .rarity(Rarity.UNCOMMON)
        );
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.literal("\u00a77\u7531\u611f\u67d3\u751f\u7269\u7684\u6389\u843d\u7269\u78e8\u5236\u800c\u6210"));
        tooltip.add(Component.literal("\u00a77\u53ef\u7528\u4e8e\u5236\u4f5c\u5b62\u5b50\u70b8\u5f39"));
    }
}
