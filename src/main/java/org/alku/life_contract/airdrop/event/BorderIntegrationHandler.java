package org.alku.life_contract.airdrop.event;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.border.WorldBorder;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.alku.life_contract.airdrop.data.AirdropSavedData;
import org.alku.life_contract.Life_contract;

@EventBusSubscriber(modid = Life_contract.MODID)
public class BorderIntegrationHandler {

    private static final double DEFAULT_BORDER_SIZE = 6.0E7D;
    private static boolean enabled = true;

    public static void setEnabled(boolean value) {
        enabled = value;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static double[] getAirdropRange(ServerLevel level) {
        WorldBorder border = level.getWorldBorder();
        AirdropSavedData data = AirdropSavedData.get(level);
        double[] configuredRange = data.hasRange() ? data.getRange() : null;
        if (border.getSize() >= DEFAULT_BORDER_SIZE) return configuredRange;
        double centerX = border.getCenterX();
        double centerZ = border.getCenterZ();
        double halfSize = border.getSize() / 2.0;
        double minX = centerX - halfSize;
        double maxX = centerX + halfSize;
        double minZ = centerZ - halfSize;
        double maxZ = centerZ + halfSize;
        if (configuredRange == null) return new double[]{minX, minZ, maxX, maxZ};

        minX = Math.max(minX, configuredRange[0]);
        minZ = Math.max(minZ, configuredRange[1]);
        maxX = Math.min(maxX, configuredRange[2]);
        maxZ = Math.min(maxZ, configuredRange[3]);
        if (minX > maxX || minZ > maxZ) return new double[]{centerX, centerZ, centerX, centerZ};
        return new double[]{minX, minZ, maxX, maxZ};
    }
}
