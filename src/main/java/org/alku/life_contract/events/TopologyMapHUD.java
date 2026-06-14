package org.alku.life_contract.events;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.alku.life_contract.ClientDataStorage;
import org.alku.life_contract.Life_contract;
import org.alku.life_contract.TeamInventory;

import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Life_contract.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class TopologyMapHUD {

    private static final int MAP_X = 10;
    private static final int MAP_Y = 10;
    private static final int MAP_SIZE = 128;
    private static final long DOUBLE_CLICK_WINDOW_MS = 260;
    private static final float MODE_ANIMATION_SPEED = 0.12f;

    private static boolean visible = true;
    private static boolean fitBorderMode = false;
    private static long firstClickAt = 0;
    private static boolean waitingForSingleClick = false;
    private static float modeProgress = 0.0f;

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.PLAYER_LIST.id(), "topology_map", TOPOLOGY_MAP_OVERLAY);
    }

    public static void onMKeyPressed() {
        long now = System.currentTimeMillis();
        if (waitingForSingleClick && now - firstClickAt <= DOUBLE_CLICK_WINDOW_MS) {
            waitingForSingleClick = false;
            fitBorderMode = !fitBorderMode;
            visible = true;
            return;
        }

        firstClickAt = now;
        waitingForSingleClick = true;
    }

    public static final IGuiOverlay TOPOLOGY_MAP_OVERLAY = (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
        resolvePendingSingleClick();

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || !visible || !playerHasTopology(mc)) {
            return;
        }

        modeProgress += ((fitBorderMode ? 1.0f : 0.0f) - modeProgress) * MODE_ANIMATION_SPEED;

        View selfView = buildSelfCenteredView(mc);
        View borderView = buildBorderFitView(mc);
        View view = View.lerp(selfView, borderView, smoothstep(modeProgress));

        drawFrame(guiGraphics);
        drawBorder(guiGraphics, view);
        drawEvents(guiGraphics, view);
        drawPlayers(guiGraphics, mc, view);
    };

    private static void resolvePendingSingleClick() {
        if (!waitingForSingleClick) {
            return;
        }

        if (System.currentTimeMillis() - firstClickAt > DOUBLE_CLICK_WINDOW_MS) {
            visible = !visible;
            waitingForSingleClick = false;
        }
    }

    private static boolean playerHasTopology(Minecraft mc) {
        for (ItemStack stack : mc.player.getInventory().items) {
            if (!stack.isEmpty() && stack.is(Life_contract.LIFE_TOPOLOGY.get())) {
                return true;
            }
        }

        TeamInventory teamInventory = TeamInventory.getOrCreate(mc.player);
        for (int i = 0; i < teamInventory.getContainerSize(); i++) {
            ItemStack stack = teamInventory.getItem(i);
            if (!stack.isEmpty() && stack.is(Life_contract.LIFE_TOPOLOGY.get())) {
                return true;
            }
        }
        return false;
    }

    private static View buildSelfCenteredView(Minecraft mc) {
        double scale = 2.5;
        return new View(mc.player.getX(), mc.player.getZ(), scale);
    }

    private static View buildBorderFitView(Minecraft mc) {
        double borderSize = ClientDataStorage.getBorderSize();
        double centerX = ClientDataStorage.getBorderCenterX();
        double centerZ = ClientDataStorage.getBorderCenterZ();

        if (borderSize <= 1) {
            WorldBorder border = mc.level.getWorldBorder();
            borderSize = border.getSize();
            centerX = border.getCenterX();
            centerZ = border.getCenterZ();
        }

        double scale = Math.max(1.0, borderSize / (MAP_SIZE - 18.0));
        return new View(centerX, centerZ, scale);
    }

    private static void drawFrame(GuiGraphics guiGraphics) {
        guiGraphics.fill(MAP_X - 2, MAP_Y - 2, MAP_X + MAP_SIZE + 2, MAP_Y + MAP_SIZE + 2, 0xAA081018);
        guiGraphics.fill(MAP_X, MAP_Y, MAP_X + MAP_SIZE, MAP_Y + MAP_SIZE, 0x77101820);
        guiGraphics.hLine(MAP_X, MAP_X + MAP_SIZE, MAP_Y, 0xAA5D7280);
        guiGraphics.hLine(MAP_X, MAP_X + MAP_SIZE, MAP_Y + MAP_SIZE, 0xAA5D7280);
        guiGraphics.vLine(MAP_X, MAP_Y, MAP_Y + MAP_SIZE, 0xAA5D7280);
        guiGraphics.vLine(MAP_X + MAP_SIZE, MAP_Y, MAP_Y + MAP_SIZE, 0xAA5D7280);
    }

    private static void drawBorder(GuiGraphics guiGraphics, View view) {
        double borderSize = ClientDataStorage.getBorderSize();
        if (borderSize <= 1) {
            return;
        }

        double half = borderSize / 2.0;
        int left = worldToMapX(ClientDataStorage.getBorderCenterX() - half, view);
        int right = worldToMapX(ClientDataStorage.getBorderCenterX() + half, view);
        int top = worldToMapZ(ClientDataStorage.getBorderCenterZ() - half, view);
        int bottom = worldToMapZ(ClientDataStorage.getBorderCenterZ() + half, view);

        int clippedLeft = clamp(left, MAP_X, MAP_X + MAP_SIZE);
        int clippedRight = clamp(right, MAP_X, MAP_X + MAP_SIZE);
        int clippedTop = clamp(top, MAP_Y, MAP_Y + MAP_SIZE);
        int clippedBottom = clamp(bottom, MAP_Y, MAP_Y + MAP_SIZE);

        if (right >= MAP_X && left <= MAP_X + MAP_SIZE && bottom >= MAP_Y && top <= MAP_Y + MAP_SIZE) {
            guiGraphics.hLine(clippedLeft, clippedRight, clippedTop, 0xFFFF3030);
            guiGraphics.hLine(clippedLeft, clippedRight, clippedBottom, 0xFFFF3030);
            guiGraphics.vLine(clippedLeft, clippedTop, clippedBottom, 0xFFFF3030);
            guiGraphics.vLine(clippedRight, clippedTop, clippedBottom, 0xFFFF3030);
        }
    }

    private static void drawEvents(GuiGraphics guiGraphics, View view) {
        List<int[]> bubbles = ClientDataStorage.getBubblePositions();
        if (bubbles != null) {
            int[] colors = {0xFFDCF8FF, 0xFF66FF88, 0xFF75A7FF, 0xFFFF8CE8, 0xFFFFE066};
            for (int i = 0; i < bubbles.size(); i++) {
                int[] bubble = bubbles.get(i);
                if (bubble.length < 4) {
                    continue;
                }
                int x = worldToMapX(bubble[0], view);
                int z = worldToMapZ(bubble[2], view);
                int radius = Math.max(2, (int) Math.round(bubble[3] / view.scale));
                int color = colors[Math.floorMod(bubble.length >= 5 ? bubble[4] : i, colors.length)];
                drawCircle(guiGraphics, x, z, radius, color);
            }
        }

        UUID bountyUUID = ClientDataStorage.getBountyTargetUUID();
        if (bountyUUID != null) {
            int x = worldToMapX(ClientDataStorage.getBountyTargetX(), view);
            int z = worldToMapZ(ClientDataStorage.getBountyTargetZ(), view);
            if (insideMap(x, z)) {
                guiGraphics.fill(x - 3, z - 3, x + 4, z + 4, 0xFFFFD12E);
                guiGraphics.fill(x - 1, z - 1, x + 2, z + 2, 0xFF5A1600);
            }
        }
    }

    private static void drawPlayers(GuiGraphics guiGraphics, Minecraft mc, View view) {
        UUID selfUUID = mc.player.getUUID();
        UUID selfLeader = null;
        ClientDataStorage.PlayerData selfData = ClientDataStorage.get(selfUUID);
        if (selfData != null) {
            selfLeader = selfData.leaderUUID != null ? selfData.leaderUUID : selfUUID;
        }

        boolean drewSelf = false;
        for (PacketSyncEvents.PlayerPosData player : ClientDataStorage.getPlayerPositions()) {
            int x = worldToMapX(player.x, view);
            int z = worldToMapZ(player.z, view);
            if (!insideMap(x, z)) {
                continue;
            }

            boolean self = selfUUID.equals(player.uuid);
            drewSelf = drewSelf || self;
            UUID playerLeader = player.leaderUUID != null ? player.leaderUUID : player.uuid;
            boolean teammate = selfLeader != null && selfLeader.equals(playerLeader);
            int color = self ? 0xFFFFFFFF : teammate ? 0xFF42D7FF : 0xFF9AA1A8;
            if (ClientDataStorage.getBountyTargetUUID() != null && ClientDataStorage.getBountyTargetUUID().equals(player.uuid)) {
                color = 0xFFFFD12E;
            }

            drawPointer(guiGraphics, x, z, player.yaw, color);
        }

        if (!drewSelf) {
            int x = worldToMapX(mc.player.getX(), view);
            int z = worldToMapZ(mc.player.getZ(), view);
            if (insideMap(x, z)) {
                drawPointer(guiGraphics, x, z, mc.player.getYRot(), 0xFFFFFFFF);
            }
        }
    }

    private static void drawPointer(GuiGraphics guiGraphics, int x, int z, float yaw, int color) {
        double radians = Math.toRadians(yaw);
        double forwardX = -Math.sin(radians);
        double forwardZ = Math.cos(radians);
        double sideX = Math.cos(radians);
        double sideZ = Math.sin(radians);

        int tipX = (int) Math.round(x + forwardX * 4.0);
        int tipZ = (int) Math.round(z + forwardZ * 4.0);
        int leftX = (int) Math.round(x - forwardX * 3.0 + sideX * 2.0);
        int leftZ = (int) Math.round(z - forwardZ * 3.0 + sideZ * 2.0);
        int rightX = (int) Math.round(x - forwardX * 3.0 - sideX * 2.0);
        int rightZ = (int) Math.round(z - forwardZ * 3.0 - sideZ * 2.0);

        fillTriangle(guiGraphics, tipX, tipZ, leftX, leftZ, rightX, rightZ, 0xCC000000);
        fillTriangle(guiGraphics, tipX, tipZ, leftX, leftZ, rightX, rightZ, color);
        guiGraphics.fill(x - 1, z - 1, x + 2, z + 2, 0xDD000000);
    }

    private static void fillTriangle(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int x3, int y3, int color) {
        int minX = Math.max(MAP_X, Math.min(x1, Math.min(x2, x3)));
        int maxX = Math.min(MAP_X + MAP_SIZE, Math.max(x1, Math.max(x2, x3)));
        int minY = Math.max(MAP_Y, Math.min(y1, Math.min(y2, y3)));
        int maxY = Math.min(MAP_Y + MAP_SIZE, Math.max(y1, Math.max(y2, y3)));

        for (int px = minX; px <= maxX; px++) {
            for (int py = minY; py <= maxY; py++) {
                if (pointInTriangle(px + 0.5, py + 0.5, x1, y1, x2, y2, x3, y3)) {
                    guiGraphics.fill(px, py, px + 1, py + 1, color);
                }
            }
        }
    }

    private static boolean pointInTriangle(double px, double py, int x1, int y1, int x2, int y2, int x3, int y3) {
        double d1 = sign(px, py, x1, y1, x2, y2);
        double d2 = sign(px, py, x2, y2, x3, y3);
        double d3 = sign(px, py, x3, y3, x1, y1);
        boolean hasNegative = d1 < 0 || d2 < 0 || d3 < 0;
        boolean hasPositive = d1 > 0 || d2 > 0 || d3 > 0;
        return !(hasNegative && hasPositive);
    }

    private static double sign(double px, double py, int x1, int y1, int x2, int y2) {
        return (px - x2) * (y1 - y2) - (x1 - x2) * (py - y2);
    }

    private static void drawCircle(GuiGraphics guiGraphics, int centerX, int centerY, int radius, int color) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz <= radius * radius) {
                    int x = centerX + dx;
                    int z = centerY + dz;
                    if (insideMap(x, z)) {
                        guiGraphics.fill(x, z, x + 1, z + 1, color);
                    }
                }
            }
        }
    }

    private static int worldToMapX(double worldX, View view) {
        return (int) Math.round(MAP_X + MAP_SIZE / 2.0 + (worldX - view.centerX) / view.scale);
    }

    private static int worldToMapZ(double worldZ, View view) {
        return (int) Math.round(MAP_Y + MAP_SIZE / 2.0 + (worldZ - view.centerZ) / view.scale);
    }

    private static boolean insideMap(int x, int z) {
        return x >= MAP_X && x <= MAP_X + MAP_SIZE && z >= MAP_Y && z <= MAP_Y + MAP_SIZE;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static float smoothstep(float value) {
        float x = Math.max(0.0f, Math.min(1.0f, value));
        return x * x * (3.0f - 2.0f * x);
    }

    private static class View {
        private final double centerX;
        private final double centerZ;
        private final double scale;

        private View(double centerX, double centerZ, double scale) {
            this.centerX = centerX;
            this.centerZ = centerZ;
            this.scale = scale;
        }

        private static View lerp(View from, View to, float t) {
            return new View(
                from.centerX + (to.centerX - from.centerX) * t,
                from.centerZ + (to.centerZ - from.centerZ) * t,
                from.scale + (to.scale - from.scale) * t
            );
        }
    }
}
