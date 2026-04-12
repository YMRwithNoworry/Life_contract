package org.alku.life_contract;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class ChaosBalanceItem extends Item implements ICurioItem {
    private static final UUID ATTACK_DAMAGE_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final UUID MAX_HEALTH_UUID = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345678901");
    private static final UUID ARMOR_UUID = UUID.fromString("c3d4e5f6-a7b8-9012-cdef-123456789012");
    private static final UUID MOVEMENT_SPEED_UUID = UUID.fromString("d4e5f6a7-b8c9-0123-def1-234567890123");
    
    private static final String TAG_BONUS_PERCENT = "BonusPercent";
    private static final String TAG_INFECTED_COUNT = "InfectedCount";
    private static final String TAG_NON_INFECTED_COUNT = "NonInfectedCount";
    
    public static final double MAX_BONUS_PERCENT = 0.40;
    public static final double BALANCED_HEALTH_PENALTY = -4.0;

    public ChaosBalanceItem() {
        super(new Item.Properties()
                .stacksTo(1)
                .rarity(Rarity.RARE));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.literal("§5混沌天平"));
        tooltip.add(Component.literal("§7场上感染/非感染玩家数量越不平衡"));
        tooltip.add(Component.literal("§7获得的增益越高 (最高+40%)"));
        tooltip.add(Component.literal("§c当两类玩家相等时，血量上限-4"));
        tooltip.add(Component.literal(""));
        
        double bonusPercent;
        int infectedCount;
        int nonInfectedCount;
        
        if (level != null && level.isClientSide) {
            bonusPercent = ClientDataStorage.getChaosBalanceBonus();
            infectedCount = ClientDataStorage.getChaosInfectedCount();
            nonInfectedCount = ClientDataStorage.getChaosNonInfectedCount();
        } else {
            CompoundTag tag = stack.getOrCreateTag();
            bonusPercent = tag.getDouble(TAG_BONUS_PERCENT);
            infectedCount = tag.getInt(TAG_INFECTED_COUNT);
            nonInfectedCount = tag.getInt(TAG_NON_INFECTED_COUNT);
        }
        
        tooltip.add(Component.literal("§6当前状态:"));
        tooltip.add(Component.literal("§7  感染玩家: §c" + infectedCount));
        tooltip.add(Component.literal("§7  非感染玩家: §a" + nonInfectedCount));
        
        if (bonusPercent > 0) {
            tooltip.add(Component.literal("§e  加成系数: §b+" + String.format("%.1f", bonusPercent * 100) + "%"));
        } else if (bonusPercent < 0) {
            tooltip.add(Component.literal("§c  血量惩罚: §4" + (int)BALANCED_HEALTH_PENALTY));
        } else if (infectedCount == nonInfectedCount && infectedCount > 0) {
            tooltip.add(Component.literal("§c  血量惩罚: §4" + (int)BALANCED_HEALTH_PENALTY));
        } else {
            tooltip.add(Component.literal("§7  加成系数: §f0%"));
        }
        
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("§6增益类型:"));
        tooltip.add(Component.literal("§7  攻击伤害、最大生命值、护甲、移动速度"));
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        if (slotContext.entity() instanceof Player player) {
            ChaosBalanceEvents.updatePlayerBonus(player);
        }
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        if (slotContext.entity() instanceof Player player) {
            removeAttributes(player);
        }
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if (slotContext.entity() instanceof Player player && !player.level().isClientSide) {
            if (player.tickCount % 40 == 0) {
                ChaosBalanceEvents.updatePlayerBonus(player);
            }
        }
    }

    public static void applyAttributes(Player player, double bonusPercent) {
        removeAttributes(player);
        
        if (bonusPercent > 0) {
            player.getAttribute(Attributes.ATTACK_DAMAGE).addPermanentModifier(
                    new AttributeModifier(ATTACK_DAMAGE_UUID, "chaos_balance_attack_damage", 
                            bonusPercent, AttributeModifier.Operation.MULTIPLY_TOTAL));
            
            player.getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(
                    new AttributeModifier(MAX_HEALTH_UUID, "chaos_balance_max_health", 
                            bonusPercent, AttributeModifier.Operation.MULTIPLY_TOTAL));
            
            player.getAttribute(Attributes.ARMOR).addPermanentModifier(
                    new AttributeModifier(ARMOR_UUID, "chaos_balance_armor", 
                            bonusPercent, AttributeModifier.Operation.MULTIPLY_TOTAL));
            
            player.getAttribute(Attributes.MOVEMENT_SPEED).addPermanentModifier(
                    new AttributeModifier(MOVEMENT_SPEED_UUID, "chaos_balance_move_speed", 
                            bonusPercent, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
    }

    public static void applyBalancedPenalty(Player player) {
        removeAttributes(player);
        
        player.getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(
                new AttributeModifier(MAX_HEALTH_UUID, "chaos_balance_penalty", 
                        BALANCED_HEALTH_PENALTY, AttributeModifier.Operation.ADDITION));
    }

    public static void removeAttributes(Player player) {
        removeModifierIfExists(player, Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE_UUID);
        removeModifierIfExists(player, Attributes.MAX_HEALTH, MAX_HEALTH_UUID);
        removeModifierIfExists(player, Attributes.ARMOR, ARMOR_UUID);
        removeModifierIfExists(player, Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED_UUID);
    }

    private static void removeModifierIfExists(Player player, Attribute attribute, UUID uuid) {
        var attrInstance = player.getAttribute(attribute);
        if (attrInstance != null) {
            var modifier = attrInstance.getModifier(uuid);
            if (modifier != null) {
                attrInstance.removeModifier(uuid);
            }
        }
    }

    @Override
    public boolean canUnequip(SlotContext slotContext, ItemStack stack) {
        return true;
    }
    
    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        if (slotContext.entity() instanceof Player player) {
            return !hasSameAccessoryEquipped(player, this.getClass());
        }
        return true;
    }
    
    private boolean hasSameAccessoryEquipped(Player player, Class<?> itemClass) {
        var curiosHelper = top.theillusivec4.curios.api.CuriosApi.getCuriosHelper();
        var handlerOpt = curiosHelper.getCuriosHandler(player).resolve();
        if (handlerOpt.isEmpty()) return false;
        
        var handler = handlerOpt.get();
        for (var entry : handler.getCurios().entrySet()) {
            var stacksHandler = entry.getValue();
            var stacks = stacksHandler.getStacks();
            for (int i = 0; i < stacks.getSlots(); i++) {
                var equippedStack = stacks.getStackInSlot(i);
                if (!equippedStack.isEmpty() && itemClass.isInstance(equippedStack.getItem())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int getEnchantmentValue() {
        return 0;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }
}
