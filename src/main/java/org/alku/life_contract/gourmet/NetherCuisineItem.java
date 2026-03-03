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

public class NetherCuisineItem extends Item {

    public NetherCuisineItem() {
        super(new Properties().stacksTo(64).food(
            new FoodProperties.Builder()
                .nutrition(9)
                .saturationMod(0.9f)
                .build()
        ));
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.literal("§6[招牌料理] 下界风味餐"));
        tooltip.add(Component.literal("§7恢复 §c10 §7血 + §e5 §7格饥饿"));
        tooltip.add(Component.literal("§7免疫火焰/岩浆伤害"));
        tooltip.add(Component.literal("§7下界环境全属性 §a+10%"));
        tooltip.add(Component.literal("§7持续时间: §b15分钟"));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof Player player) {
            player.heal(10);

            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 18000, 0, false, true));

            if (GourmetSystem.isGourmet(player) && !GourmetSystem.isWearingArmor(player)) {
                float multiplier = GourmetSystem.getFoodEffectMultiplier(player);
                int extendedDuration = (int)(18000 * multiplier);
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, extendedDuration, 1, false, true));
            }
        }
        return super.finishUsingItem(stack, level, entity);
    }
}
