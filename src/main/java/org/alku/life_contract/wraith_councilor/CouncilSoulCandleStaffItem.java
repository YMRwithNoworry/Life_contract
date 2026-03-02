package org.alku.life_contract.wraith_councilor;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class CouncilSoulCandleStaffItem extends SwordItem {
    
    private static final UUID RANGE_BONUS_UUID = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345678901");
    private static final UUID DAMAGE_BONUS_UUID = UUID.fromString("c3d4e5f6-a7b8-9012-cdef-123456789012");
    
    public CouncilSoulCandleStaffItem(Tier tier, int attackDamage, float attackSpeed, Properties properties) {
        super(tier, attackDamage, attackSpeed, properties.stacksTo(1));
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        
        tooltipComponents.add(Component.literal(""));
        tooltipComponents.add(Component.literal("§5议会魂烛法杖").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltipComponents.add(Component.literal(""));
        tooltipComponents.add(Component.literal("基础属性:").withStyle(ChatFormatting.GOLD));
        tooltipComponents.add(Component.literal("  §7- 近战伤害: 4 点").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.literal("  §7- 攻击速度: 1.0").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.literal(""));
        tooltipComponents.add(Component.literal("核心效果:").withStyle(ChatFormatting.GOLD));
        tooltipComponents.add(Component.literal("  §7- 所有法术施法距离 +2 格").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.literal("  §7- 技能范围 +10%").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.literal("  §7- 暗蚀 debuff 持续时间 +50%").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.literal("  §7- 副手空置时, 每秒额外回复 1 点冥魂值").withStyle(ChatFormatting.GRAY));
    }
    
    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
