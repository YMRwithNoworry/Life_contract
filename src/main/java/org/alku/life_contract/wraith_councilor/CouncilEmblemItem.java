package org.alku.life_contract.wraith_councilor;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CouncilEmblemItem extends Item {
    
    public CouncilEmblemItem(Properties properties) {
        super(properties.stacksTo(1));
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        
        tooltipComponents.add(Component.literal(""));
        tooltipComponents.add(Component.literal("§5冥府议员徽记").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltipComponents.add(Component.literal(""));
        tooltipComponents.add(Component.literal("核心效果:").withStyle(ChatFormatting.GOLD));
        tooltipComponents.add(Component.literal("  §7- 召唤物生命值 +30%").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.literal("  §7- 召唤物伤害 +20%").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.literal("  §7- 受到致命伤害时, 每 20 点剩余冥魂值").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.literal("    §7转化为 1 点临时生命值, 最低保留 1 点血").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.literal("  §7- 冷却时间: 90 秒").withStyle(ChatFormatting.GRAY));
    }
    
    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
