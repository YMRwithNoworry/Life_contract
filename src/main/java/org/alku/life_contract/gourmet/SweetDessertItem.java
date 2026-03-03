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

public class SweetDessertItem extends Item {

    public SweetDessertItem() {
        super(new Properties().stacksTo(64).food(
            new FoodProperties.Builder()
                .nutrition(6)
                .saturationMod(0.6f)
                .build()
        ));
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.literal("§6[招牌料理] 甜心甜品"));
        tooltip.add(Component.literal("§7恢复 §c6 §7血 + §e3 §7格饥饿"));
        tooltip.add(Component.literal("§7清除所有负面效果"));
        tooltip.add(Component.literal("§7免疫负面效果"));
        tooltip.add(Component.literal("§7获得 §a10% §7闪避"));
        tooltip.add(Component.literal("§7持续时间: §b6分钟"));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof Player player) {
            player.heal(6);

            player.removeAllEffects();

            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 7200, 0, false, true));

            if (GourmetSystem.isGourmet(player) && !GourmetSystem.isWearingArmor(player)) {
                float multiplier = GourmetSystem.getFoodEffectMultiplier(player);
                int extendedDuration = (int)(7200 * multiplier);
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, extendedDuration, 1, false, true));
            }
        }
        return super.finishUsingItem(stack, level, entity);
    }
}
