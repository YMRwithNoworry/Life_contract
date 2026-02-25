package org.alku.life_contract;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.alku.life_contract.profession.Profession;
import org.alku.life_contract.profession.ProfessionConfig;

@Mod.EventBusSubscriber(modid = Life_contract.MODID)
public class GhostSenatorSystem {

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity deadEntity = event.getEntity();
        if (deadEntity.level().isClientSide) return;
        
        if (!(deadEntity.level() instanceof ServerLevel serverLevel)) return;
        
        for (ServerPlayer player : serverLevel.getPlayers(p -> true)) {
            String professionId = ContractEvents.getEffectiveProfessionId(player);
            if (professionId == null || professionId.isEmpty()) continue;
            
            Profession profession = ProfessionConfig.getProfession(professionId);
            if (profession == null || !profession.isGhostSenator()) continue;
            
            float detectionRadius = profession.getGhostSenatorDetectionRadius();
            double distanceSq = player.distanceToSqr(deadEntity);
            
            if (distanceSq <= detectionRadius * detectionRadius) {
                float healAmount = profession.getGhostSenatorHealAmount();
                int strengthDuration = profession.getGhostSenatorStrengthDuration();
                
                player.heal(healAmount);
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, strengthDuration, 1, false, true));
                
                serverLevel.sendParticles(
                    ParticleTypes.SOUL,
                    player.getX(), player.getY() + 1, player.getZ(),
                    30, 0.5, 0.5, 0.5, 0.1
                );
                
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§8[亡魂议员] §7汲取亡魂之力，恢复生命并获得力量！"
                ));
            }
        }
    }
}
