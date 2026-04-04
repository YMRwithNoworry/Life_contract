package org.alku.life_contract;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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

public class SporeGlandItem extends Item implements ICurioItem {
    private static final String TAG_INFECTION = "SporeInfection";
    private static final UUID MOVEMENT_SPEED_UUID = UUID.fromString("1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d");
    private static final int MAX_INFECTION = 100;
    private static final int INFECTION_PER_SECOND = 1;
    private static final double SLOWDOWN_AT_50_PERCENT = 0.2;

    public SporeGlandItem() {
        super(new Item.Properties()
                .stacksTo(1)
                .rarity(Rarity.RARE));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        int infection = getInfection(stack);
        double infectionPercent = (infection * 100.0) / MAX_INFECTION;
        
        tooltip.add(Component.literal("§a孢子腺体"));
        tooltip.add(Component.literal("§7攻击时30%概率传播孢子"));
        tooltip.add(Component.literal("§7孢子会在5秒后生成小孢子怪"));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("§c感染值: §e" + infection + "/" + MAX_INFECTION + " (" + String.format("%.1f", infectionPercent) + "%)"));
        
        if (infection >= MAX_INFECTION / 2) {
            tooltip.add(Component.literal("§c[警告] 感染值超过50%，减速20%"));
        }
        
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("§6击杀孢子精英可获得此饰品"));
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if (slotContext.entity() instanceof Player player && !player.level().isClientSide) {
            if (player.tickCount % 20 == 0) {
                int infection = getInfection(stack);
                if (infection < MAX_INFECTION) {
                    setInfection(stack, infection + INFECTION_PER_SECOND);
                }
                updateSpeedModifier(player, stack);
            }
        }
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        if (slotContext.entity() instanceof Player player) {
            updateSpeedModifier(player, stack);
        }
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        if (slotContext.entity() instanceof Player player) {
            removeSpeedModifier(player);
        }
    }

    private void updateSpeedModifier(Player player, ItemStack stack) {
        removeSpeedModifier(player);
        
        int infection = getInfection(stack);
        if (infection >= MAX_INFECTION / 2) {
            player.getAttribute(Attributes.MOVEMENT_SPEED).addPermanentModifier(
                    new AttributeModifier(MOVEMENT_SPEED_UUID, "spore_gland_slowdown", 
                            -SLOWDOWN_AT_50_PERCENT, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
    }

    private void removeSpeedModifier(Player player) {
        var speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr != null && speedAttr.getModifier(MOVEMENT_SPEED_UUID) != null) {
            speedAttr.removeModifier(MOVEMENT_SPEED_UUID);
        }
    }

    public static int getInfection(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        return tag.getInt(TAG_INFECTION);
    }

    public static void setInfection(ItemStack stack, int infection) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(TAG_INFECTION, Math.min(infection, MAX_INFECTION));
    }

    public static void addInfection(ItemStack stack, int amount) {
        int infection = getInfection(stack);
        setInfection(stack, infection + amount);
    }

    public static void resetInfection(ItemStack stack) {
        setInfection(stack, 0);
    }

    public static int getMaxInfection() {
        return MAX_INFECTION;
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    @Override
    public boolean canUnequip(SlotContext slotContext, ItemStack stack) {
        return true;
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
