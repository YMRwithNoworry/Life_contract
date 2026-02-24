package org.alku.life_contract;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import org.alku.life_contract.profession.Profession;
import org.alku.life_contract.profession.ProfessionConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Life_contract.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ImpostorSystem {

    private static final String TAG_IMPOSTOR_DISGUISE_TARGET = "ImpostorDisguiseTarget";
    private static final String TAG_IMPOSTOR_DISGUISE_NAME = "ImpostorDisguiseName";
    private static final String TAG_IMPOSTOR_DISGUISE_TEAM = "ImpostorDisguiseTeam";
    private static final String TAG_IMPOSTOR_COOLDOWN_END = "ImpostorCooldownEnd";
    private static final String TAG_IMPOSTOR_DISGUISE_END = "ImpostorDisguiseEnd";
    private static final String TAG_IMPOSTOR_LAST_KILL_TIME = "ImpostorLastKillTime";
    
    private static final Map<UUID, DisguiseData> DISGUISE_MAP = new HashMap<>();
    
    public static class DisguiseData {
        public final UUID targetUUID;
        public final String targetName;
        public final UUID targetTeamLeader;
        public final long disguiseEndTime;
        
        public DisguiseData(UUID targetUUID, String targetName, UUID targetTeamLeader, long disguiseEndTime) {
            this.targetUUID = targetUUID;
            this.targetName = targetName;
            this.targetTeamLeader = targetTeamLeader;
            this.disguiseEndTime = disguiseEndTime;
        }
    }
    
    public static boolean isImpostor(Player player) {
        String professionId = player.getPersistentData().getString("LifeContractProfession");
        if (professionId == null || professionId.isEmpty()) {
            return false;
        }
        Profession profession = ProfessionConfig.getProfession(professionId);
        return profession != null && profession.isImpostor();
    }
    
    public static boolean isDisguised(Player player) {
        return player.getPersistentData().contains(TAG_IMPOSTOR_DISGUISE_TARGET);
    }
    
    public static DisguiseData getDisguiseData(Player player) {
        if (!isDisguised(player)) {
            return null;
        }
        
        UUID targetUUID = player.getPersistentData().getUUID(TAG_IMPOSTOR_DISGUISE_TARGET);
        String targetName = player.getPersistentData().getString(TAG_IMPOSTOR_DISGUISE_NAME);
        UUID targetTeam = null;
        if (player.getPersistentData().hasUUID(TAG_IMPOSTOR_DISGUISE_TEAM)) {
            targetTeam = player.getPersistentData().getUUID(TAG_IMPOSTOR_DISGUISE_TEAM);
        }
        long disguiseEnd = player.getPersistentData().getLong(TAG_IMPOSTOR_DISGUISE_END);
        
        return new DisguiseData(targetUUID, targetName, targetTeam, disguiseEnd);
    }
    
    public static boolean canUseSkill(ServerPlayer player) {
        if (!isImpostor(player)) {
            return false;
        }
        
        long currentTime = player.level().getGameTime();
        long cooldownEnd = player.getPersistentData().getLong(TAG_IMPOSTOR_COOLDOWN_END);
        
        return currentTime >= cooldownEnd;
    }
    
    public static int getRemainingCooldown(ServerPlayer player) {
        long currentTime = player.level().getGameTime();
        long cooldownEnd = player.getPersistentData().getLong(TAG_IMPOSTOR_COOLDOWN_END);
        
        long remaining = cooldownEnd - currentTime;
        return remaining > 0 ? (int)(remaining / 20) : 0;
    }
    
    public static int getRemainingDisguiseTime(ServerPlayer player) {
        if (!isDisguised(player)) {
            return 0;
        }
        
        long currentTime = player.level().getGameTime();
        long disguiseEnd = player.getPersistentData().getLong(TAG_IMPOSTOR_DISGUISE_END);
        
        long remaining = disguiseEnd - currentTime;
        return remaining > 0 ? (int)(remaining / 20) : 0;
    }
    
    public static void startDisguise(ServerPlayer impostor, ServerPlayer target) {
        if (!isImpostor(impostor)) {
            return;
        }
        
        if (ContractEvents.isSameTeam(impostor, target)) {
            impostor.sendSystemMessage(Component.literal("§c[伪装者] §f无法对队友使用伪装技能！"));
            return;
        }
        
        if (!canUseSkill(impostor)) {
            int remaining = getRemainingCooldown(impostor);
            impostor.displayClientMessage(
                Component.literal("§c[伪装者] §f技能冷却中，还需等待 §e" + remaining + " §f秒！"),
                true
            );
            return;
        }
        
        Profession profession = ProfessionConfig.getProfession(
            impostor.getPersistentData().getString("LifeContractProfession")
        );
        if (profession == null) return;
        
        int cooldownSeconds = profession.getImpostorSkillCooldown();
        int durationSeconds = profession.getImpostorDisguiseDuration();
        
        long currentTime = impostor.level().getGameTime();
        long cooldownEnd = currentTime + (cooldownSeconds * 20L);
        long disguiseEnd = currentTime + (durationSeconds * 20L);
        
        CompoundTag data = impostor.getPersistentData();
        data.putUUID(TAG_IMPOSTOR_DISGUISE_TARGET, target.getUUID());
        data.putString(TAG_IMPOSTOR_DISGUISE_NAME, target.getName().getString());
        
        UUID targetTeam = ContractEvents.getLeaderUUID(target);
        if (targetTeam != null) {
            data.putUUID(TAG_IMPOSTOR_DISGUISE_TEAM, targetTeam);
        }
        
        data.putLong(TAG_IMPOSTOR_COOLDOWN_END, cooldownEnd);
        data.putLong(TAG_IMPOSTOR_DISGUISE_END, disguiseEnd);
        
        DisguiseData disguiseData = new DisguiseData(
            target.getUUID(),
            target.getName().getString(),
            targetTeam,
            disguiseEnd
        );
        DISGUISE_MAP.put(impostor.getUUID(), disguiseData);
        
        playDisguiseStartEffects(impostor, target);
        
        impostor.sendSystemMessage(Component.literal(""));
        impostor.sendSystemMessage(Component.literal("§5§l[伪装者] §f§l伪装成功！"));
        impostor.sendSystemMessage(Component.literal("§f  你现在伪装成: §e" + target.getName().getString()));
        impostor.sendSystemMessage(Component.literal("§f  伪装持续: §b" + durationSeconds + " §f秒"));
        impostor.sendSystemMessage(Component.literal("§7  其他玩家将看到你的名字为目标的队伍颜色和名称"));
        impostor.sendSystemMessage(Component.literal(""));
        
        syncDisguiseState(impostor);
    }
    
    public static void endDisguise(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        
        data.remove(TAG_IMPOSTOR_DISGUISE_TARGET);
        data.remove(TAG_IMPOSTOR_DISGUISE_NAME);
        data.remove(TAG_IMPOSTOR_DISGUISE_TEAM);
        data.remove(TAG_IMPOSTOR_DISGUISE_END);
        
        DISGUISE_MAP.remove(player.getUUID());
        
        playDisguiseEndEffects(player);
        
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal("§5§l[伪装者] §f§l伪装已解除！"));
        player.sendSystemMessage(Component.literal("§7  你已恢复原始外观"));
        player.sendSystemMessage(Component.literal(""));
        
        syncDisguiseState(player);
    }
    
    private static void playDisguiseStartEffects(ServerPlayer impostor, ServerPlayer target) {
        if (impostor.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                ParticleTypes.SMOKE,
                impostor.getX(), impostor.getY() + 1, impostor.getZ(),
                50, 0.5, 0.5, 0.5, 0.1
            );
            
            serverLevel.sendParticles(
                ParticleTypes.REVERSE_PORTAL,
                impostor.getX(), impostor.getY() + 1, impostor.getZ(),
                30, 0.3, 0.3, 0.3, 0.05
            );
            
            serverLevel.sendParticles(
                ParticleTypes.SOUL,
                target.getX(), target.getY() + 1, target.getZ(),
                20, 0.3, 0.3, 0.3, 0.05
            );
        }
        
        impostor.level().playSound(null, impostor.blockPosition(), 
            SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 0.5F);
        impostor.level().playSound(null, impostor.blockPosition(), 
            SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 0.3F, 1.5F);
    }
    
    private static void playDisguiseEndEffects(ServerPlayer player) {
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                ParticleTypes.END_ROD,
                player.getX(), player.getY() + 1, player.getZ(),
                30, 0.5, 0.5, 0.5, 0.1
            );
            
            serverLevel.sendParticles(
                ParticleTypes.CLOUD,
                player.getX(), player.getY() + 1, player.getZ(),
                20, 0.3, 0.3, 0.3, 0.05
            );
        }
        
        player.level().playSound(null, player.blockPosition(), 
            SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.5F);
        player.level().playSound(null, player.blockPosition(), 
            SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.5F, 1.0F);
    }
    
    public static void tickImpostor(ServerPlayer player) {
        if (!isImpostor(player)) {
            return;
        }
        
        if (isDisguised(player)) {
            long currentTime = player.level().getGameTime();
            long disguiseEnd = player.getPersistentData().getLong(TAG_IMPOSTOR_DISGUISE_END);
            
            if (currentTime >= disguiseEnd) {
                endDisguise(player);
            } else {
                int remaining = getRemainingDisguiseTime(player);
                if (player.tickCount % 20 == 0 && remaining <= 10 && remaining > 0) {
                    player.displayClientMessage(
                        Component.literal("§5[伪装者] §f伪装剩余: §b" + remaining + " §f秒"),
                        true
                    );
                }
            }
        } else {
            int cooldown = getRemainingCooldown(player);
            if (player.tickCount % 20 == 0 && cooldown > 0 && cooldown <= 30) {
                player.displayClientMessage(
                    Component.literal("§5[伪装者] §f技能冷却: §e" + cooldown + " §f秒"),
                    true
                );
            }
        }
    }
    
    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer victim)) {
            return;
        }
        
        if (victim.level().isClientSide) {
            return;
        }
        
        if (event.getSource().getEntity() instanceof ServerPlayer killer) {
            if (isImpostor(killer) && !ContractEvents.isSameTeam(killer, victim)) {
                if (canUseSkill(killer) && !isDisguised(killer)) {
                    startDisguise(killer, victim);
                }
            }
        }
        
        if (isImpostor(victim) && isDisguised(victim)) {
            endDisguise(victim);
            ProfessionConfig.setPlayerProfession(victim.getUUID(), victim.getPersistentData().getString("LifeContractProfession"));
        }
    }
    
    public static void onPlayerJoin(ServerPlayer player) {
        if (isDisguised(player)) {
            DisguiseData data = getDisguiseData(player);
            if (data != null) {
                DISGUISE_MAP.put(player.getUUID(), data);
            }
            syncDisguiseState(player);
        }
    }
    
    public static void onPlayerRespawn(ServerPlayer player) {
        if (isDisguised(player)) {
            endDisguise(player);
        }
    }
    
    private static void syncDisguiseState(ServerPlayer player) {
        NetworkHandler.CHANNEL.send(
            net.minecraftforge.network.PacketDistributor.ALL.noArg(),
            new PacketSyncImpostorState(
                player.getUUID(),
                isDisguised(player),
                isDisguised(player) ? getDisguiseData(player) : null
            )
        );
    }
    
    public static String getDisguisedName(Player player) {
        if (!isDisguised(player)) {
            return player.getName().getString();
        }
        
        DisguiseData data = getDisguiseData(player);
        return data != null ? data.targetName : player.getName().getString();
    }
    
    public static UUID getDisguisedTeam(Player player) {
        if (!isDisguised(player)) {
            return ContractEvents.getLeaderUUID(player);
        }
        
        DisguiseData data = getDisguiseData(player);
        return data != null && data.targetTeamLeader != null ? 
            data.targetTeamLeader : ContractEvents.getLeaderUUID(player);
    }
    
    public static void clearDisguiseData(Player player) {
        player.getPersistentData().remove(TAG_IMPOSTOR_DISGUISE_TARGET);
        player.getPersistentData().remove(TAG_IMPOSTOR_DISGUISE_NAME);
        player.getPersistentData().remove(TAG_IMPOSTOR_DISGUISE_TEAM);
        player.getPersistentData().remove(TAG_IMPOSTOR_DISGUISE_END);
        DISGUISE_MAP.remove(player.getUUID());
    }
}
