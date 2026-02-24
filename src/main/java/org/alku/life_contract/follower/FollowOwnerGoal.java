package org.alku.life_contract.follower;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.BlockPathTypes;

import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;

public class FollowOwnerGoal extends Goal {

    private final Mob mob;
    private final UUID ownerUUID;
    private Player owner;
    private Level level;
    private final double speedModifier;
    private final float stopDistance;
    private final float startDistance;
    private int timeToRecalcPath;
    private float oldWaterCost;

    public FollowOwnerGoal(Mob mob, UUID ownerUUID, double speedModifier, float startDistance, float stopDistance) {
        this.mob = mob;
        this.ownerUUID = ownerUUID;
        this.speedModifier = speedModifier;
        this.startDistance = startDistance;
        this.stopDistance = stopDistance;
        this.level = mob.level();
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (owner == null) {
            owner = level.getPlayerByUUID(ownerUUID);
        }
        if (owner == null) {
            return false;
        }
        if (mob.distanceToSqr(owner) < startDistance * startDistance) {
            return false;
        }
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (owner == null || !owner.isAlive()) {
            return false;
        }
        if (mob.distanceToSqr(owner) <= stopDistance * stopDistance) {
            return false;
        }
        return true;
    }

    @Override
    public void start() {
        timeToRecalcPath = 0;
        oldWaterCost = mob.getPathfindingMalus(BlockPathTypes.WATER);
        mob.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
    }

    @Override
    public void stop() {
        owner = null;
        mob.getNavigation().stop();
        mob.setPathfindingMalus(BlockPathTypes.WATER, oldWaterCost);
    }

    @Override
    public void tick() {
        if (owner == null) {
            owner = level.getPlayerByUUID(ownerUUID);
            if (owner == null) return;
        }

        mob.getLookControl().setLookAt(owner, 10.0F, (float) mob.getMaxHeadXRot());

        if (--timeToRecalcPath <= 0) {
            timeToRecalcPath = 10;
            if (!mob.isLeashed() && !mob.isPassenger()) {
                if (mob.distanceToSqr(owner) >= 144.0D) {
                    teleportToOwner();
                } else {
                    mob.getNavigation().moveTo(owner, speedModifier);
                }
            }
        }
    }

    private void teleportToOwner() {
        for (int i = 0; i < 10; ++i) {
            int x = (int) owner.getX() + mob.getRandom().nextIntBetweenInclusive(-3, 3);
            int y = (int) owner.getY() + mob.getRandom().nextIntBetweenInclusive(-1, 1);
            int z = (int) owner.getZ() + mob.getRandom().nextIntBetweenInclusive(-3, 3);
            
            if (safeTeleportTo(x, y, z)) {
                return;
            }
        }
    }

    private boolean safeTeleportTo(int x, int y, int z) {
        if (mob.randomTeleport(x, y, z, true)) {
            return true;
        }
        return false;
    }
}
