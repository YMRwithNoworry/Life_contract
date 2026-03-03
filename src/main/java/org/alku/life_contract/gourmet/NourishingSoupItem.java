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

public class NourishingSoupItem extends Item {

    public NourishingSoupItem() {
        super(new Properties().stacksTo(64).food(
            new FoodProperties.Builder()
                .nutrition(10)
                .saturationMod(1.0f)
                .build()
        ));
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.literal("§6[招牌料理] 滋补炖汤"));
        tooltip.add(Component.literal("§7恢复 §c12 §7血 + §e6 §7格饥饿"));
        tooltip.add(Component.literal("§7获得 §a15% §7伤害减免"));
        tooltip.add(Component.literal("§7每秒恢复 §c1 §7点生命值"));
        tooltip.add(Component.literal("§7持续时间: §b8分钟"));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof Player player) {
            player.heal(12);

            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 9600, 0, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 9600, 0, false, true));

            if (GourmetSystem.isGourmet(player) && !GourmetSystem.isWearingArmor(player)) {
                float multiplier = GourmetSystem.getFoodEffectMultiplier(player);
                int extendedDuration = (int)(9600 * multiplier);
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, extendedDuration, 1, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, extendedDuration, 1, false, true));
            }
        }
        return super.finishUsingItem(stack, level, entity);
    }
}
