package org.alku.life_contract.gourmet;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.UUID;

public class LegendaryFeastItem extends Item {

    private static final UUID HEALTH_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-legendary001");

    public LegendaryFeastItem() {
        super(new Properties().stacksTo(16).food(
            new FoodProperties.Builder()
                .nutrition(20)
                .saturationMod(2.0f)
                .build()
        ));
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.literal("§d[传世料理] 传世满汉全席"));
        tooltip.add(Component.literal("§7恢复 §c20 §7血 + §e满饥饿"));
        tooltip.add(Component.literal("§7获得 §c抗性提升IV§7、§c力量III§7、§b速度II"));
        tooltip.add(Component.literal("§7免疫所有伤害"));
        tooltip.add(Component.literal("§7持续时间: §b3分钟"));
        tooltip.add(Component.literal("§d永久最大生命值 +5%（每玩家仅生效1次/游戏日）"));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof Player player) {
            player.heal(20);
            player.getFoodData().setFoodLevel(20);

            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 3600, 3, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 3600, 2, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 3600, 1, false, true));

            if (GourmetSystem.isGourmet(player) && !GourmetSystem.isWearingArmor(player)) {
                long lastMeal = player.getPersistentData().getLong(GourmetSystem.TAG_DAILY_LEGENDARY_MEAL);
                long currentTime = level.getGameTime();

                if (currentTime - lastMeal >= 24000) {
                    GourmetSystem.addPermanentHealth((net.minecraft.server.level.ServerPlayer) player, 1.0f);
                    player.getPersistentData().putLong(GourmetSystem.TAG_DAILY_LEGENDARY_MEAL, currentTime);
                } else {
                    player.sendSystemMessage(Component.literal("§7[传世料理] 今日已获得永久生命加成"));
                }
            }
        }
        return super.finishUsingItem(stack, level, entity);
    }
}
