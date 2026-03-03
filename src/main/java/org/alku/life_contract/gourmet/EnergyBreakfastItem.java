package org.alku.life_contract.gourmet;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class EnergyBreakfastItem extends Item {

    public EnergyBreakfastItem() {
        super(new Properties().stacksTo(64).food(
            new FoodProperties.Builder()
                .nutrition(8)
                .saturationMod(0.8f)
                .build()
        ));
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.literal("§6[招牌料理] 元气早餐"));
        tooltip.add(Component.literal("§7恢复 §c8 §7血 + §e4 §7格饥饿"));
        tooltip.add(Component.literal("§7获得 §a20% §7移速 + §a20% §7挖掘速度"));
        tooltip.add(Component.literal("§7免疫挖掘疲劳"));
        tooltip.add(Component.literal("§7持续时间: §b10分钟"));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof Player player) {
            player.heal(8);

            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 12000, 0, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 12000, 0, false, true));

            if (GourmetSystem.isGourmet(player) && !GourmetSystem.isWearingArmor(player)) {
                float multiplier = GourmetSystem.getFoodEffectMultiplier(player);
                int extendedDuration = (int)(12000 * multiplier);
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, extendedDuration, 1, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, extendedDuration, 1, false, true));
            }
        }
        return super.finishUsingItem(stack, level, entity);
    }
}
