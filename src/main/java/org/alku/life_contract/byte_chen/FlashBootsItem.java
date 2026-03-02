package org.alku.life_contract.byte_chen;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FlashBootsItem extends ArmorItem {
    
    public FlashBootsItem(ArmorMaterial material, Properties properties) {
        super(material, Type.BOOTS, properties.stacksTo(1));
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        
        tooltipComponents.add(Component.literal(""));
        tooltipComponents.add(Component.literal("§b数据终端套装 - 靴子").withStyle(ChatFormatting.AQUA));
        tooltipComponents.add(Component.literal(""));
        tooltipComponents.add(Component.literal("套装效果:").withStyle(ChatFormatting.GOLD));
        tooltipComponents.add(Component.literal("  §7- 算力值上限 +50 点").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.literal("  §7- 信息节点生效范围 +20%").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.literal("  §7- 信息读取类技能范围 +10 格").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.literal(""));
        tooltipComponents.add(Component.literal("负面效果:").withStyle(ChatFormatting.RED));
        tooltipComponents.add(Component.literal("  §c- 受到近战伤害 +20%").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.literal("  §c- 近战攻击伤害 -45%").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.literal("  §c- 全套穿戴时无法使用盾牌").withStyle(ChatFormatting.GRAY));
    }
    
    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
