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
import java.util.Set;
import java.util.HashSet;

public class SurvivorEmblemItem extends Item implements ICurioItem {
    private static final String TAG_KILLS = "SurvivorKills";
    private static final UUID ATTACK_DAMAGE_UUID = UUID.fromString("8e3c4a5b-6d7e-8f9a-0b1c-2d3e4f5a6b7c");
    private static final UUID MAX_HEALTH_UUID = UUID.fromString("9f4d5b6c-7e8f-9a0b-1c2d-3e4f5a6b7c8d");
    private static final UUID ARMOR_UUID = UUID.fromString("0a5e6c7d-8f9a-0b1c-2d3e-4f5a6b7c8d9e");
    
    public static final double ATTACK_DAMAGE_PER_KILL = 0.5;
    public static final double MAX_HEALTH_PER_KILL = 2.0;
    public static final double ARMOR_PER_KILL = 0.5;

    public SurvivorEmblemItem() {
        super(new Item.Properties()
                .stacksTo(1)
                .rarity(Rarity.UNCOMMON));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        int kills = getKills(stack);
        tooltip.add(Component.literal("§6击杀数: §e" + kills));
        tooltip.add(Component.literal("§7击杀玩家以获得永久属性加成"));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("§a当前加成:"));
        tooltip.add(Component.literal("§7  攻击伤害: §e+" + (kills * ATTACK_DAMAGE_PER_KILL)));
        tooltip.add(Component.literal("§7  最大生命值: §e+" + (kills * MAX_HEALTH_PER_KILL)));
        tooltip.add(Component.literal("§7  护甲值: §e+" + (kills * ARMOR_PER_KILL)));
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        if (slotContext.entity() instanceof Player player) {
            applyAttributes(player, stack);
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
            if (player.tickCount % 20 == 0) {
                applyAttributes(player, stack);
            }
        }
    }

    public void applyAttributes(Player player, ItemStack stack) {
        int kills = getKills(stack);
        
        removeAttributes(player);
        
        if (kills > 0) {
            double attackDamage = kills * ATTACK_DAMAGE_PER_KILL;
            double maxHealth = kills * MAX_HEALTH_PER_KILL;
            double armor = kills * ARMOR_PER_KILL;
            
            player.getAttribute(Attributes.ATTACK_DAMAGE).addPermanentModifier(
                    new AttributeModifier(ATTACK_DAMAGE_UUID, "survivor_emblem_attack_damage", 
                            attackDamage, AttributeModifier.Operation.ADDITION));
            
            player.getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(
                    new AttributeModifier(MAX_HEALTH_UUID, "survivor_emblem_max_health", 
                            maxHealth, AttributeModifier.Operation.ADDITION));
            
            player.getAttribute(Attributes.ARMOR).addPermanentModifier(
                    new AttributeModifier(ARMOR_UUID, "survivor_emblem_armor", 
                            armor, AttributeModifier.Operation.ADDITION));
        }
    }

    public void removeAttributes(Player player) {
        removeModifierIfExists(player, Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE_UUID);
        removeModifierIfExists(player, Attributes.MAX_HEALTH, MAX_HEALTH_UUID);
        removeModifierIfExists(player, Attributes.ARMOR, ARMOR_UUID);
    }

    private void removeModifierIfExists(Player player, Attribute attribute, UUID uuid) {
        var attrInstance = player.getAttribute(attribute);
        if (attrInstance != null) {
            var modifier = attrInstance.getModifier(uuid);
            if (modifier != null) {
                attrInstance.removeModifier(uuid);
            }
        }
    }

    public static int getKills(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        return tag.getInt(TAG_KILLS);
    }

    public static void setKills(ItemStack stack, int kills) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(TAG_KILLS, kills);
    }

    public static void addKill(ItemStack stack) {
        int kills = getKills(stack);
        setKills(stack, kills + 1);
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
