package org.alku.life_contract;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import org.alku.life_contract.profession.Profession;
import org.alku.life_contract.profession.ProfessionConfig;

import java.util.*;

@Mod.EventBusSubscriber(modid = Life_contract.MODID)
public class GhostSenatorSystem {

    private static final String TAG_SOUL_VALUE = "WraithSoulValue";
    private static final String TAG_SUMMON_COOLDOWN = "WraithSummonCooldown";
    private static final String TAG_DOMAIN_COOLDOWN = "WraithDomainCooldown";
    private static final String TAG_BARRAGE_COOLDOWN = "WraithBarrageCooldown";
    private static final String TAG_ULTIMATE_COOLDOWN = "WraithUltimateCooldown";
    private static final String TAG_ULTIMATE_END_TIME = "WraithUltimateEndTime";
    private static final String TAG_EXHAUST_END_TIME = "WraithExhaustEndTime";
    private static final String TAG_BARRAGE_CHARGE_START = "WraithBarrageChargeStart";
    private static final String TAG_IS_CHARGING_BARRAGE = "WraithIsChargingBarrage";
    private static final String TAG_DOMAIN_CENTER = "WraithDomainCenter";
    private static final String TAG_DOMAIN_END_TIME = "WraithDomainEndTime";
    private static final String TAG_IN_COMBAT = "WraithInCombat";
    private static final String TAG_LAST_COMBAT_TIME = "WraithLastCombatTime";
    
    private static final String EROSION_TAG = "WraithErosion";
    private static final String EROSION_STACKS = "ErosionStacks";
    private static final String EROSION_END_TIME = "ErosionEndTime";

    private static final UUID EROSION_ARMOR_UUID = UUID.fromString("d1e2f3a4-b5c6-7890-abcd-ef1234567890");
    private static final UUID EROSION_SPEED_UUID = UUID.fromString("e2f3a4b5-c6d7-8901-bcde-f12345678901");
    private static final UUID ULTIMATE_DAMAGE_UUID = UUID.fromString("f3a4b5c6-d7e8-9012-cdef-123456789012");

    private static final Map<UUID, List<UUID>> playerSummons = new HashMap<>();
    private static final Map<UUID, List<UUID>> playerClones = new HashMap<>();
    private static final Map<UUID, BlockPos> activeDomains = new HashMap<>();

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        net.minecraft.server.MinecraftServer server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        int tickCount = server.getTickCount();

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            tickWraithCouncilor(player, tickCount);
        }
    }

    private static void tickWraithCouncilor(ServerPlayer player, int tickCount) {
        String professionId = ContractEvents.getEffectiveProfessionId(player);
        if (professionId == null) return;

        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null || !profession.isWraithCouncilor()) return;

        CompoundTag data = player.getPersistentData();
        
        tickSoulRegen(player, tickCount, profession, data);
        tickCooldowns(player, data);
        tickErosionEffect(player, tickCount, profession);
        tickUltimateState(player, tickCount, profession, data);
        tickExhaustState(player, tickCount, data);
        tickDomainEffect(player, tickCount, profession, data);
        tickCombatState(player, tickCount, data);
        tickSummons(player, tickCount);
        tickClones(player, tickCount, profession);
        
        if (tickCount % 5 == 0) {
            syncClientState(player);
        }
    }

    private static void tickSoulRegen(ServerPlayer player, int tickCount, Profession profession, CompoundTag data) {
        int soulValue = data.getInt(TAG_SOUL_VALUE);
        int maxSoul = profession.getWraithSoulMax();
        
        if (soulValue >= maxSoul) return;
        
        boolean inCombat = data.getBoolean(TAG_IN_COMBAT);
        if (inCombat) return;
        
        if (tickCount % 20 != 0) return;
        
        int regenRate = profession.getWraithSoulRegenRate();
        
        boolean inDarkness = isInDarkness(player);
        boolean inSunlight = isInSunlight(player);
        
        if (inSunlight) {
            regenRate = (int)(regenRate * profession.getWraithSoulSunlightPenalty());
        }
        
        if (inDarkness) {
            regenRate += profession.getWraithSoulDarkBonus();
        }
        
        soulValue = Math.min(maxSoul, soulValue + regenRate);
        data.putInt(TAG_SOUL_VALUE, soulValue);
    }

    private static boolean isInDarkness(ServerPlayer player) {
        int lightLevel = player.level().getMaxLocalRawBrightness(player.blockPosition());
        return lightLevel < 7;
    }

    private static boolean isInSunlight(ServerPlayer player) {
        if (player.level().dimensionType().hasCeiling()) return false;
        int lightLevel = player.level().getMaxLocalRawBrightness(player.blockPosition());
        return lightLevel > 12 && player.level().canSeeSky(player.blockPosition());
    }

    private static void tickCooldowns(ServerPlayer player, CompoundTag data) {
        int summonCd = data.getInt(TAG_SUMMON_COOLDOWN);
        if (summonCd > 0) data.putInt(TAG_SUMMON_COOLDOWN, summonCd - 1);
        
        int domainCd = data.getInt(TAG_DOMAIN_COOLDOWN);
        if (domainCd > 0) data.putInt(TAG_DOMAIN_COOLDOWN, domainCd - 1);
        
        int barrageCd = data.getInt(TAG_BARRAGE_COOLDOWN);
        if (barrageCd > 0) data.putInt(TAG_BARRAGE_COOLDOWN, barrageCd - 1);
        
        int ultimateCd = data.getInt(TAG_ULTIMATE_COOLDOWN);
        if (ultimateCd > 0) data.putInt(TAG_ULTIMATE_COOLDOWN, ultimateCd - 1);
    }

    private static void tickErosionEffect(ServerPlayer player, int tickCount, Profession profession) {
        if (tickCount % 20 != 0) return;
        
        List<LivingEntity> entities = player.level().getEntitiesOfClass(
            LivingEntity.class, 
            player.getBoundingBox().inflate(50),
            e -> e != player && e.isAlive() && hasErosion(e)
        );
        
        for (LivingEntity entity : entities) {
            int stacks = getErosionStacks(entity);
            if (stacks > 0) {
                float damage = profession.getWraithErosionDamage() * stacks;
                entity.hurt(player.level().damageSources().magic(), damage);
                
                if (entity.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.SOUL, 
                        entity.getX(), entity.getY() + 1, entity.getZ(), 
                        5, 0.3, 0.3, 0.3, 0.05);
                }
            }
        }
    }

    private static void tickUltimateState(ServerPlayer player, int tickCount, Profession profession, CompoundTag data) {
        long ultimateEndTime = data.getLong(TAG_ULTIMATE_END_TIME);
        
        if (ultimateEndTime > 0 && tickCount >= ultimateEndTime) {
            data.putLong(TAG_ULTIMATE_END_TIME, 0);
            endUltimate(player, profession, data);
        } else if (ultimateEndTime > 0) {
            int soulRegen = profession.getWraithUltimateSoulRegen();
            if (tickCount % 20 == 0) {
                int soulValue = data.getInt(TAG_SOUL_VALUE);
                soulValue = Math.min(profession.getWraithSoulMax(), soulValue + soulRegen);
                data.putInt(TAG_SOUL_VALUE, soulValue);
            }
        }
    }

    private static void tickExhaustState(ServerPlayer player, int tickCount, CompoundTag data) {
        long exhaustEndTime = data.getLong(TAG_EXHAUST_END_TIME);
        
        if (exhaustEndTime > 0 && tickCount >= exhaustEndTime) {
            data.putLong(TAG_EXHAUST_END_TIME, 0);
            player.sendSystemMessage(Component.literal("§7[亡魂议员] 冥能耗尽状态结束。"));
        }
    }

    private static void tickDomainEffect(ServerPlayer player, int tickCount, Profession profession, CompoundTag data) {
        long domainEndTime = data.getLong(TAG_DOMAIN_END_TIME);
        
        if (domainEndTime <= 0 || tickCount < domainEndTime) return;
        
        data.putLong(TAG_DOMAIN_END_TIME, 0);
        activeDomains.remove(player.getUUID());
        player.sendSystemMessage(Component.literal("§8[亡魂议员] 冥域禁锢消散。"));
    }

    private static void tickCombatState(ServerPlayer player, int tickCount, CompoundTag data) {
        long lastCombatTime = data.getLong(TAG_LAST_COMBAT_TIME);
        boolean inCombat = data.getBoolean(TAG_IN_COMBAT);
        
        if (inCombat && tickCount - lastCombatTime > 100) {
            data.putBoolean(TAG_IN_COMBAT, false);
        }
    }

    private static void tickSummons(ServerPlayer player, int tickCount) {
        List<UUID> summons = playerSummons.get(player.getUUID());
        if (summons == null || summons.isEmpty()) return;
        
        List<UUID> toRemove = new ArrayList<>();
        for (UUID uuid : summons) {
            Entity entity = ((ServerLevel) player.level()).getEntity(uuid);
            if (entity == null || !entity.isAlive()) {
                toRemove.add(uuid);
            }
        }
        summons.removeAll(toRemove);
    }

    private static void tickClones(ServerPlayer player, int tickCount, Profession profession) {
        List<UUID> clones = playerClones.get(player.getUUID());
        if (clones == null || clones.isEmpty()) return;
        
        List<UUID> toRemove = new ArrayList<>();
        for (UUID uuid : clones) {
            Entity entity = ((ServerLevel) player.level()).getEntity(uuid);
            if (entity == null || !entity.isAlive()) {
                toRemove.add(uuid);
            }
        }
        clones.removeAll(toRemove);
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity deadEntity = event.getEntity();
        if (deadEntity.level().isClientSide) return;
        
        if (!(deadEntity.level() instanceof ServerLevel serverLevel)) return;
        
        for (ServerPlayer player : serverLevel.getPlayers(p -> true)) {
            String professionId = ContractEvents.getEffectiveProfessionId(player);
            if (professionId == null || professionId.isEmpty()) continue;
            
            Profession profession = ProfessionConfig.getProfession(professionId);
            if (profession == null || !profession.isWraithCouncilor()) continue;
            
            CompoundTag data = player.getPersistentData();
            
            if (event.getSource().getEntity() == player) {
                int soulBonus = profession.getWraithSoulKillBonus();
                addSoulValue(player, profession, soulBonus);
            }
            
            if (isSummonOf(player, deadEntity)) {
                int soulBonus = profession.getWraithSoulSummonKillBonus();
                addSoulValue(player, profession, soulBonus);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        
        String professionId = ContractEvents.getEffectiveProfessionId(player);
        if (professionId == null) return;

        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null || !profession.isWraithCouncilor()) return;

        LivingEntity target = event.getEntity();
        
        applyErosion(player, target, profession);
        
        CompoundTag data = player.getPersistentData();
        data.putBoolean(TAG_IN_COMBAT, true);
        data.putLong(TAG_LAST_COMBAT_TIME, player.getServer().getTickCount());
        
        int soulBonus = profession.getWraithSoulHitBonus();
        addSoulValue(player, profession, soulBonus);
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        
        String professionId = ContractEvents.getEffectiveProfessionId(player);
        if (professionId == null) return;

        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null || !profession.isWraithCouncilor()) return;

        CompoundTag data = player.getPersistentData();
        data.putBoolean(TAG_IN_COMBAT, true);
        data.putLong(TAG_LAST_COMBAT_TIME, player.getServer().getTickCount());
        
        long ultimateEndTime = data.getLong(TAG_ULTIMATE_END_TIME);
        if (ultimateEndTime > 0) {
            float newDamage = event.getAmount() * (1 + profession.getWraithUltimateDamageIncrease());
            event.setAmount(newDamage);
        }
    }

    @SubscribeEvent
    public static void onUndeadTargetPlayer(LivingChangeTargetEvent event) {
        if (!(event.getNewTarget() instanceof ServerPlayer player)) return;
        if (!(event.getEntity() instanceof Mob mob)) return;
        
        String professionId = ContractEvents.getEffectiveProfessionId(player);
        if (professionId == null) return;

        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null || !profession.isWraithCouncilor()) return;
        
        if (isUndeadMob(mob)) {
            event.setCanceled(true);
            event.setNewTarget(null);
        }
    }

    @SubscribeEvent
    public static void onMobEffectApply(MobEffectEvent.Applicable event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        
        String professionId = ContractEvents.getEffectiveProfessionId(player);
        if (professionId == null) return;

        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null || !profession.isWraithCouncilor()) return;
        
        MobEffectInstance effectInstance = event.getEffectInstance();
        if (effectInstance == null) return;
        
        MobEffect effect = effectInstance.getEffect();
        
        if (effect == MobEffects.POISON || effect == MobEffects.WITHER) {
            event.setResult(Event.Result.DENY);
            return;
        }
        
        if (effect == MobEffects.HEAL) {
            event.setResult(Event.Result.DENY);
            float healAmount = effectInstance.getAmplifier() > 0 ? 
                player.getMaxHealth() * 0.1f * (effectInstance.getAmplifier() + 1) : 
                player.getMaxHealth() * 0.1f;
            player.hurt(player.level().damageSources().magic(), healAmount);
            
            if (player.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.SOUL, 
                    player.getX(), player.getY() + 1, player.getZ(), 
                    20, 0.5, 0.5, 0.5, 0.1);
            }
            return;
        }
        
        if (effect == MobEffects.HARM) {
            event.setResult(Event.Result.DENY);
            float healAmount = effectInstance.getAmplifier() > 0 ? 
                6.0f * (effectInstance.getAmplifier() + 1) : 6.0f;
            player.heal(healAmount);
            
            if (player.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.HEART, 
                    player.getX(), player.getY() + 1, player.getZ(), 
                    10, 0.5, 0.5, 0.5, 0.1);
            }
            return;
        }
        
        if (effect == MobEffects.REGENERATION) {
            event.setResult(Event.Result.DENY);
            float damageAmount = 2.0f;
            player.hurt(player.level().damageSources().magic(), damageAmount);
            
            if (player.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.DAMAGE_INDICATOR, 
                    player.getX(), player.getY() + 1, player.getZ(), 
                    5, 0.3, 0.3, 0.3, 0.05);
            }
        }
    }
    
    @SubscribeEvent
    public static void onWraithCloneAttack(LivingAttackEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        
        Entity attackerEntity = event.getSource().getEntity();
        if (attackerEntity == null) return;
        
        if (!(attackerEntity instanceof Zombie attacker)) return;
        
        CompoundTag data = attacker.getPersistentData();
        boolean isClone = data.getBoolean("IsWraithClone");
        
        if (!isClone) return;
        
        LivingEntity target = event.getEntity();
        
        target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 1200, 0, false, true));
        target.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 0, false, true));
        
        if (attacker.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SOUL, 
                target.getX(), target.getY() + 1, target.getZ(), 
                10, 0.5, 0.5, 0.5, 0.1);
        }
    }

    private static void addSoulValue(ServerPlayer player, Profession profession, int amount) {
        CompoundTag data = player.getPersistentData();
        int soulValue = data.getInt(TAG_SOUL_VALUE);
        int maxSoul = profession.getWraithSoulMax();
        soulValue = Math.min(maxSoul, soulValue + amount);
        data.putInt(TAG_SOUL_VALUE, soulValue);
    }

    private static boolean consumeSoulValue(ServerPlayer player, int amount) {
        CompoundTag data = player.getPersistentData();
        int soulValue = data.getInt(TAG_SOUL_VALUE);
        
        if (soulValue < amount) return false;
        
        data.putInt(TAG_SOUL_VALUE, soulValue - amount);
        return true;
    }

    public static void applyErosion(ServerPlayer player, LivingEntity target, Profession profession) {
        CompoundTag targetData = target.getPersistentData();
        
        int currentStacks = targetData.getInt(EROSION_STACKS);
        int maxStacks = profession.getWraithErosionMaxStacks();
        int duration = profession.getWraithErosionDuration();
        
        duration = (int)(duration * (1.0f + org.alku.life_contract.wraith_councilor.WraithEquipmentHandler.getErosionDurationBonus(player)));
        
        int newStacks = Math.min(maxStacks, currentStacks + 1);
        targetData.putInt(EROSION_STACKS, newStacks);
        targetData.putLong(EROSION_END_TIME, player.getServer().getTickCount() + duration);
        
        applyErosionAttributes(target, newStacks, profession);
        
        if (target.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SOUL, 
                target.getX(), target.getY() + 1, target.getZ(), 
                10, 0.3, 0.3, 0.3, 0.05);
        }
    }

    private static void applyErosionAttributes(LivingEntity entity, int stacks, Profession profession) {
        float armorReduction = profession.getWraithErosionArmorReduction() * stacks;
        float slowPercent = profession.getWraithErosionSlowPercent() * stacks;
        
        var armorAttr = entity.getAttribute(Attributes.ARMOR);
        if (armorAttr != null) {
            armorAttr.removeModifier(EROSION_ARMOR_UUID);
            armorAttr.addPermanentModifier(new AttributeModifier(
                EROSION_ARMOR_UUID, "wraith_erosion_armor",
                -armorReduction, AttributeModifier.Operation.MULTIPLY_TOTAL
            ));
        }
        
        var speedAttr = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr != null) {
            speedAttr.removeModifier(EROSION_SPEED_UUID);
            speedAttr.addPermanentModifier(new AttributeModifier(
                EROSION_SPEED_UUID, "wraith_erosion_speed",
                -slowPercent, AttributeModifier.Operation.MULTIPLY_TOTAL
            ));
        }
    }

    private static boolean hasErosion(LivingEntity entity) {
        CompoundTag data = entity.getPersistentData();
        long endTime = data.getLong(EROSION_END_TIME);
        if (endTime <= 0) return false;
        
        if (entity.getServer().getTickCount() >= endTime) {
            clearErosion(entity);
            return false;
        }
        return true;
    }

    private static int getErosionStacks(LivingEntity entity) {
        if (!hasErosion(entity)) return 0;
        return entity.getPersistentData().getInt(EROSION_STACKS);
    }

    private static void clearErosion(LivingEntity entity) {
        CompoundTag data = entity.getPersistentData();
        data.putInt(EROSION_STACKS, 0);
        data.putLong(EROSION_END_TIME, 0);
        
        var armorAttr = entity.getAttribute(Attributes.ARMOR);
        if (armorAttr != null) {
            armorAttr.removeModifier(EROSION_ARMOR_UUID);
        }
        
        var speedAttr = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr != null) {
            speedAttr.removeModifier(EROSION_SPEED_UUID);
        }
    }

    public static void useSummonSkill(ServerPlayer player) {
        String professionId = ContractEvents.getEffectiveProfessionId(player);
        if (professionId == null) return;

        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null || !profession.isWraithCouncilor()) return;

        CompoundTag data = player.getPersistentData();
        int cooldown = data.getInt(TAG_SUMMON_COOLDOWN);
        
        if (cooldown > 0) {
            player.sendSystemMessage(Component.literal("§c[亡魂议员] 亡魂号令冷却中！"));
            return;
        }
        
        int cost = profession.getWraithSummonCost();
        if (!consumeSoulValue(player, cost)) {
            player.sendSystemMessage(Component.literal("§c[亡魂议员] 冥魂值不足！需要 " + cost + " 点。"));
            return;
        }
        
        int summonCount = profession.getWraithSummonCount();
        float corpseRange = profession.getWraithSummonCorpseRange();
        int extraMax = profession.getWraithSummonExtraMax();
        
        List<LivingEntity> nearbyCorpses = player.level().getEntitiesOfClass(
            LivingEntity.class,
            player.getBoundingBox().inflate(corpseRange),
            e -> !e.isAlive()
        );
        
        int extraSummons = Math.min(extraMax, nearbyCorpses.size());
        int totalSummons = summonCount + extraSummons;
        
        for (int i = 0; i < totalSummons; i++) {
            spawnWraithGuard(player, profession, i);
        }
        
        data.putInt(TAG_SUMMON_COOLDOWN, profession.getWraithSummonCooldown());
        player.sendSystemMessage(Component.literal("§8[亡魂议员] 召唤了 " + totalSummons + " 只亡魂护卫！"));
        
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SOUL, 
                player.getX(), player.getY() + 1, player.getZ(), 
                50, 1, 1, 1, 0.1);
        }
        
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 0.5f, 1.2f);
    }

    private static void spawnWraithGuard(ServerPlayer player, Profession profession, int index) {
        ServerLevel serverLevel = (ServerLevel) player.level();
        
        Zombie guard = new Zombie(serverLevel);
        
        double angle = (index * 72) * Math.PI / 180;
        double distance = 2.0;
        double x = player.getX() + Math.cos(angle) * distance;
        double z = player.getZ() + Math.sin(angle) * distance;
        
        guard.moveTo(x, player.getY(), z, 0, 0);
        
        guard.setCustomName(Component.literal("§8亡魂护卫"));
        guard.setCustomNameVisible(true);
        
        float health = profession.getWraithSummonHealth();
        float damage = profession.getWraithSummonDamage();
        
        health *= (1.0f + org.alku.life_contract.wraith_councilor.WraithEquipmentHandler.getSummonHealthBonus(player));
        damage *= (1.0f + org.alku.life_contract.wraith_councilor.WraithEquipmentHandler.getSummonDamageBonus(player));
        
        guard.getAttribute(Attributes.MAX_HEALTH).setBaseValue(health);
        guard.setHealth(health);
        
        guard.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(damage);
        guard.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.3);
        guard.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(0.5);
        
        guard.goalSelector.removeAllGoals(goal -> true);
        guard.targetSelector.removeAllGoals(goal -> true);
        
        guard.goalSelector.addGoal(0, new FloatGoal(guard));
        guard.goalSelector.addGoal(1, new MeleeAttackGoal(guard, 1.0, true));
        guard.goalSelector.addGoal(2, new MoveTowardsTargetGoal(guard, 1.0, 20));
        guard.goalSelector.addGoal(3, new FollowOwnerGoal(guard, player));
        guard.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(guard, 0.5));
        
        guard.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(guard, player));
        guard.targetSelector.addGoal(2, new OwnerHurtTargetGoal(guard, player));
        guard.targetSelector.addGoal(3, new HurtByTargetGoal(guard));
        
        serverLevel.addFreshEntity(guard);
        
        int duration = profession.getWraithSummonDuration();
        guard.getPersistentData().putLong("WraithGuardOwner", player.getUUID().getMostSignificantBits());
        guard.getPersistentData().putLong("WraithGuardOwnerLow", player.getUUID().getLeastSignificantBits());
        guard.getPersistentData().putLong("WraithGuardEndTime", player.getServer().getTickCount() + duration);
        
        playerSummons.computeIfAbsent(player.getUUID(), k -> new ArrayList<>()).add(guard.getUUID());
    }

    private static boolean isSummonOf(ServerPlayer player, LivingEntity entity) {
        List<UUID> summons = playerSummons.get(player.getUUID());
        if (summons == null) return false;
        return summons.contains(entity.getUUID());
    }
    
    private static boolean isWraithClone(LivingEntity entity) {
        return entity.getPersistentData().getBoolean("IsWraithClone");
    }

    public static void useDomainSkill(ServerPlayer player, Vec3 targetPos) {
        String professionId = ContractEvents.getEffectiveProfessionId(player);
        if (professionId == null) return;

        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null || !profession.isWraithCouncilor()) return;

        CompoundTag data = player.getPersistentData();
        int cooldown = data.getInt(TAG_DOMAIN_COOLDOWN);
        
        if (cooldown > 0) {
            player.sendSystemMessage(Component.literal("§c[亡魂议员] 冥域禁锢冷却中！"));
            return;
        }
        
        int cost = profession.getWraithDomainCost();
        if (!consumeSoulValue(player, cost)) {
            player.sendSystemMessage(Component.literal("§c[亡魂议员] 冥魂值不足！需要 " + cost + " 点。"));
            return;
        }
        
        float radius = profession.getWraithDomainRadius();
        int duration = profession.getWraithDomainDuration();
        float damage = profession.getWraithDomainDamage();
        float bossSlow = profession.getWraithDomainBossSlow();
        int charmDuration = profession.getWraithDomainCharmDuration();
        
        radius *= (1.0f + org.alku.life_contract.wraith_councilor.WraithEquipmentHandler.getAreaBonus(player));
        
        BlockPos centerPos = new BlockPos((int)targetPos.x, (int)targetPos.y, (int)targetPos.z);
        activeDomains.put(player.getUUID(), centerPos);
        data.putLong(TAG_DOMAIN_END_TIME, player.getServer().getTickCount() + duration);
        
        List<LivingEntity> targets = player.level().getEntitiesOfClass(
            LivingEntity.class,
            new AABB(centerPos).inflate(radius),
            e -> e != player && e.isAlive() && !isWraithClone(e) && !isSummonOf(player, e)
        );
        
        for (LivingEntity target : targets) {
            target.hurt(player.level().damageSources().magic(), damage);
            applyErosion(player, target, profession);
            
            if (isBoss(target)) {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 
                    (int)(bossSlow * 5), false, true));
            } else {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 
                    10, false, true));
            }
            
            if (isUndeadMob(target) && !isBoss(target)) {
                charmUndead(player, target, charmDuration);
            }
        }
        
        data.putInt(TAG_DOMAIN_COOLDOWN, profession.getWraithDomainCooldown());
        player.sendSystemMessage(Component.literal("§5[亡魂议员] 冥域禁锢释放！"));
        
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SOUL, 
                targetPos.x, targetPos.y + 1, targetPos.z, 
                100, radius, 1, radius, 0.1);
            serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL, 
                targetPos.x, targetPos.y + 1, targetPos.z, 
                50, radius, 1, radius, 0.05);
        }
        
        player.level().playSound(null, targetPos.x, targetPos.y, targetPos.z,
            SoundEvents.WITHER_AMBIENT, SoundSource.PLAYERS, 1.0f, 0.8f);
    }

    private static boolean isBoss(LivingEntity entity) {
        return entity.isCurrentlyGlowing() || 
               entity.getMaxHealth() > 100 ||
               entity.getClass().getSimpleName().contains("Boss") ||
               entity.getClass().getSimpleName().contains("Wither") ||
               entity.getClass().getSimpleName().contains("Dragon") ||
               entity.getClass().getSimpleName().contains("Elder");
    }

    private static boolean isUndeadMob(LivingEntity entity) {
        return entity instanceof Zombie ||
               entity instanceof Skeleton ||
               entity instanceof WitherSkeleton ||
               entity instanceof ZombifiedPiglin ||
               entity instanceof net.minecraft.world.entity.monster.Phantom ||
               entity instanceof net.minecraft.world.entity.monster.Drowned ||
               entity instanceof net.minecraft.world.entity.monster.Husk ||
               entity instanceof net.minecraft.world.entity.monster.Stray ||
               entity.getClass().getSimpleName().contains("Zombie") ||
               entity.getClass().getSimpleName().contains("Skeleton") ||
               entity.getClass().getSimpleName().contains("Phantom") ||
               entity.getClass().getSimpleName().contains("Drowned") ||
               entity.getClass().getSimpleName().contains("Husk") ||
               entity.getClass().getSimpleName().contains("Stray");
    }

    private static void charmUndead(ServerPlayer player, LivingEntity undead, int duration) {
        if (!(undead instanceof net.minecraft.world.entity.PathfinderMob mob)) return;
        
        mob.targetSelector.removeAllGoals(goal -> true);
        mob.targetSelector.addGoal(1, new HurtByTargetGoal(mob));
        mob.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(mob, 
            net.minecraft.world.entity.monster.Monster.class, true, 
            e -> e != mob && !isUndeadMob(e)));
        
        if (mob.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.HEART, 
                mob.getX(), mob.getY() + 1.5, mob.getZ(), 
                5, 0.3, 0.3, 0.3, 0.05);
        }
    }

    public static void startBarrageCharge(ServerPlayer player) {
        String professionId = ContractEvents.getEffectiveProfessionId(player);
        if (professionId == null) return;

        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null || !profession.isWraithCouncilor()) return;

        CompoundTag data = player.getPersistentData();
        data.putBoolean(TAG_IS_CHARGING_BARRAGE, true);
        data.putLong(TAG_BARRAGE_CHARGE_START, player.getServer().getTickCount());
    }

    public static void releaseBarrage(ServerPlayer player) {
        String professionId = ContractEvents.getEffectiveProfessionId(player);
        if (professionId == null) return;

        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null || !profession.isWraithCouncilor()) return;

        CompoundTag data = player.getPersistentData();
        
        if (!data.getBoolean(TAG_IS_CHARGING_BARRAGE)) return;
        
        data.putBoolean(TAG_IS_CHARGING_BARRAGE, false);
        
        int cooldown = data.getInt(TAG_BARRAGE_COOLDOWN);
        if (cooldown > 0) {
            player.sendSystemMessage(Component.literal("§c[亡魂议员] 暗蚀弹幕冷却中！"));
            return;
        }
        
        long chargeStart = data.getLong(TAG_BARRAGE_CHARGE_START);
        int chargeTime = player.getServer().getTickCount() - (int)chargeStart;
        int maxChargeTime = profession.getWraithBarrageMaxChargeTime();
        boolean isCharged = chargeTime >= maxChargeTime;
        
        int cost = isCharged ? 
            profession.getWraithBarrageBaseCost() + profession.getWraithBarrageChargedCost() : 
            profession.getWraithBarrageBaseCost();
        
        if (!consumeSoulValue(player, cost)) {
            player.sendSystemMessage(Component.literal("§c[亡魂议员] 冥魂值不足！需要 " + cost + " 点。"));
            return;
        }
        
        int orbCount = isCharged ? profession.getWraithBarrageChargedOrbs() : profession.getWraithBarrageBaseOrbs();
        float damage = isCharged ? profession.getWraithBarrageChargedDamage() : profession.getWraithBarrageBaseDamage();
        float range = isCharged ? profession.getWraithBarrageChargedRange() : profession.getWraithBarrageBaseRange();
        int maxHits = profession.getWraithBarrageMaxHits();
        
        range += org.alku.life_contract.wraith_councilor.WraithEquipmentHandler.getRangeBonus(player);
        range *= (1.0f + org.alku.life_contract.wraith_councilor.WraithEquipmentHandler.getAreaBonus(player));
        
        fireBarrageOrbs(player, orbCount, damage, range, maxHits, profession);
        
        data.putInt(TAG_BARRAGE_COOLDOWN, profession.getWraithBarrageCooldown());
        
        String msg = isCharged ? "§5[亡魂议员] 满蓄力暗蚀弹幕！" : "§8[亡魂议员] 暗蚀弹幕释放！";
        player.sendSystemMessage(Component.literal(msg));
    }

    private static void fireBarrageOrbs(ServerPlayer player, int orbCount, float damage, float range, int maxHits, Profession profession) {
        Vec3 lookVec = player.getLookAngle();
        ServerLevel serverLevel = (ServerLevel) player.level();
        
        Map<LivingEntity, Integer> hitCounts = new HashMap<>();
        
        for (int i = 0; i < orbCount; i++) {
            double spreadAngle = (i - orbCount / 2.0) * 15 * Math.PI / 180;
            double cos = Math.cos(spreadAngle);
            double sin = Math.sin(spreadAngle);
            
            Vec3 direction = new Vec3(
                lookVec.x * cos - lookVec.z * sin,
                lookVec.y,
                lookVec.x * sin + lookVec.z * cos
            ).normalize();
            
            Vec3 orbPos = player.position().add(0, 1.5, 0);
            Vec3 targetPos = orbPos.add(direction.scale(range));
            
            List<LivingEntity> targets = serverLevel.getEntitiesOfClass(
                LivingEntity.class,
                new AABB(orbPos, targetPos).inflate(1.0),
                e -> e != player && e.isAlive()
            );
            
            for (LivingEntity target : targets) {
                int hits = hitCounts.getOrDefault(target, 0);
                if (hits >= maxHits) continue;
                
                if (target.getBoundingBox().clip(orbPos, targetPos).isPresent()) {
                    target.hurt(player.level().damageSources().magic(), damage);
                    applyErosion(player, target, profession);
                    hitCounts.put(target, hits + 1);
                    
                    serverLevel.sendParticles(ParticleTypes.SOUL, 
                        target.getX(), target.getY() + 1, target.getZ(), 
                        5, 0.2, 0.2, 0.2, 0.05);
                    break;
                }
            }
            
            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, 
                orbPos.x, orbPos.y, orbPos.z, 
                1, direction.x * 0.5, direction.y * 0.5, direction.z * 0.5, 0.1);
        }
        
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.SOUL_ESCAPE, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    public static void useUltimate(ServerPlayer player) {
        String professionId = ContractEvents.getEffectiveProfessionId(player);
        if (professionId == null) return;

        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null || !profession.isWraithCouncilor()) return;

        CompoundTag data = player.getPersistentData();
        int cooldown = data.getInt(TAG_ULTIMATE_COOLDOWN);
        
        if (cooldown > 0) {
            player.sendSystemMessage(Component.literal("§c[亡魂议员] 亡魂议会降临冷却中！"));
            return;
        }
        
        int minCost = profession.getWraithUltimateMinCost();
        int soulValue = data.getInt(TAG_SOUL_VALUE);
        
        if (soulValue < minCost) {
            player.sendSystemMessage(Component.literal("§c[亡魂议员] 冥魂值不足！需要至少 " + minCost + " 点。"));
            return;
        }
        
        data.putInt(TAG_SOUL_VALUE, 0);
        
        int duration = profession.getWraithUltimateDuration();
        int cloneCount = profession.getWraithUltimateCloneCount();
        
        data.putLong(TAG_ULTIMATE_END_TIME, player.getServer().getTickCount() + duration);
        
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, duration, 2, false, false));
        
        for (int i = 0; i < cloneCount; i++) {
            spawnWraithClone(player, profession, i);
        }
        
        data.putInt(TAG_ULTIMATE_COOLDOWN, profession.getWraithUltimateCooldown());
        player.sendSystemMessage(Component.literal("§4[亡魂议员] 亡魂议会降临！"));
        
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SOUL, 
                player.getX(), player.getY() + 1, player.getZ(), 
                100, 2, 2, 2, 0.2);
            serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, 
                player.getX(), player.getY() + 1, player.getZ(), 
                1, 0, 0, 0, 0);
        }
        
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 1.0f, 0.6f);
    }

    private static void spawnWraithClone(ServerPlayer player, Profession profession, int index) {
        ServerLevel serverLevel = (ServerLevel) player.level();
        
        Zombie clone = new Zombie(serverLevel);
        
        double angle = (index * 120) * Math.PI / 180;
        double distance = 3.0;
        double x = player.getX() + Math.cos(angle) * distance;
        double z = player.getZ() + Math.sin(angle) * distance;
        
        clone.moveTo(x, player.getY(), z, 0, 0);
        
        clone.setCustomName(Component.literal("§5亡魂议员分身"));
        clone.setCustomNameVisible(true);
        
        clone.getAttribute(Attributes.MAX_HEALTH).setBaseValue(20.0);
        clone.setHealth(20.0f);
        
        clone.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(5.0);
        clone.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.35);
        
        clone.getPersistentData().putLong("WraithCloneOwner", player.getUUID().getMostSignificantBits());
        clone.getPersistentData().putLong("WraithCloneOwnerLow", player.getUUID().getLeastSignificantBits());
        clone.getPersistentData().putBoolean("IsWraithClone", true);
        
        clone.goalSelector.removeAllGoals(goal -> true);
        clone.targetSelector.removeAllGoals(goal -> true);
        
        clone.goalSelector.addGoal(0, new FloatGoal(clone));
        clone.goalSelector.addGoal(1, new MeleeAttackGoal(clone, 1.0, true));
        clone.goalSelector.addGoal(2, new FollowOwnerGoal(clone, player));
        
        clone.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(clone, player));
        clone.targetSelector.addGoal(2, new OwnerHurtTargetGoal(clone, player));
        clone.targetSelector.addGoal(3, new HurtByTargetGoal(clone));
        
        serverLevel.addFreshEntity(clone);
        
        playerClones.computeIfAbsent(player.getUUID(), k -> new ArrayList<>()).add(clone.getUUID());
    }

    private static void endUltimate(ServerPlayer player, Profession profession, CompoundTag data) {
        List<UUID> clones = playerClones.remove(player.getUUID());
        if (clones != null) {
            for (UUID uuid : clones) {
                Entity entity = ((ServerLevel) player.level()).getEntity(uuid);
                if (entity != null && entity.isAlive()) {
                    entity.discard();
                }
            }
        }
        
        int exhaustDuration = profession.getWraithUltimateExhaustDuration();
        data.putLong(TAG_EXHAUST_END_TIME, player.getServer().getTickCount() + exhaustDuration);
        
        player.sendSystemMessage(Component.literal("§7[亡魂议员] 亡魂议会消散，进入冥能耗尽状态..."));
        
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SMOKE, 
                player.getX(), player.getY() + 1, player.getZ(), 
                50, 1, 1, 1, 0.1);
        }
    }

    public static int getSoulValue(ServerPlayer player) {
        return player.getPersistentData().getInt(TAG_SOUL_VALUE);
    }

    public static int getSummonCooldown(ServerPlayer player) {
        return player.getPersistentData().getInt(TAG_SUMMON_COOLDOWN);
    }

    public static int getDomainCooldown(ServerPlayer player) {
        return player.getPersistentData().getInt(TAG_DOMAIN_COOLDOWN);
    }

    public static int getBarrageCooldown(ServerPlayer player) {
        return player.getPersistentData().getInt(TAG_BARRAGE_COOLDOWN);
    }

    public static int getUltimateCooldown(ServerPlayer player) {
        return player.getPersistentData().getInt(TAG_ULTIMATE_COOLDOWN);
    }

    public static boolean isUltimateActive(ServerPlayer player) {
        return player.getPersistentData().getLong(TAG_ULTIMATE_END_TIME) > 
               player.getServer().getTickCount();
    }

    public static boolean isExhausted(ServerPlayer player) {
        return player.getPersistentData().getLong(TAG_EXHAUST_END_TIME) > 
               player.getServer().getTickCount();
    }

    public static boolean isChargingBarrage(ServerPlayer player) {
        return player.getPersistentData().getBoolean(TAG_IS_CHARGING_BARRAGE);
    }

    public static int getBarrageChargeTime(ServerPlayer player) {
        long chargeStart = player.getPersistentData().getLong(TAG_BARRAGE_CHARGE_START);
        if (chargeStart <= 0) return 0;
        return player.getServer().getTickCount() - (int)chargeStart;
    }

    public static void onPlayerRespawn(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        data.putInt(TAG_SOUL_VALUE, 60);
        data.putInt(TAG_SUMMON_COOLDOWN, 0);
        data.putInt(TAG_DOMAIN_COOLDOWN, 0);
        data.putInt(TAG_BARRAGE_COOLDOWN, 0);
        data.putInt(TAG_ULTIMATE_COOLDOWN, 0);
        data.putLong(TAG_ULTIMATE_END_TIME, 0);
        data.putLong(TAG_EXHAUST_END_TIME, 0);
        data.putLong(TAG_BARRAGE_CHARGE_START, 0);
        data.putBoolean(TAG_IS_CHARGING_BARRAGE, false);
        data.putLong(TAG_DOMAIN_END_TIME, 0);
        data.putBoolean(TAG_IN_COMBAT, false);
        
        playerSummons.remove(player.getUUID());
        playerClones.remove(player.getUUID());
        activeDomains.remove(player.getUUID());
    }

    private static void syncClientState(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        int soulValue = data.getInt(TAG_SOUL_VALUE);
        int summonCd = data.getInt(TAG_SUMMON_COOLDOWN);
        int domainCd = data.getInt(TAG_DOMAIN_COOLDOWN);
        int barrageCd = data.getInt(TAG_BARRAGE_COOLDOWN);
        int ultimateCd = data.getInt(TAG_ULTIMATE_COOLDOWN);
        boolean ultimateActive = isUltimateActive(player);
        boolean exhausted = isExhausted(player);
        boolean charging = data.getBoolean(TAG_IS_CHARGING_BARRAGE);
        int chargeTime = charging ? getBarrageChargeTime(player) : 0;
        
        NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
            new PacketSyncWraithState(soulValue, summonCd, domainCd, barrageCd, ultimateCd,
                ultimateActive, exhausted, charging, chargeTime));
    }

    public static class FollowOwnerGoal extends Goal {
        private final Mob mob;
        private final Player owner;
        private final double speedModifier;
        
        public FollowOwnerGoal(Mob mob, Player owner) {
            this.mob = mob;
            this.owner = owner;
            this.speedModifier = 1.0;
        }
        
        @Override
        public boolean canUse() {
            return owner != null && owner.isAlive() && mob.distanceTo(owner) > 5;
        }
        
        @Override
        public void tick() {
            mob.getNavigation().moveTo(owner, speedModifier);
        }
    }

    public static class OwnerHurtByTargetGoal extends Goal {
        private final Mob mob;
        private final Player owner;
        private LivingEntity target;
        
        public OwnerHurtByTargetGoal(Mob mob, Player owner) {
            this.mob = mob;
            this.owner = owner;
        }
        
        @Override
        public boolean canUse() {
            if (owner == null || !owner.isAlive()) return false;
            target = owner.getLastHurtByMob();
            return target != null && target.isAlive();
        }
        
        @Override
        public void start() {
            mob.setTarget(target);
        }
    }

    public static class OwnerHurtTargetGoal extends Goal {
        private final Mob mob;
        private final Player owner;
        private LivingEntity target;
        
        public OwnerHurtTargetGoal(Mob mob, Player owner) {
            this.mob = mob;
            this.owner = owner;
        }
        
        @Override
        public boolean canUse() {
            if (owner == null || !owner.isAlive()) return false;
            target = owner.getLastHurtMob();
            return target != null && target.isAlive();
        }
        
        @Override
        public void start() {
            mob.setTarget(target);
        }
    }
}
