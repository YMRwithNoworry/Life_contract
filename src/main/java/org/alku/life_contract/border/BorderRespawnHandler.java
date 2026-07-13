package org.alku.life_contract.border;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.Collections;

public final class BorderRespawnHandler {
    private static final double BORDER_MARGIN = 2.0D;

    private BorderRespawnHandler() {
    }

    public static void ensureInsideBorder(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        WorldBorder border = level.getWorldBorder();
        if (border.isWithinBounds(player.getX(), player.getZ())) {
            return;
        }

        int targetX = clampInside(player.getX(), border.getMinX(), border.getMaxX(), border.getCenterX());
        int targetZ = clampInside(player.getZ(), border.getMinZ(), border.getMaxZ(), border.getCenterZ());
        int targetY = findSafeY(level, targetX, targetZ);

        player.teleportTo(level,
                targetX + 0.5D, targetY, targetZ + 0.5D,
                Collections.emptySet(), player.getYRot(), player.getXRot());
        player.setDeltaMovement(Vec3.ZERO);
        player.fallDistance = 0.0F;
    }

    private static int clampInside(double coordinate, double minimum, double maximum, double center) {
        double usableMinimum = minimum + BORDER_MARGIN;
        double usableMaximum = maximum - BORDER_MARGIN;
        if (usableMinimum > usableMaximum) {
            return Mth.floor(center);
        }
        return Mth.floor(Mth.clamp(coordinate, usableMinimum, usableMaximum));
    }

    private static int findSafeY(ServerLevel level, int x, int z) {
        int maximumY = level.getMaxBuildHeight() - 2;
        int y = Mth.clamp(
                level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z),
                level.getMinBuildHeight() + 1,
                maximumY);
        BlockPos.MutableBlockPos feet = new BlockPos.MutableBlockPos(x, y, z);
        while (y < maximumY && (!hasNoCollision(level, feet) || !hasNoCollision(level, feet.above()))) {
            feet.setY(++y);
        }
        return y;
    }

    private static boolean hasNoCollision(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos).getCollisionShape(level, pos).isEmpty();
    }
}
