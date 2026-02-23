package org.alku.life_contract;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = Life_contract.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ImpostorClientRenderer {
    
    private static int tickCounter = 0;
    
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) {
            return;
        }
        
        tickCounter++;
        
        if (tickCounter % 5 == 0) {
            renderDisguiseParticles(mc.level, player);
        }
    }
    
    private static void renderDisguiseParticles(Level level, LocalPlayer player) {
        UUID playerUUID = player.getUUID();
        
        if (PacketSyncImpostorState.isClientDisguised(playerUUID)) {
            if (level.random.nextFloat() < 0.3f) {
                double x = player.getX() + (level.random.nextDouble() - 0.5) * 0.5;
                double y = player.getY() + 1.0 + level.random.nextDouble() * 0.5;
                double z = player.getZ() + (level.random.nextDouble() - 0.5) * 0.5;
                
                level.addParticle(
                    ParticleTypes.SOUL,
                    x, y, z,
                    0, 0.02, 0
                );
            }
            
            if (level.random.nextFloat() < 0.1f) {
                double x = player.getX() + (level.random.nextDouble() - 0.5) * 0.8;
                double y = player.getY() + 0.5 + level.random.nextDouble() * 1.5;
                double z = player.getZ() + (level.random.nextDouble() - 0.5) * 0.8;
                
                level.addParticle(
                    ParticleTypes.END_ROD,
                    x, y, z,
                    0, -0.01, 0
                );
            }
        }
    }
    
    public static String getDisguiseDisplayName(UUID playerUUID, String originalName) {
        if (PacketSyncImpostorState.isClientDisguised(playerUUID)) {
            String disguiseName = PacketSyncImpostorState.getClientDisguiseName(playerUUID);
            return disguiseName != null ? disguiseName : originalName;
        }
        return originalName;
    }
    
    public static UUID getDisguiseTeam(UUID playerUUID, UUID originalTeam) {
        if (PacketSyncImpostorState.isClientDisguised(playerUUID)) {
            UUID disguiseTeam = PacketSyncImpostorState.getClientDisguiseTeam(playerUUID);
            return disguiseTeam != null ? disguiseTeam : originalTeam;
        }
        return originalTeam;
    }
}
