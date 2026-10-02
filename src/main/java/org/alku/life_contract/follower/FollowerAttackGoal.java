package org.alku.life_contract.follower;

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
        }
    }

    private boolean canAttack(LivingEntity target) {
        if (target == mob) {
            return false;
        }
        if (!target.isAlive()) {
            return false;
        }
        if (target instanceof Player player) {
            return FollowerEvents.isContractAlly(mob)
                    && owner != null
                    && !ContractEvents.isSameTeam(owner, player);
        }
        if (target instanceof Mob targetMob) {
            if (FollowerEvents.areMobsAllied(mob, targetMob)) {
                return false;
            }

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
