package org.alku.life_contract;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.alku.life_contract.profession.Profession;
import org.alku.life_contract.profession.ProfessionConfig;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class DonkBowItem extends BowItem {
    private static final String TAG_DONK_BOW = "DonkBow";
    private static final String TAG_DONK_TRACKING = "DonkTrackingArrow";
    private static final float CRIT_CHANCE = 0.25f;
    private static final double TRACKING_RANGE = 100.0;
    private static final double TRACKING_STRENGTH = 0.6;
    private static final double MAX_TURN_RATE = 0.8;

    private static final Random RANDOM = new Random();
    private static final int DEBUFF_DURATION = 30 * 20;
    
    private static final MobEffect[] DEBUFF_POOL = {
        MobEffects.POISON,
        MobEffects.WEAKNESS,
        MobEffects.MOVEMENT_SLOWDOWN,
        MobEffects.BLINDNESS,
        MobEffects.WITHER,
        MobEffects.LEVITATION,
        MobEffects.UNLUCK,
        MobEffects.DIG_SLOWDOWN,
        MobEffects.HUNGER
    };

    public DonkBowItem() {
        super(new Properties().stacksTo(1).rarity(Rarity.EPIC));
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entityLiving, int timeLeft) {
        if (!(entityLiving instanceof Player player)) {
            return;
        }

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            String professionId = ContractEvents.getEffectiveProfessionId(serverPlayer);
            if (professionId == null || professionId.isEmpty()) {
                serverPlayer.displayClientMessage(
                    Component.literal("§c[颗秒之弓] §r你没有职业，无法使用这个武器！"),
                    true
                );
                return;
            }
            
            Profession profession = ProfessionConfig.getProfession(professionId);
            if (profession == null || !profession.hasDonkBowAbility()) {
                serverPlayer.displayClientMessage(
                    Component.literal("§c[颗秒之弓] §r只有donk职业才能使用这个武器！"),
                    true
                );
                return;
            }
        }

        boolean hasInfinity = true;
        boolean hasPower = true;
        int powerLevel = 1;

        int charge = this.getUseDuration(stack) - timeLeft;
        charge = net.minecraftforge.event.ForgeEventFactory.onArrowLoose(stack, level, player, charge, hasInfinity);
        if (charge < 0) return;

        float velocity = getPowerForTime(charge);
        if (velocity < 0.1) return;

        velocity = 1.0F;

        if (!level.isClientSide) {
            AbstractArrow arrow = createArrow(level, player, stack, null, velocity, hasInfinity, powerLevel);
            
            arrow.setCritArrow(true);

            applyTrackingAndCrit(arrow, player, level);

            DonkBowEvents.addTrackingArrow(arrow);

            level.addFreshEntity(arrow);
            
            playShootEffects(level, player);
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F,
                1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + velocity * 0.5F);
    }

    private AbstractArrow createArrow(Level level, Player player, ItemStack bowStack, ItemStack arrowStack, float velocity, boolean hasInfinity, int powerLevel) {
        AbstractArrow arrow = new net.minecraft.world.entity.projectile.Arrow(level, player);

        arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, velocity * 3.0F, 0.0F);

        if (hasInfinity) {
            arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
        } else if (arrowStack != null && !arrowStack.isEmpty()) {
            arrow.pickup = AbstractArrow.Pickup.ALLOWED;
        }

        if (powerLevel > 0) {
            arrow.setBaseDamage(arrow.getBaseDamage() + powerLevel * 0.5D + 0.5D);
        }

        return arrow;
    }

    private void applyTrackingAndCrit(AbstractArrow arrow, Player shooter, Level level) {
        CompoundTag tag = arrow.getPersistentData();
        tag.putBoolean(TAG_DONK_TRACKING, true);

        if (shooter.getRandom().nextFloat() < CRIT_CHANCE) {
            arrow.setCritArrow(true);
            arrow.setBaseDamage(arrow.getBaseDamage() * 1.5D);
            tag.putBoolean("DonkCritArrow", true);
            
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                    ParticleTypes.CRIT,
                    arrow.getX(), arrow.getY(), arrow.getZ(),
                    10, 0.1, 0.1, 0.1, 0.1
                );
            }
        }

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                ParticleTypes.ENCHANT,
                shooter.getX(), shooter.getY() + 1.5, shooter.getZ(),
                15, 0.3, 0.3, 0.3, 0.1
            );
        }
    }

    public static void tickTrackingArrow(AbstractArrow arrow) {
        CompoundTag tag = arrow.getPersistentData();
        if (!tag.getBoolean(TAG_DONK_TRACKING)) {
            return;
        }

        if (arrow.level().isClientSide) {
            return;
        }

        Entity owner = arrow.getOwner();
        if (!(owner instanceof Player shooter)) {
            return;
        }

        LivingEntity target = findNearestTarget(arrow, shooter);
        if (target == null) {
            return;
        }

        Vec3 arrowPos = arrow.position();
        Vec3 targetPos = predictTargetPosition(arrow, target);
        
        double distanceToTarget = arrowPos.distanceTo(targetPos);
        
        if (distanceToTarget < 2.0) {
            teleportArrowToTarget(arrow, target, targetPos);
            return;
        }
        
        Vec3 directionToTarget = targetPos.subtract(arrowPos).normalize();
        Vec3 currentMotion = arrow.getDeltaMovement();
        double currentSpeed = currentMotion.length();
        
        double turnRate = calculateTurnRate(distanceToTarget);
        
        Vec3 newDirection = currentMotion.normalize().lerp(directionToTarget, turnRate);
        newDirection = newDirection.normalize();
        
        double speedMultiplier = 1.0;
        if (distanceToTarget < 15.0) {
            speedMultiplier = 1.0 + (15.0 - distanceToTarget) * 0.08;
        }
        
        Vec3 newMotion = newDirection.scale(currentSpeed * speedMultiplier);
        arrow.setDeltaMovement(newMotion);
        
        updateArrowRotation(arrow, targetPos);
        
        tag.putDouble("LastTargetX", target.getX());
        tag.putDouble("LastTargetY", target.getY());
        tag.putDouble("LastTargetZ", target.getZ());

        if (arrow.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                ParticleTypes.END_ROD,
                arrow.getX(), arrow.getY(), arrow.getZ(),
                3, 0.05, 0.05, 0.05, 0.02
            );
        }
    }
    
    private static void teleportArrowToTarget(AbstractArrow arrow, LivingEntity target, Vec3 targetPos) {
        Vec3 arrowPos = arrow.position();
        Vec3 direction = targetPos.subtract(arrowPos);
        
        if (direction.lengthSqr() > 0) {
            direction = direction.normalize();
        }
        
        arrow.setPos(targetPos.x, targetPos.y, targetPos.z);
        arrow.setDeltaMovement(direction.scale(2.0));
        
        if (arrow.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                ParticleTypes.TOTEM_OF_UNDYING,
                targetPos.x, targetPos.y, targetPos.z,
                10, 0.2, 0.2, 0.2, 0.1
            );
        }
    }
    
    private static Vec3 predictTargetPosition(AbstractArrow arrow, LivingEntity target) {
        CompoundTag tag = arrow.getPersistentData();
        
        Vec3 targetPos = target.position().add(0, target.getBbHeight() / 2, 0);
        
        if (tag.contains("LastTargetX")) {
            double lastX = tag.getDouble("LastTargetX");
            double lastY = tag.getDouble("LastTargetY");
            double lastZ = tag.getDouble("LastTargetZ");
            
            Vec3 targetVelocity = new Vec3(
                target.getX() - lastX,
                target.getY() - lastY,
                target.getZ() - lastZ
            );
            
            Vec3 arrowPos = arrow.position();
            double distance = arrowPos.distanceTo(targetPos);
            double flightTime = distance / arrow.getDeltaMovement().length();
            
            flightTime = Math.min(flightTime, 15);
            
            targetPos = targetPos.add(targetVelocity.scale(flightTime * 1.2));
        }
        
        return targetPos;
    }
    
    private static double calculateTurnRate(double distance) {
        if (distance < 3.0) {
            return 0.95;
        } else if (distance < 8.0) {
            return 0.7 + (8.0 - distance) * 0.03;
        } else if (distance < 15.0) {
            return 0.5 + (15.0 - distance) * 0.02;
        } else if (distance < 30.0) {
            return 0.35 + (30.0 - distance) * 0.01;
        } else {
            return TRACKING_STRENGTH;
        }
    }
    
    private static void updateArrowRotation(AbstractArrow arrow, Vec3 targetPos) {
        Vec3 arrowPos = arrow.position();
        double dx = targetPos.x - arrowPos.x;
        double dy = targetPos.y - arrowPos.y;
        double dz = targetPos.z - arrowPos.z;
        
        if (Math.sqrt(dx * dx + dy * dy + dz * dz) > 0) {
            arrow.setYRot((float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0));
            arrow.setXRot((float)(Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)))));
        }
    }

    private static LivingEntity findNearestTarget(AbstractArrow arrow, Player shooter) {
        Vec3 arrowPos = arrow.position();
        Vec3 arrowMotion = arrow.getDeltaMovement().normalize();
        
        AABB searchBox = new AABB(
            arrowPos.x - TRACKING_RANGE, arrowPos.y - TRACKING_RANGE / 2, arrowPos.z - TRACKING_RANGE,
            arrowPos.x + TRACKING_RANGE, arrowPos.y + TRACKING_RANGE / 2, arrowPos.z + TRACKING_RANGE
        );

        List<LivingEntity> entities = arrow.level().getEntitiesOfClass(
            LivingEntity.class,
            searchBox,
            entity -> {
                if (entity == shooter) return false;
                if (entity instanceof Player) {
                    if (ContractEvents.isSameTeam(shooter, (Player) entity)) return false;
                }
                return entity.isAlive() && !entity.isSpectator();
            }
        );

        if (entities.isEmpty()) {
            return null;
        }

        Optional<LivingEntity> nearestInFront = entities.stream()
            .filter(entity -> {
                Vec3 toEntity = entity.position().subtract(arrowPos).normalize();
                double dot = arrowMotion.dot(toEntity);
                return dot > 0.0;
            })
            .min(Comparator.comparingDouble(entity -> entity.distanceToSqr(arrowPos)));

        return nearestInFront.orElseGet(() -> 
            entities.stream()
                .min(Comparator.comparingDouble(entity -> entity.distanceToSqr(arrowPos)))
                .orElse(null)
        );
    }

    public static void applyRandomDebuff(LivingEntity target) {
        MobEffect randomDebuff = DEBUFF_POOL[RANDOM.nextInt(DEBUFF_POOL.length)];
        
        int amplifier = RANDOM.nextInt(2);
        
        MobEffectInstance effect = new MobEffectInstance(randomDebuff, DEBUFF_DURATION, amplifier, false, true);
        target.addEffect(effect);
        
        if (target instanceof Player player) {
            String debuffName = getDebuffName(randomDebuff);
            player.sendSystemMessage(Component.literal(
                "§c[颗秒之弓] §f你被施加了 §e" + debuffName + " §f效果！"
            ));
        }
    }

    private static String getDebuffName(MobEffect effect) {
        if (effect == MobEffects.POISON) return "中毒";
        if (effect == MobEffects.WEAKNESS) return "虚弱";
        if (effect == MobEffects.MOVEMENT_SLOWDOWN) return "缓慢";
        if (effect == MobEffects.BLINDNESS) return "失明";
        if (effect == MobEffects.WITHER) return "凋零";
        if (effect == MobEffects.LEVITATION) return "飘浮";
        if (effect == MobEffects.UNLUCK) return "霉运";
        if (effect == MobEffects.DIG_SLOWDOWN) return "挖掘疲劳";
        if (effect == MobEffects.HUNGER) return "饥饿";
        return "未知效果";
    }

    private void playShootEffects(Level level, Player player) {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                ParticleTypes.CRIT,
                player.getX(), player.getY() + 1.5, player.getZ(),
                5, 0.3, 0.3, 0.3, 0.1
            );
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.literal("§d§l颗秒神弓 §r§7- donk专属武器"));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("§e✦ 无限 §7- 无需箭矢"));
        tooltip.add(Component.literal("§e✦ 力量 I §7- 增加箭矢伤害"));
        tooltip.add(Component.literal("§e✦ 瞬发 §7- 无需拉满即可发射满威力箭矢"));
        tooltip.add(Component.literal("§e✦ 必中追踪 §7- 箭矢自动追踪并命中敌人"));
        tooltip.add(Component.literal("§e✦ 预判 §7- 自动预判目标移动轨迹"));
        tooltip.add(Component.literal("§e✦ 暴击 §7- 25%概率造成1.5倍伤害"));
        tooltip.add(Component.literal("§c✦ 诅咒 §7- 命中目标随机施加30秒debuff"));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("§b★ 永久绑定 - 死亡不掉落"));
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        stack.getOrCreateTag().putBoolean(TAG_DONK_BOW, true);
        return stack;
    }

    public static boolean isDonkBow(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(TAG_DONK_BOW);
    }
}
