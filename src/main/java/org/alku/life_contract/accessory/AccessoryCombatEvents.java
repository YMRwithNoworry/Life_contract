package org.alku.life_contract.accessory;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import org.alku.life_contract.Life_contract;

/** 饰品的战斗类钩子：受伤减免、吸血、击杀充能、受伤掉层。 */
@EventBusSubscriber(modid = Life_contract.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class AccessoryCombatEvents {

    private AccessoryCombatEvents() {
    }

    /** 受伤减免（含情境效果，例如"生命低于 35% 时护甲提升"）。 */
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

    /** 吸血 + 受伤掉充能层。 */
    @SubscribeEvent
    public static void onDamageDealt(LivingDamageEvent.Post event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        if (event.getSource().getEntity() instanceof Player attacker && attacker.isAlive()) {
            double lifesteal = AccessoryEffects.lifesteal(attacker);
            if (lifesteal > 0.0D) {
                float heal = (float) (event.getNewDamage() * lifesteal);
                if (heal > 0.0F) {
                    attacker.heal(heal);
                }
            }
        }

        if (event.getEntity() instanceof ServerPlayer victim && event.getNewDamage() > 0.0F) {
            loseChargeOnHurt(victim);
        }
    }

    private static void loseChargeOnHurt(ServerPlayer player) {
        boolean changed = false;
        for (AccessoryDefinition definition : AccessoryEffects.activeDefinitions(player)) {
            AccessoryCharge charge = definition.charge();
            if (charge.isEmpty() || charge.loseOnHurt() <= 0) {
                continue;
            }
            if (AccessoryState.charge(player, definition.id()) <= 0) {
                continue;
            }
            AccessoryState.addCharge(player, definition.id(), -charge.loseOnHurt(), charge.max());
            changed = true;
        }
        if (changed) {
            // 受伤后立刻重算，让"低血量强化"一类的情境效果即时生效
            AccessoryEffects.refresh(player);
        }
    }

    /** 击杀充能：只有击杀敌对生物或其他玩家才叠层，避免刷鸡充能。 */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        LivingEntity victim = event.getEntity();
        if (!(event.getSource().getEntity() instanceof ServerPlayer killer) || killer == victim) {
            return;
        }
        if (!(victim instanceof Enemy) && !(victim instanceof Player)) {
            return;
        }

        AccessoryState.markKill(killer);

        boolean changed = false;
        for (AccessoryDefinition definition : AccessoryEffects.activeDefinitions(killer)) {
            AccessoryCharge charge = definition.charge();
            if (charge.isEmpty()) {
                continue;
            }
            int before = AccessoryState.charge(killer, definition.id());
            AccessoryState.addCharge(killer, definition.id(), 1, charge.max());
            // 重新计时，避免刚叠上的层被立刻衰减掉
            AccessoryState.setLastDecayTick(killer, definition.id(),
                    killer.getServer() == null ? 0 : killer.getServer().getTickCount());
            int after = AccessoryState.charge(killer, definition.id());
            if (after != before) {
                changed = true;
                if (after == charge.max()) {
                    killer.displayClientMessage(Component.translatable(
                            "message.life_contract.accessory.charge_full",
                            Component.translatable(definition.nameKey())), true);
                } else {
                    killer.displayClientMessage(Component.translatable(
                            "message.life_contract.accessory.charge",
                            Component.translatable(definition.nameKey()), after, charge.max()), true);
                }
            }
        }
        if (changed) {
            AccessoryEffects.refresh(killer);
        }
    }
}
