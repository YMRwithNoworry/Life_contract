package org.alku.life_contract.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import org.alku.life_contract.ClientDataStorage;
import org.alku.life_contract.Life_contract;
import org.alku.life_contract.events.EventSyncPayload;

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

    public static boolean isEnabled = true;

    private EventHUD() {
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

        boolean hasAnyEvent = false;

        graphics.pose().pushPose();
        graphics.pose().scale(SCALE, SCALE, SCALE);

        int scaledWidth = (int) (graphics.guiWidth() / SCALE);
        int y = 8;

        String title = "§6== 游戏事件 ==";
        graphics.drawString(minecraft.font, title, scaledWidth - minecraft.font.width(title) - RIGHT_MARGIN, y,
                0xFFFFFF);
        y += LINE_HEIGHT + 2;

        if (ClientDataStorage.isSporeSurgeActive()) {
            hasAnyEvent = true;
            y = drawEvent(graphics, minecraft, scaledWidth, y, "§c[孢潮推进]",
                    "  §f剩余: §e" + ClientDataStorage.getSporeSurgeRemaining() + "秒");
        }

        if (ClientDataStorage.isPurificationRiftActive()) {
            hasAnyEvent = true;
            y = drawEvent(graphics, minecraft, scaledWidth, y, "§b[净化裂隙]",
                    ClientDataStorage.getSafeBubbleRemaining() > 0
                            ? "  §f气泡剩余: §e" + ClientDataStorage.getSafeBubbleRemaining() + "秒"
                            : "  §f气泡正在消散");

            List<int[]> bubbles = ClientDataStorage.getBubblePositions();
            if (bubbles != null && !bubbles.isEmpty()) {
                y = drawLine(graphics, minecraft, scaledWidth, y, "  §f安全气泡:");
                int index = 1;
                for (int[] bubble : bubbles) {
                    if (bubble.length >= 4) {
                        y = drawLine(graphics, minecraft, scaledWidth, y,
                                "    §b气泡" + index + "§7: §fX:" + bubble[0] + " Y:" + bubble[1] + " Z:" + bubble[2]);
                    }
                    index++;
                }
            }
        }

        if (ClientDataStorage.isBountyActive()) {
            hasAnyEvent = true;
            y = drawEvent(graphics, minecraft, scaledWidth, y, "§e[清道夫悬赏]",
                    "  §f目标: §c" + ClientDataStorage.getBountyTargetName());
            y = drawLine(graphics, minecraft, scaledWidth, y, "  §f目标身上有 §e发光标记");
        }

        if (ClientDataStorage.isEndgameOverloadActive()) {
            hasAnyEvent = true;
            y = drawEvent(graphics, minecraft, scaledWidth, y, "§4[终局过载]",
                    "  §f状态: §c永久");
        }

        if (ClientDataStorage.isSporeRainActive()) {
            hasAnyEvent = true;
            y = drawEvent(graphics, minecraft, scaledWidth, y, "§2[孢子雨]",
                    "  §f剩余: §e" + ClientDataStorage.getSporeRainRemaining() + "秒");
        }

        if (!hasAnyEvent) {
            String line = ClientDataStorage.isGameActive() ? "§7暂无进行中事件" : "§7游戏未开始";
            drawLine(graphics, minecraft, scaledWidth, y, line);
        }

        graphics.pose().popPose();
    }

    private static int drawEvent(GuiGraphics graphics, Minecraft minecraft, int scaledWidth, int y,
                                 String headline, String detail) {
        y = drawLine(graphics, minecraft, scaledWidth, y, headline);
        return detail == null ? y : drawLine(graphics, minecraft, scaledWidth, y, detail);
    }

    private static int drawLine(GuiGraphics graphics, Minecraft minecraft, int scaledWidth, int y, String text) {
        Component line = Component.literal(text);
        graphics.drawString(minecraft.font, line, scaledWidth - minecraft.font.width(line) - RIGHT_MARGIN, y,
                0xFFFFFF);
        return y + LINE_HEIGHT;
    }
}
