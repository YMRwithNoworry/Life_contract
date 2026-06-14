package org.alku.life_contract.follower;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import org.alku.life_contract.Life_contract;
import org.alku.life_contract.NetworkHandler;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Life_contract.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FollowerEvents {

    private static final Map<UUID, UUID> FOLLOWER_OWNER_MAP = new HashMap<>();
    private static final Map<UUID, LivingEntity> PLAYER_ATTACK_TARGET = new HashMap<>();
    private static final Map<UUID, LivingEntity> PLAYER_ATTACKER = new HashMap<>();
    private static final Map<UUID, Long> PLAYER_ATTACK_TARGET_TIME = new HashMap<>();
    private static final Map<UUID, Long> PLAYER_ATTACKER_TIME = new HashMap<>();
    private static final Map<UUID, Long> PROTECTION_MESSAGE_COOLDOWN = new HashMap<>();
    private static final long TARGET_EXPIRE_TIME = 200;
    private static final long MESSAGE_COOLDOWN_TICKS = 60;
    private static final String TAG_FOLLOWER_OWNER_UUID = "FollowerOwnerUUID";
    private static final String TAG_INHERIT_FOLLOWER_OWNER_UUID = "LifeContractInheritedFollowerOwnerUUID";
    private static final double SUMMON_INHERIT_RADIUS = 12.0D;
    private static final Set<UUID> INHERITED_SUMMONS = new HashSet<>();
    private static final String[] OWNER_METHOD_NAMES = {
        "getOwner",
        "getOwnerUUID",
        "getOwnerId",
        "getSummoner",
        "getSummonerUUID",
        "getCaster",
        "getCasterUUID",
        "getTrueOwner",
        "getTrueOwnerUUID"
    };

    @SubscribeEvent
    public static void onMobFinalizeSpawn(MobSpawnEvent.FinalizeSpawn event) {
        if (event.getLevel().isClientSide() || event.getSpawnType() != MobSpawnType.MOB_SUMMONED) {
            return;
        }

        Mob mob = event.getEntity();
        UUID ownerUUID = findSummonedFollowerOwner(mob);
        if (ownerUUID != null) {
            mob.getPersistentData().putUUID(TAG_INHERIT_FOLLOWER_OWNER_UUID, ownerUUID);
        }
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        
        Entity entity = event.getEntity();
        if (entity instanceof Mob mob) {
            CompoundTag tag = mob.getPersistentData();
            if (tag.contains(TAG_FOLLOWER_OWNER_UUID)) {
                registerFollowerWithoutHungerNotification(mob, tag.getUUID(TAG_FOLLOWER_OWNER_UUID));
                return;
            }

            if (tag.contains(TAG_INHERIT_FOLLOWER_OWNER_UUID)) {
                UUID ownerUUID = tag.getUUID(TAG_INHERIT_FOLLOWER_OWNER_UUID);
                tag.remove(TAG_INHERIT_FOLLOWER_OWNER_UUID);
                registerInheritedSummon(mob, ownerUUID);
                return;
            }

            UUID directOwnerUUID = findDirectFollowerOwner(mob);
            if (directOwnerUUID != null) {
                registerInheritedSummon(mob, directOwnerUUID);
                return;
            }

            if (mob.getSpawnType() == MobSpawnType.MOB_SUMMONED) {
                UUID ownerUUID = findNearbyFollowerOwner(mob);
                if (ownerUUID != null) {
                    registerInheritedSummon(mob, ownerUUID);
                }
            }
        }
    }

    private static void setupFollowerAI(Mob mob, UUID ownerUUID) {
        mob.targetSelector.removeAllGoals(goal -> true);
        
        mob.targetSelector.addGoal(1, new FollowerAttackGoal(mob, ownerUUID));
        mob.goalSelector.addGoal(4, new FollowOwnerGoal(mob, ownerUUID, 1.0D, 10.0F, 2.0F));
    }

    private static UUID findSummonedFollowerOwner(Mob mob) {
        UUID directOwner = findDirectFollowerOwner(mob);
        if (directOwner != null) {
            return directOwner;
        }

        return findNearbyFollowerOwner(mob);
    }

    private static UUID findDirectFollowerOwner(Mob mob) {
        for (String methodName : OWNER_METHOD_NAMES) {
            Object value = invokeZeroArgMethod(mob, methodName);
            UUID ownerUUID = resolveFollowerOwner(value, mob);
            if (ownerUUID != null) {
                return ownerUUID;
            }
        }

        return null;
    }

    private static Object invokeZeroArgMethod(Mob mob, String methodName) {
        Class<?> type = mob.getClass();
        while (type != null && type != Object.class) {
            try {
                Method method = type.getDeclaredMethod(methodName);
                if (method.getParameterCount() == 0 && !Modifier.isStatic(method.getModifiers())) {
                    method.setAccessible(true);
                    return method.invoke(mob);
                }
            } catch (NoSuchMethodException ignored) {
                type = type.getSuperclass();
                continue;
            } catch (ReflectiveOperationException | RuntimeException ignored) {
                return null;
            }
            type = type.getSuperclass();
        }

        try {
            Method method = mob.getClass().getMethod(methodName);
            if (method.getParameterCount() == 0 && !Modifier.isStatic(method.getModifiers())) {
                return method.invoke(mob);
            }
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return null;
        }

        return null;
    }

    private static UUID resolveFollowerOwner(Object value, Mob summonedMob) {
        if (value instanceof Optional<?> optional) {
            return optional.map(innerValue -> resolveFollowerOwner(innerValue, summonedMob)).orElse(null);
        }

        if (value instanceof Mob ownerMob && ownerMob != summonedMob) {
            return getOwnerUUID(ownerMob);
        }

        if (value instanceof UUID ownerEntityUUID) {
            Entity ownerEntity = summonedMob.level() instanceof ServerLevel serverLevel ? serverLevel.getEntity(ownerEntityUUID) : null;
            if (ownerEntity instanceof Mob ownerMob && ownerMob != summonedMob) {
                return getOwnerUUID(ownerMob);
            }
        }

        return null;
    }

    private static UUID findNearbyFollowerOwner(Mob summonedMob) {
        if (!(summonedMob.level() instanceof ServerLevel serverLevel)) {
            return null;
        }

        double radius = SUMMON_INHERIT_RADIUS;
        for (Mob candidate : serverLevel.getEntitiesOfClass(Mob.class, summonedMob.getBoundingBox().inflate(radius))) {
            if (candidate == summonedMob || !candidate.isAlive()) {
                continue;
            }

            UUID ownerUUID = getOwnerUUID(candidate);
            if (ownerUUID != null) {
                return ownerUUID;
            }
        }

        return null;
    }

    private static void registerFollowerWithoutHungerNotification(Mob mob, UUID ownerUUID) {
        mob.getPersistentData().putUUID(TAG_FOLLOWER_OWNER_UUID, ownerUUID);
        mob.setPersistenceRequired();
        FOLLOWER_OWNER_MAP.put(mob.getUUID(), ownerUUID);
        setupFollowerAI(mob, ownerUUID);
        syncFollowerToClients(mob, ownerUUID, true);
    }

    private static void registerInheritedSummon(Mob mob, UUID ownerUUID) {
        if (INHERITED_SUMMONS.add(mob.getUUID())) {
            registerFollower(mob, ownerUUID);
        } else {
            registerFollowerWithoutHungerNotification(mob, ownerUUID);
        }
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        
        if (event.getSource().getEntity() instanceof Player player) {
            LivingEntity target = event.getEntity();
            if (!(target instanceof Player)) {
                PLAYER_ATTACK_TARGET.put(player.getUUID(), target);
                PLAYER_ATTACK_TARGET_TIME.put(player.getUUID(), player.level().getGameTime());
            }
            
            if (target instanceof Mob mob) {
                UUID ownerUUID = getOwnerUUID(mob);
                if (ownerUUID != null && player.getUUID().equals(ownerUUID)) {
                    event.setCanceled(true);
                    
                    if (player instanceof ServerPlayer serverPlayer) {
                        showFollowerProtectionFeedback(serverPlayer, mob);
                    }
                }
            }
        }
        
        if (event.getEntity() instanceof Player player && event.getSource().getEntity() instanceof LivingEntity attacker) {
            PLAYER_ATTACKER.put(player.getUUID(), attacker);
            PLAYER_ATTACKER_TIME.put(player.getUUID(), player.level().getGameTime());
            
            if (attacker instanceof Mob mob) {
                UUID ownerUUID = getOwnerUUID(mob);
                if (ownerUUID != null && player.getUUID().equals(ownerUUID)) {
                    event.setCanceled(true);
                }
            }
        }
    }

    private static void showFollowerProtectionFeedback(ServerPlayer player, Mob follower) {
        long currentTime = player.level().getGameTime();
        Long lastMessageTime = PROTECTION_MESSAGE_COOLDOWN.get(player.getUUID());
        
        if (lastMessageTime == null || currentTime - lastMessageTime >= MESSAGE_COOLDOWN_TICKS) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§e[跟随之杖] §c无法攻击你的跟随生物！"));
            PROTECTION_MESSAGE_COOLDOWN.put(player.getUUID(), currentTime);
        }
        
        ServerLevel serverLevel = player.serverLevel();
        serverLevel.sendParticles(
            net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK,
            follower.getX(),
            follower.getY() + follower.getBbHeight() + 0.5,
            follower.getZ(),
            10,
            0.3, 0.3, 0.3, 0.1
        );
        
        serverLevel.sendParticles(
            net.minecraft.core.particles.ParticleTypes.ENCHANT,
            follower.getX(),
            follower.getY() + follower.getBbHeight() * 0.5,
            follower.getZ(),
            15,
            0.5, 0.5, 0.5, 0.5
        );
    }

    public static LivingEntity getPlayerAttackTarget(UUID playerUUID) {
        Long attackTime = PLAYER_ATTACK_TARGET_TIME.get(playerUUID);
        if (attackTime == null) return null;
        
        LivingEntity target = PLAYER_ATTACK_TARGET.get(playerUUID);
        if (target == null || !target.isAlive()) {
            PLAYER_ATTACK_TARGET.remove(playerUUID);
            PLAYER_ATTACK_TARGET_TIME.remove(playerUUID);
            return null;
        }
        
        return target;
    }

    public static LivingEntity getPlayerAttacker(UUID playerUUID) {
        LivingEntity attacker = PLAYER_ATTACKER.get(playerUUID);
        if (attacker == null || !attacker.isAlive()) {
            PLAYER_ATTACKER.remove(playerUUID);
            PLAYER_ATTACKER_TIME.remove(playerUUID);
            return null;
        }
        
        return attacker;
    }

    @SubscribeEvent
    public static void onChangeTarget(LivingChangeTargetEvent event) {
        if (event.getEntity() instanceof Mob mob && event.getNewTarget() instanceof Player player) {
            UUID ownerUUID = getOwnerUUID(mob);
            if (ownerUUID != null && player.getUUID().equals(ownerUUID)) {
                event.setNewTarget(null);
            }
        }
    }

    public static void registerFollower(Mob mob, UUID ownerUUID) {
        UUID oldOwnerUUID = getOwnerUUID(mob);
        mob.getPersistentData().putUUID(TAG_FOLLOWER_OWNER_UUID, ownerUUID);
        mob.setPersistenceRequired();
        FOLLOWER_OWNER_MAP.put(mob.getUUID(), ownerUUID);
        setupFollowerAI(mob, ownerUUID);
        syncFollowerToClients(mob, ownerUUID, true);
        
        if ((oldOwnerUUID == null || !oldOwnerUUID.equals(ownerUUID)) && mob.level() instanceof ServerLevel serverLevel) {
            net.minecraft.server.level.ServerPlayer owner = serverLevel.getServer().getPlayerList().getPlayer(ownerUUID);
            if (owner != null) {
                FollowerHungerSystem.onFollowerRegistered(owner, mob);
            }
        }
    }

    public static void unregisterFollower(Mob mob) {
        UUID ownerUUID = getOwnerUUID(mob);
        FOLLOWER_OWNER_MAP.remove(mob.getUUID());
        mob.getPersistentData().remove(TAG_FOLLOWER_OWNER_UUID);
        INHERITED_SUMMONS.remove(mob.getUUID());
        syncFollowerToClients(mob, null, false);
        
        if (ownerUUID != null && mob.level() instanceof ServerLevel serverLevel) {
            net.minecraft.server.level.ServerPlayer owner = serverLevel.getServer().getPlayerList().getPlayer(ownerUUID);
            if (owner != null) {
                FollowerHungerSystem.onFollowerUnregistered(owner);
            }
        }
    }

    private static void syncFollowerToClients(Mob mob, UUID ownerUUID, boolean isRegister) {
        if (mob.level() instanceof ServerLevel serverLevel) {
            PacketSyncFollower packet = new PacketSyncFollower(mob.getUUID(), ownerUUID, isRegister);
            NetworkHandler.CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> mob), packet);
            
            if (ownerUUID != null) {
                ServerPlayer owner = serverLevel.getServer().getPlayerList().getPlayer(ownerUUID);
                if (owner != null) {
                    NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> owner), packet);
                }
            }
        }
    }

    public static UUID getOwnerUUID(Mob mob) {
        UUID ownerUUID = FOLLOWER_OWNER_MAP.get(mob.getUUID());
        if (ownerUUID == null && mob.getPersistentData().hasUUID(TAG_FOLLOWER_OWNER_UUID)) {
            ownerUUID = mob.getPersistentData().getUUID(TAG_FOLLOWER_OWNER_UUID);
            FOLLOWER_OWNER_MAP.put(mob.getUUID(), ownerUUID);
        }
        return ownerUUID;
    }

    public static void clearFollower(UUID mobUUID) {
        FOLLOWER_OWNER_MAP.remove(mobUUID);
        INHERITED_SUMMONS.remove(mobUUID);
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        
        ServerLevel serverLevel = serverPlayer.serverLevel();
        UUID playerUUID = player.getUUID();
        
        for (Entity entity : serverLevel.getAllEntities()) {
            if (entity instanceof Mob mob) {
                CompoundTag tag = mob.getPersistentData();
                if (tag.contains(TAG_FOLLOWER_OWNER_UUID)) {
                    UUID ownerUUID = tag.getUUID(TAG_FOLLOWER_OWNER_UUID);
                    if (ownerUUID.equals(playerUUID)) {
                        PacketSyncFollower packet = new PacketSyncFollower(mob.getUUID(), ownerUUID, true);
                        NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), packet);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        
        ServerLevel serverLevel = serverPlayer.serverLevel();
        UUID playerUUID = player.getUUID();
        
        for (Entity entity : serverLevel.getAllEntities()) {
            if (entity instanceof Mob mob) {
                CompoundTag tag = mob.getPersistentData();
                if (tag.contains(TAG_FOLLOWER_OWNER_UUID)) {
                    UUID ownerUUID = tag.getUUID(TAG_FOLLOWER_OWNER_UUID);
                    if (ownerUUID.equals(playerUUID)) {
                        PacketSyncFollower packet = new PacketSyncFollower(mob.getUUID(), ownerUUID, true);
                        NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), packet);
                    }
                }
            }
        }
    }
}
