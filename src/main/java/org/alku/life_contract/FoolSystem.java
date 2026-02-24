package org.alku.life_contract;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.network.PacketDistributor;

import org.alku.life_contract.profession.Profession;
import org.alku.life_contract.profession.ProfessionConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FoolSystem {

    public static void stealProfession(ServerPlayer fool, UUID targetUUID) {
        if (fool == null) return;
        
        CompoundTag data = fool.getPersistentData();
        String professionId = data.getString("LifeContractProfession");
        
        if (professionId == null || professionId.isEmpty()) {
            fool.sendSystemMessage(Component.literal("§c[傻子] 你还没有选择职业！"));
            return;
        }
        
        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null || !profession.isFool()) {
            fool.sendSystemMessage(Component.literal("§c[傻子] 你不是傻子职业，无法使用此能力！"));
            return;
        }
        
        int cooldown = data.getInt("FoolStealCooldown");
        if (cooldown > 0) {
            int seconds = cooldown / 20;
            fool.sendSystemMessage(Component.literal("§c[傻子] 掠夺能力冷却中，还需 " + seconds + " 秒！"));
            return;
        }
        
        if (targetUUID == null) {
            ServerPlayer target = findNearestValidTarget(fool, profession.getFoolStealRange());
            if (target == null) {
                fool.sendSystemMessage(Component.literal("§c[傻子] 附近没有可以掠夺的目标！"));
                return;
            }
            targetUUID = target.getUUID();
        }
        
        ServerPlayer target = fool.server.getPlayerList().getPlayer(targetUUID);
        if (target == null) {
            fool.sendSystemMessage(Component.literal("§c[傻子] 目标玩家不在线！"));
            return;
        }
        
        if (fool.distanceTo(target) > profession.getFoolStealRange()) {
            fool.sendSystemMessage(Component.literal("§c[傻子] 目标玩家超出掠夺范围！"));
            return;
        }
        
        CompoundTag targetData = target.getPersistentData();
        String targetProfessionId = targetData.getString("LifeContractProfession");
        
        if (targetProfessionId == null || targetProfessionId.isEmpty()) {
            fool.sendSystemMessage(Component.literal("§c[傻子] 目标玩家没有职业！"));
            return;
        }
        
        if (targetProfessionId.equals("fool")) {
            fool.sendSystemMessage(Component.literal("§c[傻子] 你不能掠夺另一个傻子！"));
            return;
        }
        
        Profession targetProfession = ProfessionConfig.getProfession(targetProfessionId);
        if (targetProfession == null) {
            fool.sendSystemMessage(Component.literal("§c[傻子] 目标职业无效！"));
            return;
        }
        
        targetData.putString("LifeContractProfession", "");
        target.sendSystemMessage(Component.literal("§c[傻子] 你的职业被掠夺了！你失去了 " + targetProfession.getName() + " 职业！"));
        
        data.putString("LifeContractProfession", targetProfessionId);
        data.putInt("FoolStealCooldown", profession.getFoolStealCooldown());
        fool.sendSystemMessage(Component.literal("§a[傻子] 你成功掠夺了 " + targetProfession.getName() + " 职业！"));
        
        if (fool.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SOUL, fool.getX(), fool.getY() + 1, fool.getZ(), 50, 0.5, 0.5, 0.5, 0.1);
            serverLevel.sendParticles(ParticleTypes.SOUL, target.getX(), target.getY() + 1, target.getZ(), 50, 0.5, 0.5, 0.5, 0.1);
        }
        
        fool.level().playSound(null, fool.getX(), fool.getY(), fool.getZ(), SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 1.0f, 1.0f);
        target.level().playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 1.0f, 1.0f);
        
        ContractEvents.syncData(fool);
        ContractEvents.syncData(target);
    }
    
    private static ServerPlayer findNearestValidTarget(ServerPlayer fool, float range) {
        List<ServerPlayer> validTargets = new ArrayList<>();
        
        for (ServerPlayer player : fool.server.getPlayerList().getPlayers()) {
            if (player.equals(fool)) continue;
            if (fool.distanceTo(player) > range) continue;
            
            CompoundTag data = player.getPersistentData();
            String professionId = data.getString("LifeContractProfession");
            
            if (professionId != null && !professionId.isEmpty() && !professionId.equals("fool")) {
                validTargets.add(player);
            }
        }
        
        if (validTargets.isEmpty()) return null;
        
        ServerPlayer nearest = null;
        double nearestDist = Double.MAX_VALUE;
        
        for (ServerPlayer target : validTargets) {
            double dist = fool.distanceTo(target);
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = target;
            }
        }
        
        return nearest;
    }
    
    public static void tickCooldown(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        int cooldown = data.getInt("FoolStealCooldown");
        if (cooldown > 0) {
            data.putInt("FoolStealCooldown", cooldown - 1);
        }
    }
    
    public static int getCooldown(ServerPlayer player) {
        return player.getPersistentData().getInt("FoolStealCooldown");
    }
    
    public static void sendStealPacket(UUID targetUUID) {
        NetworkHandler.CHANNEL.sendToServer(new PacketFoolStealProfession(targetUUID));
    }
}
