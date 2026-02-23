package org.alku.life_contract;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Life_contract.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FollowerHungerSystem {

    private static final String TAG_HUNGER_MULTIPLIER = "FollowerHungerMultiplier";
    private static final String TAG_LAST_FOLLOWER_COUNT = "LastFollowerCount";
    private static final String TAG_HUNGER_TICK_COUNTER = "HungerTickCounter";
    private static final String TAG_LAST_NOTIFICATION_TICK = "LastHungerNotificationTick";
    
    private static final float BASE_HUNGER_DRAIN_RATE = 0.05f;
    private static final int HUNGER_DRAIN_INTERVAL = 80;
    private static final int NOTIFICATION_COOLDOWN_TICKS = 200;
    
    private static final int HUNGER_THRESHOLD = 4;
    private static final float BASE_DRAIN_PER_FOLLOWER = 0.01f;
    private static final float ACCELERATED_DRAIN_PER_FOLLOWER = 0.05f;
    
    private static final Map<UUID, Float> PLAYER_HUNGER_MULTIPLIERS = new HashMap<>();
    private static final Map<UUID, Integer> PLAYER_FOLLOWER_COUNTS = new HashMap<>();

    public static float calculateHungerMultiplier(int followerCount) {
        if (followerCount <= 0) {
            return 1.0f;
        }
        
        float baseIncrease = followerCount * BASE_DRAIN_PER_FOLLOWER;
        
        float acceleratedIncrease = 0.0f;
        if (followerCount >= HUNGER_THRESHOLD) {
            acceleratedIncrease = (followerCount - HUNGER_THRESHOLD + 1) * ACCELERATED_DRAIN_PER_FOLLOWER;
        }
        
        return 1.0f + baseIncrease + acceleratedIncrease;
    }
    
    public static int getHungerThreshold() {
        return HUNGER_THRESHOLD;
    }
    
    public static float getBaseDrainPerFollower() {
        return BASE_DRAIN_PER_FOLLOWER;
    }
    
    public static float getAcceleratedDrainPerFollower() {
        return ACCELERATED_DRAIN_PER_FOLLOWER;
    }
    
    public static boolean isAboveThreshold(int followerCount) {
        return followerCount >= HUNGER_THRESHOLD;
    }
    
    public static int getFollowerCount(Player player) {
        if (player.level().isClientSide) {
            return PLAYER_FOLLOWER_COUNTS.getOrDefault(player.getUUID(), 0);
        }
        
        int count = 0;
        UUID playerUUID = player.getUUID();
        
        if (player.level() instanceof ServerLevel serverLevel) {
            for (Entity entity : serverLevel.getAllEntities()) {
                if (entity instanceof Mob mob) {
                    CompoundTag tag = mob.getPersistentData();
                    if (tag.contains("FollowerOwnerUUID")) {
                        UUID ownerUUID = tag.getUUID("FollowerOwnerUUID");
                        if (playerUUID.equals(ownerUUID)) {
                            count++;
                        }
                    }
                }
            }
        }
        
        PLAYER_FOLLOWER_COUNTS.put(playerUUID, count);
        return count;
    }
    
    public static float getHungerMultiplier(Player player) {
        if (player.level().isClientSide) {
            return PLAYER_HUNGER_MULTIPLIERS.getOrDefault(player.getUUID(), 1.0f);
        }
        
        int followerCount = getFollowerCount(player);
        return calculateHungerMultiplier(followerCount);
    }
    
    public static void tickHungerDrain(ServerPlayer player, int serverTick) {
        if (player.isSpectator() || !player.isAlive()) {
            return;
        }
        
        int followerCount = getFollowerCount(player);
        if (followerCount <= 0) {
            player.getPersistentData().putFloat(TAG_HUNGER_MULTIPLIER, 1.0f);
            PLAYER_HUNGER_MULTIPLIERS.put(player.getUUID(), 1.0f);
            return;
        }
        
        float multiplier = calculateHungerMultiplier(followerCount);
        
        CompoundTag data = player.getPersistentData();
        float oldMultiplier = data.getFloat(TAG_HUNGER_MULTIPLIER);
        int lastFollowerCount = data.getInt(TAG_LAST_FOLLOWER_COUNT);
        
        if (followerCount != lastFollowerCount) {
            data.putInt(TAG_LAST_FOLLOWER_COUNT, followerCount);
            data.putFloat(TAG_HUNGER_MULTIPLIER, multiplier);
            PLAYER_HUNGER_MULTIPLIERS.put(player.getUUID(), multiplier);
            
            sendFollowerCountChangeNotification(player, followerCount, multiplier);
        }
        
        int tickCounter = data.getInt(TAG_HUNGER_TICK_COUNTER);
        tickCounter++;
        
        int effectiveInterval = Math.max(20, (int)(HUNGER_DRAIN_INTERVAL / multiplier));
        
        if (tickCounter >= effectiveInterval) {
            tickCounter = 0;
            applyHungerDrain(player, followerCount, multiplier, serverTick);
        }
        
        data.putInt(TAG_HUNGER_TICK_COUNTER, tickCounter);
    }
    
    private static void applyHungerDrain(ServerPlayer player, int followerCount, float multiplier, int serverTick) {
        FoodData foodData = player.getFoodData();
        
        if (foodData.getFoodLevel() > 0) {
            float drainAmount = BASE_HUNGER_DRAIN_RATE * multiplier;
            int currentFood = foodData.getFoodLevel();
            float currentSaturation = foodData.getSaturationLevel();
            
            if (currentSaturation > 0) {
                float newSaturation = Math.max(0, currentSaturation - drainAmount);
                foodData.setSaturation(newSaturation);
            } else {
                int newFood = Math.max(0, currentFood - 1);
                foodData.setFoodLevel(newFood);
            }
            
            if (serverTick % 400 == 0 && followerCount > 0) {
                sendPeriodicHungerWarning(player, followerCount, multiplier);
            }
        }
        
        spawnHungerDrainParticles(player, followerCount);
    }
    
    private static void sendFollowerCountChangeNotification(ServerPlayer player, int followerCount, float multiplier) {
        if (followerCount > 0) {
            String statusColor = isAboveThreshold(followerCount) ? "§c" : "§e";
            String statusText = isAboveThreshold(followerCount) ? " §7[§c加速消耗§7]" : "";
            
            player.displayClientMessage(
                Component.literal("§6[跟随生物] §f当前跟随: §e" + followerCount + 
                    " §7| 饥饿消耗: " + statusColor + String.format("%.0f", (multiplier - 1) * 100) + 
                    "% §7额外" + statusText),
                true
            );
            
            if (followerCount == HUNGER_THRESHOLD) {
                player.sendSystemMessage(Component.literal(""));
                player.sendSystemMessage(Component.literal("§c§l[警告] §f跟随生物达到 §e" + HUNGER_THRESHOLD + " §f个!"));
                player.sendSystemMessage(Component.literal("§7  饥饿值消耗开始 §c加速§7!"));
                player.sendSystemMessage(Component.literal(""));
            }
        } else {
            player.displayClientMessage(
                Component.literal("§6[跟随生物] §f已无跟随生物，饥饿消耗恢复正常"),
                true
            );
        }
    }
    
    private static void sendPeriodicHungerWarning(ServerPlayer player, int followerCount, float multiplier) {
        int extraPercent = (int)((multiplier - 1) * 100);
        
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal("§6§l[饥饿消耗]"));
        player.sendSystemMessage(Component.literal("§f  跟随生物数量: §e" + followerCount + 
            (isAboveThreshold(followerCount) ? " §c[加速阈值已触发]" : "")));
        player.sendSystemMessage(Component.literal("§f  额外饥饿消耗: §c+" + extraPercent + "%"));
        
        if (isAboveThreshold(followerCount)) {
            player.sendSystemMessage(Component.literal("§7  提示: 超过 §e" + HUNGER_THRESHOLD + " §7个生物后，消耗加速增长"));
        } else {
            int remaining = HUNGER_THRESHOLD - followerCount;
            player.sendSystemMessage(Component.literal("§7  提示: 再增加 §e" + remaining + " §7个生物将触发加速消耗"));
        }
        player.sendSystemMessage(Component.literal(""));
    }
    
    private static void spawnHungerDrainParticles(ServerPlayer player, int followerCount) {
        if (player.level() instanceof ServerLevel serverLevel && followerCount > 0) {
            int particleCount = Math.min(followerCount * 3, 15);
            
            serverLevel.sendParticles(
                ParticleTypes.ANGRY_VILLAGER,
                player.getX(), player.getY() + 1.5, player.getZ(),
                particleCount, 0.5, 0.3, 0.5, 0.05
            );
            
            if (followerCount >= HUNGER_THRESHOLD) {
                serverLevel.sendParticles(
                    ParticleTypes.SMOKE,
                    player.getX(), player.getY() + 1, player.getZ(),
                    followerCount * 2, 0.4, 0.3, 0.4, 0.03
                );
                serverLevel.sendParticles(
                    ParticleTypes.DAMAGE_INDICATOR,
                    player.getX(), player.getY() + 1.5, player.getZ(),
                    3, 0.3, 0.2, 0.3, 0.02
                );
            } else if (followerCount >= 2) {
                serverLevel.sendParticles(
                    ParticleTypes.SMOKE,
                    player.getX(), player.getY() + 1, player.getZ(),
                    followerCount, 0.3, 0.2, 0.3, 0.02
                );
            }
        }
    }
    
    public static void onFollowerRegistered(ServerPlayer player, Mob follower) {
        int newCount = getFollowerCount(player) + 1;
        float newMultiplier = calculateHungerMultiplier(newCount);
        
        player.getPersistentData().putInt(TAG_LAST_FOLLOWER_COUNT, newCount);
        player.getPersistentData().putFloat(TAG_HUNGER_MULTIPLIER, newMultiplier);
        PLAYER_HUNGER_MULTIPLIERS.put(player.getUUID(), newMultiplier);
        PLAYER_FOLLOWER_COUNTS.put(player.getUUID(), newCount);
        
        sendFollowerCountChangeNotification(player, newCount, newMultiplier);
        spawnFollowerChangeParticles(player, true, newCount);
    }
    
    public static void onFollowerUnregistered(ServerPlayer player) {
        int newCount = Math.max(0, getFollowerCount(player) - 1);
        float newMultiplier = calculateHungerMultiplier(newCount);
        
        player.getPersistentData().putInt(TAG_LAST_FOLLOWER_COUNT, newCount);
        player.getPersistentData().putFloat(TAG_HUNGER_MULTIPLIER, newMultiplier);
        PLAYER_HUNGER_MULTIPLIERS.put(player.getUUID(), newMultiplier);
        PLAYER_FOLLOWER_COUNTS.put(player.getUUID(), newCount);
        
        sendFollowerCountChangeNotification(player, newCount, newMultiplier);
        
        if (newCount > 0) {
            spawnFollowerChangeParticles(player, false, newCount);
        }
    }
    
    private static void spawnFollowerChangeParticles(ServerPlayer player, boolean added, int newCount) {
        if (player.level() instanceof ServerLevel serverLevel) {
            if (added) {
                if (newCount >= HUNGER_THRESHOLD) {
                    serverLevel.sendParticles(
                        ParticleTypes.ANGRY_VILLAGER,
                        player.getX(), player.getY() + 1.5, player.getZ(),
                        15, 0.5, 0.3, 0.5, 0.1
                    );
                    serverLevel.sendParticles(
                        ParticleTypes.SMOKE,
                        player.getX(), player.getY() + 1, player.getZ(),
                        20, 0.5, 0.3, 0.5, 0.08
                    );
                } else {
                    serverLevel.sendParticles(
                        ParticleTypes.HEART,
                        player.getX(), player.getY() + 1.5, player.getZ(),
                        10, 0.5, 0.3, 0.5, 0.1
                    );
                    serverLevel.sendParticles(
                        ParticleTypes.HAPPY_VILLAGER,
                        player.getX(), player.getY() + 1, player.getZ(),
                        15, 0.5, 0.3, 0.5, 0.1
                    );
                }
            } else {
                serverLevel.sendParticles(
                    ParticleTypes.SMOKE,
                    player.getX(), player.getY() + 1, player.getZ(),
                    10, 0.5, 0.3, 0.5, 0.05
                );
            }
        }
    }
    
    public static void syncHungerMultiplierToClient(ServerPlayer player) {
        int followerCount = getFollowerCount(player);
        float multiplier = calculateHungerMultiplier(followerCount);
        
        PLAYER_HUNGER_MULTIPLIERS.put(player.getUUID(), multiplier);
        PLAYER_FOLLOWER_COUNTS.put(player.getUUID(), followerCount);
        
        player.getPersistentData().putFloat(TAG_HUNGER_MULTIPLIER, multiplier);
        player.getPersistentData().putInt(TAG_LAST_FOLLOWER_COUNT, followerCount);
        
        PacketSyncFollowerHunger packet = new PacketSyncFollowerHunger(player.getUUID(), followerCount, multiplier);
        net.minecraftforge.network.PacketDistributor.PacketTarget target = 
            net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player);
        NetworkHandler.CHANNEL.send(target, packet);
    }
    
    public static String getHungerMultiplierDisplay(float multiplier) {
        if (multiplier <= 1.0f) {
            return "§a正常";
        } else if (multiplier <= 1.05f) {
            return "§e轻微增加";
        } else if (multiplier <= 1.15f) {
            return "§c明显增加";
        } else {
            return "§4大幅增加";
        }
    }
    
    public static String getHungerStatusDescription(int followerCount) {
        if (followerCount <= 0) {
            return "§a饥饿消耗正常";
        } else if (followerCount < HUNGER_THRESHOLD) {
            return "§e饥饿消耗轻微增加";
        } else if (followerCount == HUNGER_THRESHOLD) {
            return "§c饥饿消耗开始加速";
        } else {
            return "§4饥饿消耗大幅加速";
        }
    }
    
    public static void clearPlayerData(UUID playerUUID) {
        PLAYER_HUNGER_MULTIPLIERS.remove(playerUUID);
        PLAYER_FOLLOWER_COUNTS.remove(playerUUID);
    }
}
