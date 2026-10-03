package org.alku.life_contract.items;
import net.neoforged.fml.common.EventBusSubscriber;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import org.alku.life_contract.Life_contract;
import org.alku.life_contract.accessory.AccessoryEffects;

import java.util.Random;

@EventBusSubscriber(modid = Life_contract.MODID)
public final class SublimationDropHandler {

    private static final Random RANDOM = new Random();

    private SublimationDropHandler() {}

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (event.getEntity().level().isClientSide) return;

        ItemEntity drop = new ItemEntity(
                event.getEntity().level(),
                event.getEntity().getX(),
                event.getEntity().getY(),
                event.getEntity().getZ(),
                new ItemStack(Life_contract.SUBLIMATION.get(), dropCount(event))
        );
        event.getDrops().add(drop);
    }

    /** 基础掉落 1 个；击杀者的饰品可能提供额外掉落（SUBLIMATION_PERCENT）。 */
    private static int dropCount(LivingDropsEvent event) {
        Player killer = resolveKiller(event);
        if (killer == null) {
            return 1;
        }

        double bonus = AccessoryEffects.sublimationBonus(killer);
        if (bonus <= 0.0D) {
            return 1;
        }

        int count = 1 + (int) Math.floor(bonus);
        if (RANDOM.nextDouble() < bonus - Math.floor(bonus)) {
            count++;
        }
        return count;
    }

    private static Player resolveKiller(LivingDropsEvent event) {
        Entity attacker = event.getSource().getEntity();
        if (attacker instanceof Player player) {
            return player;
        }
        LivingEntity victim = event.getEntity();
        if (victim instanceof ServerPlayer) {
            return null;
        }
        return victim.getKillCredit() instanceof Player credit ? credit : null;
    }
}
