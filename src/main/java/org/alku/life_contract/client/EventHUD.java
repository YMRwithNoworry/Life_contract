package org.alku.life_contract.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import org.alku.life_contract.ClientDataStorage;
import org.alku.life_contract.Life_contract;
import org.alku.life_contract.events.EventSyncPayload;

import java.util.ArrayList;
import java.util.List;

/**
 * 游戏事件 HUD：屏幕右上角列出进行中的事件、剩余时间与安全气泡坐标，
 * 并在孢子雨期间为暴露在天空下的玩家叠加一层黄色色调。
 */
@EventBusSubscriber(modid = Life_contract.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class EventHUD {

    private static final ResourceLocation TINT_LAYER_ID =
            ResourceLocation.fromNamespaceAndPath(Life_contract.MODID, "event_spore_rain_tint");
    private static final ResourceLocation STATUS_LAYER_ID =
            ResourceLocation.fromNamespaceAndPath(Life_contract.MODID, "event_status");

    private static final float SCALE = 0.8F;
    private static final int RIGHT_MARGIN = 6;
    private static final int LINE_HEIGHT = 10;
    /** 每帧重建字符串与测量宽度没有意义：内容最多每 5 tick 刷新一次。 */
    private static final int CONTENT_REFRESH_TICKS = 5;

    public static boolean isEnabled = true;

    /** 一行状态文本及其宽度与行距（宽度只在重建时测量一次）。 */
    private record StatusLine(String text, int width, int lineAdvance) {
    }

    private static List<StatusLine> cachedStatusLines = List.of();
    private static int lastStatusRefreshTick = -1000;

    private EventHUD() {
    }

    /** 接收到关键坐标同步包时刷新左侧 HUD 的坐标显示。 */
    public static void updateWaypoints(org.alku.life_contract.PacketSyncWaypoints payload) {
        ClientDataStorage.setWaypoints(
                payload.active(),
                payload.portalX(), payload.portalY(), payload.portalZ(), payload.portalActivated(),
                payload.borderCenterX(), payload.borderCenterZ());
    }

    /** 接收到服务端事件同步包时刷新客户端事件状态。 */
    public static void update(EventSyncPayload payload) {
        ClientDataStorage.setEventData(
                payload.gameActive(),
                payload.sporeSurgeActive(),
                payload.sporeSurgeRemaining(),
                payload.purificationRiftActive(),
                payload.safeBubbleRemaining(),
                payload.bubbles(),
                payload.bountyActive(),
                payload.bountyTargetName(),
                payload.endgameOverloadActive(),
                payload.sporeRainActive(),
                payload.sporeRainRemaining());
    }

    @SubscribeEvent
    public static void registerLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.CROSSHAIR, TINT_LAYER_ID, EventHUD::renderSporeRainTint);
        event.registerAbove(VanillaGuiLayers.EXPERIENCE_BAR, STATUS_LAYER_ID, EventHUD::renderEventStatus);
    }

    private static void renderSporeRainTint(GuiGraphics graphics, DeltaTracker partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) return;
        if (minecraft.options.hideGui) return;
        if (!ClientDataStorage.isSporeRainActive()) return;
        if (!minecraft.level.canSeeSky(minecraft.player.blockPosition())) return;

        graphics.fill(0, 0, graphics.guiWidth(), graphics.guiHeight(), 0x33FFD45A);
    }

    private static void renderEventStatus(GuiGraphics graphics, DeltaTracker partialTick) {
        if (!isEnabled) return;

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui) return;

        if (minecraft.player.tickCount - lastStatusRefreshTick >= CONTENT_REFRESH_TICKS) {
            lastStatusRefreshTick = minecraft.player.tickCount;
            cachedStatusLines = buildStatusLines(minecraft);
        }

        graphics.pose().pushPose();
        graphics.pose().scale(SCALE, SCALE, SCALE);

        int scaledWidth = (int) (graphics.guiWidth() / SCALE);
        int y = 8;
        for (StatusLine line : cachedStatusLines) {
            graphics.drawString(minecraft.font, line.text(), scaledWidth - line.width() - RIGHT_MARGIN, y, 0xFFFFFF);
            y += line.lineAdvance();
        }

        graphics.pose().popPose();
    }

    /** 组装右上角状态文本（仅在缓存过期时调用）。 */
    private static List<StatusLine> buildStatusLines(Minecraft minecraft) {
        List<StatusLine> lines = new ArrayList<>();
        boolean hasAnyEvent = false;

        addLine(lines, minecraft, "§6== 游戏事件 ==", LINE_HEIGHT + 2);

        if (ClientDataStorage.isSporeSurgeActive()) {
            hasAnyEvent = true;
            addEvent(lines, minecraft, "§c[孢潮推进]",
                    "  §f剩余: §e" + ClientDataStorage.getSporeSurgeRemaining() + "秒");
        }

        if (ClientDataStorage.isPurificationRiftActive()) {
            hasAnyEvent = true;
            addEvent(lines, minecraft, "§b[净化裂隙]",
                    ClientDataStorage.getSafeBubbleRemaining() > 0
                            ? "  §f气泡剩余: §e" + ClientDataStorage.getSafeBubbleRemaining() + "秒"
                            : "  §f气泡正在消散");

            List<int[]> bubbles = ClientDataStorage.getBubblePositions();
            if (bubbles != null && !bubbles.isEmpty()) {
                addLine(lines, minecraft, "  §f安全气泡:", LINE_HEIGHT);
                int index = 1;
                for (int[] bubble : bubbles) {
                    if (bubble.length >= 4) {
                        addLine(lines, minecraft,
                                "    §b气泡" + index + "§7: §fX:" + bubble[0] + " Y:" + bubble[1] + " Z:" + bubble[2],
                                LINE_HEIGHT);
                    }
                    index++;
                }
            }
        }

        if (ClientDataStorage.isBountyActive()) {
            hasAnyEvent = true;
            addEvent(lines, minecraft, "§e[清道夫悬赏]",
                    "  §f目标: §c" + ClientDataStorage.getBountyTargetName());
            addLine(lines, minecraft, "  §f目标身上有 §e发光标记", LINE_HEIGHT);
        }

        if (ClientDataStorage.isEndgameOverloadActive()) {
            hasAnyEvent = true;
            addEvent(lines, minecraft, "§4[终局过载]", "  §f状态: §c永久");
        }

        if (ClientDataStorage.isSporeRainActive()) {
            hasAnyEvent = true;
            addEvent(lines, minecraft, "§2[孢子雨]",
                    "  §f剩余: §e" + ClientDataStorage.getSporeRainRemaining() + "秒");
        }

        if (!hasAnyEvent) {
            addLine(lines, minecraft,
                    ClientDataStorage.isGameActive() ? "§7暂无进行中事件" : "§7游戏未开始", LINE_HEIGHT);
        }

        return lines;
    }

    private static void addEvent(List<StatusLine> lines, Minecraft minecraft, String headline, String detail) {
        addLine(lines, minecraft, headline, LINE_HEIGHT);
        if (detail != null) {
            addLine(lines, minecraft, detail, LINE_HEIGHT);
        }
    }

    private static void addLine(List<StatusLine> lines, Minecraft minecraft, String text, int lineAdvance) {
        lines.add(new StatusLine(text, minecraft.font.width(text), lineAdvance));
    }
}
