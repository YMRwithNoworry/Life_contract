package org.alku.life_contract.accessory;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import org.alku.life_contract.Life_contract;

/** 饰品的战斗类效果：受伤减免与吸血。 */
@EventBusSubscriber(modid = Life_contract.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class AccessoryCombatEvents {

    private AccessoryCombatEvents() {
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        double reduction = AccessoryEffects.damageReduction(player);
        if (reduction <= 0.0D) {
            return;
        }
        event.setAmount((float) (event.getAmount() * (1.0D - reduction)));
    }

    @SubscribeEvent
    public static void onDamageDealt(LivingDamageEvent.Post event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        if (!(event.getSource().getEntity() instanceof Player player) || !player.isAlive()) {
            return;
        }
        double lifesteal = AccessoryEffects.lifesteal(player);
        if (lifesteal <= 0.0D) {
            return;
        }
        float heal = (float) (event.getNewDamage() * lifesteal);
        if (heal > 0.0F) {
            player.heal(heal);
        }
    }
}