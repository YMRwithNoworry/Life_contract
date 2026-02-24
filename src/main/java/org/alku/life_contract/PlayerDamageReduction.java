package org.alku.life_contract;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Life_contract.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerDamageReduction {

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
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
        
        float originalDamage = event.getAmount();
        float reducedDamage = originalDamage * 0.6f;
        
        event.setAmount(reducedDamage);
    }
}
