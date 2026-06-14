package org.alku.life_contract.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class LifeTopologyItem extends Item {

    public LifeTopologyItem() {
        super(new Properties().stacksTo(1).rarity(Rarity.RARE));
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("背包或队伍背包中持有时显示左上角战术地图").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("M 单击关闭，双击切换地图视角").withStyle(ChatFormatting.DARK_GRAY));
    }
}
