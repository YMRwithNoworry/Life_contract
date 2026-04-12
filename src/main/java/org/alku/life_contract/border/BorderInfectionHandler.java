package org.alku.life_contract.border;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import org.alku.life_contract.Life_contract;
import org.alku.life_contract.PlayerInfectionSystem;

@Mod.EventBusSubscriber(modid = Life_contract.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BorderInfectionHandler {
    
    private static final double[] INFECTION_RATES = {0.5, 1.0, 4.0, 5.0, 10.0, 15.0, 20.0};
    
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        
        net.minecraft.server.MinecraftServer server = event.getServer();
        if (server == null) return;
        
        for (ServerLevel level : server.getAllLevels()) {
            WorldBorder worldBorder = level.getWorldBorder();
            
            if (worldBorder.getSize() >= 59999900) continue;
            
            for (ServerPlayer player : level.getPlayers(p -> true)) {
                if (player.isCreative() || player.isSpectator()) continue;
                
                if (!isInsideBorder(worldBorder, player)) {
                    handlePlayerOutsideBorder(player, worldBorder);
                }
            }
        }
    }
    
    private static boolean isInsideBorder(WorldBorder border, ServerPlayer player) {
        double centerX = border.getCenterX();
        double centerZ = border.getCenterZ();
        double halfSize = border.getSize() / 2.0;
        
        double playerX = player.getX();
        double playerZ = player.getZ();
        
        return playerX >= centerX - halfSize && playerX <= centerX + halfSize &&
               playerZ >= centerZ - halfSize && playerZ <= centerZ + halfSize;
    }
    
    private static int estimateShrinkCount(WorldBorder border) {
        double size = border.getSize();
        if (size >= 550) return 0;
        if (size >= 400) return 1;
        if (size >= 300) return 2;
        if (size >= 200) return 3;
        if (size >= 100) return 4;
        if (size >= 50) return 5;
        return 6;
    }
    
    private static void handlePlayerOutsideBorder(ServerPlayer player, WorldBorder border) {
        int shrinkCount = estimateShrinkCount(border);
        double infectionRate = getInfectionRate(shrinkCount);
        
        if (player.tickCount % 20 == 0) {
            int currentInfection = PlayerInfectionSystem.getInfection(player);
            int newInfection = (int) Math.ceil(currentInfection + infectionRate);
            
            PlayerInfectionSystem.setInfection(player, newInfection);
            
            if (currentInfection % 5 == 0 || currentInfection == 0) {
                player.sendSystemMessage(Component.literal(
                    "§c[边界警告] §f你正在边界外！感染值增加: §e+" + infectionRate + 
                    " §f(当前: §c" + newInfection + "/" + PlayerInfectionSystem.getMaxInfection() + "§f)"));
            }
        }
    }
    
    private static double getInfectionRate(int shrinkCount) {
        int index = Math.min(shrinkCount, INFECTION_RATES.length - 1);
        return INFECTION_RATES[index];
    }
    
    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide) return;
        
        WorldBorder worldBorder = player.level().getWorldBorder();
        if (worldBorder.getSize() >= 59999900) return;
        
        if (!isInsideBorder(worldBorder, player)) {
            event.setNewSpeed(event.getOriginalSpeed());
        }
    }
}
