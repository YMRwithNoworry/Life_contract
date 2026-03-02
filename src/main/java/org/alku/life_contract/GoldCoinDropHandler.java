package org.alku.life_contract;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Life_contract.MODID)
public class GoldCoinDropHandler {
    
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        
        LivingEntity entity = event.getEntity();
        
        if (event.getSource().getEntity() instanceof Player) {
            entity.spawnAtLocation(new ItemStack(Life_contract.GOLD_COIN.get()));
        }
    }
}
