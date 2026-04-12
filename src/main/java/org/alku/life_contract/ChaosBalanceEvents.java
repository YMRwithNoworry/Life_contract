package org.alku.life_contract;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.network.PacketDistributor;
import org.alku.life_contract.follower.FollowerWandItem;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = Life_contract.MODID)
public class ChaosBalanceEvents {

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        
        List<? extends Player> players = event.getServer().getPlayerList().getPlayers();
        
        for (Player player : players) {
            if (player.tickCount % 40 == 0 && !player.level().isClientSide) {
                updatePlayerBonus(player);
            }
        }
    }

    public static void updatePlayerBonus(Player player) {
        if (player.level().isClientSide) return;
        
        Optional<net.minecraft.world.item.ItemStack> chaosBalance = 
                CuriosIntegration.getEquippedItem(player, ChaosBalanceItem.class);
        
        if (chaosBalance.isEmpty()) {
            return;
        }
        
        ItemStack stack = chaosBalance.get();
        
        List<? extends Player> allPlayers = player.level().getServer()
                .getPlayerList().getPlayers();
        
        List<? extends Player> survivalPlayers = allPlayers.stream()
                .filter(p -> isSurvivalMode(p))
                .collect(Collectors.toList());
        
        int infectedCount = 0;
        int nonInfectedCount = 0;
        
        for (Player p : survivalPlayers) {
            if (isInfected(p)) {
                infectedCount++;
            } else {
                nonInfectedCount++;
            }
        }
        
        double bonusPercent = calculateBonusPercent(infectedCount, nonInfectedCount);
        
        updateStackNBT(stack, bonusPercent, infectedCount, nonInfectedCount);
        syncToClient(player, bonusPercent, infectedCount, nonInfectedCount);
        
        if (bonusPercent == 0 && infectedCount == nonInfectedCount && infectedCount > 0) {
            ChaosBalanceItem.applyBalancedPenalty(player);
        } else {
            ChaosBalanceItem.applyAttributes(player, bonusPercent);
        }
    }
    
    private static boolean isSurvivalMode(Player player) {
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            GameType gameType = serverPlayer.gameMode.getGameModeForPlayer();
            return gameType == GameType.SURVIVAL || gameType == GameType.ADVENTURE;
        }
        return !player.isCreative() && !player.isSpectator();
    }
    
    private static void updateStackNBT(ItemStack stack, double bonusPercent, int infectedCount, int nonInfectedCount) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putDouble("BonusPercent", bonusPercent);
        tag.putInt("InfectedCount", infectedCount);
        tag.putInt("NonInfectedCount", nonInfectedCount);
    }
    
    private static void syncToClient(Player player, double bonusPercent, int infectedCount, int nonInfectedCount) {
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            PacketSyncChaosBalance packet = new PacketSyncChaosBalance(bonusPercent, infectedCount, nonInfectedCount);
            NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), packet);
        }
    }

    private static boolean isInfected(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof FollowerWandItem) {
                return true;
            }
        }
        return false;
    }

    private static double calculateBonusPercent(int infected, int nonInfected) {
        if (infected == 0 && nonInfected == 0) {
            return 0;
        }
        
        if (infected == nonInfected) {
            return 0;
        }
        
        int total = infected + nonInfected;
        
        double difference = Math.abs(infected - nonInfected);
        
        double imbalanceRatio = difference / (double) total;
        
        int minPlayersForMaxBonus = 4;
        double playerCountFactor = Math.min(1.0, (double) total / minPlayersForMaxBonus);
        
        double maxEffectiveBonus = playerCountFactor * ChaosBalanceItem.MAX_BONUS_PERCENT;
        
        return imbalanceRatio * maxEffectiveBonus;
    }
}
