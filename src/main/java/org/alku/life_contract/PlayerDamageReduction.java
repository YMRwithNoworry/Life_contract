package org.alku.life_contract;
import net.neoforged.fml.common.EventBusSubscriber;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

@EventBusSubscriber(modid = Life_contract.MODID, bus = EventBusSubscriber.Bus.GAME)
public class PlayerDamageReduction {

    @SubscribeEvent
    public static void onLivingHurt(LivingDamageEvent.Pre event) {
        if (event.getEntity().level().isClientSide) {
            return;
        }
        
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        DamageSource source = event.getSource();
        
        if (source.getEntity() instanceof Player) {
            return;
        }
        
        float originalDamage = event.getNewDamage();
        float reducedDamage = originalDamage * 0.4f;
        
        event.setNewDamage(reducedDamage);
    }
}
