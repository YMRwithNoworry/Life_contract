package org.alku.life_contract.gourmet;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.alku.life_contract.ContractEvents;

import java.util.List;

public class SeasoningBoxItem extends Item {

    public SeasoningBoxItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.literal("§6[专属饰品] 万能调味盒"));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("§e被动效果:"));
        tooltip.add(Component.literal("§7- 食物增益持续时间 §a+100%"));
        tooltip.add(Component.literal("§7- 鲜味值获取速度 §a+60%"));
        tooltip.add(Component.literal("§7- 每20秒抵消1次负面效果"));
        tooltip.add(Component.literal("§7- 投喂队友时清除负面效果"));
        tooltip.add(Component.literal("§7- 永久免疫火焰/岩浆伤害"));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!GourmetSystem.isGourmet(player) || GourmetSystem.isWearingArmor(player)) {
            return InteractionResultHolder.pass(stack);
        }

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            applySeasoning(serverPlayer);
        }

        return InteractionResultHolder.success(stack);
    }

    private void applySeasoning(ServerPlayer player) {
        int seasoningCd = player.getPersistentData().getInt(GourmetSystem.TAG_SEASONING_NEGATIVE_COOLDOWN);
        if (seasoningCd > 0) {
            player.sendSystemMessage(Component.literal("§c[调味盒] 冷却中！"));
            return;
        }

        boolean removed = false;

        if (player.hasEffect(MobEffects.POISON)) {
            player.removeEffect(MobEffects.POISON);
            removed = true;
        }
        if (player.hasEffect(MobEffects.HUNGER)) {
            player.removeEffect(MobEffects.HUNGER);
            removed = true;
        }
        if (player.hasEffect(MobEffects.CONFUSION)) {
            player.removeEffect(MobEffects.CONFUSION);
            removed = true;
        }
        if (player.hasEffect(MobEffects.WITHER)) {
            player.removeEffect(MobEffects.WITHER);
            removed = true;
        }

        if (removed) {
            GourmetSystem.addUmami(player, 10);
            player.getPersistentData().putInt(GourmetSystem.TAG_SEASONING_NEGATIVE_COOLDOWN, 
                GourmetSystem.SEASONING_NEGATIVE_COOLDOWN);

            player.sendSystemMessage(Component.literal("§a[调味盒] 清除负面效果！鲜味值+10"));

            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.GENERIC_DRINK, SoundSource.PLAYERS, 1.0f, 1.5f);
        } else {
            player.sendSystemMessage(Component.literal("§7[调味盒] 没有可清除的负面效果"));
        }
    }

    public static void onFeedAlly(ServerPlayer gourmet, ServerPlayer ally) {
        if (!GourmetSystem.isGourmet(gourmet) || GourmetSystem.isWearingArmor(gourmet)) {
            return;
        }

        ally.removeAllEffects();

        ally.addEffect(new MobEffectInstance(MobEffects.SATURATION, 160, 0, false, true));

        gourmet.sendSystemMessage(Component.literal("§a[调味盒] 为队友清除负面效果！"));
        ally.sendSystemMessage(Component.literal("§a[调味增益] 获得食物效果翻倍，持续8秒！"));
    }

    public static float getUmamiGainBonus(Player player) {
        if (!GourmetSystem.isGourmet(player) || GourmetSystem.isWearingArmor(player)) {
            return 1.0f;
        }

        boolean hasSeasoning = false;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i).getItem() instanceof SeasoningBoxItem) {
                hasSeasoning = true;
                break;
            }
        }

        return hasSeasoning ? 1.6f : 1.0f;
    }

    public static float getFoodDurationBonus(Player player) {
        if (!GourmetSystem.isGourmet(player) || GourmetSystem.isWearingArmor(player)) {
            return 1.0f;
        }

        boolean hasSeasoning = false;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i).getItem() instanceof SeasoningBoxItem) {
                hasSeasoning = true;
                break;
            }
        }

        return hasSeasoning ? 2.0f : 1.0f;
    }
}
