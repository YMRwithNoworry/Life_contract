package org.alku.life_contract.heavy_knight;

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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.ShieldBlockEvent;
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
public class HeavyKnightSystem {

    private static final String TAG_BATTLE_WILL = "HeavyKnightBattleWill";
    private static final String TAG_SHIELD_WALL_ACTIVE = "HeavyKnightShieldWall";
    private static final String TAG_SHIELD_WALL_START_TIME = "HeavyKnightShieldWallStart";
    private static final String TAG_CHARGE_COOLDOWN = "HeavyKnightChargeCooldown";
    private static final String TAG_PROTECT_COOLDOWN = "HeavyKnightProtectCooldown";
    private static final String TAG_SHIELD_BASH_COOLDOWN = "HeavyKnightShieldBashCooldown";
    private static final String TAG_LAST_COMBAT_TIME = "HeavyKnightLastCombat";
    private static final String TAG_IS_CHARGING = "HeavyKnightIsCharging";
    private static final String TAG_CHARGE_START_POS = "HeavyKnightChargeStartPos";

    private static final UUID SPEED_PENALTY_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567901");
    private static final UUID SHIELD_WALL_SPEED_UUID = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345679012");
    private static final UUID FULL_BATTLE_WILL_DAMAGE_UUID = UUID.fromString("c3d4e5f6-a7b8-9012-cdef-123456790123");
    private static final UUID FULL_BATTLE_WILL_REDUCTION_UUID = UUID.fromString("d4e5f6a7-b8c9-0123-defa-234567890234");

    private static final int BATTLE_WILL_MAX = 100;
    private static final int SHIELD_WALL_TRIGGER_TICKS = 40;
    private static final int COMBAT_EXPIRE_TICKS = 100;

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        net.minecraft.server.MinecraftServer server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        int tickCount = server.getTickCount();

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            tickHeavyKnight(player, tickCount);
        }
    }

    private static void tickHeavyKnight(ServerPlayer player, int tickCount) {
        String professionId = ContractEvents.getEffectiveProfessionId(player);
        if (professionId == null) return;

        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null || !profession.isHeavyKnight()) return;

        tickCooldowns(player);
        tickBattleWillDecay(player, tickCount);
        tickShieldWall(player, tickCount, profession);
        applyBattleWillBonuses(player, profession);
        applySpeedPenalty(player, profession);
        
        if (tickCount % 5 == 0) {
            syncClientState(player);
        }
    }

    private static void tickCooldowns(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        
        int chargeCooldown = data.getInt(TAG_CHARGE_COOLDOWN);
        if (chargeCooldown > 0) data.putInt(TAG_CHARGE_COOLDOWN, chargeCooldown - 1);
        
        int protectCooldown = data.getInt(TAG_PROTECT_COOLDOWN);
        if (protectCooldown > 0) data.putInt(TAG_PROTECT_COOLDOWN, protectCooldown - 1);
        
        int bashCooldown = data.getInt(TAG_SHIELD_BASH_COOLDOWN);
        if (bashCooldown > 0) data.putInt(TAG_SHIELD_BASH_COOLDOWN, bashCooldown - 1);
    }

    private static void tickBattleWillDecay(ServerPlayer player, int tickCount) {
        CompoundTag data = player.getPersistentData();
        int lastCombat = data.getInt(TAG_LAST_COMBAT_TIME);
        
        if (tickCount - lastCombat > COMBAT_EXPIRE_TICKS) {
            int currentWill = data.getInt(TAG_BATTLE_WILL);
            if (currentWill > 0) {
                int newWill = Math.max(0, currentWill - 4);
                data.putInt(TAG_BATTLE_WILL, newWill);
            }
        }
    }

    private static void tickShieldWall(ServerPlayer player, int tickCount, Profession profession) {
        CompoundTag data = player.getPersistentData();
        boolean isBlocking = player.isBlocking();
        boolean shieldWallActive = data.getBoolean(TAG_SHIELD_WALL_ACTIVE);

        if (isBlocking) {
            if (!shieldWallActive) {
                long startTime = data.getLong(TAG_SHIELD_WALL_START_TIME);
                if (startTime == 0) {
                    data.putLong(TAG_SHIELD_WALL_START_TIME, tickCount);
                } else if (tickCount - startTime >= SHIELD_WALL_TRIGGER_TICKS) {
                    activateShieldWall(player, profession);
                }
            }
        } else {
            if (shieldWallActive) {
                deactivateShieldWall(player, profession);
            }
            data.putLong(TAG_SHIELD_WALL_START_TIME, 0);
        }
    }

    private static void activateShieldWall(ServerPlayer player, Profession profession) {
        CompoundTag data = player.getPersistentData();
        data.putBoolean(TAG_SHIELD_WALL_ACTIVE, true);
        
        var moveSpeedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (moveSpeedAttr != null) {
            moveSpeedAttr.removeModifier(SHIELD_WALL_SPEED_UUID);
            moveSpeedAttr.addPermanentModifier(new AttributeModifier(
                SHIELD_WALL_SPEED_UUID, "heavy_knight_shield_wall_speed",
                -0.3, AttributeModifier.Operation.MULTIPLY_TOTAL
            ));
        }
        
        player.sendSystemMessage(Component.literal("§6[重甲骑士] 盾墙激活！"));
        
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.ENCHANTED_HIT, 
                player.getX(), player.getY() + 1, player.getZ(), 30, 1, 0.5, 1, 0.1);
        }
    }

    private static void deactivateShieldWall(ServerPlayer player, Profession profession) {
        CompoundTag data = player.getPersistentData();
        data.putBoolean(TAG_SHIELD_WALL_ACTIVE, false);
        
        var moveSpeedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (moveSpeedAttr != null) {
            moveSpeedAttr.removeModifier(SHIELD_WALL_SPEED_UUID);
        }
        
        player.sendSystemMessage(Component.literal("§7[重甲骑士] 盾墙结束"));
    }

    private static void applyBattleWillBonuses(ServerPlayer player, Profession profession) {
        CompoundTag data = player.getPersistentData();
        int battleWill = data.getInt(TAG_BATTLE_WILL);
        
        var damageAttr = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damageAttr != null) {
            damageAttr.removeModifier(FULL_BATTLE_WILL_DAMAGE_UUID);
            if (battleWill >= BATTLE_WILL_MAX) {
                damageAttr.addPermanentModifier(new AttributeModifier(
                    FULL_BATTLE_WILL_DAMAGE_UUID, "heavy_knight_full_will_damage",
                    0.1, AttributeModifier.Operation.MULTIPLY_TOTAL
                ));
            }
        }
    }

    private static void applySpeedPenalty(ServerPlayer player, Profession profession) {
        var moveSpeedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (moveSpeedAttr != null) {
            moveSpeedAttr.removeModifier(SPEED_PENALTY_UUID);
            
            float penalty = profession.getHeavyKnightSpeedPenalty();
            if (penalty > 0) {
                moveSpeedAttr.addPermanentModifier(new AttributeModifier(
                    SPEED_PENALTY_UUID, "heavy_knight_speed_penalty",
                    -penalty, AttributeModifier.Operation.MULTIPLY_TOTAL
                ));
            }
        }
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        
        if (event.getSource().getEntity() instanceof ServerPlayer player) {
            String professionId = ContractEvents.getEffectiveProfessionId(player);
            if (professionId == null) return;

            Profession profession = ProfessionConfig.getProfession(professionId);
            if (profession == null || !profession.isHeavyKnight()) return;

            addBattleWill(player, profession.getHeavyKnightWillOnHit(), profession);
        }
        
        if (event.getEntity() instanceof ServerPlayer player) {
            String professionId = ContractEvents.getEffectiveProfessionId(player);
            if (professionId == null) return;

            Profession profession = ProfessionConfig.getProfession(professionId);
            if (profession == null || !profession.isHeavyKnight()) return;

            tryTriggerProtect(player, event, profession);
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        
        if (event.getEntity() instanceof ServerPlayer player) {
            String professionId = ContractEvents.getEffectiveProfessionId(player);
            if (professionId == null) return;

            Profession profession = ProfessionConfig.getProfession(professionId);
            if (profession == null || !profession.isHeavyKnight()) return;

            CompoundTag data = player.getPersistentData();
            int battleWill = data.getInt(TAG_BATTLE_WILL);
            
            if (battleWill >= BATTLE_WILL_MAX) {
                float reduction = profession.getHeavyKnightFullWillDamageReduction();
                float newDamage = event.getAmount() * (1.0f - reduction);
                event.setAmount(newDamage);
            }
            
            if (data.getBoolean(TAG_SHIELD_WALL_ACTIVE)) {
                float reduction = profession.getHeavyKnightShieldWallReduction();
                float newDamage = event.getAmount() * (1.0f - reduction);
                event.setAmount(newDamage);
            }
        }
    }

    @SubscribeEvent
    public static void onShieldBlock(ShieldBlockEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        
        String professionId = ContractEvents.getEffectiveProfessionId(player);
        if (professionId == null) return;

        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null || !profession.isHeavyKnight()) return;

        addBattleWill(player, profession.getHeavyKnightWillOnBlock(), profession);
    }

    private static void addBattleWill(ServerPlayer player, int amount, Profession profession) {
        CompoundTag data = player.getPersistentData();
        int currentWill = data.getInt(TAG_BATTLE_WILL);
        int newWill = Math.min(BATTLE_WILL_MAX, currentWill + amount);
        data.putInt(TAG_BATTLE_WILL, newWill);
        data.putInt(TAG_LAST_COMBAT_TIME, player.getServer().getTickCount());
        
        if (currentWill < BATTLE_WILL_MAX && newWill >= BATTLE_WILL_MAX) {
            player.sendSystemMessage(Component.literal("§c[重甲骑士] 战意已满！伤害+10%，伤害减免+10%"));
            if (player.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.DAMAGE_INDICATOR,
                    player.getX(), player.getY() + 1, player.getZ(), 20, 0.5, 0.5, 0.5, 0.1);
            }
        }
    }

    private static void tryTriggerProtect(ServerPlayer protectedPlayer, LivingAttackEvent event, Profession profession) {
        CompoundTag data = protectedPlayer.getPersistentData();
        int cooldown = data.getInt(TAG_PROTECT_COOLDOWN);
        if (cooldown > 0) return;

        net.minecraft.server.MinecraftServer server = protectedPlayer.getServer();
        if (server == null) return;

        int protectRange = profession.getHeavyKnightProtectRange();
        int willCost = profession.getHeavyKnightProtectWillCost();

        for (ServerPlayer nearbyPlayer : server.getPlayerList().getPlayers()) {
            if (nearbyPlayer == protectedPlayer) continue;
            if (nearbyPlayer.distanceTo(protectedPlayer) > protectRange) continue;
            if (!ContractEvents.isSameTeam(protectedPlayer, nearbyPlayer)) continue;

            String nearbyProfessionId = ContractEvents.getEffectiveProfessionId(nearbyPlayer);
            if (nearbyProfessionId == null) continue;
            
            Profession nearbyProfession = ProfessionConfig.getProfession(nearbyProfessionId);
            if (nearbyProfession == null || !nearbyProfession.isHeavyKnight()) continue;

            CompoundTag nearbyData = nearbyPlayer.getPersistentData();
            int nearbyWill = nearbyData.getInt(TAG_BATTLE_WILL);
            int nearbyCooldown = nearbyData.getInt(TAG_PROTECT_COOLDOWN);
            
            if (nearbyWill >= willCost && nearbyCooldown <= 0) {
                executeProtect(nearbyPlayer, protectedPlayer, event, profession);
                return;
            }
        }
    }

    private static void executeProtect(ServerPlayer knight, ServerPlayer protectedPlayer, 
                                       LivingAttackEvent event, Profession profession) {
        CompoundTag knightData = knight.getPersistentData();
        int willCost = profession.getHeavyKnightProtectWillCost();
        
        knightData.putInt(TAG_BATTLE_WILL, knightData.getInt(TAG_BATTLE_WILL) - willCost);
        knightData.putInt(TAG_PROTECT_COOLDOWN, profession.getHeavyKnightProtectCooldown());
        
        knight.teleportTo(protectedPlayer.getX(), protectedPlayer.getY(), protectedPlayer.getZ());
        
        float damage = event.getAmount();
        protectedPlayer.setHealth(protectedPlayer.getHealth() + damage);
        knight.hurt(event.getSource(), damage);
        
        event.setCanceled(true);
        
        knight.sendSystemMessage(Component.literal("§b[重甲骑士] 援护！为队友承受伤害！"));
        protectedPlayer.sendSystemMessage(Component.literal("§b[重甲骑士] 你被骑士援护了！"));
        
        if (knight.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                protectedPlayer.getX(), protectedPlayer.getY() + 1, protectedPlayer.getZ(), 
                30, 0.5, 0.5, 0.5, 0.2);
        }
    }

    public static void useCharge(ServerPlayer player) {
        String professionId = ContractEvents.getEffectiveProfessionId(player);
        if (professionId == null) return;

        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null || !profession.isHeavyKnight()) return;

        CompoundTag data = player.getPersistentData();
        int cooldown = data.getInt(TAG_CHARGE_COOLDOWN);
        if (cooldown > 0) {
            player.sendSystemMessage(Component.literal("§c[重甲骑士] 冲锋冷却中！"));
            return;
        }

        int willCost = profession.getHeavyKnightChargeWillCost();
        int currentWill = data.getInt(TAG_BATTLE_WILL);
        if (currentWill < willCost) {
            player.sendSystemMessage(Component.literal("§c[重甲骑士] 战意不足！需要 " + willCost + " 点战意"));
            return;
        }

        data.putInt(TAG_BATTLE_WILL, currentWill - willCost);
        data.putInt(TAG_CHARGE_COOLDOWN, profession.getHeavyKnightChargeCooldown());
        
        Vec3 lookVec = player.getLookAngle();
        double chargeDistance = profession.getHeavyKnightChargeDistance();
        Vec3 startPos = player.position();
        Vec3 endPos = startPos.add(lookVec.x * chargeDistance, 0, lookVec.z * chargeDistance);
        
        player.setDeltaMovement(lookVec.x * 1.5, 0.1, lookVec.z * 1.5);
        player.hurtMarked = true;
        player.hasImpulse = true;
        
        data.putBoolean(TAG_IS_CHARGING, true);
        data.putDouble(TAG_CHARGE_START_POS + "_x", startPos.x);
        data.putDouble(TAG_CHARGE_START_POS + "_y", startPos.y);
        data.putDouble(TAG_CHARGE_START_POS + "_z", startPos.z);
        
        float chargeDamage = profession.getHeavyKnightChargeDamage();
        float knockback = profession.getHeavyKnightChargeKnockback();
        
        List<LivingEntity> targets = player.level().getEntitiesOfClass(
            LivingEntity.class,
            player.getBoundingBox().inflate(chargeDistance),
            e -> e != player && e.isAlive()
        );
        
        for (LivingEntity target : targets) {
            Vec3 toTarget = target.position().subtract(startPos).normalize();
            double dot = lookVec.dot(toTarget);
            if (dot > 0.7) {
                target.hurt(player.level().damageSources().playerAttack(player), chargeDamage);
                Vec3 knockbackVec = lookVec.scale(knockback);
                target.setDeltaMovement(knockbackVec.x, 0.3, knockbackVec.z);
                target.hurtMarked = true;
            }
        }
        
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20, 10, false, false));
        
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CLOUD,
                player.getX(), player.getY() + 0.5, player.getZ(), 30, 1, 0.3, 1, 0.1);
            serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK,
                player.getX(), player.getY() + 1, player.getZ(), 10, 1, 0.3, 1, 0.05);
        }
        
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.5f, 0.8f);
        
        player.sendSystemMessage(Component.literal("§e[重甲骑士] 冲锋！"));
    }

    public static void useShieldBash(ServerPlayer player) {
        String professionId = ContractEvents.getEffectiveProfessionId(player);
        if (professionId == null) return;

        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null || !profession.isHeavyKnight()) return;

        if (!player.isBlocking()) {
            player.sendSystemMessage(Component.literal("§c[重甲骑士] 需要在格挡状态下使用盾击！"));
            return;
        }

        CompoundTag data = player.getPersistentData();
        int cooldown = data.getInt(TAG_SHIELD_BASH_COOLDOWN);
        if (cooldown > 0) {
            player.sendSystemMessage(Component.literal("§c[重甲骑士] 盾击冷却中！"));
            return;
        }

        data.putInt(TAG_SHIELD_BASH_COOLDOWN, profession.getHeavyKnightShieldBashCooldown());
        
        float bashDamage = profession.getHeavyKnightShieldBashDamage();
        int stunDuration = profession.getHeavyKnightShieldBashStunDuration();
        float bashRange = 3.0f;
        
        Vec3 lookVec = player.getLookAngle();
        List<LivingEntity> targets = player.level().getEntitiesOfClass(
            LivingEntity.class,
            player.getBoundingBox().inflate(bashRange),
            e -> e != player && e.isAlive()
        );
        
        for (LivingEntity target : targets) {
            Vec3 toTarget = target.position().subtract(player.position()).normalize();
            double dot = lookVec.dot(toTarget);
            if (dot > 0.5) {
                target.hurt(player.level().damageSources().playerAttack(player), bashDamage);
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, stunDuration, 10, false, true));
            }
        }
        
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CRIT,
                player.getX() + lookVec.x * 2, player.getY() + 1, player.getZ() + lookVec.z * 2,
                20, 0.5, 0.5, 0.5, 0.1);
        }
        
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.8f, 1.2f);
        
        player.sendSystemMessage(Component.literal("§6[重甲骑士] 盾击！"));
    }

    public static void applyShieldWallBonusToTeammates(ServerPlayer player, Profession profession) {
        if (!player.getPersistentData().getBoolean(TAG_SHIELD_WALL_ACTIVE)) return;
        
        float radius = profession.getHeavyKnightShieldWallRadius();
        float armorBonus = profession.getHeavyKnightShieldWallArmorBonus();
        
        List<Player> nearbyPlayers = player.level().getEntitiesOfClass(
            Player.class,
            player.getBoundingBox().inflate(radius),
            p -> p != player && ContractEvents.isSameTeam(player, p)
        );
        
        for (Player teammate : nearbyPlayers) {
            var armorAttr = teammate.getAttribute(Attributes.ARMOR);
            if (armorAttr != null) {
                UUID bonusUuid = UUID.nameUUIDFromBytes(("shield_wall_" + player.getUUID()).getBytes());
                armorAttr.removeModifier(bonusUuid);
                armorAttr.addPermanentModifier(new AttributeModifier(
                    bonusUuid, "shield_wall_armor_bonus",
                    armorBonus, AttributeModifier.Operation.ADDITION
                ));
            }
        }
    }

    public static int getBattleWill(ServerPlayer player) {
        return player.getPersistentData().getInt(TAG_BATTLE_WILL);
    }

    public static int getChargeCooldown(ServerPlayer player) {
        return player.getPersistentData().getInt(TAG_CHARGE_COOLDOWN);
    }

    public static int getProtectCooldown(ServerPlayer player) {
        return player.getPersistentData().getInt(TAG_PROTECT_COOLDOWN);
    }

    public static int getShieldBashCooldown(ServerPlayer player) {
        return player.getPersistentData().getInt(TAG_SHIELD_BASH_COOLDOWN);
    }

    public static boolean isShieldWallActive(ServerPlayer player) {
        return player.getPersistentData().getBoolean(TAG_SHIELD_WALL_ACTIVE);
    }

    public static void onPlayerRespawn(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        data.putInt(TAG_BATTLE_WILL, 0);
        data.putBoolean(TAG_SHIELD_WALL_ACTIVE, false);
        data.putLong(TAG_SHIELD_WALL_START_TIME, 0);
        data.putInt(TAG_CHARGE_COOLDOWN, 0);
        data.putInt(TAG_PROTECT_COOLDOWN, 0);
        data.putInt(TAG_SHIELD_BASH_COOLDOWN, 0);
        data.putInt(TAG_LAST_COMBAT_TIME, 0);
        data.putBoolean(TAG_IS_CHARGING, false);
    }

    private static void syncClientState(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        int battleWill = data.getInt(TAG_BATTLE_WILL);
        boolean shieldWall = data.getBoolean(TAG_SHIELD_WALL_ACTIVE);
        int chargeCooldown = data.getInt(TAG_CHARGE_COOLDOWN);
        int protectCooldown = data.getInt(TAG_PROTECT_COOLDOWN);
        int bashCooldown = data.getInt(TAG_SHIELD_BASH_COOLDOWN);

        NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
            new PacketSyncHeavyKnightState(battleWill, shieldWall, chargeCooldown, protectCooldown, bashCooldown));
    }
}
