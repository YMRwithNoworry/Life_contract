package org.alku.life_contract.compat;

import net.minecraft.server.level.ServerPlayer;
import org.alku.life_contract.ContractEvents;

public final class UltimineRestriction {
    private static final double ENEMY_BLOCK_RADIUS = 10.0D;
    private static final double ENEMY_BLOCK_RADIUS_SQUARED = ENEMY_BLOCK_RADIUS * ENEMY_BLOCK_RADIUS;

    private UltimineRestriction() {
    }

    public static boolean canUse(ServerPlayer player) {
        return player.hurtTime <= 0 && !hasNearbyEnemyPlayer(player);
    }

    private static boolean hasNearbyEnemyPlayer(ServerPlayer player) {
        return !player.serverLevel().getEntitiesOfClass(
                ServerPlayer.class,
                player.getBoundingBox().inflate(ENEMY_BLOCK_RADIUS),
                other -> other != player
                        && other.isAlive()
                        && !other.isSpectator()
                        && player.distanceToSqr(other) <= ENEMY_BLOCK_RADIUS_SQUARED
                        && !ContractEvents.isSameTeam(player, other)
        ).isEmpty();
    }
}
