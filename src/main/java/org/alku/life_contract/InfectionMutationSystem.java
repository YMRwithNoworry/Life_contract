package org.alku.life_contract;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Life_contract.MODID)
public class InfectionMutationSystem {
    
    private static final int MUTATION_CHECK_INTERVAL = 40;
    
    private static final int THRESHOLD_1 = 25;
    private static final int THRESHOLD_2 = 50;
    private static final int THRESHOLD_3 = 75;
    
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player.level().isClientSide) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        if (player.isCreative() || player.isSpectator()) return;
        
        if (player.tickCount % MUTATION_CHECK_INTERVAL != 0) return;
        
        int infection = PlayerInfectionSystem.getInfection(player);
        
        applyMutationEffects(player, infection);
    }
    
    private static void applyMutationEffects(ServerPlayer player, int infection) {
        if (infection >= THRESHOLD_3) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 80, 1, false, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 80, 0, false, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 80, 0, false, false, true));
        } else if (infection >= THRESHOLD_2) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 80, 0, false, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 80, 0, false, false, true));
        } else if (infection >= THRESHOLD_1) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 80, 0, false, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 80, 0, false, false, true));
        }
    }
    
    public static String getMutationStageName(int infection) {
        if (infection >= THRESHOLD_3) return "§4深度感染";
        if (infection >= THRESHOLD_2) return "§c中度感染";
        if (infection >= THRESHOLD_1) return "§e轻度感染";
        return "§a健康";
    }
    
    public static String getMutationDescription(int infection) {
        if (infection >= THRESHOLD_3) return "§4抗性提升II + 反胃 + 挖掘减速";
        if (infection >= THRESHOLD_2) return "§c力量I + 饥饿I";
        if (infection >= THRESHOLD_1) return "§e夜视 + 速度I";
        return "§a无效果";
    }
}
