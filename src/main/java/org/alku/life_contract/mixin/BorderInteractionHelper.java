package org.alku.life_contract.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

final class BorderInteractionHelper {
    private BorderInteractionHelper() {
    }

    static boolean mayInteractIgnoringWorldBorder(ServerLevel level, Player player, BlockPos pos) {
        if (level.getWorldBorder().isWithinBounds(pos)) {
            return level.mayInteract(player, pos);
        }

        return !(player instanceof ServerPlayer serverPlayer)
                || !level.getServer().isUnderSpawnProtection(level, pos, serverPlayer);
    }
}
