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

public class EmergencyDishItem extends Item {

    public EmergencyDishItem() {
        super(new Properties().stacksTo(64).food(
            new FoodProperties.Builder()
                .nutrition(4)
                .saturationMod(0.5f)
                .fast()
                .build()
        ));
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.literal("§c[应急料理]"));
        tooltip.add(Component.literal("§7瞬发恢复 §c10 §7血 + §e4 §7格饥饿"));
        tooltip.add(Component.literal("§7获得 §a5秒抗性提升II§7 + 免疫击退"));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof Player player) {
            player.heal(10);

            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 1, false, true));

            if (GourmetSystem.isGourmet(player) && !GourmetSystem.isWearingArmor(player)) {
                GourmetSystem.onFoodEaten((net.minecraft.server.level.ServerPlayer) player, stack);
            }
        }
        return super.finishUsingItem(stack, level, entity);
    }
}
