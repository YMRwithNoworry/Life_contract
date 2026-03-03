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

public class SpicyHotPotItem extends Item {

    public SpicyHotPotItem() {
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
        tooltip.add(Component.literal("§6[招牌料理] 辛辣干锅"));
        tooltip.add(Component.literal("§7恢复 §c10 §7血 + §e5 §7格饥饿"));
        tooltip.add(Component.literal("§7攻击附加 §c3 §7点火焰伤害"));
        tooltip.add(Component.literal("§7近战命中附加 §a20% §7减速"));
        tooltip.add(Component.literal("§7持续时间: §b5分钟"));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof Player player) {
            player.heal(10);

            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 6000, 0, false, true));

            if (GourmetSystem.isGourmet(player) && !GourmetSystem.isWearingArmor(player)) {
                float multiplier = GourmetSystem.getFoodEffectMultiplier(player);
                int extendedDuration = (int)(6000 * multiplier);
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, extendedDuration, 1, false, true));
            }
        }
        return super.finishUsingItem(stack, level, entity);
    }
}
