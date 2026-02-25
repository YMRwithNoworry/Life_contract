package org.alku.life_contract.jungle_ape_god;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import org.alku.life_contract.Life_contract;
import org.alku.life_contract.NetworkHandler;
import org.alku.life_contract.ContractEvents;
import org.alku.life_contract.profession.Profession;
import org.alku.life_contract.profession.ProfessionConfig;

import java.util.*;

@Mod.EventBusSubscriber(modid = Life_contract.MODID)
public class JungleApeGodSystem {

    private static final String TAG_RHYTHM_STACKS = "JungleApeRhythmStacks";
    private static final String TAG_BERSERK_END_TIME = "JungleApeBerserkEndTime";
    private static final String TAG_Q1_COOLDOWN = "JungleApeQ1Cooldown";
    private static final String TAG_Q2_COOLDOWN = "JungleApeQ2Cooldown";
    private static final String TAG_Q3_COOLDOWN = "JungleApeQ3Cooldown";
    private static final String TAG_R_COOLDOWN = "JungleApeRCooldown";
    private static final String TAG_R_END_TIME = "JungleApeREndTime";
    private static final String TAG_Q2_BONUS_ATTACK_END = "JungleApeQ2BonusAttackEnd";
    private static final String TAG_R_HEALTH_BONUS = "JungleApeRHealthBonus";

    private static final UUID RHYTHM_ATTACK_SPEED_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final UUID RHYTHM_MOVE_SPEED_UUID = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345678901");
    private static final UUID R_HEALTH_UUID = UUID.fromString("c3d4e5f6-a7b8-9012-cdef-123456789012");

    private static final Random RANDOM = new Random();

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        net.minecraft.server.MinecraftServer server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        int tickCount = server.getTickCount();

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            tickJungleApeGod(player, tickCount);
        }
    }

    private static void tickJungleApeGod(ServerPlayer player, int tickCount) {
        String professionId = ContractEvents.getEffectiveProfessionId(player);
        if (professionId == null) return;

        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null || !profession.isJungleApeGod()) return;

        tickBerserkState(player, tickCount, profession);
        tickCooldowns(player);
        tickRState(player, tickCount, profession);
        applyRhythmBonuses(player, profession);
        
        if (tickCount % 5 == 0) {
            syncClientState(player);
        }
    }

    private static void tickBerserkState(ServerPlayer player, int tickCount, Profession profession) {
        CompoundTag data = player.getPersistentData();
        long berserkEndTime = data.getLong(TAG_BERSERK_END_TIME);

        if (berserkEndTime > 0 && tickCount >= berserkEndTime) {
            data.putLong(TAG_BERSERK_END_TIME, 0);
            data.putInt(TAG_RHYTHM_STACKS, 0);
            player.sendSystemMessage(Component.literal("§c[丛林猿神] 暴走状态结束！"));
            if (player.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.SMOKE, player.getX(), player.getY() + 1, player.getZ(), 30, 0.5, 0.5, 0.5, 0.05);
            }
        }
    }

    private static void tickCooldowns(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        
        int q1Cooldown = data.getInt(TAG_Q1_COOLDOWN);
        if (q1Cooldown > 0) data.putInt(TAG_Q1_COOLDOWN, q1Cooldown - 1);
        
        int q2Cooldown = data.getInt(TAG_Q2_COOLDOWN);
        if (q2Cooldown > 0) data.putInt(TAG_Q2_COOLDOWN, q2Cooldown - 1);
        
        int q3Cooldown = data.getInt(TAG_Q3_COOLDOWN);
        if (q3Cooldown > 0) data.putInt(TAG_Q3_COOLDOWN, q3Cooldown - 1);
        
        int rCooldown = data.getInt(TAG_R_COOLDOWN);
        if (rCooldown > 0) data.putInt(TAG_R_COOLDOWN, rCooldown - 1);
    }

    private static void tickRState(ServerPlayer player, int tickCount, Profession profession) {
        CompoundTag data = player.getPersistentData();
        long rEndTime = data.getLong(TAG_R_END_TIME);

        if (rEndTime > 0 && tickCount >= rEndTime) {
            data.putLong(TAG_R_END_TIME, 0);
            endRUltimate(player, profession);
        }
    }

    private static void applyRhythmBonuses(ServerPlayer player, Profession profession) {
        CompoundTag data = player.getPersistentData();
        int stacks = data.getInt(TAG_RHYTHM_STACKS);
        
        float attackSpeedBonus = stacks * profession.getRhythmAttackSpeedPerStack();
        float moveSpeedBonus = stacks * profession.getRhythmMoveSpeedPerStack();

        var attackSpeedAttr = player.getAttribute(Attributes.ATTACK_SPEED);
        if (attackSpeedAttr != null) {
            attackSpeedAttr.removeModifier(RHYTHM_ATTACK_SPEED_UUID);
            if (attackSpeedBonus > 0) {
                attackSpeedAttr.addPermanentModifier(new AttributeModifier(
                    RHYTHM_ATTACK_SPEED_UUID, "jungle_ape_rhythm_attack_speed", 
                    attackSpeedBonus, AttributeModifier.Operation.MULTIPLY_TOTAL
                ));
            }
        }

        var moveSpeedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (moveSpeedAttr != null) {
            moveSpeedAttr.removeModifier(RHYTHM_MOVE_SPEED_UUID);
            if (moveSpeedBonus > 0) {
                moveSpeedAttr.addPermanentModifier(new AttributeModifier(
                    RHYTHM_MOVE_SPEED_UUID, "jungle_ape_rhythm_move_speed", 
                    moveSpeedBonus, AttributeModifier.Operation.MULTIPLY_TOTAL
                ));
            }
        }
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        
        String professionId = ContractEvents.getEffectiveProfessionId(player);
        if (professionId == null) return;

        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null || !profession.isJungleApeGod()) return;

        addRhythmStack(player, profession);
        
        if (isInBerserkState(player)) {
            applyBerserkLifeSteal(player, event.getAmount(), profession);
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        
        String professionId = ContractEvents.getEffectiveProfessionId(player);
        if (professionId == null) return;

        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null || !profession.isJungleApeGod()) return;

        float reduction = profession.getFlatDamageReduction();
        float newDamage = Math.max(0, event.getAmount() - reduction);
        event.setAmount(newDamage);

        if (RANDOM.nextFloat() < profession.getResistanceChance()) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, profession.getResistanceDuration(), 1, false, true));
        }
    }

    private static void addRhythmStack(ServerPlayer player, Profession profession) {
        CompoundTag data = player.getPersistentData();
        int currentStacks = data.getInt(TAG_RHYTHM_STACKS);
        int maxStacks = profession.getRhythmStacksMax();

        if (currentStacks < maxStacks) {
            data.putInt(TAG_RHYTHM_STACKS, currentStacks + 1);
            
            if (currentStacks + 1 >= maxStacks) {
                triggerBerserk(player, profession);
            }
        }
    }

    private static void triggerBerserk(ServerPlayer player, Profession profession) {
        CompoundTag data = player.getPersistentData();
        int duration = profession.getBerserkDuration();
        
        data.putLong(TAG_BERSERK_END_TIME, player.getServer().getTickCount() + duration);
        
        player.sendSystemMessage(Component.literal("§4[丛林猿神] 暴走状态触发！"));
        
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.ANGRY_VILLAGER, player.getX(), player.getY() + 1.5, player.getZ(), 20, 0.5, 0.3, 0.5, 0.1);
            serverLevel.sendParticles(ParticleTypes.CRIMSON_SPORE, player.getX(), player.getY() + 1, player.getZ(), 30, 0.5, 0.5, 0.5, 0.05);
        }
        
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), 
            SoundEvents.RAVAGER_ROAR, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    private static void applyBerserkLifeSteal(ServerPlayer player, float damage, Profession profession) {
        float healAmount = damage * profession.getBerserkLifeSteal();
        player.heal(healAmount);
    }

    public static boolean isInBerserkState(ServerPlayer player) {
        return player.getPersistentData().getLong(TAG_BERSERK_END_TIME) > player.getServer().getTickCount();
    }

    public static boolean isInRState(ServerPlayer player) {
        return player.getPersistentData().getLong(TAG_R_END_TIME) > player.getServer().getTickCount();
    }

    public static void useQ1(ServerPlayer player) {
        String professionId = ContractEvents.getEffectiveProfessionId(player);
        if (professionId == null) return;

        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null || !profession.isJungleApeGod()) return;

        CompoundTag data = player.getPersistentData();
        int cooldown = data.getInt(TAG_Q1_COOLDOWN);
        
        if (isInBerserkState(player)) {
            cooldown = (int)(cooldown * (1 - profession.getBerserkCooldownReduction()));
        }

        if (cooldown > 0) {
            player.sendSystemMessage(Component.literal("§c[丛林猿神] 崩地重击冷却中！"));
            return;
        }

        float angle = profession.getQ1Angle();
        float range = 4.0f;
        float damageMultiplier = profession.getQ1DamageMultiplier();
        float movingDamageMultiplier = profession.getQ1MovingTargetDamageMultiplier();
        int slowDuration = profession.getQ1SlowDuration();

        Vec3 lookVec = player.getLookAngle();
        List<LivingEntity> targets = player.level().getEntitiesOfClass(
            LivingEntity.class, 
            player.getBoundingBox().inflate(range),
            e -> e != player && e.isAlive() && isInFront(player, e, lookVec, angle)
        );

        for (LivingEntity target : targets) {
            float damage = (float)player.getAttributeValue(Attributes.ATTACK_DAMAGE) * damageMultiplier;
            
            if (isTargetMoving(target)) {
                damage = (float)player.getAttributeValue(Attributes.ATTACK_DAMAGE) * movingDamageMultiplier;
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, slowDuration, 2, false, true));
            }
            
            target.hurt(player.level().damageSources().playerAttack(player), damage);
        }

        if (player.level() instanceof ServerLevel serverLevel) {
            Vec3 forward = player.getLookAngle().multiply(1, 0, 1).normalize();
            for (int i = 0; i < 20; i++) {
                double angleRad = (i - 10) * (angle / 2) * Math.PI / 180;
                double x = player.getX() + forward.x * Math.cos(angleRad) * range * 0.5;
                double z = player.getZ() + forward.z * Math.cos(angleRad) * range * 0.5;
                serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, x, player.getY() + 1, z, 1, 0, 0, 0, 0);
            }
        }

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), 
            SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0f, 0.8f);

        data.putInt(TAG_Q1_COOLDOWN, profession.getQ1Cooldown());
        player.sendSystemMessage(Component.literal("§6[丛林猿神] 崩地重击！"));
    }

    private static boolean isInFront(Player player, LivingEntity target, Vec3 lookVec, float angle) {
        Vec3 toTarget = target.position().subtract(player.position()).normalize();
        double dot = lookVec.dot(toTarget);
        double angleRad = Math.acos(Math.min(1, Math.max(-1, dot)));
        return Math.toDegrees(angleRad) <= angle / 2;
    }

    private static boolean isTargetMoving(LivingEntity target) {
        Vec3 motion = target.getDeltaMovement();
        return motion.horizontalDistance() > 0.05;
    }

    public static void useQ2(ServerPlayer player, Entity target) {
        String professionId = ContractEvents.getEffectiveProfessionId(player);
        if (professionId == null) return;

        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null || !profession.isJungleApeGod()) return;

        CompoundTag data = player.getPersistentData();
        int cooldown = data.getInt(TAG_Q2_COOLDOWN);
        
        if (isInBerserkState(player)) {
            cooldown = (int)(cooldown * (1 - profession.getBerserkCooldownReduction()));
        }

        if (cooldown > 0) {
            player.sendSystemMessage(Component.literal("§c[丛林猿神] 掠影突袭冷却中！"));
            return;
        }

        float maxDistance = profession.getQ2MaxDistance();
        float damageMultiplier = profession.getQ2DamageMultiplier();
        float knockbackDuration = profession.getQ2KnockbackDuration();
        float splashPercent = profession.getQ2SplashDamagePercent();

        Vec3 targetPos;
        Entity targetEntity = null;

        if (target != null && player.distanceTo(target) <= maxDistance) {
            targetEntity = target;
            targetPos = target.position();
        } else {
            Vec3 lookVec = player.getLookAngle();
            targetPos = player.position().add(lookVec.scale(maxDistance));
        }

        Vec3 direction = targetPos.subtract(player.position()).normalize();
        Vec3 teleportPos = targetPos.subtract(direction.scale(1.5));

        List<LivingEntity> pathEntities = player.level().getEntitiesOfClass(
            LivingEntity.class,
            player.getBoundingBox().minmax(new AABB(teleportPos, teleportPos)).inflate(2),
            e -> e != player && e.isAlive()
        );

        for (LivingEntity pathEntity : pathEntities) {
            float splashDamage = (float)player.getAttributeValue(Attributes.ATTACK_DAMAGE) * damageMultiplier * splashPercent;
            pathEntity.hurt(player.level().damageSources().playerAttack(player), splashDamage);
        }

        player.teleportTo(teleportPos.x, teleportPos.y, teleportPos.z);

        if (targetEntity instanceof LivingEntity livingTarget) {
            float damage = (float)player.getAttributeValue(Attributes.ATTACK_DAMAGE) * damageMultiplier;
            livingTarget.hurt(player.level().damageSources().playerAttack(player), damage);
            
            Vec3 knockback = livingTarget.position().subtract(player.position()).normalize().scale(0.5);
            livingTarget.setDeltaMovement(knockback.x, 0.3, knockback.z);
            livingTarget.hurtMarked = true;
        }

        data.putLong(TAG_Q2_BONUS_ATTACK_END, player.getServer().getTickCount() + profession.getQ2BonusAttackDuration());

        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL, player.getX(), player.getY() + 1, player.getZ(), 30, 0.5, 0.5, 0.5, 0.1);
        }

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), 
            SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.2f);

        data.putInt(TAG_Q2_COOLDOWN, profession.getQ2Cooldown());
        player.sendSystemMessage(Component.literal("§b[丛林猿神] 掠影突袭！"));
    }

    public static boolean hasQ2BonusAttack(ServerPlayer player) {
        return player.getPersistentData().getLong(TAG_Q2_BONUS_ATTACK_END) > player.getServer().getTickCount();
    }

    public static void useQ3(ServerPlayer player) {
        String professionId = ContractEvents.getEffectiveProfessionId(player);
        if (professionId == null) return;

        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null || !profession.isJungleApeGod()) return;

        CompoundTag data = player.getPersistentData();
        int cooldown = data.getInt(TAG_Q3_COOLDOWN);
        
        if (isInBerserkState(player)) {
            cooldown = (int)(cooldown * (1 - profession.getBerserkCooldownReduction()));
        }

        if (cooldown > 0) {
            player.sendSystemMessage(Component.literal("§c[丛林猿神] 野性咆哮冷却中！"));
            return;
        }

        float radius = profession.getQ3Radius();
        float damageMultiplier = profession.getQ3DamageMultiplier();
        float fearDuration = isInBerserkState(player) ? profession.getQ3BerserkFearDuration() : profession.getQ3FearDuration();
        int weaknessDuration = profession.getQ3WeaknessDuration();

        List<LivingEntity> targets = player.level().getEntitiesOfClass(
            LivingEntity.class,
            player.getBoundingBox().inflate(radius),
            e -> e != player && e.isAlive()
        );

        for (LivingEntity target : targets) {
            float damage = (float)player.getAttributeValue(Attributes.ATTACK_DAMAGE) * damageMultiplier;
            target.hurt(player.level().damageSources().playerAttack(player), damage);
            
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, weaknessDuration, 1, false, true));
            
            applyFearEffect(player, target, (int)(fearDuration * 20));
        }

        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, player.getX(), player.getY() + 1, player.getZ(), 1, 0, 0, 0, 0);
            serverLevel.sendParticles(ParticleTypes.CRIMSON_SPORE, player.getX(), player.getY() + 1, player.getZ(), 50, radius, 1, radius, 0.1);
        }

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), 
            SoundEvents.RAVAGER_ROAR, SoundSource.PLAYERS, 2.0f, 0.8f);

        data.putInt(TAG_Q3_COOLDOWN, profession.getQ3Cooldown());
        player.sendSystemMessage(Component.literal("§c[丛林猿神] 野性咆哮！"));
    }

    private static void applyFearEffect(Player source, LivingEntity target, int duration) {
        Vec3 fleeDirection = target.position().subtract(source.position()).normalize();
        target.setDeltaMovement(fleeDirection.x * 0.5, 0.2, fleeDirection.z * 0.5);
        target.hurtMarked = true;
        
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 0, false, true));
    }

    public static void useR(ServerPlayer player) {
        String professionId = ContractEvents.getEffectiveProfessionId(player);
        if (professionId == null) return;

        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null || !profession.isJungleApeGod()) return;

        CompoundTag data = player.getPersistentData();
        int cooldown = data.getInt(TAG_R_COOLDOWN);

        if (cooldown > 0) {
            player.sendSystemMessage(Component.literal("§c[丛林猿神] 猿神·混沌降临冷却中！"));
            return;
        }

        int duration = profession.getRDuration();
        float healPercent = profession.getRHealPercent();
        float healthBonusPercent = profession.getRHealthBonusPercent();
        int powerLevel = profession.getRPowerLevel();
        int speedLevel = profession.getRSpeedLevel();

        float healAmount = player.getMaxHealth() * healPercent;
        player.heal(healAmount);

        float healthBonus = player.getMaxHealth() * healthBonusPercent;
        var healthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr != null) {
            healthAttr.addPermanentModifier(new AttributeModifier(
                R_HEALTH_UUID, "jungle_ape_r_health", 
                healthBonus, AttributeModifier.Operation.ADDITION
            ));
            player.setHealth(player.getHealth() + healthBonus);
        }
        data.putFloat(TAG_R_HEALTH_BONUS, healthBonus);

        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, duration, powerLevel - 1, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, speedLevel - 1, false, true));

        data.putLong(TAG_R_END_TIME, player.getServer().getTickCount() + duration);

        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, player.getX(), player.getY() + 1, player.getZ(), 3, 1, 1, 1, 0);
            serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1, player.getZ(), 100, 2, 2, 2, 0.2);
        }

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), 
            SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 1.0f, 0.8f);

        data.putInt(TAG_R_COOLDOWN, profession.getRCooldown());
        player.sendSystemMessage(Component.literal("§4[丛林猿神] 猿神·混沌降临！"));
    }

    private static void endRUltimate(ServerPlayer player, Profession profession) {
        CompoundTag data = player.getPersistentData();
        
        var healthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr != null) {
            healthAttr.removeModifier(R_HEALTH_UUID);
            
            float healthBonus = data.getFloat(TAG_R_HEALTH_BONUS);
            float currentHealth = player.getHealth();
            float newHealth = Math.max(1, currentHealth - healthBonus);
            player.setHealth(newHealth);
        }

        int fatigueDuration = profession.getRFatigueDuration();
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, fatigueDuration, 1, false, true));

        player.sendSystemMessage(Component.literal("§7[丛林猿神] 混沌降临结束，进入疲惫状态..."));
        
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SMOKE, player.getX(), player.getY() + 1, player.getZ(), 30, 0.5, 0.5, 0.5, 0.05);
        }
    }

    public static int getRhythmStacks(ServerPlayer player) {
        return player.getPersistentData().getInt(TAG_RHYTHM_STACKS);
    }

    public static int getQ1Cooldown(ServerPlayer player) {
        return player.getPersistentData().getInt(TAG_Q1_COOLDOWN);
    }

    public static int getQ2Cooldown(ServerPlayer player) {
        return player.getPersistentData().getInt(TAG_Q2_COOLDOWN);
    }

    public static int getQ3Cooldown(ServerPlayer player) {
        return player.getPersistentData().getInt(TAG_Q3_COOLDOWN);
    }

    public static int getRCooldown(ServerPlayer player) {
        return player.getPersistentData().getInt(TAG_R_COOLDOWN);
    }

    public static void onPlayerRespawn(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        data.putInt(TAG_RHYTHM_STACKS, 0);
        data.putLong(TAG_BERSERK_END_TIME, 0);
        data.putInt(TAG_Q1_COOLDOWN, 0);
        data.putInt(TAG_Q2_COOLDOWN, 0);
        data.putInt(TAG_Q3_COOLDOWN, 0);
        data.putInt(TAG_R_COOLDOWN, 0);
        data.putLong(TAG_R_END_TIME, 0);
        data.putLong(TAG_Q2_BONUS_ATTACK_END, 0);
        data.putFloat(TAG_R_HEALTH_BONUS, 0);
    }

    private static void syncClientState(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        int rhythmStacks = data.getInt(TAG_RHYTHM_STACKS);
        boolean isBerserk = isInBerserkState(player);
        boolean isRActive = isInRState(player);
        int q1Cooldown = data.getInt(TAG_Q1_COOLDOWN);
        int q2Cooldown = data.getInt(TAG_Q2_COOLDOWN);
        int q3Cooldown = data.getInt(TAG_Q3_COOLDOWN);
        int rCooldown = data.getInt(TAG_R_COOLDOWN);

        NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), 
            new PacketSyncJungleApeState(rhythmStacks, isBerserk, isRActive, q1Cooldown, q2Cooldown, q3Cooldown, rCooldown));
    }
}
