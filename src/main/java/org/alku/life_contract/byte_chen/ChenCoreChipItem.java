package org.alku.life_contract.byte_chen;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ChenCoreChipItem extends Item {
    
    public ChenCoreChipItem(Properties properties) {
        super(properties.stacksTo(1));
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        
        tooltipComponents.add(Component.literal(""));
        tooltipComponents.add(Component.literal("§b陈式核心芯片").withStyle(ChatFormatting.AQUA));
        tooltipComponents.add(Component.literal(""));
        tooltipComponents.add(Component.literal("核心效果:").withStyle(ChatFormatting.GOLD));
        tooltipComponents.add(Component.literal("  §7- 所有主动技能冷却时间缩短 15%").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.literal("  §7- 算力值低于 20 点时，触发「应急算力」").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.literal("    §e瞬间回复 50 点算力，冷却 60 秒").withStyle(ChatFormatting.YELLOW));
        tooltipComponents.add(Component.literal("  §7- 友方单位被击杀时:").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.literal("    §e- 瞬间获得 30 点算力").withStyle(ChatFormatting.YELLOW));
        tooltipComponents.add(Component.literal("    §e- 全图高亮击杀者位置，持续 10 秒").withStyle(ChatFormatting.YELLOW));
    }
    
    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
