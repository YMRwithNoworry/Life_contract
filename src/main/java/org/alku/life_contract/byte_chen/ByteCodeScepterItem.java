package org.alku.life_contract.byte_chen;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ByteCodeScepterItem extends SwordItem {
    
    public ByteCodeScepterItem(Tier tier, int attackDamage, float attackSpeed, Properties properties) {
        super(tier, attackDamage, attackSpeed, properties.stacksTo(1));
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        
        tooltipComponents.add(Component.literal(""));
        tooltipComponents.add(Component.literal("§b字节编码权杖").withStyle(ChatFormatting.AQUA));
        tooltipComponents.add(Component.literal(""));
        tooltipComponents.add(Component.literal("核心效果:").withStyle(ChatFormatting.GOLD));
        tooltipComponents.add(Component.literal("  §7- 部署信息节点的施法前摇缩短 50%").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.literal("  §7- 节点算力消耗降低 10%").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.literal("  §7- 所有信息类技能持续时间 +30%").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.literal("  §7- 副手空置时，算力值自然回复速度 +50%").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.literal(""));
        tooltipComponents.add(Component.literal("战斗属性:").withStyle(ChatFormatting.YELLOW));
        tooltipComponents.add(Component.literal("  §7- 伤害: 3 点").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.literal("  §7- 攻击速度: 1.2").withStyle(ChatFormatting.GRAY));
    }
    
    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
