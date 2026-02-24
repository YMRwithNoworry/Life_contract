package org.alku.life_contract.revive;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import org.alku.life_contract.ContractEvents;
import org.alku.life_contract.Life_contract;
import org.alku.life_contract.NetworkHandler;

import java.util.*;

@Mod.EventBusSubscriber(modid = Life_contract.MODID)
public class ReviveTeammateSystem {

    private static final String TAG_DEATH_TIME = "ReviveSystemDeathTime";
    private static final String TAG_WAITING_REVIVE = "WaitingForRevive";
    
    private static final Map<UUID, List<UUID>> pendingReviveChoices = new HashMap<>();
    private static final Map<UUID, DeadTeammateInfo> deadTeammatesInfo = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide) return;
        if (!(event.getEntity() instanceof ServerPlayer deadPlayer)) return;
        
        UUID leaderUUID = ContractEvents.getLeaderUUID(deadPlayer);
        if (leaderUUID != null) {
            recordDeadTeammate(deadPlayer);
        }
        
        if (event.getSource().getEntity() instanceof ServerPlayer killer) {
            if (!ContractEvents.isSameTeam(killer, deadPlayer)) {
                handleEnemyKill(killer, deadPlayer);
            }
        }
    }

    private static void recordDeadTeammate(ServerPlayer deadPlayer) {
        UUID playerUUID = deadPlayer.getUUID();
        deadTeammatesInfo.put(playerUUID, new DeadTeammateInfo(
            playerUUID,
            deadPlayer.getName().getString(),
            System.currentTimeMillis(),
            deadPlayer.getX(), deadPlayer.getY(), deadPlayer.getZ(),
            deadPlayer.level().dimension().location()
        ));
        
        CompoundTag data = deadPlayer.getPersistentData();
        data.putBoolean(TAG_WAITING_REVIVE, true);
        data.putLong(TAG_DEATH_TIME, System.currentTimeMillis());
    }

    private static void handleEnemyKill(ServerPlayer killer, ServerPlayer deadEnemy) {
        List<DeadTeammateInfo> deadTeammates = getDeadTeammatesInfo(killer);
        
        if (deadTeammates.isEmpty()) {
            return;
        }
        
        killer.sendSystemMessage(Component.literal("§a[复活] §f你击杀了敌对玩家！"));
        killer.sendSystemMessage(Component.literal("§e[复活] §f你有 §a" + deadTeammates.size() + " §f名死亡的队友可以复活。"));
        
        NetworkHandler.CHANNEL.send(
            PacketDistributor.PLAYER.with(() -> killer),
            new PacketSyncDeadTeammates(deadTeammates)
        );
    }

    public static List<DeadTeammateInfo> getDeadTeammatesInfo(ServerPlayer player) {
        List<DeadTeammateInfo> deadTeammates = new ArrayList<>();
        UUID leaderUUID = ContractEvents.getLeaderUUID(player);
        
        if (leaderUUID == null) {
            return deadTeammates;
        }
        
        for (ServerPlayer otherPlayer : player.getServer().getPlayerList().getPlayers()) {
            if (otherPlayer == player) continue;
            
            UUID otherLeader = ContractEvents.getLeaderUUID(otherPlayer);
            if (leaderUUID.equals(otherLeader)) {
                if (isPlayerWaitingForRevive(otherPlayer)) {
                    DeadTeammateInfo info = deadTeammatesInfo.get(otherPlayer.getUUID());
                    if (info != null) {
                        deadTeammates.add(info);
                    }
                }
            }
        }
        
        return deadTeammates;
    }

    public static boolean isPlayerWaitingForRevive(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        return data.getBoolean(TAG_WAITING_REVIVE);
    }

    public static void openReviveMenu(ServerPlayer player) {
        List<DeadTeammateInfo> deadTeammates = getDeadTeammatesInfo(player);
        
        if (deadTeammates.isEmpty()) {
            player.sendSystemMessage(Component.literal("§c[复活] 没有死亡的队友可以复活！"));
            return;
        }
        
        player.openMenu(new SimpleMenuProvider(
            (windowId, inv, p) -> new ReviveTeammateMenu(windowId, inv, deadTeammates),
            Component.literal("选择复活的队友")
        ));
    }

    public static void reviveTeammate(ServerPlayer killer, UUID teammateUUID) {
        if (killer.getServer() == null) return;
        
        ServerPlayer teammate = killer.getServer().getPlayerList().getPlayer(teammateUUID);
        if (teammate == null) {
            killer.sendSystemMessage(Component.literal("§c[复活] 找不到该队友！"));
            return;
        }
        
        if (!ContractEvents.isSameTeam(killer, teammate)) {
            killer.sendSystemMessage(Component.literal("§c[复活] 该玩家不是你的队友！"));
            return;
        }
        
        if (!isPlayerWaitingForRevive(teammate)) {
            killer.sendSystemMessage(Component.literal("§c[复活] 该队友不需要复活！"));
            return;
        }
        
        performRevive(killer, teammate);
    }

    private static void performRevive(ServerPlayer killer, ServerPlayer teammate) {
        CompoundTag data = teammate.getPersistentData();
        data.remove(TAG_WAITING_REVIVE);
        deadTeammatesInfo.remove(teammate.getUUID());
        
        boolean wasDead = teammate.isDeadOrDying();
        
        if (wasDead) {
            teammate.respawn();
        }
        
        teammate.setHealth(teammate.getMaxHealth());
        teammate.removeAllEffects();
        
        if (teammate.level() != killer.level()) {
            teammate.teleportTo(killer.getServer().getLevel(killer.level().dimension()),
                killer.getX(), killer.getY(), killer.getZ(), java.util.Collections.emptySet(), 0, 0);
        } else {
            teammate.teleportTo(killer.getX(), killer.getY(), killer.getZ());
        }
        
        if (teammate.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                ParticleTypes.TOTEM_OF_UNDYING,
                teammate.getX(), teammate.getY() + teammate.getBbHeight() / 2, teammate.getZ(),
                50, 1.0, 1.0, 1.0, 0.5
            );
            serverLevel.sendParticles(
                ParticleTypes.END_ROD,
                teammate.getX(), teammate.getY() + teammate.getBbHeight() / 2, teammate.getZ(),
                30, 0.5, 0.5, 0.5, 0.2
            );
        }
        
        killer.level().playSound(null, killer.getX(), killer.getY(), killer.getZ(),
            SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0f, 1.0f);
        
        killer.sendSystemMessage(Component.literal("§a[复活] §f你成功复活了 §e" + teammate.getName().getString() + "§f！"));
        teammate.sendSystemMessage(Component.literal("§a[复活] §f你被 §e" + killer.getName().getString() + " §f复活了！"));
        
        pendingReviveChoices.remove(killer.getUUID());
    }

    public static void skipRevive(ServerPlayer player) {
        pendingReviveChoices.remove(player.getUUID());
        player.sendSystemMessage(Component.literal("§7[复活] 你放弃了复活队友的机会。"));
    }

    public static void onPlayerRespawn(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        data.remove(TAG_WAITING_REVIVE);
        deadTeammatesInfo.remove(player.getUUID());
    }

    public static class DeadTeammateInfo {
        private final UUID uuid;
        private final String name;
        private final long deathTime;
        private final double deathX, deathY, deathZ;
        private final net.minecraft.resources.ResourceLocation dimension;

        public DeadTeammateInfo(UUID uuid, String name, long deathTime, 
                double deathX, double deathY, double deathZ, 
                net.minecraft.resources.ResourceLocation dimension) {
            this.uuid = uuid;
            this.name = name;
            this.deathTime = deathTime;
            this.deathX = deathX;
            this.deathY = deathY;
            this.deathZ = deathZ;
            this.dimension = dimension;
        }

        public UUID getUuid() { return uuid; }
        public String getName() { return name; }
        public long getDeathTime() { return deathTime; }
        public double getDeathX() { return deathX; }
        public double getDeathY() { return deathY; }
        public double getDeathZ() { return deathZ; }
        public net.minecraft.resources.ResourceLocation getDimension() { return dimension; }
    }
}
