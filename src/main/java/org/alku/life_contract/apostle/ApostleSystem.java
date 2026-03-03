package org.alku.life_contract.apostle;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.CaveSpider;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import org.alku.life_contract.Life_contract;
import org.alku.life_contract.NetworkHandler;
import org.alku.life_contract.ContractEvents;
import org.alku.life_contract.profession.Profession;
import org.alku.life_contract.profession.ProfessionConfig;

import java.util.*;

@Mod.EventBusSubscriber(modid = Life_contract.MODID)
public class ApostleSystem {

    private static final String TAG_TELEPORT_COOLDOWN = "ApostleTeleportCooldown";
    private static final String TAG_FIREBALL_COOLDOWN = "ApostleFireballCooldown";
    private static final String TAG_DEBUFF_END_TIME = "ApostleDebuffEndTime";
    private static final String TAG_HEALING_REDUCTION_END = "ApostleHealingReductionEnd";
    private static final String TAG_MARKED_TARGET = "ApostleMarkedTarget";

    private static final UUID APOSTLE_MELEE_DAMAGE_UUID = UUID.fromString("d4e5f6a7-b8c9-0123-defa-456789012345");
    
    private static final Set<EntityType<?>> UNDEAD_TYPES = new HashSet<>(Arrays.asList(
        EntityType.ZOMBIE, EntityType.ZOMBIE_VILLAGER, EntityType.HUSK, EntityType.DROWNED,
        EntityType.SKELETON, EntityType.STRAY, EntityType.WITHER_SKELETON,
        EntityType.WITHER, EntityType.ZOMBIFIED_PIGLIN, EntityType.ZOGLIN,
        EntityType.PHANTOM, EntityType.SKELETON_HORSE, EntityType.ZOMBIE_HORSE
    ));

    private static final Random RANDOM = new Random();

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        net.minecraft.server.MinecraftServer server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        int tickCount = server.getTickCount();

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            tickApostle(player, tickCount);
        }
    }

    private static void tickApostle(ServerPlayer player, int tickCount) {
        String professionId = ContractEvents.getEffectiveProfessionId(player);
        if (professionId == null) return;

        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null || !profession.isApostle()) return;

        tickCooldowns(player);
        applyMeleeDamagePenalty(player, profession);
        
        if (tickCount % 5 == 0) {
            syncClientState(player);
        }
    }

    private static void tickCooldowns(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        
        boolean inFire = isInFire(player);
        data.putBoolean("ApostleInFire", inFire);
        
        int reduction = inFire ? 2 : 1;
        
        int teleportCooldown = data.getInt(TAG_TELEPORT_COOLDOWN);
        if (teleportCooldown > 0) data.putInt(TAG_TELEPORT_COOLDOWN, Math.max(0, teleportCooldown - reduction));
        
        int fireballCooldown = data.getInt(TAG_FIREBALL_COOLDOWN);
        if (fireballCooldown > 0) data.putInt(TAG_FIREBALL_COOLDOWN, Math.max(0, fireballCooldown - reduction));
    }
    
    private static boolean isInFire(ServerPlayer player) {
        return player.isOnFire() || 
               player.level().getBlockState(player.blockPosition()).is(net.minecraft.tags.BlockTags.FIRE) ||
               player.level().getBlockState(player.blockPosition()).is(net.minecraft.world.level.block.Blocks.LAVA) ||
               player.isInLava();
    }

    private static void applyMeleeDamagePenalty(ServerPlayer player, Profession profession) {
        var attackDamageAttr = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamageAttr != null) {
            attackDamageAttr.removeModifier(APOSTLE_MELEE_DAMAGE_UUID);
            float penalty = profession.getApostleMeleeDamagePercent() - 1.0f;
            if (penalty < 0) {
                attackDamageAttr.addPermanentModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                    APOSTLE_MELEE_DAMAGE_UUID, "apostle_melee_penalty",
                    penalty, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.MULTIPLY_TOTAL
                ));
            }
        }
    }

    public static void useTeleport(ServerPlayer player) {
        String professionId = ContractEvents.getEffectiveProfessionId(player);
        if (professionId == null) return;

        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null || !profession.isApostle()) return;

        CompoundTag data = player.getPersistentData();
        int cooldown = data.getInt(TAG_TELEPORT_COOLDOWN);
        
        if (cooldown > 0) {
            player.sendSystemMessage(Component.literal("§c[使徒] §f瞬移冷却中，还需等待 §e" + (cooldown / 20) + " §f秒！"));
            return;
        }

        float distance = profession.getApostleTeleportDistance();
        Vec3 lookVec = player.getLookAngle();
        Vec3 targetPos = player.position().add(lookVec.scale(distance));
        
        BlockPos targetBlockPos = new BlockPos((int)targetPos.x, (int)targetPos.y, (int)targetPos.z);
        
        for (int i = 0; i < 10; i++) {
            if (player.level().getBlockState(targetBlockPos).isAir() && 
                player.level().getBlockState(targetBlockPos.above()).isAir()) {
                break;
            }
            targetBlockPos = targetBlockPos.above();
        }
        
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 1, player.getZ(), 50, 0.5, 0.5, 0.5, 0.1);
            
            player.teleportTo(targetBlockPos.getX() + 0.5, targetBlockPos.getY(), targetBlockPos.getZ() + 0.5);
            
            serverLevel.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 1, player.getZ(), 50, 0.5, 0.5, 0.5, 0.1);
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.0f);
        }
        
        data.putInt(TAG_TELEPORT_COOLDOWN, profession.getApostleTeleportCooldown());
        syncClientState(player);
        player.sendSystemMessage(Component.literal("§6[使徒] §f瞬移成功！"));
    }

    public static void useFireball(ServerPlayer player) {
        String professionId = ContractEvents.getEffectiveProfessionId(player);
        if (professionId == null) return;

        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null || !profession.isApostle()) return;

        CompoundTag data = player.getPersistentData();
        int cooldown = data.getInt(TAG_FIREBALL_COOLDOWN);
        
        if (cooldown > 0) {
            player.sendSystemMessage(Component.literal("§c[使徒] §f火球冷却中，还需等待 §e" + (cooldown / 20) + " §f秒！"));
            return;
        }

        LivingEntity target = findTargetInSight(player, 32.0);
        
        if (target == null) {
            player.sendSystemMessage(Component.literal("§c[使徒] §f未找到目标！"));
            return;
        }

        Vec3 eyePos = player.getEyePosition();
        Vec3 targetPos = target.getEyePosition();
        Vec3 direction = targetPos.subtract(eyePos).normalize();
        
        LargeFireball fireball = new LargeFireball(player.level(), player, 
            direction.x, direction.y, direction.z, 1);
        fireball.setPos(eyePos.x + direction.x, eyePos.y + direction.y, eyePos.z + direction.z);
        fireball.setOwner(player);
        player.level().addFreshEntity(fireball);
        
        applyApostleDebuff(target, profession);
        
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), 
                SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.0f, 1.0f);
        }
        
        data.putInt(TAG_FIREBALL_COOLDOWN, profession.getApostleFireballCooldown());
        syncClientState(player);
        player.sendSystemMessage(Component.literal("§6[使徒] §f释放火球！目标获得易伤和治疗效果减半！"));
    }

    private static LivingEntity findTargetInSight(ServerPlayer player, double range) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle();
        
        List<LivingEntity> entities = player.level().getEntitiesOfClass(
            LivingEntity.class,
            player.getBoundingBox().inflate(range),
            e -> e != player && e.isAlive() && !(e instanceof Player)
        );
        
        LivingEntity bestTarget = null;
        double bestAngle = Double.MAX_VALUE;
        
        for (LivingEntity entity : entities) {
            Vec3 toEntity = entity.getEyePosition().subtract(eyePos).normalize();
            double angle = Math.acos(lookVec.dot(toEntity));
            
            if (angle < Math.toRadians(30) && angle < bestAngle) {
                bestAngle = angle;
                bestTarget = entity;
            }
        }
        
        return bestTarget;
    }

    public static void applyApostleDebuff(LivingEntity target, Profession profession) {
        CompoundTag data = target.getPersistentData();
        long endTime = target.level().getGameTime() + profession.getApostleDebuffDuration();
        data.putLong(TAG_DEBUFF_END_TIME, endTime);
        data.putLong(TAG_HEALING_REDUCTION_END, endTime);
    }

    public static boolean hasApostleDebuff(LivingEntity entity) {
        CompoundTag data = entity.getPersistentData();
        long endTime = data.getLong(TAG_DEBUFF_END_TIME);
        return endTime > entity.level().getGameTime();
    }

    public static boolean hasHealingReduction(LivingEntity entity) {
        CompoundTag data = entity.getPersistentData();
        long endTime = data.getLong(TAG_HEALING_REDUCTION_END);
        return endTime > entity.level().getGameTime();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingDamage(LivingDamageEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        
        if (event.getEntity() instanceof ServerPlayer player) {
            String professionId = ContractEvents.getEffectiveProfessionId(player);
            if (professionId == null) return;
            
            Profession profession = ProfessionConfig.getProfession(professionId);
            if (profession == null || !profession.isApostle()) return;
            
            DamageSource source = event.getSource();
            
            if (source.is(DamageTypes.ON_FIRE) || source.is(DamageTypes.IN_FIRE) || 
                source.is(DamageTypes.LAVA) || source.is(DamageTypes.HOT_FLOOR)) {
                event.setCanceled(true);
                return;
            }
            
            if (isInNether(player)) {
                float reduction = profession.getApostleNetherDamageReduction();
                event.setAmount(event.getAmount() * (1.0f - reduction));
            }
        }
        
        if (hasApostleDebuff(event.getEntity())) {
            String professionId = null;
            Profession profession = null;
            
            if (event.getSource().getEntity() instanceof ServerPlayer attacker) {
                professionId = ContractEvents.getEffectiveProfessionId(attacker);
                if (professionId != null) {
                    profession = ProfessionConfig.getProfession(professionId);
                }
            }
            
            if (profession != null && profession.isApostle()) {
                float increase = profession.getApostleDebuffDamageIncrease();
                event.setAmount(event.getAmount() * (1.0f + increase));
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        
        if (event.getEntity() instanceof ServerPlayer player) {
            String professionId = ContractEvents.getEffectiveProfessionId(player);
            if (professionId == null) return;
            
            Profession profession = ProfessionConfig.getProfession(professionId);
            if (profession == null || !profession.isApostle()) return;
            
            DamageSource source = event.getSource();
            
            if (source.is(DamageTypes.ON_FIRE) || source.is(DamageTypes.IN_FIRE) || 
                source.is(DamageTypes.LAVA) || source.is(DamageTypes.HOT_FLOOR)) {
                event.setCanceled(true);
                return;
            }
            
            if (isInNether(player)) {
                float reduction = profession.getApostleNetherDamageReduction();
                event.setAmount(event.getAmount() * (1.0f - reduction));
            }
        }
    }

    @SubscribeEvent
    public static void onMobEffectApply(MobEffectEvent.Applicable event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            String professionId = ContractEvents.getEffectiveProfessionId(player);
            if (professionId == null) return;
            
            Profession profession = ProfessionConfig.getProfession(professionId);
            if (profession == null || !profession.isApostle()) return;
            
            MobEffectInstance effectInstance = event.getEffectInstance();
            if (effectInstance != null) {
                event.setResult(Event.Result.DENY);
            }
        }
    }

    @SubscribeEvent
    public static void onMobTarget(LivingChangeTargetEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        
        if (!(event.getEntity() instanceof Mob mob)) return;
        
        if (!(event.getNewTarget() instanceof ServerPlayer player)) return;
        
        String professionId = ContractEvents.getEffectiveProfessionId(player);
        if (professionId == null) return;
        
        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null || !profession.isApostle()) return;
        
        if (isUndead(mob)) {
            event.setNewTarget(null);
        }
    }

    @SubscribeEvent
    public static void onMobAttack(LivingAttackEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        
        String professionId = ContractEvents.getEffectiveProfessionId(player);
        if (professionId == null) return;
        
        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null || !profession.isApostle()) return;
        
        Entity attacker = event.getSource().getEntity();
        if (attacker instanceof Mob mob && isUndead(mob)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onArrowImpact(ProjectileImpactEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        
        if (!(event.getEntity() instanceof AbstractArrow arrow)) return;
        
        if (!(arrow.getOwner() instanceof ServerPlayer player)) return;
        
        String professionId = ContractEvents.getEffectiveProfessionId(player);
        if (professionId == null) return;
        
        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null || !profession.isApostle()) return;
        
        arrow.setSecondsOnFire(10);
        
        Level level = player.level();
        if (level instanceof ServerLevel serverLevel) {
            Vec3 hitPos;
            if (event.getRayTraceResult().getType() == HitResult.Type.ENTITY) {
                EntityHitResult entityHit = (EntityHitResult) event.getRayTraceResult();
                hitPos = entityHit.getEntity().position();
                
                if (entityHit.getEntity() instanceof LivingEntity livingTarget) {
                    applyApostleDebuff(livingTarget, profession);
                }
            } else if (event.getRayTraceResult().getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockHit = (BlockHitResult) event.getRayTraceResult();
                hitPos = Vec3.atCenterOf(blockHit.getBlockPos());
            } else {
                return;
            }
            
            int radius = profession.getApostleArrowFireRadius();
            float fireDuration = profession.getApostleArrowFireDuration();
            
            BlockPos centerPos = new BlockPos((int)hitPos.x, (int)hitPos.y, (int)hitPos.z);
            
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x * x + z * z <= radius * radius) {
                        BlockPos pos = centerPos.offset(x, 0, z);
                        if (level.getBlockState(pos).isAir() && !level.getBlockState(pos.below()).isAir()) {
                            level.setBlock(pos, net.minecraft.world.level.block.Blocks.FIRE.defaultBlockState(), 3);
                        }
                    }
                }
            }
            
            serverLevel.sendParticles(ParticleTypes.FLAME, hitPos.x, hitPos.y + 0.5, hitPos.z, 30, 1.0, 0.5, 1.0, 0.05);
        }
    }

    private static boolean isUndead(Mob mob) {
        return UNDEAD_TYPES.contains(mob.getType());
    }

    private static boolean isInNether(ServerPlayer player) {
        return player.level().dimension() == Level.NETHER;
    }

    private static void syncClientState(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        NetworkHandler.CHANNEL.send(
            net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player),
            new PacketSyncApostleState(
                data.getInt(TAG_TELEPORT_COOLDOWN),
                data.getInt(TAG_FIREBALL_COOLDOWN),
                data.getBoolean("ApostleInFire")
            )
        );
    }

    public static int getTeleportCooldown(ServerPlayer player) {
        return player.getPersistentData().getInt(TAG_TELEPORT_COOLDOWN);
    }

    public static int getFireballCooldown(ServerPlayer player) {
        return player.getPersistentData().getInt(TAG_FIREBALL_COOLDOWN);
    }

    public static void onPlayerJoin(ServerPlayer player) {
        syncClientState(player);
    }

    public static void onPlayerRespawn(ServerPlayer player) {
        syncClientState(player);
    }
}
