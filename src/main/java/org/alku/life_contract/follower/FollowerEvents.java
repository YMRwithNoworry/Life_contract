package org.alku.life_contract.follower;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import org.alku.life_contract.ContractEvents;
import org.alku.life_contract.Life_contract;
import org.alku.life_contract.NetworkHandler;
import org.alku.life_contract.TeamIronGolemSystem;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Life_contract.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FollowerEvents {

    private static final Map<UUID, UUID> FOLLOWER_OWNER_MAP = new HashMap<>();
    private static final Map<UUID, Set<UUID>> OWNER_FOLLOWER_MAP = new HashMap<>();
    private static final Map<UUID, LivingEntity> PLAYER_ATTACK_TARGET = new HashMap<>();
    private static final Map<UUID, LivingEntity> PLAYER_ATTACKER = new HashMap<>();
    private static final Map<UUID, Long> PLAYER_ATTACK_TARGET_TIME = new HashMap<>();
    private static final Map<UUID, Long> PLAYER_ATTACKER_TIME = new HashMap<>();
    private static final Map<UUID, Long> PROTECTION_MESSAGE_COOLDOWN = new HashMap<>();
    private static final long TARGET_EXPIRE_TIME = 200;
    private static final long MESSAGE_COOLDOWN_TICKS = 60;
    private static final String TAG_FOLLOWER_OWNER_UUID = "FollowerOwnerUUID";
    private static final String TAG_INHERIT_FOLLOWER_OWNER_UUID = "LifeContractInheritedFollowerOwnerUUID";
    private static final String TAG_FACTION_UUID = "LifeContractFactionUUID";
    private static final String TAG_CONTRACT_ALLY = "LifeContractSoulAlly";
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
    private static final ClassValue<List<Method>> OWNER_METHOD_CACHE = new ClassValue<>() {
        @Override
        protected List<Method> computeValue(Class<?> type) {
            List<Method> methods = new ArrayList<>();
            for (String methodName : OWNER_METHOD_NAMES) {
                Method method = findOwnerMethod(type, methodName);
                if (method != null) {
                    methods.add(method);
                }
            }
            return List.copyOf(methods);
        }
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
        if (isContractAlly(mob) && mob instanceof PathfinderMob pathfinderMob) {
            pathfinderMob.goalSelector.addGoal(3, new MeleeAttackGoal(pathfinderMob, 1.2D, true));
        }
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
        for (Method method : OWNER_METHOD_CACHE.get(mob.getClass())) {
            Object value = invokeOwnerMethod(mob, method);
            UUID ownerUUID = resolveFollowerOwner(value, mob);
            if (ownerUUID != null) {
                return ownerUUID;
            }
        }

        return null;
    }

    private static Method findOwnerMethod(Class<?> mobType, String methodName) {
        Class<?> type = mobType;
        while (type != null && type != Object.class) {
            try {
                Method method = type.getDeclaredMethod(methodName);
                if (method.getParameterCount() == 0 && !Modifier.isStatic(method.getModifiers())) {
                    method.setAccessible(true);
                    return method;
                }
            } catch (NoSuchMethodException ignored) {
                type = type.getSuperclass();
                continue;
            } catch (RuntimeException ignored) {
                return null;
            }
            type = type.getSuperclass();
        }

        try {
            Method method = mobType.getMethod(methodName);
            if (method.getParameterCount() == 0 && !Modifier.isStatic(method.getModifiers())) {
                return method;
            }
        } catch (NoSuchMethodException | RuntimeException ignored) {
            return null;
        }

        return null;
    }

    @SubscribeEvent
    public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof Mob mob) {
            clearFollower(mob.getUUID());
        }
    }

    private static Object invokeOwnerMethod(Mob mob, Method method) {
        try {
            return method.invoke(mob);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return null;
        }
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
        updateFactionTag(mob, ownerUUID);
        mob.setPersistenceRequired();
        indexFollower(mob.getUUID(), ownerUUID);
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

        if (event.getEntity() instanceof Mob targetMob
                && event.getSource().getEntity() instanceof Mob attackerMob
                && areMobsAllied(attackerMob, targetMob)) {
            event.setCanceled(true);
            return;
        }
        
        if (event.getSource().getEntity() instanceof Player player) {
            LivingEntity target = event.getEntity();
            if (target instanceof Mob mob && isAlliedWithPlayer(player, mob)) {
                event.setCanceled(true);
                if (player instanceof ServerPlayer serverPlayer) {
                    showFollowerProtectionFeedback(serverPlayer, mob);
                }
                return;
            }

            if (!(target instanceof Player)) {
                PLAYER_ATTACK_TARGET.put(player.getUUID(), target);
                PLAYER_ATTACK_TARGET_TIME.put(player.getUUID(), player.level().getGameTime());
            }
        }
        
        if (event.getEntity() instanceof Player player && event.getSource().getEntity() instanceof LivingEntity attacker) {
            if (attacker instanceof Mob mob && isAlliedWithPlayer(player, mob)) {
                event.setCanceled(true);
                return;
            }

            PLAYER_ATTACKER.put(player.getUUID(), attacker);
            PLAYER_ATTACKER_TIME.put(player.getUUID(), player.level().getGameTime());
        }
    }

    private static void showFollowerProtectionFeedback(ServerPlayer player, Mob follower) {
        long currentTime = player.level().getGameTime();
        Long lastMessageTime = PROTECTION_MESSAGE_COOLDOWN.get(player.getUUID());
        
        if (lastMessageTime == null || currentTime - lastMessageTime >= MESSAGE_COOLDOWN_TICKS) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§e[生灵契约] §c无法攻击己方生物！"));
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
            if (isAlliedWithPlayer(player, mob)) {
                event.setNewTarget(null);
            }
        }
    }

    public static void registerContractAlly(Mob mob, UUID ownerUUID) {
        mob.getPersistentData().putBoolean(TAG_CONTRACT_ALLY, true);
        registerFollower(mob, ownerUUID);
    }

    public static boolean isContractAlly(Mob mob) {
        return mob.getPersistentData().getBoolean(TAG_CONTRACT_ALLY);
    }

    public static boolean isAlliedWithPlayer(Player player, Mob mob) {
        UUID ownerUUID = getOwnerUUID(mob);
        if (ownerUUID != null) {
            if (ownerUUID.equals(player.getUUID())) {
                return true;
            }

            Player owner = player.level().getPlayerByUUID(ownerUUID);
            if (owner != null) {
                return ContractEvents.isSameTeam(player, owner);
            }

            if (mob.getPersistentData().hasUUID(TAG_FACTION_UUID)) {
                return mob.getPersistentData().getUUID(TAG_FACTION_UUID).equals(getFactionId(player));
            }
            return false;
        }

        if (mob.getPersistentData().hasUUID(TAG_FACTION_UUID)
                && mob.getPersistentData().getUUID(TAG_FACTION_UUID).equals(getFactionId(player))) {
            return true;
        }

        if (mob instanceof IronGolem ironGolem && TeamIronGolemSystem.isSameTeam(ironGolem, player)) {
            return true;
        }

        String factionMod = ContractEvents.getEffectiveContractMod(player);
        ResourceLocation entityType = ForgeRegistries.ENTITY_TYPES.getKey(mob.getType());
        return factionMod != null && !factionMod.isEmpty()
                && entityType != null && factionMod.equals(entityType.getNamespace());
    }

    public static boolean areMobsAllied(Mob first, Mob second) {
        if (first == second) {
            return true;
        }

        UUID firstOwnerUUID = getOwnerUUID(first);
        UUID secondOwnerUUID = getOwnerUUID(second);
        if (firstOwnerUUID != null && firstOwnerUUID.equals(secondOwnerUUID)) {
            return true;
        }

        Player firstOwner = firstOwnerUUID == null ? null : first.level().getPlayerByUUID(firstOwnerUUID);
        if (firstOwner != null && isAlliedWithPlayer(firstOwner, second)) {
            return true;
        }

        Player secondOwner = secondOwnerUUID == null ? null : second.level().getPlayerByUUID(secondOwnerUUID);
        if (secondOwner != null && isAlliedWithPlayer(secondOwner, first)) {
            return true;
        }

        return first.getPersistentData().hasUUID(TAG_FACTION_UUID)
                && second.getPersistentData().hasUUID(TAG_FACTION_UUID)
                && first.getPersistentData().getUUID(TAG_FACTION_UUID)
                        .equals(second.getPersistentData().getUUID(TAG_FACTION_UUID));
    }

    public static void registerFollower(Mob mob, UUID ownerUUID) {
        mob.getPersistentData().putUUID(TAG_FOLLOWER_OWNER_UUID, ownerUUID);
        updateFactionTag(mob, ownerUUID);
        mob.setPersistenceRequired();
        indexFollower(mob.getUUID(), ownerUUID);
        setupFollowerAI(mob, ownerUUID);
        syncFollowerToClients(mob, ownerUUID, true);
    }

    public static void unregisterFollower(Mob mob) {
        UUID ownerUUID = getOwnerUUID(mob);
        FOLLOWER_OWNER_MAP.remove(mob.getUUID());
        unindexFollower(mob.getUUID(), ownerUUID);
        mob.getPersistentData().remove(TAG_FOLLOWER_OWNER_UUID);
        mob.getPersistentData().remove(TAG_FACTION_UUID);
        mob.getPersistentData().remove(TAG_CONTRACT_ALLY);
        INHERITED_SUMMONS.remove(mob.getUUID());
        syncFollowerToClients(mob, null, false);
    }

    private static void syncFollowerToClients(Mob mob, UUID ownerUUID, boolean isRegister) {
        if (mob.level() instanceof ServerLevel serverLevel) {
            PacketSyncFollower packet = new PacketSyncFollower(mob.getUUID(), mob.getId(), ownerUUID, isRegister);
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

    private static UUID getFactionId(Player player) {
        UUID leaderUUID = ContractEvents.getLeaderUUID(player);
        return leaderUUID != null ? leaderUUID : player.getUUID();
    }

    private static void updateFactionTag(Mob mob, UUID ownerUUID) {
        if (!(mob.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        ServerPlayer owner = serverLevel.getServer().getPlayerList().getPlayer(ownerUUID);
        if (owner != null) {
            mob.getPersistentData().putUUID(TAG_FACTION_UUID, getFactionId(owner));
        }
    }

    public static void clearFollower(UUID mobUUID) {
        UUID ownerUUID = FOLLOWER_OWNER_MAP.remove(mobUUID);
        unindexFollower(mobUUID, ownerUUID);
        INHERITED_SUMMONS.remove(mobUUID);
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        
        syncOwnedFollowers(serverPlayer);
    }

    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        
        syncOwnedFollowers(serverPlayer);
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID playerUUID = event.getEntity().getUUID();
        PLAYER_ATTACK_TARGET.remove(playerUUID);
        PLAYER_ATTACKER.remove(playerUUID);
        PLAYER_ATTACK_TARGET_TIME.remove(playerUUID);
        PLAYER_ATTACKER_TIME.remove(playerUUID);
        PROTECTION_MESSAGE_COOLDOWN.remove(playerUUID);
    }

    private static void indexFollower(UUID mobUUID, UUID ownerUUID) {
        UUID previousOwner = FOLLOWER_OWNER_MAP.put(mobUUID, ownerUUID);
        if (previousOwner != null && !previousOwner.equals(ownerUUID)) {
            unindexFollower(mobUUID, previousOwner);
        }
        OWNER_FOLLOWER_MAP.computeIfAbsent(ownerUUID, ignored -> new HashSet<>()).add(mobUUID);
    }

    private static void unindexFollower(UUID mobUUID, UUID ownerUUID) {
        if (ownerUUID == null) return;
        Set<UUID> followers = OWNER_FOLLOWER_MAP.get(ownerUUID);
        if (followers != null) {
            followers.remove(mobUUID);
            if (followers.isEmpty()) OWNER_FOLLOWER_MAP.remove(ownerUUID);
        }
    }

    private static void syncOwnedFollowers(ServerPlayer player) {
        Set<UUID> followerIds = OWNER_FOLLOWER_MAP.getOrDefault(player.getUUID(), Collections.emptySet());
        ServerLevel level = player.serverLevel();
        for (UUID followerId : followerIds) {
            Entity entity = level.getEntity(followerId);
            if (entity instanceof Mob mob) {
                PacketSyncFollower packet = new PacketSyncFollower(mob.getUUID(), mob.getId(), player.getUUID(), true);
                NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
            }
        }
    }
}
