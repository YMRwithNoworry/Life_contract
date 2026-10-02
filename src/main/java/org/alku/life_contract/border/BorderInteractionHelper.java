package org.alku.life_contract.border;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class BorderInteractionHelper {
    private BorderInteractionHelper() {
    }

    public static boolean mayInteractIgnoringWorldBorder(ServerLevel level, Player player, BlockPos pos) {
        if (!mayIgnoreWorldBorder(level, pos)) {
            return level.mayInteract(player, pos);
        }

        return !(player instanceof ServerPlayer serverPlayer)
                || !level.getServer().isUnderSpawnProtection(level, pos, serverPlayer);
    }

    public static boolean mayIgnoreWorldBorder(ServerLevel level, BlockPos pos) {
        BorderManager.BorderData border = BorderManager.getCurrentBorder();
        return org.alku.life_contract.events.GameEventManager.isGameActive()
                && border != null
                && border.getLevel() == level
                && !level.getWorldBorder().isWithinBounds(pos);
    }

}
