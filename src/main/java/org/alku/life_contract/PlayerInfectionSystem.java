package org.alku.life_contract;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import org.alku.life_contract.border.EliminationHandler;

@Mod.EventBusSubscriber(modid = Life_contract.MODID)
public class PlayerInfectionSystem {
    
    private static final String TAG_INFECTION = "PlayerInfection";
    private static final int MAX_INFECTION = 100;
    private static final int INFECTION_PER_SECOND = 1;
    
    public static int getInfection(Player player) {
        CompoundTag data = player.getPersistentData();
        return data.getInt(TAG_INFECTION);
    }
    
    public static void setInfection(Player player, int value) {
        CompoundTag data = player.getPersistentData();
        int newValue = Math.max(0, Math.min(value, MAX_INFECTION));
        data.putInt(TAG_INFECTION, newValue);
        syncToClient(player);
        
        if (newValue >= MAX_INFECTION && player instanceof ServerPlayer serverPlayer) {
            checkAndEliminatePlayer(serverPlayer);
        }
    }
    
    public static void addInfection(Player player, int amount) {
        int current = getInfection(player);
        setInfection(player, current + amount);
    }
    
    public static void resetInfection(Player player) {
        setInfection(player, 0);
    }
    
    public static int getMaxInfection() {
        return MAX_INFECTION;
    }
    
    public static float getInfectionPercent(Player player) {
        return (float) getInfection(player) / MAX_INFECTION;
    }
    
    private static void syncToClient(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            int infection = getInfection(player);
            PacketSyncInfection packet = new PacketSyncInfection(infection);
            NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), packet);
        }
    }
    
    private static void checkAndEliminatePlayer(ServerPlayer player) {
        if (player.isCreative() || player.isSpectator()) return;
        
        ServerLevel level = player.serverLevel();
        
        player.setGameMode(net.minecraft.world.level.GameType.SPECTATOR);
        
        resetInfection(player);
        
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§4[淘汰] §f你的感染值已满，已被淘汰！"));
        
        level.getPlayers(p -> true).forEach(p -> 
            p.sendSystemMessage(net.minecraft.network.chat.Component.literal("§c[淘汰] §f玩家 §e" + player.getName().getString() + " §f已被感染淘汰！")));
        
        EliminationHandler.spawnEliminationMobAtPlayer(level, player);
    }
    
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            syncToClient(serverPlayer);
        }
    }
    
    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            syncToClient(serverPlayer);
        }
    }
}
