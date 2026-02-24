package org.alku.life_contract.follower;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import org.alku.life_contract.ContractEvents;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

public class FollowerAttackGoal extends TargetGoal {

    private final Mob mob;
    private final UUID ownerUUID;
    private Player owner;
    private LivingEntity target;
    private int lastAttackTime;
    private int lastSearchTime;
    private static final double PASSIVE_SEARCH_RANGE = 16.0D;
    private static final double ACTIVE_SEARCH_RANGE = 24.0D;
    private static final int SEARCH_COOLDOWN = 40;

    public FollowerAttackGoal(Mob mob, UUID ownerUUID) {
        super(mob, false, true);
        this.mob = mob;
        this.ownerUUID = ownerUUID;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (owner == null) {
            owner = mob.level().getPlayerByUUID(ownerUUID);
        }
        if (owner == null || !owner.isAlive()) {
            return false;
        }

        LivingEntity ownerTarget = FollowerEvents.getPlayerAttackTarget(owner.getUUID());
        if (ownerTarget != null && ownerTarget.isAlive() && canAttack(ownerTarget)) {
            this.target = ownerTarget;
            return true;
        }

        LivingEntity attacker = FollowerEvents.getPlayerAttacker(owner.getUUID());
        if (attacker != null && attacker.isAlive() && canAttack(attacker)) {
            this.target = attacker;
            return true;
        }

        LivingEntity nearbyEnemy = findNearbyEnemy(ACTIVE_SEARCH_RANGE);
        if (nearbyEnemy != null) {
            this.target = nearbyEnemy;
            return true;
        }

        return false;
    }

    @Override
    public boolean canContinueToUse() {
        if (target == null || !target.isAlive()) {
            return false;
        }
        if (owner == null || !owner.isAlive()) {
            return false;
        }
        
        double distance = mob.distanceToSqr(target);
        if (distance > 400.0D) {
            return false;
        }

        return true;
    }

    @Override
    public void start() {
        mob.setTarget(target);
        lastAttackTime = mob.tickCount;
        lastSearchTime = mob.tickCount;
    }

    @Override
    public void stop() {
        target = null;
        mob.setTarget(null);
    }

    @Override
    public void tick() {
        if (target != null && target.isAlive()) {
            LivingEntity ownerTarget = FollowerEvents.getPlayerAttackTarget(owner.getUUID());
            if (ownerTarget != null && ownerTarget != target && ownerTarget.isAlive() && canAttack(ownerTarget)) {
                this.target = ownerTarget;
                mob.setTarget(target);
            }

            if (mob.tickCount - lastAttackTime > 100) {
                LivingEntity attacker = FollowerEvents.getPlayerAttacker(owner.getUUID());
                if (attacker != null && attacker != target && attacker.isAlive() && canAttack(attacker)) {
                    this.target = attacker;
                    mob.setTarget(target);
                    lastAttackTime = mob.tickCount;
                }
            }

            if (mob.tickCount - lastSearchTime > SEARCH_COOLDOWN) {
                LivingEntity nearbyEnemy = findNearbyEnemy(PASSIVE_SEARCH_RANGE);
                if (nearbyEnemy != null && nearbyEnemy != target) {
                    this.target = nearbyEnemy;
                    mob.setTarget(target);
                }
                lastSearchTime = mob.tickCount;
            }
        }
    }

    private LivingEntity findNearbyEnemy(double range) {
        Level level = mob.level();
        AABB searchBox = mob.getBoundingBox().inflate(range);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, searchBox);
        
        LivingEntity nearestEnemy = null;
        double nearestDistance = Double.MAX_VALUE;
        
        for (LivingEntity entity : entities) {
            if (!canAttack(entity)) {
                continue;
            }
            
            double distance = mob.distanceToSqr(entity);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestEnemy = entity;
            }
        }
        
        return nearestEnemy;
    }

    private boolean canAttack(LivingEntity target) {
        if (target == mob) {
            return false;
        }
        if (!target.isAlive()) {
            return false;
        }
        if (target instanceof Player player) {
            if (owner != null && ContractEvents.isSameTeam(owner, player)) {
                return false;
            }
            return false;
        }
        if (target instanceof Mob targetMob) {
            UUID targetOwner = FollowerEvents.getOwnerUUID(targetMob);
            if (targetOwner != null) {
                if (targetOwner.equals(ownerUUID)) {
                    return false;
                }
                if (owner != null) {
                    Player targetOwnerPlayer = mob.level().getPlayerByUUID(targetOwner);
                    if (targetOwnerPlayer != null && ContractEvents.isSameTeam(owner, targetOwnerPlayer)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
